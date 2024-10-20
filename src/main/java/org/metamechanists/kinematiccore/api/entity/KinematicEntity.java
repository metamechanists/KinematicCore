package org.metamechanists.kinematiccore.api.entity;


import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.metamechanists.kinematiccore.api.Exceptions;
import org.metamechanists.kinematiccore.internal.entity.EntityStorage;
import org.metamechanists.kinematiccore.api.state.StateReader;
import org.metamechanists.kinematiccore.api.state.StateWriter;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;


@SuppressWarnings({"WeakerAccess", "unused"})
public abstract class KinematicEntity<T extends Entity, S extends KinematicEntitySchema> {
    private final String id;
    private final UUID uuid;
    private transient WeakReference<T> entityRef;

    protected KinematicEntity(@NotNull S schema, @NotNull Supplier<T> spawnEntity) {
        this.id = schema.id();
        T entity = spawnEntity.get();

        // Check the spawned entity is the correct type (sadly can't be done at compile-time because... Java)
        if (entity.getType() != schema.entityType()) {
            throw new Exceptions.EntityTypeMismatchException(id, entity.getType(), schema.entityType());
        }

        this.uuid = entity.getUniqueId();

        //noinspection ThisEscapedInObjectConstruction
        EntityStorage.instance().create(this);
    }

    protected KinematicEntity(@NotNull StateReader reader) {
        this.id = reader.id();
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
        if (entityFromWorld != null && schema().entityType() == entityFromWorld.getType()) {
            Class<?> entityClass = schema().entityType().getEntityClass();
            assert entityClass != null;
            assert entityClass.isInstance(entityFromWorld);
            //noinspection unchecked
            T castEntity = (T) entityClass.cast(entityFromWorld);
            entityRef = new WeakReference<>(castEntity);
            return castEntity;
        }

        return null;
    }

    public final @NotNull UUID uuid() {
        return uuid;
    }

    public final void tick(long tick) {
        T entity = entity();
        if (entity != null) {
            tick(entity, tick);
        }
    }

    protected void tick(@NotNull T entity, long tick) {}

    public void onRightClick(@NotNull Player player) {}

    public void onRemove() {}

    /*
     * Not marked as nullable because in almost all cases we can assume this to be not null
     */
    public S schema() {
        KinematicEntitySchema schema = KinematicEntitySchema.get(id);
        assert schema != null;
        //noinspection unchecked
        return (S) schema;
    }

    public static @Nullable KinematicEntity<?, ?> get(UUID uuid) {
        return EntityStorage.instance().get(uuid);
    }

    public static @NotNull Map<String, Set<UUID>> loaded() {
        return EntityStorage.instance().loaded();
    }

    public static @Nullable Set<UUID> loadedById(@NotNull String id) {
        return EntityStorage.instance().loadedById(id);
    }

    public static @Nullable Set<UUID> loadedById(@NotNull KinematicEntitySchema schema) {
        return loadedById(schema.id());
    }
}
