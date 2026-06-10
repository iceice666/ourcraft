package com.ourcraft.ecs.systems;

import com.ourcraft.ecs.components.GameResultComponent;
import com.ourcraft.ecs.components.GameResultComponent.Result;
import com.ourcraft.ecs.components.PhaseComponent;
import com.ourcraft.ecs.components.PhaseComponent.Phase;
import com.ourcraft.ecs.components.RoundComponent;
import com.simsilica.es.EntityData;
import com.simsilica.es.EntityId;

public class RoundSystem {

    public static final double ATTACK_DURATION = 60.0;

    private final EntityData ed;
    private EntityId gameStateId;

    public RoundSystem(EntityData ed) {
        this.ed = ed;
    }

    public void initialize() {
        gameStateId = ed.createEntity();
        ed.setComponents(gameStateId,
            new RoundComponent(1, 4, ATTACK_DURATION),
            new PhaseComponent(Phase.BUILD),
            new GameResultComponent(Result.IN_PROGRESS));
    }

    public EntityId getGameStateId() {
        return gameStateId;
    }

    /** Transitions from BUILD to ATTACK, resetting the attack timer. */
    public void beginAttackPhase() {
        RoundComponent round = ed.getComponent(gameStateId, RoundComponent.class);
        ed.setComponents(gameStateId,
            new PhaseComponent(Phase.ATTACK),
            new RoundComponent(round.currentRound(), round.maxRounds(), ATTACK_DURATION));
    }

    public void update(float tpf) {
        GameResultComponent result = ed.getComponent(gameStateId, GameResultComponent.class);
        if (result.result() != Result.IN_PROGRESS) return;

        PhaseComponent phase = ed.getComponent(gameStateId, PhaseComponent.class);
        if (phase.phase() != Phase.ATTACK) return;

        RoundComponent round = ed.getComponent(gameStateId, RoundComponent.class);
        if (round.remainingSeconds() <= 0.0) return; // expiry already processed

        double newRemaining = Math.max(0.0, round.remainingSeconds() - tpf);

        if (newRemaining <= 0.0 && round.currentRound() < round.maxRounds()) {
            ed.setComponents(gameStateId,
                new RoundComponent(round.currentRound() + 1, round.maxRounds(), ATTACK_DURATION),
                new PhaseComponent(Phase.BUILD));
        } else {
            ed.setComponent(gameStateId,
                new RoundComponent(round.currentRound(), round.maxRounds(), newRemaining));
        }
    }
}
