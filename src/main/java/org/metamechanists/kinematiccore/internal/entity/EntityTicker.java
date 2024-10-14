package org.metamechanists.kinematiccore.internal.entity;

import org.bukkit.Bukkit;
import org.metamechanists.kinematiccore.KinematicCore;
import org.metamechanists.kinematiccore.api.entity.KinematicEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;


public class EntityTicker implements Runnable {
public class KinematicEntityTicker implements Runnable {
    private static class ErrorTracker {
        private static final int MAX_ERRORS = 5;
        private static final int TRACKER_TIME = 30;

        private int errors;
        private int ticksSinceLastError;

        public ErrorTracker() {
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

    private static final int INTERVAL = 1;
    private static final Map<UUID, ErrorTracker> errorTrackers = new HashMap<>();

    @Override
    public void run() {
        long tick = Bukkit.getCurrentTick();
        for (Map.Entry<String, Set<UUID>> kinematicEntityType : EntityStorage.allLoadedEntitiesByType().entrySet()) {
            for (UUID uuid : kinematicEntityType.getValue()) {
                KinematicEntity<?, ?> kinematicEntity = EntityStorage.kinematicEntity(uuid);

                try {
                    //noinspection DataFlowIssue
                    kinematicEntity.tick(tick);
                } catch (RuntimeException e) {
                    KinematicCore.getInstance().getLogger().severe("Failed to tick " + uuid);
                    if (kinematicEntity.schema() != null) {
                        KinematicCore.getInstance().getLogger().severe("Failed to tick " + kinematicEntity.schema().getId());
                    } else {
                        KinematicCore.getInstance().getLogger().severe("Failed to tick " + uuid);
                    }
                    errorTrackers.computeIfAbsent(uuid, k -> new ErrorTracker()).incrementErrors();
                    e.printStackTrace();
                }
            }
        }

        List<UUID> trackersToRemove = new ArrayList<>();
        for (Map.Entry<UUID, ErrorTracker> entry : errorTrackers.entrySet()) {
            if (entry.getValue().shouldDeleteEntity()) {
                KinematicEntity<?, ?> kinematicEntity = EntityStorage.kinematicEntity(entry.getKey());
                assert kinematicEntity != null;
                kinematicEntity.remove();
            }
            if (entry.getValue().shouldRemoveTracker()) {
                trackersToRemove.add(entry.getKey());
            }
        }
        for (UUID uuid : trackersToRemove) {
            errorTrackers.remove(uuid);
        }
    }

    public static void init() {
        Bukkit.getServer().getScheduler().runTaskTimer(KinematicCore.getInstance(), new EntityTicker(), 0, INTERVAL);
    }
}
