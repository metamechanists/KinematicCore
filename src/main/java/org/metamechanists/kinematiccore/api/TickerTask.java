package org.metamechanists.kinematiccore.api;

import org.bukkit.Bukkit;
import org.metamechanists.kinematiccore.KinematicCore;

import java.util.Map;
import java.util.Set;
import java.util.UUID;


public class TickerTask implements Runnable {
    @Override
    public void run() {
        long tick = Bukkit.getCurrentTick();
        for (Map.Entry<String, Set<UUID>> kinematicEntityType : EntityStorage.allLoadedEntitiesByType().entrySet()) {
            for (UUID uuid : kinematicEntityType.getValue()) {
                EntityStorage.kinematicEntity(uuid).tick(tick);
            }
        }
    }

    public static void init() {
        Bukkit.getServer().getScheduler().runTaskTimer(KinematicCore.getInstance(), new TickerTask(), 0, 1);
    }
}
