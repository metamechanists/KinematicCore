package org.metamechanists.kinematiccore.test.entity;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Pig;
import org.jetbrains.annotations.NotNull;
import org.metamechanists.kinematiccore.KinematicCore;
import org.metamechanists.kinematiccore.api.entity.KinematicEntity;
import org.metamechanists.kinematiccore.api.entity.KinematicEntitySchema;
import org.metamechanists.kinematiccore.api.state.StateReader;
import org.metamechanists.kinematiccore.api.state.StateWriter;
import org.metamechanists.kinematiccore.test.BaseTest;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.*;


public class TestDiskFieldStorageSuccess implements BaseTest {
    @Getter
    private static class TestEntity extends KinematicEntity<Pig, KinematicEntitySchema> {
        private int integer;
        private final List<String> list;
        private final Location location;

        private static final KinematicEntitySchema SCHEMA = new KinematicEntitySchema(
                "test_disk_field_storage_success",
                KinematicCore.class,
                TestEntity.class,
                Pig.class
        );

        private TestEntity(@NotNull Location location) {
            super(SCHEMA, () -> location.getWorld().spawn(location, Pig.class));
            this.integer = 5;
            this.list = new ArrayList<>();
            this.location = location;
        }

        @SuppressWarnings({"unused", "DataFlowIssue"})
        private TestEntity(@NotNull StateReader reader) {
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
            TestEntity testEntity = (TestEntity) KinematicEntity.get(uuid.get());
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
            assertThat(KinematicEntity.get(uuid.get()))
                    .isNull();
        });

        TestUtil.loadChunk(location);

        TestUtil.runSync(() -> {
            TestEntity testEntity = (TestEntity) KinematicEntity.get(uuid.get());
            assertThat(KinematicEntitySchema.get(TestEntity.SCHEMA.getId()))
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
            KinematicEntity.get(uuid.get()).entity().remove();
        });

        TestUtil.runSync(() -> {
            assertThat(KinematicEntity.get(uuid.get()))
                    .isNull();
        });

        TestUtil.unloadChunk(location);
    }
}
