package org.metamechanists.kinematiccore.internal.entity;

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import com.esotericsoftware.kryo.KryoException;
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
import org.metamechanists.kinematiccore.api.addon.KinematicAddon;
import org.metamechanists.kinematiccore.api.entity.KinematicEntity;
import org.metamechanists.kinematiccore.api.entity.KinematicEntitySchema;
import org.metamechanists.kinematiccore.api.state.StateReader;
import org.metamechanists.kinematiccore.api.state.StateWriter;

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
    private static final long COMMIT_INTERVAL = 10 * 20;
    private static DB db;

    private static HTreeMap<UUID, byte[]> entities;
    private static HTreeMap<String, Set<UUID>> entitiesByType;

    private static final Map<String, Set<UUID>> loadedEntitiesByType = new ConcurrentHashMap<>();
    private static final Map<UUID, KinematicEntity<?, ?>> loadedEntities = new ConcurrentHashMap<>();

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

        Bukkit.getScheduler().runTaskTimerAsynchronously(KinematicCore.getInstance(), () -> db.commit(), 0, COMMIT_INTERVAL);
    }

    /*
     * This DOES NOT save any entities! That's the job of cleanup and its callers
     */
    @ApiStatus.Internal
    public static void close() {
        db.commit();
        db.close();
    }

    @ApiStatus.Internal
    public static void cleanup(KinematicAddon addon) {
        Set<String> schemasToCleanup = EntitySchemas.registeredSchemasByAddon(addon);

        for (String type : schemasToCleanup) {
            Set<UUID> uuids = loadedEntitiesByType(type);
            if (uuids == null) {
                KinematicCore.getInstance().getLogger().warning("Failed to save loaded entities of type " + type);
                continue;
            }

            for (UUID uuid : uuids) {
                KinematicEntity<?, ?> kinematicEntity = loadedEntities.remove(uuid);
                if (kinematicEntity == null) {
                    return;
                }

                trySave(kinematicEntity);
            }
        }
    }

    /*
     * Takes an existing KinematicEntity and writes it from memory to disk.
     */
    private static void trySave(@NotNull KinematicEntity<?, ?> kinematicEntity) {
        KinematicCore.getInstance().getLogger().info("Writing to disk " + kinematicEntity.uuid());

        try {
            StateWriter writer = new StateWriter(kinematicEntity.schema().getId(), kinematicEntity.uuid());
            kinematicEntity.write(writer);

            entities.put(kinematicEntity.uuid(), writer.toBytes());
            entitiesByType.computeIfAbsent(kinematicEntity.schema().getId(), k -> ConcurrentHashMap.newKeySet()).add(kinematicEntity.uuid());
        } catch (IllegalArgumentException e) {
            KinematicCore.getInstance().getLogger()
                    .severe("The class " + e.getClass().getSimpleName() + " cannot be serialized (in entity " + kinematicEntity.schema().getId() + ")");
            e.printStackTrace();
        } catch (Exception e) {
            KinematicCore.getInstance().getLogger().severe("Error while saving entity " + kinematicEntity.uuid() + " of type " + kinematicEntity.schema().getId());
            e.printStackTrace();
        }
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

        StateReader reader = new StateReader(bytes);

        KinematicEntitySchema schema = EntitySchemas.schema(reader.id());
        if (schema == null) {
            KinematicCore.getInstance().getLogger().warning("Failed to load " + uuid + " of type " + reader.id() + " (schema not found)");
            KinematicCore.getInstance().getLogger().warning(EntitySchemas.registeredSchemas().toString());
            return;
        }

        KinematicEntity<?, ?> kinematicEntity;
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
     * Adds a completely new KinematicEntity to the cache.
     */
    @ApiStatus.Internal
    public static void add(@NotNull KinematicEntity<?, ?> kinematicEntity) {
        Set<UUID> uuids = loadedEntitiesByType.computeIfAbsent(kinematicEntity.schema().getId(), k -> ConcurrentHashMap.newKeySet());
        uuids.add(kinematicEntity.uuid());

        loadedEntities.put(kinematicEntity.uuid(), kinematicEntity);
    }

    public static @Nullable KinematicEntity<?, ?> kinematicEntity(@NotNull UUID uuid) {
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
            Bukkit.getScheduler().runTaskAsynchronously(KinematicCore.getInstance(), () -> {
                try {
                    tryLoad(entity.getUniqueId());
                } catch (KryoException e) {
                    KinematicCore.getInstance().getLogger().warning("Class unrecognized when loading " + entity.getUniqueId()
                            + "; this indicates an addon/entity type has been removed, and should be nothing to worry about");
                } catch (RuntimeException e) {
                    KinematicCore.getInstance().getLogger().severe("Error while loading entity " + entity.getUniqueId());
                    e.printStackTrace();
                }
            });
        }
    }

    @EventHandler
    private static void onEntityUnload(@NotNull EntityRemoveFromWorldEvent event) {
        Entity entity = event.getEntity();
        UUID uuid = entity.getUniqueId();

        KinematicEntity<?, ?> kinematicEntity = kinematicEntity(uuid);
        if (kinematicEntity == null) {
            return;
        }

        loadedEntitiesByType.get(kinematicEntity.schema().getId()).remove(kinematicEntity.uuid());
        loadedEntities.remove(kinematicEntity.uuid());

        if (entity.isValid()) {
            Bukkit.getScheduler().runTaskAsynchronously(KinematicCore.getInstance(), () -> trySave(kinematicEntity));
        }
    }
}
