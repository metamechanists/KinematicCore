package org.metamechanists.kinematiccore.api;


import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.UUID;
import java.util.function.Supplier;


public abstract class KinematicEntity<T extends Entity> {
    private final KinematicEntitySchema schema;
    private final UUID uuid;
    private transient WeakReference<T> entityRef;

    protected KinematicEntity(@NotNull KinematicEntitySchema schema, @NotNull Supplier<T> spawnEntity) {
        this.schema = schema;
        T entity = spawnEntity.get();

        // Check the spawned entity is the correct type (sadly can't be done at compile-time)
        Class<?> type = entity.getType().getEntityClass();
        if (type != null) {
            // Not sure why the type would be null, but just in case
            String provided = type.getName();
            String expected = schema().getEntityClass().getName();
            if (!provided.equals(expected)) {
                throw new Exceptions.EntityTypeMismatchException(this.schema.getId(), provided, expected);
            }
        }

        this.uuid = entity.getUniqueId();

        //noinspection ThisEscapedInObjectConstruction
        EntityStorage.add(this);
    }

    public void remove() {
        Entity entity = entity();
        if (entity != null) {
            entity.remove();
        }

        EntityStorage.remove(this);
    }

    protected KinematicEntity(@NotNull KinematicEntitySchema schema, @NotNull StateReader reader) {
        this.schema = schema;
        this.uuid = reader.uuid();
    }

    protected void tick(long tick) {}

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
            // TODO better checking here
            T castEntity = (T) schema().getEntityClass().cast(entityFromWorld);
            entityRef = new WeakReference<>(castEntity);
            return castEntity;
        }

        return null;
    }

    public final @NotNull UUID uuid() {
        return uuid;
    }

    public KinematicEntitySchema schema() {
        return schema;
    }

    protected abstract void write(@NotNull StateWriter writer);
}
