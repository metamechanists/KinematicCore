package org.metamechanists.kinematiccore.api;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Pig;
import org.jetbrains.annotations.NotNull;
import org.metamechanists.kinematiccore.KinematicCore;


public class TestEntity extends KinematicEntity<Pig> {
    private static final KinematicEntitySchema SCHEMA = new KinematicEntitySchema(
            "test_entity",
            KinematicCore.class,
            TestEntity.class,
            Pig.class
    );

    private int bruh = 5;
    private final Location location;
    private final String name;

    static {
        EntityStorage.register(SCHEMA);
    }

    protected TestEntity(@NotNull Location location) {
        super(SCHEMA, () -> location.getWorld().spawn(location, Pig.class));
        this.location = location;
        this.name = "bob";
    }

    @SuppressWarnings("DataFlowIssue")
    public TestEntity(@NotNull StateReader reader) {
        super(SCHEMA, reader);
        this.bruh = reader.getInt("bruh");
        this.location = new Location(Bukkit.getWorld(reader.getString("world")), reader.getInt("x"), reader.getInt("y"), reader.getInt("z"));
        this.name = "bob";
    }

    @Override
    protected void write(@NotNull StateWriter writer) {
        writer.set("bruh", bruh);
        writer.set("world", location.getWorld().getName());
        writer.set("x", location.getBlockX());
        writer.set("y", location.getBlockY());
        writer.set("z", location.getBlockZ());
    }

    @Override
    public void tick(long tick) {
        if (entity() != null) {
            KinematicCore.getInstance().getLogger().warning(entity().getUniqueId() + name);
        }
    }
}
