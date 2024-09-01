package org.metamechanists.kinematiccore.api;


import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.UUID;
import java.util.function.Supplier;


public abstract class KinematicEntity<T extends Entity> {
    private final UUID uuid;
    private transient WeakReference<T> entityRef;

    protected KinematicEntity(@NotNull Supplier<T> spawnEntity) {
        T entity = spawnEntity.get();
        String provided = entity.getClass().getName();
        String expected = schema().entityClass().getName();
        if (!provided.equals(expected)) {
            throw new RuntimeException("The provided entity (" + provided + ") does not match the entity type specified in the schema (" + expected + ")");
        }

        this.uuid = entity.getUniqueId();
        this.entityRef = new WeakReference<>(entity);

        //noinspection ThisEscapedInObjectConstruction
        EntityStorage.add(this);
    }

    protected void tick(long tick) {}

    protected final @Nullable T entity() {
        // Use weakref if available
        T entityFromRef = entityRef.get();
        if (entityFromRef != null && entityFromRef.isValid()) {
            return entityFromRef;
        }

        // Fall back to getting entity from world, and if found, update the weakref
        Entity entityFromWorld = Bukkit.getEntity(uuid);
        if (schema().entityClass().isInstance(entityFromWorld)) {
            // Safe to cast because the schema should always contain the correct entity type
            T castEntity = (T) schema().entityClass().cast(entityFromWorld);
            entityRef = new WeakReference<>(castEntity);
            return castEntity;
        }

        return null;
    }

    protected final @NotNull UUID uuid() {
        return uuid;
    }

    public abstract KinematicEntitySchema schema();
}
