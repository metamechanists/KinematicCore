package org.metamechanists.kinematiccore.api;

public record KinematicEntitySchema(String id, Class<? extends KinematicEntity<?>> clazz) {}
