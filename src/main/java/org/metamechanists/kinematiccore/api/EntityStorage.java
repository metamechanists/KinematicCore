package org.metamechanists.kinematiccore.api;

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import org.metamechanists.kinematiccore.KinematicCore;

import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


public final class EntityStorage implements Listener {
    // 1MB max persistent entities storage in RAM because we are doing
    // our own caching according to loaded chunks, so storing a lot of
    // MapDB data in memory is effectively duplicating data
    private static final long MAX_PERSISTENT_ENTITIES_SIZE = 1024 * 1024;
    private static DB db;

    private static final Output output = new Output();
    private static final Input input = new Input();
    private static final Kryo kryo = new Kryo();

    private static HTreeMap<UUID, byte[]> entities;
    private static HTreeMap<String, Set<UUID>> entitiesByType;
    private static final Map<UUID, KinematicEntity<?>> loadedEntities = new ConcurrentHashMap<>();
    private static final Map<String, Set<UUID>> loadedEntitiesByType = new ConcurrentHashMap<>();
    private static final Map<String, KinematicEntitySchema> schemas = new ConcurrentHashMap<>();

    private EntityStorage() {}

    @ApiStatus.Internal
    public static void init() {
        //noinspection ResultOfMethodCallIgnored
        KinematicCore.getInstance().getDataFolder().mkdir();

        db = DBMaker.fileDB(new File(KinematicCore.getInstance().getDataFolder(), "data.mapdb"))
                .closeOnJvmShutdown()
                .fileMmapEnableIfSupported()
                .transactionEnable()
                .make();

        entities = db.hashMap("entities", Serializer.UUID, Serializer.BYTE_ARRAY)
                .expireStoreSize(MAX_PERSISTENT_ENTITIES_SIZE)
                .createOrOpen();

        //noinspection unchecked
        entitiesByType = db.hashMap("entitiesByType ", Serializer.STRING, Serializer.JAVA)
                .expireStoreSize(MAX_PERSISTENT_ENTITIES_SIZE)
                .createOrOpen();
    }

    @ApiStatus.Internal
    public static void cleanup() {
        db.close();
    }

    public static void register(@NotNull KinematicEntitySchema schema) {
        schemas.put(schema.id(), schema);
    }

    @ApiStatus.Internal
    public static void add(KinematicEntity<?> kinematicEntity) {
        loadedEntities.put(kinematicEntity.uuid(), kinematicEntity);
        Set<UUID> uuids = entitiesByType.computeIfAbsent(kinematicEntity.schema().id(), k -> ConcurrentHashMap.newKeySet());
        uuids.add(kinematicEntity.uuid());
    }

    public static KinematicEntity<?> kinematicEntity(UUID uuid) {
        return loadedEntities.get(uuid);
    }

    public static KinematicEntitySchema schema(String id) {
        return schemas.get(id);
    }

    public static @NotNull Map<String, Set<UUID>> allLoadedEntitiesByType() {
        return loadedEntitiesByType;
    }

    public static @Nullable Set<UUID> loadedEntitiesByType(String type) {
        return loadedEntitiesByType.get(type);
    }

    @EventHandler
    private static void onEntityLoad(@NotNull EntitiesLoadEvent event) {
        for (Entity entity : event.getEntities()) {
            byte[] bytes = entities.get(entity.getUniqueId());
            if (bytes == null) {
                continue;
            }

            // TODO register with kryo
            kryo.reset();
            input.reset();
            input.setBuffer(bytes);
            KinematicEntity<?> object = (KinematicEntity<?>) kryo.readClassAndObject(input);

            loadedEntities.put(entity.getUniqueId(), object);
        }
    }

    @EventHandler
    private static void onEntityUnload(@NotNull EntityRemoveFromWorldEvent event) {
        Entity entity = event.getEntity();
        KinematicEntity<?> kinematicEntity = loadedEntities.remove(entity.getUniqueId());
        if (kinematicEntity == null) {
            return;
        }

        kryo.reset();
        output.reset();
        kryo.writeClassAndObject(output, kinematicEntity);

        entities.put(entity.getUniqueId(), output.getBuffer());
    }
}
