## Context

M0 delivered the Zay-ES + jME3 skeleton with `EntityData`, `PositionComponent`, and `ModelViewState`. M1 layers the headless game-loop on top: round sequencing, phase transitions, a countdown timer, and win/loss resolution. All new code must be testable with a synthetic `EntityData` (no renderer required).

## Goals / Non-Goals

**Goals:**
- Drive the `BUILD → ATTACK` phase cycle and 60 s attack timer via `RoundSystem`
- Determine win/loss by counting `BlockComponent`-tagged entities via `VictorySystem`
- Pass `RoundSystemTest` and `VictorySystemTest` without a running jME application
- Define a minimal `BlockComponent` stub so `VictorySystem` can query for it (M2 will expand it)

**Non-Goals:**
- Rendering, input, or sound — these are M4 concerns
- Weapon damage or block durability — M2 concerns
- Persisting game state across sessions

## Decisions

### 1. Singleton entity for game state

**Decision:** Store `RoundComponent`, `PhaseComponent`, and `GameResultComponent` on a single "game state" entity. Its `EntityId` is returned by a static factory / held in a known field on the system that creates it.

**Why:** Zay-ES has no global registry; the cleanest pattern is one authoritative entity per singleton concern. Callers query `ed.getComponent(gameStateId, RoundComponent.class)` — no iteration needed.

**Alternative considered:** Tag the entity with a unique marker component and iterate — adds indirection with no benefit when there is exactly one such entity.

---

### 2. Time injection for testability

**Decision:** `RoundSystem.update(double tpf)` (Zay-ES `AbstractGameSystem` signature) advances `remainingSeconds` by `tpf`. Tests drive time by calling `update(n)` repeatedly.

**Why:** No clock abstraction needed — `tpf` is already the jME time delta. Headless tests simply pass any double value to simulate elapsed time deterministically.

**Alternative considered:** Inject a `Clock` interface — adds complexity with no benefit since the tests control `tpf` directly.

---

### 3. Responsibility split between RoundSystem and VictorySystem

**Decision:**
- `RoundSystem` owns phase transitions and round advancement:
  - On ATTACK-phase update: decrement `remainingSeconds` by `tpf`, clamp to 0.
  - When `remainingSeconds == 0`: if `currentRound < maxRounds`, increment round and reset to BUILD; otherwise write `GameResultComponent(LOSS)`.
- `VictorySystem` owns win/loss by block count:
  - On ATTACK-phase update: if block-entity count == 0, write `GameResultComponent(WIN)`.
  - Takes priority — it checks first; `RoundSystem`'s timer-expiry loss fires only if `VictorySystem` hasn't already set a result.

**Why:** Separates "when does the round end" from "what does the result mean". Each system has one clear responsibility.

---

### 4. BlockComponent stub in M1

**Decision:** Define `BlockComponent` as a minimal Zay-ES component (implements `EntityComponent`) with no fields in M1. `VictorySystem` queries `ed.getEntities(BlockComponent.class)` to count blocks. M2 adds `blockType`, `durability`, `maxDurability` to the same class.

**Why:** VictorySystem needs to know which entities are blocks. Defining the stub now avoids a forward dependency on M2 and keeps `VictorySystemTest` self-contained.

**Alternative considered:** Use a separate `BlockMarkerComponent` — creates churn when M2 arrives; one class is cleaner.

---

### 5. GameResultComponent for outcome signaling

**Decision:** Add `GameResultComponent` (enum field: `IN_PROGRESS | WIN | LOSS`) on the game-state entity. Systems write to it; future UI/AppState reads from it.

**Why:** Keeps outcome state in the ECS data model (inspectable, serialisable, testable) rather than side-channel callbacks or statics.

## Risks / Trade-offs

- **Timer precision in tests** — floating-point accumulation across many small `tpf` steps could miss the exact 0 boundary. Mitigation: clamp `remainingSeconds = Math.max(0, remainingSeconds - tpf)` and trigger on `remainingSeconds <= 0`.
- **BlockComponent stub divergence** — if M2 changes the class signature incompatibly, M1 tests break. Mitigation: M1 tests only rely on the class existing (no field access), so additive changes in M2 are safe.
- **System update order** — if `VictorySystem` runs after `RoundSystem` in the same frame, both could write `GameResultComponent`. Mitigation: `RoundSystem` checks `GameResultComponent != IN_PROGRESS` before writing its own loss; last-writer-wins is safe here because both agree on loss.
