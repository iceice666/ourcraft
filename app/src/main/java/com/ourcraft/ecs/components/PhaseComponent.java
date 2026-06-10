package com.ourcraft.ecs.components;

import com.simsilica.es.EntityComponent;

public record PhaseComponent(Phase phase) implements EntityComponent {
    public enum Phase { BUILD, ATTACK }
}
