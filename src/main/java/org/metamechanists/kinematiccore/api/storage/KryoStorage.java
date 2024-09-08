package org.metamechanists.kinematiccore.api.storage;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;


final class KryoStorage {
    private static final Kryo writeKryo = new Kryo();
    private static final Kryo readKryo = new Kryo();

    private KryoStorage() {}

    public static void register(Class<?> clazz) {
        readKryo.register(clazz);
        writeKryo.register(clazz);
    }

    public static synchronized byte[] write(@NotNull BiConsumer<Kryo, Output> consumer) {
        Output output = new Output(1024, -1);
        consumer.accept(writeKryo, output);
        return output.toBytes();
    }

    public static synchronized void read(byte[] bytes,@NotNull BiConsumer<Kryo, Input> consumer) {
        Input input = new Input(bytes);
        consumer.accept(writeKryo, input);
    }
}
