package org.metamechanists.kinematiccore.internal.entity;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mapdb.Serializer;
import org.metamechanists.kinematiccore.KinematicCore;
import org.metamechanists.kinematiccore.api.addon.KinematicAddon;
import org.metamechanists.kinematiccore.api.entity.KinematicEntity;
import org.metamechanists.kinematiccore.api.entity.KinematicEntitySchema;
import org.metamechanists.kinematiccore.api.state.StateReader;
import org.metamechanists.kinematiccore.api.state.StateWriter;
import org.metamechanists.kinematiccore.internal.storage.PersistentStorage;

import java.lang.reflect.InvocationTargetException;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;


public final class EntityStorage extends PersistentStorage<UUID, KinematicEntity<?, ?>> {
    @Getter
    private static EntityStorage instance;

    private EntityStorage() {
        super("entity", Serializer.UUID);
    }

    public static void init() {
        instance = new EntityStorage();
    }

    @Override
    protected @NotNull UUID key(@NotNull KinematicEntity<?, ?> kinematicEntity) {
        return kinematicEntity.uuid();
    }

    @Override
    protected @Nullable String id(@NotNull KinematicEntity<?, ?> kinematicEntity) {
        if (kinematicEntity.schema() == null) {
            return null;
        }
        return kinematicEntity.schema().id();
    }

    @Override
    protected @Nullable Map.Entry<String, KinematicEntity<?, ?>> deserialize(UUID uuid, byte @NotNull [] bytes) {
        StateReader reader = new StateReader(uuid, bytes);

        KinematicEntitySchema schema = KinematicEntitySchema.get(reader.id());
        if (schema == null) {
            KinematicCore.getInstance().getLogger().warning("Failed to load entity with ID " + reader.id() + " (schema not found)");
            KinematicCore.getInstance().getLogger().warning(KinematicEntitySchema.registeredSchemas().toString());
            return null;
        }

        try {
            return new AbstractMap.SimpleEntry<>(schema.id(), schema.constructor().newInstance(reader));
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 IllegalArgumentException e) {
            KinematicCore.getInstance().getLogger().severe("Error while loading entity with ID " + reader.id());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected byte @NotNull [] serialize(@NotNull KinematicEntity<?, ?> kinematicEntity) {
        StateWriter writer = new StateWriter(kinematicEntity.schema().id());
        kinematicEntity.write(writer);
        return writer.toBytes();
    }

    @Override
    protected void onDelete(@NotNull KinematicEntity<?, ?> kinematicEntity) {
        kinematicEntity.onRemove();
    }

    public void cleanup(KinematicAddon addon) {
        Set<String> schemas = KinematicEntitySchema.registeredSchemasByAddon(addon);
        cleanup(schemas);
    }
}
