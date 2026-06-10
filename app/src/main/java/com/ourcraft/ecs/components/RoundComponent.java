package com.ourcraft.ecs.components;

import com.simsilica.es.EntityComponent;

public record RoundComponent(int currentRound, int maxRounds, double remainingSeconds) implements EntityComponent {}
