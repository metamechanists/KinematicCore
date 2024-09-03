package org.metamechanists.kinematiccore.api;

import com.esotericsoftware.kryo.io.Output;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@SuppressWarnings("unused")
public class StateWriter {
    private final String id;
    private final Map<String, Object> map = new HashMap<>();
    private int version;

    StateWriter(String id) {
        this.id = id;
    }

    void write(@NotNull Output output) {
        output.writeString(id);
        output.writeInt(version);

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            output.writeString(entry.getKey());

            // TODO switch out for pattern matching in JDK 17+
            if (entry.getValue() instanceof String cast) {
                output.writeByte(StateType.STRING.ordinal());
                output.writeString(cast);
            } else if (entry.getValue() instanceof Byte cast) {
                output.writeByte(StateType.BYTE.ordinal());
                output.writeByte(cast);
            } else if (entry.getValue() instanceof Boolean cast) {
                output.writeByte(StateType.BOOLEAN.ordinal());
                output.writeBoolean(cast);
            } else if (entry.getValue() instanceof Short cast) {
                output.writeByte(StateType.SHORT.ordinal());
                output.writeShort(cast);
            } else if (entry.getValue() instanceof Integer cast) {
                output.writeByte(StateType.INTEGER.ordinal());
                output.writeInt(cast);
            } else if (entry.getValue() instanceof Long cast) {
                output.writeByte(StateType.LONG.ordinal());
                output.writeLong(cast);
            } else if (entry.getValue() instanceof Float cast) {
                output.writeByte(StateType.FLOAT.ordinal());
                output.writeFloat(cast);
            } else if (entry.getValue() instanceof Double cast) {
                output.writeByte(StateType.DOUBLE.ordinal());
                output.writeDouble(cast);
            } else if (entry.getValue() instanceof UUID cast) {
                output.writeByte(StateType.UUID.ordinal());
                output.writeLong(cast.getMostSignificantBits());
                output.writeLong(cast.getLeastSignificantBits());
            }
        }
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public void set(String key, String value) {
        map.put(key, value);
    }

    public void set(String key, Byte value) {
        map.put(key, value);
    }

    public void set(String key, Boolean value) {
        map.put(key, value);
    }

    public void set(String key, Short value) {
        map.put(key, value);
    }

    public void set(String key, Integer value) {
        map.put(key, value);
    }

    public void set(String key, Long value) {
        map.put(key, value);
    }

    public void set(String key, Float value) {
        map.put(key, value);
    }

    public void set(String key, Double value) {
        map.put(key, value);
    }

    public void set(String key, UUID value) {
        map.put(key, value);
    }
}
