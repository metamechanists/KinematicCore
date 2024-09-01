package org.metamechanists.kinematiccore.api;

import org.bukkit.Location;
import org.bukkit.entity.Pig;
import org.jetbrains.annotations.NotNull;
import org.metamechanists.kinematiccore.KinematicCore;


public class TestEntity extends KinematicEntity<Pig> {
    private static final KinematicEntitySchema SCHEMA = new KinematicEntitySchema("test_entity", TestEntity.class, Pig.class);

    static {
        EntityStorage.register(SCHEMA);
    }

    protected TestEntity(@NotNull Location location) {
        super(() -> location.getWorld().spawn(location, Pig.class));
    }

    @Override
    public KinematicEntitySchema schema() {
        return SCHEMA;
    }

    @Override
    public void tick(long tick) {
        KinematicCore.getInstance().getLogger().warning(entity().getUniqueId().toString());
    }
}
