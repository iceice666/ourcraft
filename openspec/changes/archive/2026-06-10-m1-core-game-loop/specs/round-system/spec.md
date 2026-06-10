## ADDED Requirements

### Requirement: Game state singleton
The system SHALL maintain exactly one game-state entity holding `RoundComponent` (currentRound, maxRounds), `PhaseComponent` (BUILD | ATTACK), and `GameResultComponent` (IN_PROGRESS | WIN | LOSS).

#### Scenario: Initial state on system start
- **WHEN** the game starts and `RoundSystem` initializes
- **THEN** currentRound = 1, maxRounds = 4, phase = BUILD, result = IN_PROGRESS, remainingSeconds = 60.0

---

### Requirement: Phase transition to ATTACK
The system SHALL transition from BUILD to ATTACK phase when explicitly triggered, resetting the attack timer to the full duration (60 s).

#### Scenario: BUILD to ATTACK transition
- **WHEN** the BUILD phase is signaled to end
- **THEN** `PhaseComponent` changes to ATTACK and `remainingSeconds` is set to 60.0

---

### Requirement: Attack timer countdown
During ATTACK phase, the system SHALL decrement `remainingSeconds` by the elapsed time (`tpf`) each update tick, clamped to a minimum of 0.

#### Scenario: Timer counts down normally
- **WHEN** in ATTACK phase with `remainingSeconds = 60.0` and `update(30.0)` is called
- **THEN** `remainingSeconds == 30.0`

#### Scenario: Timer clamps to zero
- **WHEN** `remainingSeconds = 10.0` and `update(15.0)` is called
- **THEN** `remainingSeconds == 0.0` (not negative)

#### Scenario: Timer does not decrement in BUILD phase
- **WHEN** in BUILD phase and `update(30.0)` is called
- **THEN** `remainingSeconds` is unchanged

---

### Requirement: Round advancement on timer expiry
When the ATTACK phase timer reaches 0 and the current round is not the final round, the system SHALL increment `currentRound` by 1 and transition to BUILD phase with the timer reset.

#### Scenario: Advance to next round
- **WHEN** currentRound = 1, maxRounds = 4, and the ATTACK timer expires
- **THEN** currentRound = 2, phase = BUILD, remainingSeconds = 60.0, result = IN_PROGRESS

#### Scenario: No result written on non-final round expiry
- **WHEN** currentRound = 2, maxRounds = 4, and the ATTACK timer expires
- **THEN** `GameResultComponent` remains IN_PROGRESS

---

### Requirement: No duplicate game result write
The system SHALL NOT overwrite `GameResultComponent` if it is already WIN or LOSS.

#### Scenario: Timer expires but result is already set
- **WHEN** currentRound = 4, timer reaches 0, and `GameResultComponent` is already WIN
- **THEN** `GameResultComponent` remains WIN
