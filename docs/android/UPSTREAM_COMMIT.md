# Upstream commit

- Repository: https://github.com/anomalyco/opencode
- Branch: `dev`
- Commit: `b02acc1e30ef55f7f181fec8d2f241d26f022683`
  - subject: `chore(stats): update GitHub star fallback (#49654)`
  - date: 2026-09-17 20:50:22 -0500
- Local working branch for this work: `android-port` (created from the pinned commit)
- Tree state before modifications: clean
  - `git status --short` was empty at the pinned commit (only pre-existing ignored
    artifacts from an earlier experimental branch, `packages/android/app/src/main/jniLibs/`
    and build caches, were present on disk; nothing tracked was dirty).

Verification commands:

```sh
git clone --branch dev https://github.com/anomalyco/opencode.git
cd opencode
git checkout b02acc1e30ef55f7f181fec8d2f241d26f022683
git rev-parse HEAD   # b02acc1e30ef55f7f181fec8d2f241d26f022683
git status           # clean before modifications
```

Note: the local clone used for this work is a shallow clone that was later
deepened with `git fetch origin dev`; the pinned commit is an ancestor of
`origin/dev` (`git merge-base --is-ancestor b02acc1e origin/dev` succeeds).
