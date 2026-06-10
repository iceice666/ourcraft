package com.ourcraft.ecs.components;

import com.simsilica.es.EntityComponent;

public record GameResultComponent(Result result) implements EntityComponent {
    public enum Result { IN_PROGRESS, WIN, LOSS }
}
