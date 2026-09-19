#!/usr/bin/env python3
"""Fetch the Termux runtime tools that the Android host ships inside the APK.

Downloads pinned Termux .deb packages for aarch64, resolves the dependency
closure of the requested roots, extracts the runtime parts into a single ZIP
that is stored in `packages/android/app/src/main/assets/`, and writes a
manifest with versions and SHA-256 hashes.

The ZIP is extracted at first launch into the app's private storage. The
binaries are never executed with `execve` from app storage (blocked by SELinux
on targetSdk 29+); the host runs them through `/system/bin/linker64` and shell
wrappers instead.

Usage: python script/android/fetch-termux-runtime.py [--out <zip>]
"""

from __future__ import annotations

import argparse
import hashlib
import io
import json
import lzma
import os
import sys
import tarfile
import urllib.request

INDEX_URL = "https://packages.termux.dev/apt/termux-main/dists/stable/main/binary-aarch64/Packages"
BASE_URL = "https://packages.termux.dev/apt/termux-main/"
ROOTS = ["git"]
SKIP_DEPS = {"less", "bash", "coreutils", "dash", "findutils", "grep", "sed", "tar", "gzip"}

REPO_ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
PREFIX = "data/data/com.termux/files/usr/"


def fetch(url: str) -> bytes:
    print(f"  GET {url}")
    with urllib.request.urlopen(url, timeout=120) as response:
        return response.read()


def parse_index(text: str) -> dict[str, dict[str, str]]:
    packages: dict[str, dict[str, str]] = {}
    for block in text.split("\n\n"):
        if not block.strip():
            continue
        fields: dict[str, str] = {}
        for line in block.splitlines():
            if line.startswith(" ") or ":" not in line:
                continue
            key, _, value = line.partition(":")
            fields[key.strip()] = value.strip()
        name = fields.get("Package")
        if name:
            packages[name] = fields
    return packages


def resolve(packages: dict[str, dict[str, str]], roots: list[str]) -> list[str]:
    seen: set[str] = set()
    order: list[str] = []
    queue = list(roots)
    while queue:
        name = queue.pop(0)
        if name in seen or name in SKIP_DEPS:
            continue
        fields = packages.get(name)
        if not fields:
            print(f"  ! unknown dependency: {name}")
            continue
        seen.add(name)
        order.append(name)
        for dep in (fields.get("Depends") or "").split(","):
            dep = dep.strip()
            if dep:
                queue.append(dep)
    return order


def ar_members(data: bytes) -> dict[str, bytes]:
    if data[:8] != b"!<arch>\n":
        raise ValueError("not an ar archive")
    offset = 8
    members: dict[str, bytes] = {}
    while offset + 60 <= len(data):
        header = data[offset : offset + 60]
        name = header[0:16].decode("utf-8", "replace").strip()
        size = int(header[48:58].decode("utf-8").strip())
        offset += 60
        members[name.rstrip("/")] = data[offset : offset + size]
        offset += size + (size % 2)
    return members


def deb_data(members: dict[str, bytes]) -> tarfile.TarFile:
    for name, payload in members.items():
        if name.startswith("data.tar"):
            if name.endswith(".xz"):
                return tarfile.open(fileobj=io.BytesIO(lzma.decompress(payload)), mode="r:")
            if name.endswith(".gz"):
                return tarfile.open(fileobj=io.BytesIO(payload), mode="r:gz")
            if name.endswith(".zst"):
                raise SystemExit("zstd data.tar is not supported")
            return tarfile.open(fileobj=io.BytesIO(payload), mode="r:")
    raise ValueError("no data.tar member")


def wanted(member: str) -> bool:
    if not member.startswith("./" + PREFIX):
        return False
    rest = member[len("./" + PREFIX) :].rstrip("/")
    if rest == "bin/git":
        return True
    if rest.startswith("lib/"):
        return True
    if rest.startswith("libexec/git-core/"):
        return True
    if rest.startswith("share/git-core/templates/"):
        return True
    if rest.startswith("etc/ssl/") or rest.startswith("etc/tls/") or rest.startswith("etc/openssl/"):
        return True
    return False


def elf_needed(payload: bytes) -> list[str]:
    """Return DT_NEEDED entries of a little-endian 64-bit ELF, if readable."""
    import struct

    if payload[:4] != b"\x7fELF" or payload[4] != 2 or payload[5] != 1:
        return []
    try:
        e_shoff = struct.unpack_from("<Q", payload, 0x28)[0]
        e_shentsize = struct.unpack_from("<H", payload, 0x3A)[0]
        e_shnum = struct.unpack_from("<H", payload, 0x3C)[0]
        sections = []
        for index in range(e_shnum):
            offset = e_shoff + index * e_shentsize
            sh_type = struct.unpack_from("<I", payload, offset + 4)[0]
            sh_offset = struct.unpack_from("<Q", payload, offset + 0x18)[0]
            sh_size = struct.unpack_from("<Q", payload, offset + 0x20)[0]
            sh_link = struct.unpack_from("<I", payload, offset + 0x28)[0]
            sections.append((sh_type, sh_offset, sh_size, sh_link))
        dynamic = next((s for s in sections if s[0] == 6), None)
        if dynamic is None:
            return []
        strings = next((s for i, s in enumerate(sections) if i == dynamic[3]), None)
        if strings is None:
            return []
        strtab = strings[1]
        needed: list[str] = []
        offset, size = dynamic[1], dynamic[2]
        for cursor in range(offset, offset + size, 16):
            d_tag = struct.unpack_from("<q", payload, cursor)[0]
            d_val = struct.unpack_from("<Q", payload, cursor + 8)[0]
            if d_tag == 0:
                break
            if d_tag == 1:  # DT_NEEDED, offset into .dynstr
                start = strtab + d_val
                end = payload.index(b"\0", start)
                needed.append(payload[start:end].decode("utf-8", "replace"))
        return needed
    except Exception:
        return []


def synthesize_needed(files: dict[str, bytes]) -> dict[str, bytes]:
    """Create SONAME aliases the debs did not ship as symlinks."""
    import fnmatch

    result = dict(files)
    for path, payload in list(files.items()):
        if not path.startswith("lib/") or ".so" not in path:
            continue
        base = os.path.dirname(path)
        for needed in elf_needed(payload):
            candidate = f"{base}/{needed}" if base else needed
            if candidate in result:
                continue
            matches = sorted(
                name for name in files if os.path.dirname(name) == base and fnmatch.fnmatch(os.path.basename(name), needed + "*")
            )
            if not matches:
                print(f"  ! missing library {needed!r} for {path}")
                continue
            result[candidate] = files[matches[-1]]
            print(f"  + alias {candidate} -> {matches[-1]}")
    return result


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--out",
        default=os.path.join(REPO_ROOT, "packages", "android", "app", "src", "main", "assets", "git-runtime.zip"),
    )
    args = parser.parse_args()

    print("Fetching Termux package index")
    packages = parse_index(fetch(INDEX_URL).decode("utf-8", "replace"))
    names = resolve(packages, ROOTS)
    print("Dependency closure:", ", ".join(names))

    files: dict[str, bytes] = {}
    manifest = {"index": INDEX_URL, "roots": ROOTS, "packages": []}
    for name in names:
        fields = packages[name]
        url = BASE_URL + fields["Filename"]
        raw = fetch(url)
        sha = hashlib.sha256(raw).hexdigest()
        manifest["packages"].append(
            {"name": name, "version": fields.get("Version"), "url": url, "sha256": sha}
        )
        archive = deb_data(ar_members(raw))
        for member in archive.getmembers():
            if member.issym() or member.islnk():
                continue
            if not member.isfile() or not wanted(member.name):
                continue
            rest = member.name[len("./" + PREFIX) :]
            files[rest] = archive.extractfile(member).read()
        # recreate symlinks by dereferencing their targets
        for member in archive.getmembers():
            if not member.issym() or not wanted(member.name):
                continue
            rest = member.name[len("./" + PREFIX) :]
            target = member.linkname
            target_rest = target if not target.startswith("/") else target.lstrip("/")
            if target_rest.startswith(PREFIX):
                target_rest = target_rest[len(PREFIX) :]
            base = os.path.dirname(rest)
            candidate = os.path.normpath(os.path.join(base, target_rest))
            if candidate in files:
                files[rest] = files[candidate]

    files = synthesize_needed(files)

    os.makedirs(os.path.dirname(args.out), exist_ok=True)
    import zipfile

    with zipfile.ZipFile(args.out, "w", zipfile.ZIP_DEFLATED, compresslevel=9) as archive:
        for path, payload in sorted(files.items()):
            archive.writestr(path, payload)
    print(f"Wrote {args.out} ({os.path.getsize(args.out)} bytes, {len(files)} files)")

    manifest_path = os.path.join(os.path.dirname(os.path.abspath(__file__)), "termux-manifest.json")
    with open(manifest_path, "w", encoding="utf-8") as handle:
        json.dump(manifest, handle, indent=2)
        handle.write("\n")
    print(f"Wrote {manifest_path}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
