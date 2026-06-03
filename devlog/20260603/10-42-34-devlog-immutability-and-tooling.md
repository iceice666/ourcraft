# Devlog — 2026-06-03 10:42:34 — `devlog-immutability-and-tooling`

> **Author**: brian
> **Build / Version**: pre-alpha
> **Branch / Commit**: main @ e01816a

---

## Summary

Tightened the devlog policy: past entries are immutable except for the two forward-looking sections, which can be ticked off with a back-link. Added a small bash helper to surface every still-unchecked item across all entries.

---

## Goals for this session

- [x] Document devlog immutability in `devlog/README.md`
- [x] Add the immutability rule to `AGENTS.md`
- [x] Ship `devlog/tools/open-items.sh` to list outstanding items

---

## What I worked on

### Feature / System: `devlog policy — immutability`

- Past entries are frozen records. Editing committed entries (typo fixes, rewording, backfills) is prohibited.
- Exception: `## Open questions / blockers` and `## Next session` may be updated to flip `- [ ]` → `- [x]`, appended with a link to the entry/PR/issue that addressed it.
- Documented in `devlog/README.md` § Immutability with a worked example, and codified as rule #7 in `AGENTS.md`.

### Feature / System: `devlog/tools/open-items.sh`

- Bash + awk script. No deps beyond what's already in the Nix shell.
- Globs `devlog/[0-9]*/*.md`, walks each file once, prints only entries that have at least one unchecked item.
- Output groups by file, then by section, indented for readability.
- Exits 0 with a friendly message if there's nothing outstanding (or no entries yet).

### Bugs fixed

| ID | Description | Cause | Fix |
|----|-------------|-------|-----|
| —  | n/a         |       |     |

### Refactors / cleanup

- None.

---

## Technical notes

The script parses headings by literal match rather than a general Markdown parser. Two reasons:

1. The template fixes the heading text — `## Open questions / blockers` and `## Next session` are stable strings, not arbitrary user content.
2. Keeps the script to ~50 lines with zero runtime dependencies. A real Markdown parser (Python `markdown-it`, Node `remark`) would be heavier than the problem.

If we ever rename those sections, this script and `TEMPLATE.md` must change together. Worth a note in `AGENTS.md` if it actually happens.

Tested against the existing two entries (`set-up-devlog` and `scaffold-jme3-zay-es`) — both surface their items correctly.

---

## Playtest / observations

n/a — no game code touched.

---

## Decisions made

- **Decision**: Bash + awk over Python for the open-items script.
  **Reason**: One file, no runtime to install, fits the problem. Python would need a venv or system dep.
  **Alternatives considered**: Python with `markdown-it-py`; a Gradle task; a generic ripgrep one-liner in the README instead of a script.

- **Decision**: Resolved items stay in their original entry, marked `- [x]` with a link — they don't move.
  **Reason**: Preserves the historical record. A reader of the original entry sees both the question and how it eventually got answered, without chasing a moved item.
  **Alternatives considered**: Migrating resolved items into a central `RESOLVED.md`; deleting them outright on close.

---

## Open questions / blockers

- [ ] Should `open-items.sh` also warn when an entry has *no* `## Next session` content at all (suggests an abandoned session)?
- [ ] Worth wiring `open-items.sh` into a Gradle task (`./gradlew devlog:open`) for discoverability?

---

## Next session

- [ ] Try the immutability rule in anger — next entry that resolves something from `20260603/10-33-19-set-up-devlog.md` should close those items with back-links.
- [ ] Decide whether to add a `devlog/tools/new-entry.sh` scaffolder (one of the open items from the first entry).

---

## Screenshots / media

<!-- none -->

---

## References

- `devlog/README.md` § Immutability — full convention
- `AGENTS.md` rule #7 — enforcement
- `devlog/tools/open-items.sh` — the script added this session
