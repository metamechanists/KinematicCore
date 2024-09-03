package org.metamechanists.kinematiccore.api;

enum StateType {
    STRING,
    BYTE,
    BOOLEAN,
    SHORT,
    INTEGER,
    LONG,
    FLOAT,
    DOUBLE,
    UUID;

    // Cache for perfomance
    public static final StateType[] VALUES = values();
}