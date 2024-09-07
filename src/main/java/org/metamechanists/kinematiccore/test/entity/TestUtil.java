package org.metamechanists.kinematiccore.test.entity;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.metamechanists.kinematiccore.KinematicCore;

import java.util.concurrent.CompletableFuture;


public final class TestUtil {
    private static final long EXTRA_MILLISECONDS_TO_WAIT = 50;

    private TestUtil() {}

    public static boolean isChunkLoaded(@NotNull Location location) {
        return location.getWorld().isChunkLoaded(location.getBlockX() / 16, location.getBlockZ() / 16);
    }

    public static void loadChunk(@NotNull Location location) {
        runSync(() -> location.getWorld().loadChunk(location.getBlockX() / 16, location.getBlockZ() / 16));

        try {
            Thread.sleep(EXTRA_MILLISECONDS_TO_WAIT);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void unloadChunk(@NotNull Location location) {
        runSync(() -> location.getWorld().unloadChunk(location.getBlockX() / 16, location.getBlockZ() / 16));

        try {
            Thread.sleep(EXTRA_MILLISECONDS_TO_WAIT);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void runSync(@NotNull Runnable runnable) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        Bukkit.getScheduler().runTask(KinematicCore.getInstance(), () -> {
            try {
                runnable.run();
            } catch (Exception | AssertionError e) {
                future.completeExceptionally(e);
            }
            future.complete(null);
        });

        future.join();
    }
}