<!--
File path: devlog/20260610/06-44-23-milestone-breakdown.md
-->

# Devlog — 2026-06-10 06:44:23 — `milestone-breakdown`

> **Author**: iceice666
> **Build / Version**: skeleton (M0 done)
> **Branch / Commit**: main

---

## Summary

Split the GDD + TDD into a concrete milestone roadmap (`design/milestones.md`).
No production code changed — this session was pure planning.

---

## Goals for this session

- [x] Read GDD and TDD in full
- [x] Agree on slicing philosophy (horizontal / test-first) and delivery cadence
- [x] Write `design/milestones.md` mapping every TDD component/system/test to a milestone

---

## What I worked on

### Design artifact: `design/milestones.md`

Eight milestones (M0 already done, M1–M8 ahead):

| Milestone | Lands | Shippable? |
|-----------|-------|-----------|
| M0 Foundation | jME3+Zay-ES skeleton | no |
| M1 Core game loop | RoundSystem, VictorySystem | no |
| M2 Blocks & destruction | BlockComponent, WeaponSystem, counter-matrix | no |
| M3 NPC builder | NpcBuilderSystem, per-round scripts | no |
| M4 FPS playable | AppState machine, PlayerControlState, raycast wiring | **YES** |
| M5 Block special effects | BlockEffectSystem, EffectComponent | yes |
| M6 UI / HUD | HudSystem, Lemur menus | yes |
| M7 Balancing | Fill all TBD constants, retune tests | yes |
| M8 Stretch | Playable builder, real 3D art | yes (optional) |

---

## Decisions made

- **Decision**: Horizontal / test-first slicing (M1–M3 headless, M4 wires it).
  **Reason**: Matches existing TDD §10 culture and devlog test-first stance.
  **Alternatives considered**: Vertical (runnable each milestone) — rejected
  because logic bugs are much harder to chase in a running renderer.

- **Decision**: TBD values are placeholder constants until M7.
  **Reason**: No system should be blocked on balance numbers that aren't decided
  yet. Build with named constants; tune in a dedicated pass.
  **Alternatives considered**: Decide values now — rejected, playtest is needed.

- **Decision**: Lemur GUI deferred to M6 (not yet in `libs.versions.toml`).
  **Reason**: Adding a new dep requires explicit approval (CLAUDE.md rule 7).
  M4 can use a minimal placeholder UI.
  **Alternatives considered**: Add Lemur now — unnecessarily couples UI infra
  to the logic milestones.

---

## Open questions / blockers

- [ ] What placeholder block count per round should M3 use? (GDD says TBD)

---

## Next session

- [ ] Implement M1: `RoundComponent`, `PhaseComponent`, `RoundSystem`, `VictorySystem`
- [ ] Write `RoundSystemTest` and `VictorySystemTest` (TDD §10) — tests first
- [ ] All tests green before committing M1

---

## References

- `design/gdd.md` — the game design document
- `design/tdd.md` — technical design document (source of truth for components/systems/tests)
- `design/milestones.md` — the new roadmap (this session's output)
