package org.metamechanists.kinematiccore.api;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;


public class TestListener implements Listener {
    @EventHandler
    public static void onJoin(@NotNull PlayerJoinEvent e) {
        new TestEntity(e.getPlayer().getLocation());
    }
}
