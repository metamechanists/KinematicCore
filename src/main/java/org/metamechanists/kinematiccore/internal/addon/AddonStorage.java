package org.metamechanists.kinematiccore.internal.addon;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.jetbrains.annotations.NotNull;
import org.metamechanists.kinematiccore.KinematicCore;
import org.metamechanists.kinematiccore.api.addon.KinematicAddon;

import java.util.HashSet;
import java.util.Set;


public class AddonStorage implements Listener {
    @Getter
    private static final Set<KinematicAddon> loadedAddons = new HashSet<>();

    public static void init() {
        Bukkit.getPluginManager().registerEvents(new AddonStorage(), KinematicCore.getInstance());
    }

    @EventHandler
    private static void onAddonEnable(@NotNull PluginEnableEvent event) {
        if (event.getPlugin() instanceof KinematicAddon addon) {
            Bukkit.getLogger().severe("oh no");
            loadedAddons.add(addon);
        }
    }

    @EventHandler
    private static void onAddonDisable(@NotNull PluginDisableEvent event) {
        if (event.getPlugin() instanceof KinematicAddon addon) {
            Bukkit.getLogger().severe("bruh");
            loadedAddons.remove(addon);
        }
    }
}
