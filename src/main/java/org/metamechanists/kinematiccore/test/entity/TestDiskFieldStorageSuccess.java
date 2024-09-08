package org.metamechanists.kinematiccore.test.entity;

import lombok.Getter;
import org.bukkit.Location;
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
    public void test(@NotNull Location loaded, @NotNull Location unloaded) {
        TestUtil.loadChunk(unloaded);

        AtomicReference<TestEntity> entity = new AtomicReference<>();
        TestUtil.runSync(() -> entity.set(new TestEntity(unloaded)));

        TestUtil.runSync(() -> {
            TestEntity testEntity = (TestEntity) EntityStorage.kinematicEntity(entity.get().uuid());
            assertThat(testEntity)
                    .isNotNull();
            assertThat(testEntity.getInteger())
                    .isEqualTo(5);
            assertThat(testEntity.getList())
                    .isEmpty();
            assertThat(testEntity.getLocation().x())
                    .isEqualTo(unloaded.x());
        });

        TestUtil.unloadChunk(unloaded);

        TestUtil.runSync(() -> {
            assertThat(EntityStorage.kinematicEntity(entity.get().uuid()))
                    .isNull();
        });

        TestUtil.loadChunk(unloaded);

        TestUtil.runSync(() -> {
            TestEntity testEntity = (TestEntity) EntityStorage.kinematicEntity(entity.get().uuid());

            assertThat(EntitySchemas.schema(TestEntity.SCHEMA.getId()))
                    .isEqualTo(TestEntity.SCHEMA);
            assertThat(testEntity)
                    .isNotNull();
            assertThat(testEntity.getInteger())
                    .isEqualTo(6);
            assertThat(testEntity.getList())
                    .hasSize(1);
            assertThat(testEntity.getLocation().x())
                    .isEqualTo(unloaded.x());
        });

        TestUtil.runSync(() -> entity.get().entity().remove());

        TestUtil.unloadChunk(unloaded);
    }
}