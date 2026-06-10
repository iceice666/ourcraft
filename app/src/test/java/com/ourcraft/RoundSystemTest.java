package com.ourcraft;

import com.ourcraft.ecs.components.GameResultComponent;
import com.ourcraft.ecs.components.GameResultComponent.Result;
import com.ourcraft.ecs.components.PhaseComponent;
import com.ourcraft.ecs.components.PhaseComponent.Phase;
import com.ourcraft.ecs.components.RoundComponent;
import com.ourcraft.ecs.systems.RoundSystem;
import com.simsilica.es.EntityData;
import com.simsilica.es.EntityId;
import com.simsilica.es.base.DefaultEntityData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.ourcraft.ecs.systems.RoundSystem.ATTACK_DURATION;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RoundSystemTest {

    private EntityData ed;
    private RoundSystem system;
    private EntityId gsId;

    @BeforeEach
    void setUp() {
        ed = new DefaultEntityData();
        system = new RoundSystem(ed);
        system.initialize();
        gsId = system.getGameStateId();
    }

    @Test
    void initialState() {
        assertEquals(1, round().currentRound());
        assertEquals(4, round().maxRounds());
        assertEquals(Phase.BUILD, phase().phase());
        assertEquals(Result.IN_PROGRESS, result().result());
        assertEquals(ATTACK_DURATION, round().remainingSeconds(), 0.001);
    }

    @Test
    void beginAttackPhaseSetsAttackAndResetsTimer() {
        system.beginAttackPhase();
        assertEquals(Phase.ATTACK, phase().phase());
        assertEquals(ATTACK_DURATION, round().remainingSeconds(), 0.001);
    }

    @Test
    void timerDecrementsInAttackPhase() {
        system.beginAttackPhase();
        system.update(30.0f);
        assertEquals(30.0, round().remainingSeconds(), 0.001);
    }

    @Test
    void timerClampsToZeroAtFinalRound() {
        // Put game at final round so no advancement fires, just the clamp
        system.beginAttackPhase();
        ed.setComponent(gsId, new RoundComponent(4, 4, 10.0));
        system.update(15.0f);
        assertEquals(0.0, round().remainingSeconds(), 0.001);
    }

    @Test
    void timerDoesNotDecrementInBuildPhase() {
        double before = round().remainingSeconds();
        system.update(30.0f);
        assertEquals(before, round().remainingSeconds(), 0.001);
    }

    @Test
    void nonFinalRoundAdvancesOnTimerExpiry() {
        system.beginAttackPhase();
        system.update(61.0f);
        assertEquals(2, round().currentRound());
        assertEquals(Phase.BUILD, phase().phase());
        assertEquals(Result.IN_PROGRESS, result().result());
        assertEquals(ATTACK_DURATION, round().remainingSeconds(), 0.001);
    }

    @Test
    void noOverwriteWhenResultAlreadySet() {
        // Mark WIN before the final-round timer can write LOSS
        system.beginAttackPhase();
        ed.setComponent(gsId, new RoundComponent(4, 4, 60.0));
        ed.setComponent(gsId, new GameResultComponent(Result.WIN));

        system.update(61.0f);

        assertEquals(Result.WIN, result().result());
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private RoundComponent round() { return ed.getComponent(gsId, RoundComponent.class); }
    private PhaseComponent phase() { return ed.getComponent(gsId, PhaseComponent.class); }
    private GameResultComponent result() { return ed.getComponent(gsId, GameResultComponent.class); }
}
