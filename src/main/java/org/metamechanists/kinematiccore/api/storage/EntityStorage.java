package org.metamechanists.kinematiccore.api.storage;

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.DefaultSerializers;
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
import org.metamechanists.kinematiccore.api.entity.KinematicEntity;
import org.metamechanists.kinematiccore.api.entity.KinematicEntitySchema;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


@SuppressWarnings("WeakerAccess")
public final class EntityStorage implements Listener {
    // 1MB max persistent entities storage in RAM because we are doing
    // our own caching according to loaded chunks, so storing a lot of
    // MapDB data in memory is effectively duplicating data
    private static final long MAX_PERSISTENT_ENTITIES_SIZE = 1024 * 1024;
    private static final int OUTPUT_BUFFER_START_SIZE = 1024;
    private static final long COMMIT_INTERVAL = 20 * 30;
    private static DB db;

    private static final Kryo kryo = new Kryo();

    private static HTreeMap<UUID, byte[]> entities;
    private static HTreeMap<String, Set<UUID>> entitiesByType;

    private static final Map<String, Set<UUID>> loadedEntitiesByType = new ConcurrentHashMap<>();
    private static final Map<UUID, KinematicEntity<?>> loadedEntities = new ConcurrentHashMap<>();

    private EntityStorage() {}

    static void register(@NotNull KinematicEntitySchema schema) {
        kryo.register(schema.getKinematicClass());
    }

    @ApiStatus.Internal
    public static void init() {
        kryo.setRegistrationRequired(false);

        // https://github.com/EsotericSoftware/kryo/issues/885
        kryo.addDefaultSerializer(UUID.class, DefaultSerializers.UUIDSerializer.class);

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

        Bukkit.getScheduler().runTaskTimerAsynchronously(KinematicCore.getInstance(), () -> db.commit(), 0, COMMIT_INTERVAL);
    }

    @ApiStatus.Internal
    public static void close() {
        for (UUID uuid : loadedEntities.keySet()) {
            tryUnload(uuid);
        }
        db.commit();
        db.close();
    }


    /*
     * Takes an existing KinematicEntity and writes it from memory to disk.
     * The KinematicEntity referenced by the UUID must NOT be null.
     */
    private static void save(UUID uuid) {
        KinematicCore.getInstance().getLogger().info("Writing to disk " + uuid);

        KinematicEntity<?> kinematicEntity = loadedEntities.get(uuid);

        Output output = new Output(OUTPUT_BUFFER_START_SIZE, -1);
        StateWriter writer = new StateWriter(kinematicEntity.schema().getId(), uuid);
        kinematicEntity.write(writer);
        writer.write(output);

        entities.put(uuid, output.toBytes());
        entitiesByType.computeIfAbsent(kinematicEntity.schema().getId(), k -> ConcurrentHashMap.newKeySet()).add(uuid);

        db.commit();
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
        StateReader reader = new StateReader(input);

        KinematicEntitySchema schema = EntitySchemas.schema(reader.id());
        if (schema == null) {
            KinematicCore.getInstance().getLogger().warning("Failed to load " + uuid + " of type " + reader.id() + " (schema not found)");
            return;
        }

        KinematicEntity<?> kinematicEntity;
        try {
            kinematicEntity = schema.getConstructor().newInstance(reader);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | IllegalArgumentException e) {
            KinematicCore.getInstance().getLogger().severe("Error while loading " + uuid + " of type " + reader.id());
            e.printStackTrace();
            return;
        }

        loadedEntitiesByType.computeIfAbsent(schema.getId(), k -> ConcurrentHashMap.newKeySet()).add(uuid);
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

        try {
            save(uuid);
        } finally {
            // Delete entity data even if saving fails
            loadedEntitiesByType.get(kinematicEntity.schema().getId()).remove(uuid);
            loadedEntities.remove(uuid);
        }
    }

    /*
     * Adds a completely new KinematicEntity to the cache.
     */
    @ApiStatus.Internal
    public static void add(@NotNull KinematicEntity<?> kinematicEntity) {
        Set<UUID> uuids = loadedEntitiesByType.computeIfAbsent(kinematicEntity.schema().getId(), k -> ConcurrentHashMap.newKeySet());
        uuids.add(kinematicEntity.uuid());

        loadedEntities.put(kinematicEntity.uuid(), kinematicEntity);
    }

    /*
     * Completely destroys a KinematicEntity. The entity must be loaded.
     */
    @ApiStatus.Internal
    public static void remove(@NotNull KinematicEntity<?> kinematicEntity) {
        Entity entity = kinematicEntity.entity();
        if (entity != null) {
            entity.remove();
        }

        loadedEntitiesByType.get(kinematicEntity.schema().getId()).remove(kinematicEntity.uuid());
        loadedEntities.remove(kinematicEntity.uuid());
    }

    public static @Nullable KinematicEntity<?> kinematicEntity(@NotNull UUID uuid) {
        return loadedEntities.get(uuid);
    }

    public static @NotNull Map<String, Set<UUID>> allLoadedEntitiesByType() {
        return loadedEntitiesByType;
    }

    public static @Nullable Set<UUID> loadedEntitiesByType(@NotNull KinematicEntitySchema schema) {
        return loadedEntitiesByType(schema.getId());
    }

    public static @Nullable Set<UUID> loadedEntitiesByType(@NotNull String type) {
        return loadedEntitiesByType.get(type);
    }

    @EventHandler
    private static void onEntityLoad(@NotNull EntitiesLoadEvent event) {
        for (Entity entity : event.getEntities()) {
            try {
                tryLoad(entity.getUniqueId());
            } catch (RuntimeException e) {
                KinematicCore.getInstance().getLogger().severe("Error while loading entity " + entity.getUniqueId());
                e.printStackTrace();
            }
        }
    }

    @EventHandler
    private static void onEntityUnload(@NotNull EntityRemoveFromWorldEvent event) {
        try {
            Entity entity = event.getEntity();
            UUID uuid = entity.getUniqueId();
            KinematicEntity<?> kinematicEntity = kinematicEntity(uuid);
            if (kinematicEntity == null) {
                return;
            }

            if (entity.isDead()) {
                remove(kinematicEntity);
            } else {
                tryUnload(uuid);
            }
        } catch (RuntimeException e) {
            KinematicCore.getInstance().getLogger().severe("Error while unloading entity; entity data will be lost!");
            e.printStackTrace();
        }
    }
}
