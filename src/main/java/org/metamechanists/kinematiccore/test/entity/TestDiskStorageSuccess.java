package org.metamechanists.kinematiccore.test.entity;

import org.bukkit.Location;
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
    private static class TestEntity extends KinematicEntity<Pig> {
        private static final KinematicEntitySchema SCHEMA = new KinematicEntitySchema(
                "test_disk_storage_success",
                KinematicCore.class,
                TestEntity.class,
                Pig.class
        );

        static {
            SCHEMA.register();
        }

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

    @SuppressWarnings("CodeBlock2Expr")
    @Override
    public void test(@NotNull Location loaded, @NotNull Location unloaded) {
        TestUtil.loadChunk(unloaded);

        AtomicReference<UUID> uuid = new AtomicReference<>();
        TestUtil.runSync(() -> uuid.set(new TestEntity(unloaded).uuid()));

        TestUtil.unloadChunk(unloaded);

        TestUtil.runSync(() -> {
            assertThat(EntityStorage.kinematicEntity(uuid.get()))
                    .isNull();
            assertThat(EntitySchemas.schema(TestEntity.SCHEMA.getId()))
                    .isEqualTo(TestEntity.SCHEMA);
            assertThat(EntityStorage.loadedEntitiesByType(TestEntity.SCHEMA))
                    .isEmpty();
            assertThat(uuid.get())
                    .isNull();
        });

        TestUtil.loadChunk(unloaded);

        TestUtil.runSync(() -> {
            assertThat(EntitySchemas.schema(TestEntity.SCHEMA.getId()))
                    .isEqualTo(TestEntity.SCHEMA);
            assertThat(EntityStorage.loadedEntitiesByType(TestEntity.SCHEMA))
                    .hasSize(1)
                    .contains(uuid.get());
            assertThat(uuid.get())
                    .isNotNull();
        });

        TestUtil.runSync(() -> {
            EntityStorage.kinematicEntity(uuid.get()).entity().remove();
        });

        TestUtil.unloadChunk(unloaded);

        TestUtil.loadChunk(unloaded);

        TestUtil.runSync(() -> {
            assertThat(EntityStorage.kinematicEntity(uuid.get()))
                    .isNull();
            assertThat(EntitySchemas.schema(TestEntity.SCHEMA.getId()))
                    .isEqualTo(TestEntity.SCHEMA);
            assertThat(EntityStorage.loadedEntitiesByType(TestEntity.SCHEMA))
                    .isEmpty();
            assertThat(uuid.get())
                    .isNull();
        });

        TestUtil.unloadChunk(unloaded);
    }
}
