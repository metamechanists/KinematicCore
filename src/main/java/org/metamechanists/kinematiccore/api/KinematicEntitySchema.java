package org.metamechanists.kinematiccore.api;

import org.bukkit.entity.Entity;


public record KinematicEntitySchema(String id, Class<? extends KinematicEntity<?>> kinematicClass, Class<? extends Entity> entityClass) {}
