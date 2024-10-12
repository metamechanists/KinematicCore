package org.metamechanists.kinematiccore.internal.addon;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.jetbrains.annotations.NotNull;
import org.metamechanists.kinematiccore.KinematicCore;
import org.metamechanists.kinematiccore.api.addon.KinematicAddon;
import org.metamechanists.kinematiccore.internal.entity.EntityStorage;


/*
 * The disable order of addons cannot be guaranteed
 * So when an addon is disabled, we save all the data associated with it just before it is disabled
 * Then, when KinematicCore is disabled, we save all the data associated with all the addons that
 * have not been disabled yet
 */
public class AddonLifecycle implements Listener {
    public static void init() {
        Bukkit.getPluginManager().registerEvents(new AddonLifecycle(), KinematicCore.getInstance());
    }

    @EventHandler
    public static void onAddonDisable(@NotNull PluginDisableEvent event) {
        if (event.getPlugin() instanceof KinematicAddon addon) {
            KinematicCore.getInstance().getLogger().info("Cleaning up addon " + addon.getClass().getSimpleName());
            EntityStorage.cleanup(addon);
        }
    }

    public static void cleanup() {
        for (KinematicAddon addon : AddonStorage.getLoadedAddons()) {
            KinematicCore.getInstance().getLogger().info("Cleaning up addon " + addon.getClass().getSimpleName());
            EntityStorage.cleanup(addon);
        }
    }
}
