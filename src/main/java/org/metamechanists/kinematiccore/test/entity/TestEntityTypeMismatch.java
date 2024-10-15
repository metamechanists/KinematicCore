package org.metamechanists.kinematiccore.test.entity;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Pig;
import org.jetbrains.annotations.NotNull;
import org.metamechanists.kinematiccore.KinematicCore;
import org.metamechanists.kinematiccore.api.Exceptions;
import org.metamechanists.kinematiccore.api.entity.KinematicEntity;
import org.metamechanists.kinematiccore.api.entity.KinematicEntitySchema;
import org.metamechanists.kinematiccore.api.state.StateReader;
import org.metamechanists.kinematiccore.test.BaseTest;

import static org.assertj.core.api.Assertions.*;


public class TestEntityTypeMismatch implements BaseTest {
    private static class TestEntity extends KinematicEntity<Pig, KinematicEntitySchema> {
        private static final KinematicEntitySchema SCHEMA = new KinematicEntitySchema(
                "test_entity_type_mismatch",
                KinematicCore.class,
                TestEntity.class,
                Cow.class
        );

        private TestEntity(@NotNull Location location) {
            super(SCHEMA, () -> location.getWorld().spawn(location, Pig.class));
        }

        @SuppressWarnings("unused")
        private TestEntity(@NotNull StateReader reader) {
            super(reader);
        }
    }

    @SuppressWarnings({"ResultOfObjectAllocationIgnored", "CodeBlock2Expr"})
    @Override
    public void test(World world) {
        Location location = TestUtil.findUnloadedChunk(world);

        TestUtil.loadChunk(location);

        TestUtil.runSync(() -> {
            assertThatThrownBy(() -> new TestEntity(location))
                    .isInstanceOf(Exceptions.EntityTypeMismatchException.class);
        });

        TestUtil.unloadChunk(location);
    }
}
