package org.metamechanists.kinematiccore.api;

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.bukkit.Bukkit;
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

//    private static final Output output = new Output();
//    private static final Input input = new Input();
    private static final Kryo kryo = new Kryo();

    private static HTreeMap<UUID, byte[]> entities;
    private static HTreeMap<String, Set<UUID>> entitiesByType;

    private static final Map<String, Set<UUID>> loadedEntitiesByType = new ConcurrentHashMap<>();
    private static final Map<UUID, KinematicEntity<?>> loadedEntities = new ConcurrentHashMap<>();
    private static final Map<String, KinematicEntitySchema> schemas = new ConcurrentHashMap<>();


    private EntityStorage() {}

    @ApiStatus.Internal
    public static void init() {
        //noinspection ResultOfMethodCallIgnored
        KinematicCore.getInstance().getDataFolder().mkdir();
        Bukkit.getServer().getPluginManager().registerEvents(new EntityStorage(), KinematicCore.getInstance());

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
        kryo.register(schema.kinematicClass());
        schemas.put(schema.id(), schema);
    }

    /*
     * Takes an existing KinematicEntity and writes it from memory to disk.
     * The KinematicEntity referenced by the UUID must NOT be null.
     */
    private static void save(UUID uuid) {
        KinematicCore.getInstance().getLogger().info("Writing to disk " + uuid);

        KinematicEntity<?> kinematicEntity = loadedEntities.get(uuid);
        Output output = new Output(1024 * 1024);
        kryo.writeClassAndObject(output, kinematicEntity);
        entities.put(uuid, output.getBuffer());
        entitiesByType.computeIfAbsent(kinematicEntity.schema().id(), k -> ConcurrentHashMap.newKeySet()).add(uuid);
    }

    /*
     * Takes an existing KinematicEntity and reads it from disk to memory.
     * The KinematicEntity referenced by the UUID can be null.
     */
    private static void tryLoad(UUID uuid) {
        byte[] bytes = entities.get(uuid);
        if (bytes == null) {
            return;
        }

        KinematicCore.getInstance().getLogger().info("Loading " + uuid);

        Input input = new Input();
        input.setBuffer(bytes);
        KinematicEntity<?> kinematicEntity = (KinematicEntity<?>) kryo.readClassAndObject(input);

        loadedEntitiesByType.computeIfAbsent(kinematicEntity.schema().id(), k -> ConcurrentHashMap.newKeySet()).add(uuid);
        loadedEntities.put(uuid, kinematicEntity);
    }

    /*
     * Takes an existing KinematicEntity and transfers it from memory to disk.
     * The KinematicEntity referenced by the UUID can be null.
     */
    private static void tryUnload(UUID uuid) {
        KinematicEntity<?> kinematicEntity = kinematicEntity(uuid);
        if (kinematicEntity == null) {
            return;
        }

        KinematicCore.getInstance().getLogger().info("Unloading " + uuid);

        save(uuid);

        loadedEntitiesByType.get(kinematicEntity.schema().id()).remove(uuid);
        loadedEntities.remove(uuid);
    }

    /*
     * Adds a completely new KinematicEntity to the cache.
     */
    @ApiStatus.Internal
    public static void add(@NotNull KinematicEntity<?> kinematicEntity) {
        KinematicCore.getInstance().getLogger().info("Add " + kinematicEntity.uuid());

        Set<UUID> uuids = loadedEntitiesByType.computeIfAbsent(kinematicEntity.schema().id(), k -> ConcurrentHashMap.newKeySet());
        uuids.add(kinematicEntity.uuid());

        loadedEntities.put(kinematicEntity.uuid(), kinematicEntity);
    }

    public static @Nullable KinematicEntity<?> kinematicEntity(UUID uuid) {
        return loadedEntities.get(uuid);
    }

    public static @Nullable KinematicEntitySchema schema(String id) {
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
            tryLoad(entity.getUniqueId());
        }
    }

    @EventHandler
    private static void onEntityUnload(@NotNull EntityRemoveFromWorldEvent event) {
        tryUnload(event.getEntity().getUniqueId());
    }
}