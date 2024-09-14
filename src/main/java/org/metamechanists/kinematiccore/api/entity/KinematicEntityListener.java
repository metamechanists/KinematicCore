package org.metamechanists.kinematiccore.api.entity;


import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.jetbrains.annotations.NotNull;
import org.metamechanists.kinematiccore.KinematicCore;
import org.metamechanists.kinematiccore.api.storage.EntityStorage;


public class KinematicEntityListener implements Listener {
    public static void init() {
        Bukkit.getPluginManager().registerEvents(new KinematicEntityListener(), KinematicCore.getInstance());
    }

    @EventHandler
    public static void onRightClick(@NotNull PlayerInteractEntityEvent event) {
        Bukkit.getLogger().severe("1");
        KinematicEntity<?, ?> kinematicEntity = EntityStorage.kinematicEntity(event.getRightClicked().getUniqueId());
        if (kinematicEntity != null) {
            Bukkit.getLogger().severe("2");
            kinematicEntity.onRightClick(event.getPlayer());
        }
    }
}
