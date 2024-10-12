package org.metamechanists.kinematiccore.internal.addon;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.jetbrains.annotations.NotNull;
import org.metamechanists.kinematiccore.KinematicCore;
import org.metamechanists.kinematiccore.api.addon.KinematicAddon;
import org.metamechanists.kinematiccore.internal.entity.EntityStorage;


public class AddonLifecycle implements Listener {
    public static void init() {
        Bukkit.getPluginManager().registerEvents(new AddonStorage(), KinematicCore.getInstance());
    }

    @EventHandler
    public static void onAddonDisable(@NotNull PluginDisableEvent event) {
        if (event.getPlugin() instanceof KinematicAddon addon) {
            EntityStorage.cleanup(addon);
        }
    }

    public static void cleanup() {
        for (KinematicAddon addon : AddonStorage.getLoadedAddons()) {
            EntityStorage.cleanup(addon);
        }
    }
}
