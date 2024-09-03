package org.metamechanists.kinematiccore.api;

import com.esotericsoftware.kryo.io.Input;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@SuppressWarnings("unused")
public class StateReader {
    private final Map<String, Object> map = new HashMap<>();

    StateReader(@NotNull Input input) {
        while (input.position() < input.limit()) {
            String key = input.readString();
            Object value = switch (StateType.VALUES[input.readByte()]) {
                case STRING -> input.readString();
                case BYTE -> input.readByte();
                case BOOLEAN -> input.readBoolean();
                case SHORT -> input.readShort();
                case INTEGER -> input.readInt();
                case LONG -> input.readLong();
                case FLOAT -> input.readFloat();
                case DOUBLE -> input.readDouble();
                case UUID -> new UUID(input.readLong(), input.readLong());
            };

            map.put(key, value);
        }
    }

    public @Nullable String getString(String key) {
        return map.get(key) instanceof String cast ? cast : null;
    }

    public @Nullable Byte getByte(String key) {
        return map.get(key) instanceof Byte cast ? cast : null;
    }

    public @Nullable Boolean getBoolean(String key) {
        return map.get(key) instanceof Boolean cast ? cast : null;
    }

    public @Nullable Short getShort(String key) {
        return map.get(key) instanceof Short cast ? cast : null;
    }

    public @Nullable Integer getInt(String key) {
        return map.get(key) instanceof Integer cast ? cast : null;
    }

    public @Nullable Long getLong(String key) {
        return map.get(key) instanceof Long cast ? cast : null;
    }

    public @Nullable Float getFloat(String key) {
        return map.get(key) instanceof Float cast ? cast : null;
    }

    public @Nullable Double getDouble(String key) {
        return map.get(key) instanceof Double cast ? cast : null;
    }

    public @Nullable UUID getUUID(String key) {
        return map.get(key) instanceof UUID cast ? cast : null;
    }
}
