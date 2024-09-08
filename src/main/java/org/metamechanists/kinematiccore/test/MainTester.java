package org.metamechanists.kinematiccore.test;


import org.bukkit.Location;
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


public final class MainTester {
    private static final int MIN_UNLOADED_DISTANCE = 256;
    private final Location loaded;
    private final Location unloaded;
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

        unloaded = loaded.clone();
        // Using getChunk() would load the chunk (lol)
        int distance = 0;
        while (TestUtil.isChunkLoaded(unloaded) || distance < MIN_UNLOADED_DISTANCE) {
            unloaded.add(16, 0, 0);
            distance += 16;
        }


        unloaded.add(200, 0, 0);
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
