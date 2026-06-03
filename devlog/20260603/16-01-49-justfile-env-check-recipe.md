<!--
File path: devlog/20260603/16-01-49-justfile-env-check-recipe.md
-->

# Devlog — 2026-06-03 16:01:49 — `justfile-env-check-recipe`

> **Author**: brian1061225@gmail.com
> **Build / Version**: pre-commit infra
> **Branch / Commit**: main

---

## Summary

Add a `just check-env` recipe that verifies the dev environment has the
tools needed to build, test, and use the Claude pre-commit hooks. Runs
without entering `nix develop` — devs on any platform can sanity-check
their setup.

---

## What I worked on

### Build: `just check-env` recipe — `justfile`

Verifies, in order:

- **Java 21+** — required by Gradle and the project (`AGENTS.md` rule).
- **Gradle access** — `./gradlew` preferred, system `gradle` accepted as
  fallback (mirrors `pre-commit-test.sh`).
- **`python3`** — used by `.claude/hooks/pre-commit-{devlog,test}.sh` to
  parse the tool-input JSON from Claude Code.
- **`git`** — used everywhere.

Prints ✅ / ❌ per check, exits non-zero if any required tool is missing,
and points Nix users at `nix develop` as the easy fix.

Smoke-tested locally — all four checks pass on the current environment
(Zulu 21.0.11, Python 3.13.13, git 2.54.0, `./gradlew` present).

---

## Decisions made

- **Decision**: Use a shebang recipe (`#!/usr/bin/env bash`) instead of
  multiple `just`-native lines.
  **Reason**: Need shared state (the `bad` counter) and helper functions
  across checks; shebang recipes are the clean way to do that.
  **Alternatives considered**: a chain of `||` lines — rejected, would
  bail on the first failure instead of reporting all gaps.

- **Decision**: Don't run `./gradlew --version` as a check.
  **Reason**: Cold runs download the wrapper distribution (slow,
  network-bound). Just verify the wrapper script is present + executable
  and let `just build` / `just test` exercise it.
  **Alternatives considered**: actually invoke gradlew — rejected,
  defeats the "quick env check" purpose.

---

## Open questions / blockers

- [ ] Should the recipe also check `nix` / `direnv` presence and warn
  if neither is found? Currently it just suggests them in the failure
  footer. Leaving for follow-up.

## Next session

- [ ] Possibly wire `just check-env` into a CI job so PRs from new
  contributors fail loudly on missing tooling.
