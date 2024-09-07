package org.metamechanists.kinematiccore.test.entity;

import org.bukkit.Location;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Pig;
import org.jetbrains.annotations.NotNull;
import org.metamechanists.kinematiccore.KinematicCore;
import org.metamechanists.kinematiccore.api.EntityStorage;
import org.metamechanists.kinematiccore.api.Exceptions;
import org.metamechanists.kinematiccore.api.KinematicEntity;
import org.metamechanists.kinematiccore.api.KinematicEntitySchema;
import org.metamechanists.kinematiccore.api.StateReader;
import org.metamechanists.kinematiccore.api.StateWriter;
import org.metamechanists.kinematiccore.test.BaseTest;

import static org.assertj.core.api.Assertions.*;


public class TestEntityTypeMismatch implements BaseTest {
    private static class TestEntity extends KinematicEntity<Pig> {
        private static final KinematicEntitySchema SCHEMA = new KinematicEntitySchema(
                "test_entity_type_mismatch",
                KinematicCore.class,
                TestEntity.class,
                Cow.class
        );

        static {
            EntityStorage.register(SCHEMA);
        }

        public TestEntity(@NotNull Location location) {
            super(SCHEMA, () -> location.getWorld().spawn(location, Pig.class));
        }

        @SuppressWarnings("unused")
        public TestEntity(@NotNull StateReader reader) {
            super(SCHEMA, reader);
        }

        @Override
        protected void write(@NotNull StateWriter writer) {}
    }

    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    @Override
    public void test(@NotNull Location loaded, @NotNull Location unloaded) {
        assertThatThrownBy(() -> new TestEntity(loaded))
                .isInstanceOf(Exceptions.EntityTypeMismatchException.class);
    }
}