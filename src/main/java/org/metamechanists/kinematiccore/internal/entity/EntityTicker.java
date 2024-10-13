package org.metamechanists.kinematiccore.internal.entity;

import org.bukkit.Bukkit;
import org.metamechanists.kinematiccore.KinematicCore;
import org.metamechanists.kinematiccore.api.entity.KinematicEntity;

import java.util.Map;
import java.util.Set;
import java.util.UUID;


public class EntityTicker implements Runnable {
    private static final int INTERVAL = 1;

    @Override
    public void run() {
        long tick = Bukkit.getCurrentTick();
        for (Map.Entry<String, Set<UUID>> kinematicEntityType : EntityStorage.allLoadedEntitiesByType().entrySet()) {
            for (UUID uuid : kinematicEntityType.getValue()) {
                KinematicEntity<?, ?> kinematicEntity = EntityStorage.kinematicEntity(uuid);

                try {
                    kinematicEntity.tick(tick);
                } catch (RuntimeException e) {
                    KinematicCore.getInstance().getLogger().severe("Failed to tick " + uuid);
                    e.printStackTrace();
                }
            }
        }
    }

    public static void init() {
        Bukkit.getServer().getScheduler().runTaskTimer(KinematicCore.getInstance(), new EntityTicker(), 0, INTERVAL);
    }
}
