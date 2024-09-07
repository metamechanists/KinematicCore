package org.metamechanists.kinematiccore.test.entity;

import org.bukkit.Location;
import org.bukkit.entity.Pig;
import org.jetbrains.annotations.NotNull;
import org.metamechanists.kinematiccore.KinematicCore;
import org.metamechanists.kinematiccore.api.EntityStorage;
import org.metamechanists.kinematiccore.api.Exceptions;
import org.metamechanists.kinematiccore.api.KinematicEntity;
import org.metamechanists.kinematiccore.api.KinematicEntitySchema;
import org.metamechanists.kinematiccore.api.StateWriter;
import org.metamechanists.kinematiccore.test.BaseTest;

import static org.assertj.core.api.Assertions.*;


public class TestMissingConstructor implements BaseTest {
    private static class TestEntity extends KinematicEntity<Pig> {
        private static final KinematicEntitySchema SCHEMA = new KinematicEntitySchema(
                "test_missing_constructor",
                KinematicCore.class,
                TestEntity.class,
                Pig.class
        );

        static {
            EntityStorage.register(SCHEMA);
        }

        public TestEntity(@NotNull Location location) {
            super(SCHEMA, () -> location.getWorld().spawn(location, Pig.class));
        }

        @Override
        protected void write(@NotNull StateWriter writer) {}
    }

    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    @Override
    public void test(@NotNull Location loaded, @NotNull Location unloaded) {
        assertThatThrownBy(() -> new TestEntity(loaded))
                .isInstanceOf(Exceptions.MissingConstructorException.class);
    }
}
