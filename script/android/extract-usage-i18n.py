#!/usr/bin/env python3
"""Extract the `context.*` translations used by the Android usage panel.

The Android host renders its context-usage overlay with the app's own
translations so the overlay follows the application language. This script
reads `packages/app/src/i18n/*.ts` and writes
`packages/android/app/src/main/assets/usage-i18n.json`.

Usage: python script/android/extract-usage-i18n.py
"""

from __future__ import annotations

import json
import os
import re
import sys

REPO_ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
I18N_DIR = os.path.join(REPO_ROOT, "packages", "app", "src", "i18n")
OUT = os.path.join(REPO_ROOT, "packages", "android", "app", "src", "main", "assets", "usage-i18n.json")

ENTRY = re.compile(r'^\s*"(?P<key>[^"]+)":\s*"(?P<value>(?:[^"\\]|\\.)*)",?\s*$')

KEYS = (
    "context.usage.",
    "context.stats.",
    "context.breakdown.",
    "context.systemPrompt.title",
    "context.rawMessages.title",
    "palette.group.files",
    "dialog.directory.parent",
    "toast.file.list",
)


def main() -> int:
    translations: dict[str, dict[str, str]] = {}
    for name in sorted(os.listdir(I18N_DIR)):
        if not name.endswith(".ts") or name in ("desktop-native.ts",) or name.endswith(".test.ts"):
            continue
        locale = name[:-3]
        entries: dict[str, str] = {}
        with open(os.path.join(I18N_DIR, name), encoding="utf-8") as handle:
            for line in handle:
                match = ENTRY.match(line)
                if not match:
                    continue
                key = match.group("key")
                if not key.startswith(KEYS):
                    continue
                entries[key] = json.loads('"' + match.group("value") + '"')
        if entries:
            translations[locale] = entries

    os.makedirs(os.path.dirname(OUT), exist_ok=True)
    with open(OUT, "w", encoding="utf-8") as handle:
        json.dump(translations, handle, ensure_ascii=False, indent=1)
        handle.write("\n")
    print(f"Wrote {OUT} ({len(translations)} locales)")
    return 0


if __name__ == "__main__":
    sys.exit(main())
