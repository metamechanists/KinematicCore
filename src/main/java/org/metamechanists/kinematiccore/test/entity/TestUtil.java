package org.metamechanists.kinematiccore.test.entity;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;


public final class TestUtil {
    private TestUtil() {}

    public static boolean isChunkLoaded(@NotNull Location location) {
        return location.getWorld().isChunkLoaded(location.getBlockX() / 16, location.getBlockZ() / 16);
    }

    public static void loadChunk(@NotNull Location location) {
        location.getWorld().loadChunk(location.getBlockX() / 16, location.getBlockZ() / 16);
    }

    public static void unloadChunk(@NotNull Location location) {
        location.getWorld().unloadChunk(location.getBlockX() / 16, location.getBlockZ() / 16);
    }
}
