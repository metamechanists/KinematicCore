package org.metamechanists.kinematiccore.test.entity;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Pig;
import org.jetbrains.annotations.NotNull;
import org.metamechanists.kinematiccore.KinematicCore;
import org.metamechanists.kinematiccore.api.Exceptions;
import org.metamechanists.kinematiccore.api.entity.KinematicEntity;
import org.metamechanists.kinematiccore.api.entity.KinematicEntitySchema;
import org.metamechanists.kinematiccore.api.state.StateReader;
import org.metamechanists.kinematiccore.test.BaseTest;

import static org.assertj.core.api.Assertions.*;


public class TestDoubleRegister implements BaseTest {
    private static class TestEntity extends KinematicEntity<Pig, KinematicEntitySchema> {
        private static final KinematicEntitySchema SCHEMA = new KinematicEntitySchema(
                "test_double_register",
                KinematicCore.class,
                TestEntity.class,
                Cow.class
        );

        @SuppressWarnings("unused")
        private TestEntity(@NotNull Location location) {
            super(SCHEMA, () -> location.getWorld().spawn(location, Pig.class));
        }

        @SuppressWarnings("unused")
        private TestEntity(@NotNull StateReader reader) {
            super(reader);
        }
    }

    @Override
    public void test(World world) {
        TestUtil.runSync(() -> {
            if (KinematicEntitySchema.get(TestEntity.SCHEMA.getId()) == null) {
                Bukkit.getLogger().info("1");
                KinematicEntitySchema.register(TestEntity.SCHEMA);
            }
            Bukkit.getLogger().info("2");

            assertThatThrownBy(() -> KinematicEntitySchema.register(TestEntity.SCHEMA))
                    .isInstanceOf(Exceptions.IdConflictException.class);
            Bukkit.getLogger().info("3");
        });
    }
}
