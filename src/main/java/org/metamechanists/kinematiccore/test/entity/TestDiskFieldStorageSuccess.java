package org.metamechanists.kinematiccore.test.entity;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Pig;
import org.jetbrains.annotations.NotNull;
import org.metamechanists.kinematiccore.KinematicCore;
import org.metamechanists.kinematiccore.api.entity.KinematicEntity;
import org.metamechanists.kinematiccore.api.entity.KinematicEntitySchema;
import org.metamechanists.kinematiccore.api.storage.EntitySchemas;
import org.metamechanists.kinematiccore.api.storage.EntityStorage;
import org.metamechanists.kinematiccore.api.storage.StateReader;
import org.metamechanists.kinematiccore.api.storage.StateWriter;
import org.metamechanists.kinematiccore.test.BaseTest;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.*;


public class TestDiskFieldStorageSuccess implements BaseTest {
    @Getter
    private static class TestEntity extends KinematicEntity<Pig> {
        private int integer;
        private final List<String> list;
        private final Location location;

        private static final KinematicEntitySchema SCHEMA = new KinematicEntitySchema(
                "test_disk_field_storage_success",
                KinematicCore.class,
                TestEntity.class,
                Pig.class
        );

        static {
            SCHEMA.register();
        }

        public TestEntity(@NotNull Location location) {
            super(SCHEMA, () -> location.getWorld().spawn(location, Pig.class));
            this.integer = 5;
            this.list = new ArrayList<>();
            this.location = location;
        }

        @SuppressWarnings({"unused", "DataFlowIssue"})
        public TestEntity(@NotNull StateReader reader) {
            super(reader);
            integer = reader.get("integer", Integer.class);
            list = reader.get("list", new ArrayList<>());
            location = reader.get("location", Location.class);
        }

        @Override
        public void write(@NotNull StateWriter writer) {
            integer += 1;
            list.add("bruh");
            writer.set("integer", integer);
            writer.set("list", list);
            writer.set("location", location);
        }
    }

    @SuppressWarnings("CodeBlock2Expr")
    @Override
    public void test(World world) {
        Location location = TestUtil.findUnloadedChunk(world);

        TestUtil.loadChunk(location);

        AtomicReference<UUID> uuid = new AtomicReference<>();
        TestUtil.runSync(() -> uuid.set(new TestEntity(location).uuid()));

        TestUtil.runSync(() -> {
            TestEntity testEntity = (TestEntity) EntityStorage.kinematicEntity(uuid.get());
            assertThat(testEntity)
                    .isNotNull();
            assertThat(testEntity.getInteger())
                    .isEqualTo(5);
            assertThat(testEntity.getList())
                    .isEmpty();
            assertThat(testEntity.getLocation().x())
                    .isEqualTo(location.x());
        });

        TestUtil.unloadChunk(location);

        TestUtil.runSync(() -> {
            assertThat(EntityStorage.kinematicEntity(uuid.get()))
                    .isNull();
        });

        TestUtil.loadChunk(location);

        TestUtil.runSync(() -> {
            TestEntity testEntity = (TestEntity) EntityStorage.kinematicEntity(uuid.get());
            assertThat(EntitySchemas.schema(TestEntity.SCHEMA.getId()))
                    .isEqualTo(TestEntity.SCHEMA);
            assertThat(testEntity)
                    .isNotNull();
            assertThat(testEntity.getInteger())
                    .isEqualTo(6);
            assertThat(testEntity.getList())
                    .hasSize(1);
            assertThat(testEntity.getLocation().x())
                    .isEqualTo(location.x());
        });

        TestUtil.runSync(() -> {
            //noinspection DataFlowIssue
            EntityStorage.kinematicEntity(uuid.get()).entity().remove();
        });

        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        TestUtil.runSync(() -> {
            assertThat(EntityStorage.kinematicEntity(uuid.get()))
                    .isNotNull();
        });

        TestUtil.unloadChunk(location);
    }
}
