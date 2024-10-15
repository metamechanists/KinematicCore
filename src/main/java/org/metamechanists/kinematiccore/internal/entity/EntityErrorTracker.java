package org.metamechanists.kinematiccore.internal.entity;

class EntityErrorTracker {
    public static final int MAX_ERRORS = 5;
    public static final int TRACKER_TIME = 30;

    private int errors;
    private int ticksSinceLastError;

    EntityErrorTracker() {
        errors = 0;
        ticksSinceLastError = 0;
    }

    public void incrementErrors() {
        errors++;
        ticksSinceLastError = 0;
    }

    public void tick() {
        ticksSinceLastError++;
    }

    public boolean shouldDeleteEntity() {
        return errors > MAX_ERRORS;
    }

    public boolean shouldRemoveTracker() {
        return ticksSinceLastError > TRACKER_TIME;
    }
}
