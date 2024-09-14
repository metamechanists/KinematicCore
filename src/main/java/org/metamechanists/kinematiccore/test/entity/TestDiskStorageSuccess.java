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

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.*;


public class TestDiskStorageSuccess implements BaseTest {
    private static class TestEntity extends KinematicEntity<Pig, KinematicEntitySchema> {
        private static final KinematicEntitySchema SCHEMA = new KinematicEntitySchema(
                "test_disk_storage_success",
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

        AtomicReference<UUID> uuid = new AtomicReference<>();
        TestUtil.runSync(() -> uuid.set(new TestEntity(location).uuid()));

        TestUtil.unloadChunk(location);

        TestUtil.runSync(() -> {
            assertThat(EntityStorage.kinematicEntity(uuid.get()))
                    .isNull();
            assertThat(EntitySchemas.schema(TestEntity.SCHEMA.getId()))
                    .isEqualTo(TestEntity.SCHEMA);
            assertThat(EntityStorage.loadedEntitiesByType(TestEntity.SCHEMA))
                    .isEmpty();
        });

        TestUtil.loadChunk(location);

        TestUtil.runSync(() -> {
            assertThat(EntitySchemas.schema(TestEntity.SCHEMA.getId()))
                    .isEqualTo(TestEntity.SCHEMA);
            assertThat(EntityStorage.loadedEntitiesByType(TestEntity.SCHEMA))
                    .hasSize(1)
                    .contains(uuid.get());
        });

        TestUtil.runSync(() -> {
            //noinspection DataFlowIssue
            EntityStorage.kinematicEntity(uuid.get()).entity().remove();
        });

        TestUtil.unloadChunk(location);

        TestUtil.loadChunk(location);

        TestUtil.runSync(() -> {
            assertThat(EntityStorage.kinematicEntity(uuid.get()))
                    .isNull();
            assertThat(EntitySchemas.schema(TestEntity.SCHEMA.getId()))
                    .isEqualTo(TestEntity.SCHEMA);
            assertThat(EntityStorage.loadedEntitiesByType(TestEntity.SCHEMA))
                    .isEmpty();
        });

        TestUtil.unloadChunk(location);
    }
}
