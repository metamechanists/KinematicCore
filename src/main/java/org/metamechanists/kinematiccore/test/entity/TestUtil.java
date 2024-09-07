package org.metamechanists.kinematiccore.test.entity;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.metamechanists.kinematiccore.KinematicCore;


public final class TestUtil {
    private static final long TICKS_TO_WAIT_AFTER_CHUNK_OPERATION = 3;

    private TestUtil() {}

    public static boolean isChunkLoaded(@NotNull Location location) {
        return location.getWorld().isChunkLoaded(location.getBlockX() / 16, location.getBlockZ() / 16);
    }

    public static void loadChunk(@NotNull Location location, Runnable onLoad) {
        location.getWorld().loadChunk(location.getBlockX() / 16, location.getBlockZ() / 16);
        Bukkit.getScheduler().runTaskLater(KinematicCore.getInstance(), onLoad, TICKS_TO_WAIT_AFTER_CHUNK_OPERATION);
    }

    public static void unloadChunk(@NotNull Location location, Runnable onUnload) {
        location.getWorld().unloadChunk(location.getBlockX() / 16, location.getBlockZ() / 16);
        Bukkit.getScheduler().runTaskLater(KinematicCore.getInstance(), onUnload, TICKS_TO_WAIT_AFTER_CHUNK_OPERATION);
    }
}
