package org.metamechanists.kinematiccore.api.entity;


import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.metamechanists.kinematiccore.api.Exceptions;
import org.metamechanists.kinematiccore.api.storage.EntitySchemas;
import org.metamechanists.kinematiccore.api.storage.EntityStorage;
import org.metamechanists.kinematiccore.api.storage.StateReader;
import org.metamechanists.kinematiccore.api.storage.StateWriter;

import java.lang.ref.WeakReference;
import java.util.UUID;
import java.util.function.Supplier;


@SuppressWarnings({"WeakerAccess", "unused"})
public abstract class KinematicEntity<T extends Entity, S extends KinematicEntitySchema> {
    private final String schema;
    private final UUID uuid;
    private transient WeakReference<T> entityRef;

    protected KinematicEntity(@NotNull S schema, @NotNull Supplier<T> spawnEntity) {
        this.schema = schema.getId();
        T entity = spawnEntity.get();

        // Check the spawned entity is the correct type (sadly can't be done at compile-time)
        Class<?> type = entity.getType().getEntityClass();
        if (type != null) {
            // Not sure why the type would be null, but just in case
            String provided = type.getName();
            String expected = schema().getEntityClass().getName();
            if (!provided.equals(expected)) {
                throw new Exceptions.EntityTypeMismatchException(schema.getId(), provided, expected);
            }
        }

        this.uuid = entity.getUniqueId();

        //noinspection ThisEscapedInObjectConstruction
        EntityStorage.add(this);
    }

    protected KinematicEntity(@NotNull StateReader reader) {
        this.schema = reader.id();
        this.uuid = reader.uuid();
    }

    public void write(@NotNull StateWriter writer) {}

    public final @Nullable T entity() {
        // Use weakref if available
        if (entityRef != null) {
            T entityFromRef = entityRef.get();
            if (entityFromRef != null && entityFromRef.isValid()) {
                return entityFromRef;
            }
        }

        // Fall back to getting entity from world, and if found, update the weakref
        Entity entityFromWorld = Bukkit.getEntity(uuid);
        if (schema().getEntityClass().isInstance(entityFromWorld)) {
            //noinspection unchecked
            T castEntity = (T) schema().getEntityClass().cast(entityFromWorld);
            entityRef = new WeakReference<>(castEntity);
            return castEntity;
        }

        return null;
    }

    public final @NotNull UUID uuid() {
        return uuid;
    }

    public S schema() {
        return (S) EntitySchemas.schema(schema);
    }

    public void remove() {
        EntityStorage.remove(this);
    }

    public final void tick(long tick) {
        T entity = entity();
        if (entity != null) {
            tick(entity, tick);
        }
    }

    @SuppressWarnings("unused")
    protected void tick(@NotNull T entity, long tick) {}

    @SuppressWarnings("unused")
    public void onRightClick(Player player) {}
}
