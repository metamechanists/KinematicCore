package org.metamechanists.kinematiccore.test.entity;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Pig;
import org.jetbrains.annotations.NotNull;
import org.metamechanists.kinematiccore.KinematicCore;
import org.metamechanists.kinematiccore.api.storage.EntitySchemas;
import org.metamechanists.kinematiccore.api.storage.EntityStorage;
import org.metamechanists.kinematiccore.api.entity.KinematicEntity;
import org.metamechanists.kinematiccore.api.entity.KinematicEntitySchema;
import org.metamechanists.kinematiccore.api.storage.StateReader;
import org.metamechanists.kinematiccore.api.storage.StateWriter;
import org.metamechanists.kinematiccore.test.BaseTest;

import static org.assertj.core.api.Assertions.*;


public class TestMemoryStorageSuccess implements BaseTest {
    private static class TestEntity extends KinematicEntity<Pig, KinematicEntitySchema> {
        private static final KinematicEntitySchema SCHEMA = new KinematicEntitySchema(
                "test_memory_storage_success",
                KinematicCore.class,
                TestEntity.class,
                Pig.class
        );

        public TestEntity(@NotNull Location location) {
            super(SCHEMA, () -> location.getWorld().spawn(location, Pig.class));
        }

        @SuppressWarnings("unused")
        public TestEntity(@NotNull StateReader reader) {
            super(reader);
        }

        @Override
        public void write(@NotNull StateWriter writer) {}
    }

    @Override
    public void test(World world) {
        Location location = TestUtil.findUnloadedChunk(world);

        TestUtil.loadChunk(location);

        TestUtil.runSync(() -> {
            TestEntity kinematicEntity = new TestEntity(location);

            assertThat(EntityStorage.kinematicEntity(kinematicEntity.uuid()))
                    .isEqualTo(kinematicEntity);
            assertThat(EntitySchemas.schema(TestEntity.SCHEMA.getId()))
                    .isEqualTo(TestEntity.SCHEMA);
            assertThat(EntityStorage.loadedEntitiesByType(TestEntity.SCHEMA))
                    .hasSize(1)
                    .contains(kinematicEntity.uuid());
            assertThat(kinematicEntity.entity())
                    .isNotNull();

            //noinspection DataFlowIssue
            kinematicEntity.entity().remove();

            assertThat(EntityStorage.kinematicEntity(kinematicEntity.uuid()))
                    .isNull();
            assertThat(EntitySchemas.schema(TestEntity.SCHEMA.getId()))
                    .isEqualTo(TestEntity.SCHEMA);
            assertThat(EntityStorage.loadedEntitiesByType(TestEntity.SCHEMA))
                    .isEmpty();
            assertThat(kinematicEntity.entity())
                    .isNull();
        });

        TestUtil.unloadChunk(location);
    }
}
