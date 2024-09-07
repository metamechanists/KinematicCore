package org.metamechanists.kinematiccore.test;


import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;


public final class MainTester {
    private final Location loaded;
    private final Location unloaded;
    private final List<BaseTest> nonDestructiveTests = List.of(
            new TestMemoryStorageSuccess()
    );

    public record TestResult(int total, int passed, int failed, List<String> failures) {}

    public MainTester(@NotNull Location center) {
        loaded = center.clone();
        loaded.setY(310);

        unloaded = loaded.clone();
        // Using getChunk() would load the chunk (lol)
        while (unloaded.getWorld().isChunkLoaded(unloaded.getBlockX() / 16, unloaded.getBlockZ() / 16)) {
            unloaded.add(16, 0, 0);
        }
    }

    public @NotNull TestResult allNonDestructive() {
        int total = 0;
        int passed = 0;
        int failed = 0;
        List<String> failures = new ArrayList<>();

        for (BaseTest test : nonDestructiveTests) {
            boolean success = test(test);
            total += 1;
            if (success) {
                passed += 1;
            } else {
                failed += 1;
                failures.add(test.getClass().getName());
            }
        }

        return new TestResult(total, passed, failed, failures);
    }

    private boolean test(@NotNull BaseTest test) {
        try {
            test.test(loaded, unloaded);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}
