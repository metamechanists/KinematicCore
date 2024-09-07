package org.metamechanists.kinematiccore.api.entity;

import org.bukkit.Bukkit;
import org.metamechanists.kinematiccore.KinematicCore;
import org.metamechanists.kinematiccore.api.storage.EntityStorage;

import java.util.Map;
import java.util.Set;
import java.util.UUID;


public class KinematicEntityTicker implements Runnable {
    private static final int INTERVAL = 1;

    @Override
    public void run() {
        long tick = Bukkit.getCurrentTick();
        for (Map.Entry<String, Set<UUID>> kinematicEntityType : EntityStorage.allLoadedEntitiesByType().entrySet()) {
            for (UUID uuid : kinematicEntityType.getValue()) {
                KinematicEntity<?> kinematicEntity = EntityStorage.kinematicEntity(uuid);

                try {
                    kinematicEntity.tick(tick);
                } catch (RuntimeException e) {
                    KinematicCore.getInstance().getLogger().severe("Failed to tick " + kinematicEntity.schema().getId());
                    e.printStackTrace();
                }
            }
        }
    }

    public static void init() {
        Bukkit.getServer().getScheduler().runTaskTimer(KinematicCore.getInstance(), new KinematicEntityTicker(), 0, INTERVAL);
    }
}
