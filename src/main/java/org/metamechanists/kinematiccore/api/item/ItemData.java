package org.metamechanists.kinematiccore.api.item;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.metamechanists.kinematiccore.KinematicCore;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;


@SuppressWarnings({"ClassWithOnlyPrivateConstructors", "NonFinalUtilityClass", "unused", "WeakerAccess"})
public class ItemData {
    private static final Map<String, NamespacedKey> keys = new ConcurrentHashMap<>();

    private ItemData() {}

    private static @NotNull NamespacedKey key(@NotNull String key) {
        return keys.computeIfAbsent(key, k -> new NamespacedKey(KinematicCore.getInstance(), key));
    }

    private static void editMeta(@NotNull ItemStack stack, @NotNull String key, @NotNull Consumer<PersistentDataContainer> editor) {
        stack.editMeta(meta -> editor.accept(meta.getPersistentDataContainer()));
    }

    public static void unset(@NotNull ItemStack stack, @NotNull String key) {
        editMeta(stack, key, pdc -> pdc.remove(key(key)));
    }

    public static void set(@NotNull ItemStack stack, @NotNull String key, Byte value) {
        editMeta(stack, key, pdc -> pdc.set(key(key), PersistentDataType.BYTE, value));
    }

    public static boolean has(@NotNull ItemStack stack, @NotNull String key) {
        return stack.getItemMeta().getPersistentDataContainer().has(key(key));
    }

    public static @Nullable Byte getByte(@NotNull ItemStack stack, @NotNull String key) {
        return stack.getItemMeta().getPersistentDataContainer().get(key(key), PersistentDataType.BYTE);
    }

    public static void set(@NotNull ItemStack stack, @NotNull String key, Short value) {
        editMeta(stack, key, pdc -> pdc.set(key(key), PersistentDataType.SHORT, value));
    }

    public static @Nullable Short getShort(@NotNull ItemStack stack, @NotNull String key) {
        return stack.getItemMeta().getPersistentDataContainer().get(key(key), PersistentDataType.SHORT);
    }

    public static void set(@NotNull ItemStack stack, @NotNull String key, Integer value) {
        editMeta(stack, key, pdc -> pdc.set(key(key), PersistentDataType.INTEGER, value));
    }

    public static @Nullable Integer getInteger(@NotNull ItemStack stack, @NotNull String key) {
        return stack.getItemMeta().getPersistentDataContainer().get(key(key), PersistentDataType.INTEGER);
    }

    public static void set(@NotNull ItemStack stack, @NotNull String key, Long value) {
        editMeta(stack, key, pdc -> pdc.set(key(key), PersistentDataType.LONG, value));
    }

    public static @Nullable Long getLong(@NotNull ItemStack stack, @NotNull String key) {
        return stack.getItemMeta().getPersistentDataContainer().get(key(key), PersistentDataType.LONG);
    }

    public static void set(@NotNull ItemStack stack, @NotNull String key, Float value) {
        editMeta(stack, key, pdc -> pdc.set(key(key), PersistentDataType.FLOAT, value));
    }

    public static @Nullable Float getFloat(@NotNull ItemStack stack, @NotNull String key) {
        return stack.getItemMeta().getPersistentDataContainer().get(key(key), PersistentDataType.FLOAT);
    }

    public static void set(@NotNull ItemStack stack, @NotNull String key, Double value) {
        editMeta(stack, key, pdc -> pdc.set(key(key), PersistentDataType.DOUBLE, value));
    }

    public static @Nullable Double getDouble(@NotNull ItemStack stack, @NotNull String key) {
        return stack.getItemMeta().getPersistentDataContainer().get(key(key), PersistentDataType.DOUBLE);
    }

    public static void set(@NotNull ItemStack stack, @NotNull String key, Boolean value) {
        editMeta(stack, key, pdc -> pdc.set(key(key), PersistentDataType.BOOLEAN, value));
    }

    public static @Nullable Boolean getBoolean(@NotNull ItemStack stack, @NotNull String key) {
        return stack.getItemMeta().getPersistentDataContainer().get(key(key), PersistentDataType.BOOLEAN);
    }

    public static void set(@NotNull ItemStack stack, @NotNull String key, String value) {
        editMeta(stack, key, pdc -> pdc.set(key(key), PersistentDataType.STRING, value));
    }

    public static @Nullable String getString(@NotNull ItemStack stack, @NotNull String key) {
        return stack.getItemMeta().getPersistentDataContainer().get(key(key), PersistentDataType.STRING);
    }

    public static void set(@NotNull ItemStack stack, @NotNull String key, byte[] value) {
        editMeta(stack, key, pdc -> pdc.set(key(key), PersistentDataType.BYTE_ARRAY, value));
    }

    public static byte @Nullable [] getByteArray(@NotNull ItemStack stack, @NotNull String key) {
        return stack.getItemMeta().getPersistentDataContainer().get(key(key), PersistentDataType.BYTE_ARRAY);
    }

    public static void set(@NotNull ItemStack stack, @NotNull String key, int[] value) {
        editMeta(stack, key, pdc -> pdc.set(key(key), PersistentDataType.INTEGER_ARRAY, value));
    }

    public static int @Nullable [] getIntArray(@NotNull ItemStack stack, @NotNull String key) {
        return stack.getItemMeta().getPersistentDataContainer().get(key(key), PersistentDataType.INTEGER_ARRAY);
    }

    public static void set(@NotNull ItemStack stack, @NotNull String key, long[] value) {
        editMeta(stack, key, pdc -> pdc.set(key(key), PersistentDataType.LONG_ARRAY, value));
    }

    public static long @Nullable [] getLongArray(@NotNull ItemStack stack, @NotNull String key) {
        return stack.getItemMeta().getPersistentDataContainer().get(key(key), PersistentDataType.LONG_ARRAY);
    }

    public static void set(@NotNull ItemStack stack, @NotNull String key, UUID value) {
        if (value == null) {
            unset(stack, key);
            return;
        }

        ByteBuffer buffer = ByteBuffer.wrap(new byte[16]);
        buffer.putLong(value.getMostSignificantBits());
        buffer.putLong(value.getLeastSignificantBits());
        set(stack, key, buffer.array());
    }

    public static @Nullable UUID getUUID(@NotNull ItemStack stack, @NotNull String key) {
        byte[] bytes = getByteArray(stack, key);
        if (bytes == null) {
            return null;
        }

        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        return new UUID(buffer.getLong(), buffer.getLong());
    }

    public static void set(@NotNull ItemStack stack, @NotNull String key, Vector value) {
        if (value == null) {
            unset(stack, key);
            return;
        }

        ByteBuffer buffer = ByteBuffer.wrap(new byte[16]);
        buffer.putDouble(value.getX());
        buffer.putDouble(value.getY());
        buffer.putDouble(value.getZ());
        set(stack, key, buffer.array());
    }

    public static @Nullable Vector getVector(@NotNull ItemStack stack, @NotNull String key) {
        byte[] bytes = getByteArray(stack, key);
        if (bytes == null) {
            return null;
        }

        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        return new Vector(buffer.getDouble(), buffer.getDouble(), buffer.getDouble());
    }

    public static void set(@NotNull ItemStack stack, @NotNull String key, Vector3d value) {
        if (value == null) {
            unset(stack, key);
            return;
        }

        ByteBuffer buffer = ByteBuffer.wrap(new byte[16]);
        buffer.putDouble(value.x);
        buffer.putDouble(value.y);
        buffer.putDouble(value.z);
        set(stack, key, buffer.array());
    }

    public static @Nullable Vector3d getVector3d(@NotNull ItemStack stack, @NotNull String key) {
        byte[] bytes = getByteArray(stack, key);
        if (bytes == null) {
            return null;
        }

        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        return new Vector3d(buffer.getDouble(), buffer.getDouble(), buffer.getDouble());
    }

    public static void set(@NotNull ItemStack stack, @NotNull String key, Vector3f value) {
        if (value == null) {
            unset(stack, key);
            return;
        }

        ByteBuffer buffer = ByteBuffer.wrap(new byte[16]);
        buffer.putFloat(value.x);
        buffer.putFloat(value.y);
        buffer.putFloat(value.z);
        set(stack, key, buffer.array());
    }

    public static @Nullable Vector3f getVector3f(@NotNull ItemStack stack, @NotNull String key) {
        byte[] bytes = getByteArray(stack, key);
        if (bytes == null) {
            return null;
        }

        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        return new Vector3f(buffer.getFloat(), buffer.getFloat(), buffer.getFloat());
    }

    public static void set(@NotNull ItemStack stack, @NotNull String key, World value) {
        if (value == null) {
            unset(stack, key);
            return;
        }

        set(stack, key, value.getUID());
    }

    public static @Nullable World getWorld(@NotNull ItemStack stack, @NotNull String key) {
        UUID uuid = getUUID(stack, key);
        if (uuid == null) {
            return null;
        }

        return Bukkit.getWorld(uuid);
    }

    public static void set(@NotNull ItemStack stack, @NotNull String key, Location value) {
        if (value == null) {
            unset(stack, key);
            return;
        }

        ByteBuffer buffer = ByteBuffer.wrap(new byte[16]);
        buffer.putLong(value.getWorld().getUID().getMostSignificantBits());
        buffer.putLong(value.getWorld().getUID().getLeastSignificantBits());
        buffer.putDouble(value.x());
        buffer.putDouble(value.y());
        buffer.putDouble(value.z());
        buffer.putFloat(value.getYaw());
        buffer.putFloat(value.getPitch());
        set(stack, key, buffer.array());
    }

    public static @Nullable Location getLocation(@NotNull ItemStack stack, @NotNull String key) {
        byte[] bytes = getByteArray(stack, key);
        if (bytes == null) {
            return null;
        }

        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        World world = Bukkit.getWorld(new UUID(buffer.getLong(), buffer.getLong()));
        return new Location(world, buffer.getDouble(), buffer.getDouble(), buffer.getDouble(), buffer.getFloat(), buffer.getFloat());
    }

    public static <P, C> void set(@NotNull ItemStack stack, @NotNull String key, PersistentDataType<P, C> type, C value) {
        editMeta(stack, key, pdc -> pdc.set(key(key), type, value));
    }

    public static <P, C> @Nullable C get(@NotNull ItemStack stack, @NotNull String key, PersistentDataType<P, C> type) {
        return stack.getItemMeta().getPersistentDataContainer().get(key(key), type);
    }
}