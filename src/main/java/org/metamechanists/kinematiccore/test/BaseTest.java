package org.metamechanists.kinematiccore.test;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;


@FunctionalInterface
public interface BaseTest {
    void test(@NotNull Location loaded, @NotNull Location unloaded);
}
