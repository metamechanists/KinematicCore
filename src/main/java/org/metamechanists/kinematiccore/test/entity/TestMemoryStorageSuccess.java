package org.metamechanists.kinematiccore.test.entity;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Pig;
import org.jetbrains.annotations.NotNull;
import org.metamechanists.kinematiccore.KinematicCore;
import org.metamechanists.kinematiccore.api.entity.KinematicEntity;
import org.metamechanists.kinematiccore.api.entity.KinematicEntitySchema;
import org.metamechanists.kinematiccore.api.state.StateReader;
import org.metamechanists.kinematiccore.test.BaseTest;

import static org.assertj.core.api.Assertions.*;


public class TestMemoryStorageSuccess implements BaseTest {
    private static final class TestEntity extends KinematicEntity<Pig, KinematicEntitySchema> {
        private static final KinematicEntitySchema SCHEMA = new KinematicEntitySchema(
                "test_memory_storage_success",
                EntityType.PIG,
                TestEntity.class
        );

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
        TestMemoryStorageSuccess.TestEntity.SCHEMA.register(KinematicCore.getInstance());

        Location location = TestUtil.findUnloadedChunk(world);

        TestUtil.loadChunk(location);

        TestUtil.runSync(() -> {
            TestEntity kinematicEntity = new TestEntity(location);

            assertThat(KinematicEntity.get(kinematicEntity.uuid()))
                    .isEqualTo(kinematicEntity);
            assertThat(KinematicEntitySchema.get(TestEntity.SCHEMA.id()))
                    .isEqualTo(TestEntity.SCHEMA);
            assertThat(KinematicEntity.loadedById(TestEntity.SCHEMA))
                    .hasSize(1)
                    .contains(kinematicEntity.uuid());
            assertThat(kinematicEntity.entity())
                    .isNotNull();

            //noinspection DataFlowIssue
            kinematicEntity.entity().remove();

            assertThat(KinematicEntity.get(kinematicEntity.uuid()))
                    .isNull();
            assertThat(KinematicEntitySchema.get(TestEntity.SCHEMA.id()))
                    .isEqualTo(TestEntity.SCHEMA);
            assertThat(KinematicEntity.loadedById(TestEntity.SCHEMA))
                    .isEmpty();
            assertThat(kinematicEntity.entity())
                    .isNull();
        });

        TestUtil.unloadChunk(location);
    }
}
