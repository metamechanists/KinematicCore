package org.metamechanists.kinematiccore.test;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Pig;
import org.jetbrains.annotations.NotNull;
import org.metamechanists.kinematiccore.KinematicCore;
import org.metamechanists.kinematiccore.api.EntityStorage;
import org.metamechanists.kinematiccore.api.KinematicEntity;
import org.metamechanists.kinematiccore.api.KinematicEntitySchema;
import org.metamechanists.kinematiccore.api.StateReader;
import org.metamechanists.kinematiccore.api.StateWriter;

import static org.assertj.core.api.Assertions.*;


public class TestMemoryStorageSuccess implements BaseTest  {
    private static class TestEntity extends KinematicEntity<Pig> {
        private static final KinematicEntitySchema SCHEMA = new KinematicEntitySchema(
                "test_simple_kinematic_entity",
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

        @SuppressWarnings("unused")
        public TestEntity(@NotNull StateReader reader) {
            super(SCHEMA, reader);
        }

        @Override
        protected void write(@NotNull StateWriter writer) {}
    }

    @Override
    public void test(@NotNull Location loaded, @NotNull Location unloaded) {
        TestEntity kinematicEntity = new TestEntity(loaded);

        assertThat(EntityStorage.kinematicEntity(kinematicEntity.uuid()))
                .isEqualTo(kinematicEntity);
        assertThat(EntityStorage.schema(TestEntity.SCHEMA.getId()))
                .isEqualTo(TestEntity.SCHEMA);
        assertThat(EntityStorage.loadedEntitiesByType(TestEntity.SCHEMA.getId()))
                .hasSize(1)
                .contains(kinematicEntity.uuid());
        assertThat(kinematicEntity.entity())
                .isNotNull();

        kinematicEntity.remove();

        assertThat(EntityStorage.kinematicEntity(kinematicEntity.uuid()))
                .isNull();
        assertThat(EntityStorage.schema(TestEntity.SCHEMA.getId()))
                .isEqualTo(TestEntity.SCHEMA);
        assertThat(EntityStorage.loadedEntitiesByType(TestEntity.SCHEMA.getId()))
                .isEmpty();
        assertThat(kinematicEntity.entity())
                .isNull();

        assertThat(true).isFalse();
    }
}
