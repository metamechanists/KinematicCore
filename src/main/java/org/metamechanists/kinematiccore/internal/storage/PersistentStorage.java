package org.metamechanists.kinematiccore.internal.storage;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import org.metamechanists.kinematiccore.KinematicCore;

import java.io.File;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.LinkedBlockingQueue;


public abstract class PersistentStorage<K extends Comparable<K>, V> {
    // 1MB max persistent because we are doing our own caching chunks, so storing a lot of
    // MapDB data in memory is effectively duplicating data
    private static final long MAX_DB_CACHE_SIZE = 1024 * 1024;
    private static final long COMMIT_INTERVAL_TICKS = 20;

    private final DB db;

    private final Queue<K> loadQueue = new LinkedBlockingQueue<>();
    private final Queue<V> saveQueue = new LinkedBlockingQueue<>();
    private final Queue<V> deletionQueue = new LinkedBlockingQueue<>();

    private final Set<K> scheduledForDeletion = new ConcurrentSkipListSet<>();

    private final HTreeMap<K, byte[]> peristentData;
    private final HTreeMap<String, Set<K>> persistentDataByType;

    private final Map<K, V> loadedData = new ConcurrentHashMap<>();
    private final Map<String, Set<K>> loadedDataByType = new ConcurrentHashMap<>();

    private String errorMessage(V value, String message) {
        String errorMessage = message;
        K key = key(value);
        String type = type(value);
        errorMessage += " Key: " + key + ".";
        if (type != null) {
            errorMessage += " Type " + type + ".";
        }
        return errorMessage;
    }

    private void commitLoad(K key) {
        try {
            byte[] bytes = peristentData.get(key);
            if (bytes == null) {
                return;
            }

            Map.Entry<String, V> pair = deserialize(key, bytes);
            assert pair != null;
            String type = pair.getKey();
            V value = pair.getValue();

            loadedDataByType.computeIfAbsent(type, k -> ConcurrentHashMap.newKeySet()).add(key);
            loadedData.put(key, value);
        } catch (Exception e) {
            KinematicCore.getInstance().getLogger().severe("Error while loading " + key);
            e.printStackTrace();
        }
    }

    private void commitSave(V value) {
        try {
            K key = key(value);
            String type = type(value);
            assert type != null;
            byte[] bytes = serialize(value);
            peristentData.put(key, bytes);
            persistentDataByType.computeIfAbsent(type, k -> ConcurrentHashMap.newKeySet()).add(key);
        } catch (IllegalArgumentException e) {
            String message = errorMessage(value, "The class " + e.getClass().getSimpleName() + " cannot be serialized.");
            KinematicCore.getInstance().getLogger().severe(message);
            e.printStackTrace();
        } catch (Exception e) {
            String message = errorMessage(value, "Error while saving.");
            KinematicCore.getInstance().getLogger().severe(message);
            e.printStackTrace();
        }
    }

    private void commitDelete(V value) {
        try {
            K key = key(value);
            loadedData.remove(key);
            loadedDataByType.computeIfAbsent(type(value), k -> ConcurrentHashMap.newKeySet())
                    .remove(key);
            peristentData.remove(key);
            persistentDataByType.computeIfAbsent(type(value), k -> ConcurrentHashMap.newKeySet())
                    .remove(key);
        } catch (Exception e) {
            String message = errorMessage(value,"Error while deleting.");
            KinematicCore.getInstance().getLogger().severe(message);
            e.printStackTrace();
        } finally {
            scheduledForDeletion.remove(key(value));
        }
    }

    /*
     * Commits scheduled changes
     * Do not call on the main thread
     */
    private void commit() {
        while (loadQueue.peek() != null) {
            commitLoad(loadQueue.poll());
        }
        while (saveQueue.peek() != null) {
            commitSave(saveQueue.poll());
        }
        while (deletionQueue.peek() != null) {
            commitDelete(deletionQueue.poll());
        }
        db.commit();
    }

    protected PersistentStorage(String database, Serializer<K> keySerializer) {
        //noinspection ResultOfMethodCallIgnored
        KinematicCore.getInstance().getDataFolder().mkdir();

        db = DBMaker.fileDB(new File(KinematicCore.getInstance().getDataFolder(), database + ".mapdb"))
                .closeOnJvmShutdown()
                .fileMmapEnableIfSupported()
                .transactionEnable()
                .make();

        peristentData = db.hashMap("data", keySerializer, Serializer.BYTE_ARRAY)
                .expireStoreSize(MAX_DB_CACHE_SIZE)
                .createOrOpen();

        //noinspection unchecked
        persistentDataByType = db.hashMap("dataByType ", Serializer.STRING, Serializer.JAVA)
                .expireStoreSize(MAX_DB_CACHE_SIZE)
                .createOrOpen();

        Bukkit.getScheduler().runTaskTimerAsynchronously(KinematicCore.getInstance(), this::commit, 0, COMMIT_INTERVAL_TICKS);
    }

    protected abstract @NotNull K key(@NotNull V value);

    /*
     * Nullable because eg: An entity could have been spawned with a schema, but then
     * the plugin providing the entity was deleted. The entity no longer has a schema
     * and thus also has no type.
     */
    protected abstract @Nullable String type(@NotNull V value);

    protected abstract @Nullable Map.Entry<String, V> deserialize(K key, byte @NotNull[] bytes);

    protected abstract byte @Nullable[] serialize(@NotNull V value);

    /*
     * This DOES NOT save any data! That's the job of cleanup and its callers.
     */
    public final void close() {
        db.commit();
        db.close();
    }

    /*
     * Unloads all data from a specific addon on the main thread.
     */
    public void cleanup(@NotNull Set<String> typesToCleanup) {
        for (String type : typesToCleanup) {
            Set<K> keys = loadedByType(type);
            if (keys == null) {
                KinematicCore.getInstance().getLogger().warning("Failed to save data of type " + type);
                continue;
            }

            for (K key : keys) {
                V value = loadedData.remove(key);
                if (value == null) {
                    return;
                }

                // Called on main thread because we absolutely want to wait until the data is saved
                save(value);
            }
        }

        commit();
    }

    /*
     * Takes existing data and writes it from memory to disk.
     * Do not call on the main thread.
     */
    public void save(@NotNull V value) {
        if (!scheduledForDeletion.contains(key(value))) {
            saveQueue.add(value);
        }
    }

    /*
     * Takes an existing piece of data and reads it from disk to memory.
     * The data referenced by the K can be null.
     * Do not call on the main thread.
     */
    public void load(K key) {
        if (!scheduledForDeletion.contains(key)) {
            loadQueue.add(key);
        }
    }

    /*
     * Takes an existing piece of data and writes it from memory to disk.
     * Do not call on the main thread.
     */
    public void delete(@NotNull V value) {
        scheduledForDeletion.add(key(value));
        deletionQueue.add(value);
    }

    /*
     * Removes a piece of data from cache.
     * Does not save the data to disk.
     */
    public void unload(@NotNull V value) {
        loadedDataByType.get(type(value)).remove(key(value));
        loadedData.remove(key(value));
    }

    /*
     * Adds a completely new piece of data.
     */
    public void create(@NotNull V value) {
        Set<K> keys = loadedDataByType.computeIfAbsent(type(value), k -> ConcurrentHashMap.newKeySet());
        keys.add(key(value));
        loadedData.put(key(value), value);
    }

    public @Nullable V get(@NotNull K key) {
        return loadedData.get(key);
    }

    public @NotNull Map<String, Set<K>> loaded() {
        return loadedDataByType;
    }

    public @Nullable Set<K> loadedByType(@NotNull String type) {
        return loadedDataByType.get(type);
    }
}
