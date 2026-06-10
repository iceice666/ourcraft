<!--
File path: devlog/20260610/09-56-27-m3-npc-builder.md
-->

# Devlog — 2026-06-10 09:56:27 — `m3-npc-builder`

> **Author**: iceice666
> **Build / Version**: M3
> **Branch / Commit**: main

---

## Summary

Implemented deterministic headless NPC construction for all four rounds,
including placement priority, survivor persistence, block composition, and
BUILD-to-ATTACK signaling. Synced and archived the completed OpenSpec change.

---

## Goals for this session

- [x] Add deterministic mascot-relative block placement
- [x] Implement all four round-specific block scripts
- [x] Preserve survivors and refill high-priority gaps
- [x] Cover construction behavior with headless tests
- [x] Pass the complete test suite
- [x] Archive M3 and sync the `npc-building` main spec

---

## What I worked on

### Feature / System: `NpcBuilderSystem`

- Added an eight-block placeholder quota with one placement per update
- Gated construction on an in-progress BUILD phase
- Generated deterministic concentric rings with positive Z as front
- Skipped occupied block positions without changing surviving entities
- Created full-health typed blocks with position and model components
- Alternated the documented block scripts for rounds 1 through 4
- Transitioned to ATTACK through `RoundSystem` after the eighth placement
- Released the tracked `EntitySet` through an explicit cleanup method

### Tests and specifications

- Added `NpcBuilderTest` coverage for phase gating, quotas, placement order,
  translated mascot positions, round scripts, occupancy, survivor identity,
  durability, model identifiers, refilling, and completed-game no-ops
- Synced the `npc-building` capability into the main OpenSpec specs
- Archived the completed change as `2026-06-10-m3-npc-builder`
- Marked M3 complete and M4 as the next milestone

---

## Decisions made

- **Decision**: Compare exact mascot-relative grid positions for occupancy.
  **Reason**: Builder-created positions use integer-valued floats, so exact
  component equality is deterministic and avoids introducing another type.
  **Alternatives considered**: Quantize arbitrary world coordinates or add a
  dedicated grid-position component before M4 requires one.

- **Decision**: Advance the block script only after a successful placement.
  **Reason**: Occupied positions affect location selection, not the documented
  per-round composition order.
  **Alternatives considered**: Advance the script for skipped candidates,
  which would make composition depend on surviving defenses.

- **Decision**: Preserve the authoritative phase transition in `RoundSystem`.
  **Reason**: It already owns the ATTACK timer reset invariant.
  **Alternatives considered**: Write phase and timer components directly from
  the builder.

---

## Technical notes

- The first ring order is front center, side centers, front corners, rear
  corners, then rear center.
- Candidate generation expands without a fixed slot limit.
- Only entities with both `BlockComponent` and `PositionComponent` occupy
  construction positions.
- No dependencies, rendering, physics, input, pathfinding, or AppState
  lifecycle code were added.

---

## Next session

- [ ] Start M4: first-person AppState and player-control integration

---

## References

- [Archived M3 change](../../openspec/changes/archive/2026-06-10-m3-npc-builder/)
- [NPC building spec](../../openspec/specs/npc-building/spec.md)
