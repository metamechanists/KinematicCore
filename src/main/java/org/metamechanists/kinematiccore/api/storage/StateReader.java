package org.metamechanists.kinematiccore.api.storage;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.metamechanists.kinematiccore.KinematicCore;
import org.metamechanists.kinematiccore.api.Exceptions;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@SuppressWarnings("unused")
public class StateReader {
    private String id;
    private int version;
    private UUID uuid;
    private final Map<String, Object> map = new HashMap<>();

    StateReader(byte[] bytes) {
        KryoStorage.read(bytes, (kryo, input) -> {
            id = input.readString();
            version = input.readInt();
            uuid = new UUID(input.readLong(), input.readLong());

            while (input.position() < input.limit()) {
                KinematicCore.getInstance().getLogger().severe(Arrays.toString(input.getBuffer()));
                KinematicCore.getInstance().getLogger().severe(String.valueOf(input.position()));

                String key = input.readString();
//                Object value = kryo.readClassAndObject(input);
                map.put(key, 6);
                KinematicCore.getInstance().getLogger().severe(key);
            }
        });
    }

    public String id() {
        return id;
    }

    public UUID uuid() {
        return uuid;
    }

    public int version() {
        return version;
    }

    public @Nullable <T> T get(@NotNull String key, @NotNull Class<T> clazz) {
        Object object = map.get(key);
        if (object == null) {
            throw new Exceptions.ValueNotFoundException(key, map.keySet());
        }
        if (!clazz.isInstance(object)) {
            throw new Exceptions.ValueWrongTypeException(key, clazz.getSimpleName(), object.getClass().getSimpleName());
        }
        return clazz.cast(object);
    }

    @SuppressWarnings("unchecked")
    public @Nullable <T> T get(@NotNull String key, @NotNull T instance) {
        Object object = map.get(key);
        if (object == null) {
            throw new Exceptions.ValueNotFoundException(key, map.keySet());
        }
        if (!instance.getClass().isInstance(object)) {
            throw new Exceptions.ValueWrongTypeException(key, instance.getClass().getSimpleName(), object.getClass().getSimpleName());
        }
        return (T) object;
    }
}
