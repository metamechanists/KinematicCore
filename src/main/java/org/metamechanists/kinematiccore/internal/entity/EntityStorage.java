package org.metamechanists.kinematiccore.internal.entity;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mapdb.Serializer;
import org.metamechanists.kinematiccore.KinematicCore;
import org.metamechanists.kinematiccore.api.entity.KinematicEntity;
import org.metamechanists.kinematiccore.api.entity.KinematicEntitySchema;
import org.metamechanists.kinematiccore.api.state.StateReader;
import org.metamechanists.kinematiccore.api.state.StateWriter;
import org.metamechanists.kinematiccore.internal.storage.PersistentStorage;

import java.lang.reflect.InvocationTargetException;
import java.util.AbstractMap;
import java.util.Map;
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
    protected @Nullable String type(@NotNull KinematicEntity<?, ?> kinematicEntity) {
        return kinematicEntity.schema().getId();
    }

    @Override
    protected @Nullable Map.Entry<String, KinematicEntity<?, ?>> deserialize(UUID uuid, byte @NotNull [] bytes) {
        StateReader reader = new StateReader(uuid, bytes);

        KinematicEntitySchema schema = KinematicEntitySchema.get(reader.id());
        if (schema == null) {
            KinematicCore.getInstance().getLogger().warning("Failed to load entity of type " + reader.id() + " (schema not found)");
            KinematicCore.getInstance().getLogger().warning(KinematicEntitySchema.registeredSchemas().toString());
            return null;
        }

        try {
            return new AbstractMap.SimpleEntry<>(schema.getId(), schema.getConstructor().newInstance(reader));
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 IllegalArgumentException e) {
            KinematicCore.getInstance().getLogger().severe("Error while loading entity of type " + reader.id());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected byte @NotNull [] serialize(@NotNull KinematicEntity<?, ?> kinematicEntity) {
        StateWriter writer = new StateWriter(kinematicEntity.schema().getId());
        kinematicEntity.write(writer);
        return writer.toBytes();
    }
}
