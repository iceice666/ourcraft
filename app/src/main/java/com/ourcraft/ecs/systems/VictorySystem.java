package com.ourcraft.ecs.systems;

import com.ourcraft.ecs.components.BlockComponent;
import com.ourcraft.ecs.components.GameResultComponent;
import com.ourcraft.ecs.components.GameResultComponent.Result;
import com.ourcraft.ecs.components.PhaseComponent;
import com.ourcraft.ecs.components.PhaseComponent.Phase;
import com.ourcraft.ecs.components.RoundComponent;
import com.simsilica.es.EntityData;
import com.simsilica.es.EntityId;
import com.simsilica.es.EntitySet;

public class VictorySystem {

    private final EntityData ed;
    private final EntityId gameStateId;
    private final EntitySet blocks;

    public VictorySystem(EntityData ed, EntityId gameStateId) {
        this.ed = ed;
        this.gameStateId = gameStateId;
        this.blocks = ed.getEntities(BlockComponent.class);
    }

    public void update(float tpf) {
        GameResultComponent result = ed.getComponent(gameStateId, GameResultComponent.class);
        if (result.result() != Result.IN_PROGRESS) return;

        PhaseComponent phase = ed.getComponent(gameStateId, PhaseComponent.class);
        if (phase.phase() != Phase.ATTACK) return;

        blocks.applyChanges();
        int blockCount = blocks.size();

        if (blockCount == 0) {
            ed.setComponent(gameStateId, new GameResultComponent(Result.WIN));
            return;
        }

        RoundComponent round = ed.getComponent(gameStateId, RoundComponent.class);
        if (round.remainingSeconds() <= 0.0 && round.currentRound() == round.maxRounds()) {
            ed.setComponent(gameStateId, new GameResultComponent(Result.LOSS));
        }
    }

    public void close() {
        blocks.release();
    }
}
