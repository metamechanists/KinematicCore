package org.metamechanists.kinematiccore.test;


import org.bukkit.Location;
import org.bukkit.WorldBorder;
import org.jetbrains.annotations.NotNull;
import org.metamechanists.kinematiccore.test.entity.TestDiskFieldStorageSuccess;
import org.metamechanists.kinematiccore.test.entity.TestDiskStorageSuccess;
import org.metamechanists.kinematiccore.test.entity.TestDoubleRegister;
import org.metamechanists.kinematiccore.test.entity.TestEntityTypeMismatch;
import org.metamechanists.kinematiccore.test.entity.TestMemoryStorageSuccess;
import org.metamechanists.kinematiccore.test.entity.TestMissingConstructor;
import org.metamechanists.kinematiccore.test.entity.TestUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public final class MainTester {
    private final List<BaseTest> nonDestructiveTests = List.of(
            new TestDoubleRegister(),
            new TestEntityTypeMismatch(),
            new TestMissingConstructor(),
            new TestMemoryStorageSuccess(),
            new TestDiskStorageSuccess(),
            new TestDiskFieldStorageSuccess()
    );

    public record TestResult(int total, int passed, int failed, List<String> failures) {}

    public MainTester(@NotNull Location center) {
        loaded = center.clone();
        loaded.setY(310);

        Location unloaded = loaded;
        WorldBorder border = unloaded.getWorld().getWorldBorder();
        int max = (int) Math.min(border.getSize(), 10000);
        while (unloaded.isChunkLoaded()) {
            unloaded = border.getCenter().clone().add(random.nextInt(max), 1000, random.nextInt(max));
        }
        this.unloaded = unloaded;
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
                failures.add(test.getClass().getSimpleName());
            }
        }

        return new TestResult(total, passed, failed, failures);
    }

    private boolean test(@NotNull BaseTest test) {
        try {
            test.test(loaded, unloaded);
        } catch (Exception | AssertionError e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}
