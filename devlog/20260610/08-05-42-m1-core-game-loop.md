<!--
File path: devlog/20260610/08-05-42-m1-core-game-loop.md
-->

# Devlog — 2026-06-10 08:05:42 — `m1-core-game-loop`

> **Author**: iceice666
> **Build / Version**: M1
> **Branch / Commit**: main

---

## Summary

Implemented the headless core game loop: round/phase lifecycle (RoundSystem) and win/loss determination (VictorySystem), with full JUnit 5 test coverage of all spec scenarios.

---

## Goals for this session

- [x] Implement RoundComponent, PhaseComponent, GameResultComponent, BlockComponent stub
- [x] Implement RoundSystem (timer, phase transitions, round advancement)
- [x] Implement VictorySystem (WIN on blocks cleared, LOSS on final-round expiry)
- [x] Pass all 16 gating tests (RoundSystemTest, VictorySystemTest)
- [x] Archive M1 OpenSpec change and sync main specs

---

## What I worked on

### Feature / System: `RoundSystem`

- Plain class (no framework inheritance — AbstractGameSystem not in zay-es 1.6.0)
- Singleton game-state entity holds RoundComponent, PhaseComponent, GameResultComponent
- `initialize()` creates the entity; `beginAttackPhase()` transitions BUILD→ATTACK
- `update(float tpf)` decrements timer, clamps to 0, advances rounds on expiry
- Early-return guard: skips all writes if result is not IN_PROGRESS or timer already at 0

### Feature / System: `VictorySystem`

- Caches `EntitySet<BlockComponent>` for efficient per-tick count
- WIN: blocks == 0 during ATTACK phase
- LOSS: remainingSeconds <= 0 AND currentRound == maxRounds AND blocks > 0
- `close()` releases the EntitySet

### Decisions made

- **Decision**: VictorySystem owns all outcome writes (WIN and LOSS); RoundSystem only advances rounds
  **Reason**: Avoids a race where RoundSystem writes LOSS before VictorySystem can write WIN when blocks hit 0 exactly as the timer expires
  **Alternatives considered**: Both systems write LOSS (guarded by IN_PROGRESS check) — rejected due to update-order sensitivity

---

## Technical notes

- `BlockComponent` is a no-field stub; M2 adds `blockType`, `durability`, `maxDurability` to the same class
- `RoundSystem.ATTACK_DURATION` is `public static final` so tests can reference it directly
- Timer clamp test uses `currentRound=4=maxRounds` to prevent round advancement from resetting `remainingSeconds` before the assertion

---

## Next session

- [ ] Start M2: BlockComponent (expand stub), WeaponSystem, counter-matrix
