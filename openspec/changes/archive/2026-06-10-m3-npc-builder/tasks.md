## 1. Headless Test Foundation

- [x] 1.1 Add `NpcBuilderTest` setup using `DefaultEntityData`, `RoundSystem`, a positioned mascot entity, and helpers for querying placed blocks in creation order.
- [x] 1.2 Add failing coverage for BUILD-phase eligibility, one-block-per-update behavior, the eight-block quota, and the BUILD-to-ATTACK transition.
- [x] 1.3 Add failing coverage for the exact first-ring placement order and translation from a non-origin mascot position.

## 2. NPC Builder Core

- [x] 2.1 Add `NpcBuilderSystem` with the named `BLOCKS_PER_ROUND` placeholder, injected `EntityData`, `RoundSystem`, and mascot entity ID.
- [x] 2.2 Implement in-progress BUILD-phase gating, per-round script state, and at-most-one successful placement per update.
- [x] 2.3 Implement the deterministic concentric-ring candidate generator with positive Z as front and symmetric front, side, and rear ordering.
- [x] 2.4 Track positioned block entities, skip occupied candidates, and preserve all existing block state.
- [x] 2.5 Create each placed entity with mascot-relative `PositionComponent`, a full-health scripted `BlockComponent`, and a stable type-derived `ModelComponent`.
- [x] 2.6 Invoke `RoundSystem.beginAttackPhase()` after the eighth successful placement and release the builder's `EntitySet` through a lifecycle cleanup method.

## 3. Composition and Persistence Coverage

- [x] 3.1 Add `NpcBuilderTest` coverage for all-SAND round 1 and alternating SAND/CORAL, ROCK/SHELL, and ROCK/JELLYFISH scripts in rounds 2 through 4.
- [x] 3.2 Add coverage that occupied priority positions are skipped and destroyed high-priority positions are refilled before lower-priority positions.
- [x] 3.3 Add coverage that surviving blocks retain entity identity, type, durability, and position while each later BUILD phase adds exactly eight blocks.
- [x] 3.4 Add coverage that placed blocks have full durability and the expected type-derived model identifier.
- [x] 3.5 Add coverage that ATTACK-phase and completed-game updates are no-ops after construction finishes.

## 4. Verification

- [x] 4.1 Run `nix develop --command ./gradlew test` and resolve all failures.
- [x] 4.2 Confirm M3 adds no dependencies and no rendering, physics, input, pathfinding, or AppState lifecycle code.
