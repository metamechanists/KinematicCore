package org.metamechanists.kinematiccore.api.state;

import org.metamechanists.kinematiccore.internal.state.KryoStorage;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@SuppressWarnings("unused")
public class StateWriter {
    private final String id;
    private final UUID uuid;
    private final Map<String, Object> map = new HashMap<>();
    private int version;

    public StateWriter(String id, UUID uuid) {
        this.id = id;
        this.uuid = uuid;
    }

    public byte[] toBytes() {
        return KryoStorage.write((kryo, output) -> {
            output.writeString(id);
            output.writeInt(version);
            output.writeLong(uuid.getMostSignificantBits());
            output.writeLong(uuid.getLeastSignificantBits());

            for (Map.Entry<String, Object> entry : map.entrySet()) {
                output.writeString(entry.getKey());
                kryo.writeClassAndObject(output, entry.getValue());
            }

            output.close();
        });
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public void set(String key, Object value) {
        map.put(key, value);
    }
}
