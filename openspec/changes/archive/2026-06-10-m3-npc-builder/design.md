## Context

The current headless game loop starts each round in BUILD and exposes `RoundSystem.beginAttackPhase()` as the authoritative transition into ATTACK. Blocks are independent Zay-ES entities, and M2 already defines their durability and lifetime. M3 must create those entities around a mascot without depending on jME rendering, input, physics, or pathfinding.

The design documents specify a placement priority and per-round block mixes but do not define exact coordinates, mixed-type ordering, survivor behavior, or a headless lifecycle. Those details must be deterministic so `NpcBuilderTest` can verify the complete script.

## Goals / Non-Goals

**Goals:**

- Place a deterministic quota of new blocks during each BUILD phase.
- Preserve surviving blocks and refill the highest-priority available positions.
- Produce entities that M2 can damage and M4 can render without conversion.
- Use the documented round-specific block compositions.
- Signal BUILD completion through the existing `RoundSystem` API.
- Keep all behavior testable with `DefaultEntityData`.

**Non-Goals:**

- NPC movement, pathfinding, construction animation, or elapsed build timing.
- Mascot orientation or rotation-aware placement.
- Collision, structural support, or validation against scene geometry.
- Final block-count balancing; M7 will replace the placeholder quota.
- AppState attachment and detachment; M4 owns runtime state composition.

## Decisions

### Place one block per logical update with an eight-block quota

`NpcBuilderSystem.update(float tpf)` will place at most one block while the game is in an in-progress BUILD phase. `BLOCKS_PER_ROUND` will be a named placeholder constant with value 8. After the eighth successful placement, the system immediately calls `RoundSystem.beginAttackPhase()`.

The system tracks the current round and successful placement count. A newly observed BUILD round resets the script counter; ATTACK updates and completed games are no-ops.

This makes placement order directly observable in headless tests and isolates the M7 tuning point. Building the entire round in one call was considered, but it would make ordering less visible and provide no incremental hook for later presentation.

### Use mascot-relative concentric grid rings

The mascot's `PositionComponent` is the origin, its Y coordinate is preserved, and positive Z is defined as front because no orientation component exists yet. Candidate offsets are generated from increasing Chebyshev-distance rings.

For each ring radius `r`, positions are ordered as:

1. Front center `(0, r)`.
2. Left and right centers `(-r, 0)`, `(r, 0)`.
3. Remaining front edge in symmetric pairs moving outward.
4. Left and right edges in symmetric pairs moving from front to rear, excluding the already emitted side centers.
5. Remaining rear edge in symmetric pairs, ending at rear center `(0, -r)`.

The first ring is therefore:

```text
(0, 1),
(-1, 0), (1, 0),
(-1, 1), (1, 1),
(-1, -1), (1, -1),
(0, -1)
```

An algorithmic sequence avoids a hard-coded 32-slot limit while preserving the documented front, sides, then outer-ring priority. A fixed coordinate table was considered, but it would couple placement capacity to the current four-round quota.

### Treat existing blocks as occupied and preserve them

Before selecting a position, the system applies changes to an `EntitySet` containing `BlockComponent` and `PositionComponent`. Candidate positions already occupied by a block are skipped. Existing entities are never moved, healed, replaced, or removed by the builder.

This makes round progression cumulative and follows the existing `RoundSystem`, which advances into the next BUILD phase without clearing surviving entities. Considering all positioned entities as blockers was rejected because a moving player or mascot must not permanently alter the construction script.

### Alternate each round's documented block types

The successful placement index selects from an ordered round script:

| Round | Repeating script |
|-------|------------------|
| 1 | SAND |
| 2 | SAND, CORAL |
| 3 | ROCK, SHELL |
| 4 | ROCK, JELLYFISH |

Only successful placements advance the script index. This produces an even four/four split for mixed rounds with the placeholder quota and remains deterministic when occupied positions are skipped.

Random selection and weighted distributions were considered, but they add no documented gameplay value and would weaken headless reproducibility.

### Create complete block entities

Every placement creates one entity with:

- `PositionComponent` at the selected mascot-relative grid coordinate.
- A full-health `BlockComponent` for the scripted type.
- `ModelComponent` using a stable type-derived identifier such as `sand-block`.

No new component is needed. The entity is immediately compatible with `WeaponSystem`, `VictorySystem`, and `ModelViewState`.

### Delegate the phase transition to RoundSystem

`NpcBuilderSystem` receives the existing `RoundSystem` and mascot entity ID. It reads game state through `RoundSystem.getGameStateId()` and invokes `beginAttackPhase()` after completing the quota.

Writing `PhaseComponent` directly was considered, but that would duplicate the timer reset invariant already owned by `RoundSystem`. A generic callback was also considered, but the current single transition target does not justify the added abstraction.

## Risks / Trade-offs

- [Positive Z may not match the mascot's eventual visual facing direction] → Keep the coordinate policy isolated so M4 or a later orientation component can transform offsets without changing the build script.
- [One placement per frame makes the visible BUILD phase brief and frame-rate dependent] → M3 defines logical ordering only; a later presentation layer can schedule update calls or add animation without changing placement rules.
- [Cumulative defenses can reach 32 blocks after four untouched rounds] → This follows current round semantics and remains small; M7 will tune the quota through the named constant.
- [Float positions are compared as grid coordinates] → The builder creates integer-valued floats and compares exact component values, avoiding accumulated arithmetic.
- [Direct dependency on `RoundSystem` couples construction to the current round implementation] → The dependency preserves one authoritative phase transition and is limited to the existing public API.
