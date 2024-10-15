package org.metamechanists.kinematiccore.api.entity;

import lombok.Getter;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.metamechanists.kinematiccore.api.Exceptions;
import org.metamechanists.kinematiccore.api.addon.KinematicAddon;
import org.metamechanists.kinematiccore.api.state.StateReader;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


@Getter
@SuppressWarnings("unused")
public class KinematicEntitySchema {
    private static final Map<String, KinematicEntitySchema> schemas = new ConcurrentHashMap<>();

    private final String id;
    private final String addonName;
    private final Class<? extends KinematicEntity<?, ?>> kinematicClass;
    private final Class<? extends Entity> entityClass;
    private final Constructor<? extends KinematicEntity<?, ?>> constructor;

    public KinematicEntitySchema(
            @NotNull String id,
            @NotNull Class<? extends KinematicAddon> addonClass,
            @NotNull Class<? extends KinematicEntity<?, ?>> kinematicClass,
            @NotNull Class<? extends Entity> entityClass
    ) {
        this.addonName = addonClass.getSimpleName().toLowerCase();
        this.id = addonName + ":" + id.toLowerCase();
        this.kinematicClass = kinematicClass;
        this.entityClass = entityClass;

        try {
            constructor = kinematicClass.getConstructor(StateReader.class);
        } catch (NoSuchMethodException e) {
            throw new Exceptions.MissingConstructorException(id);
        }

        constructor.setAccessible(true);

        register(this);
    }

    @SuppressWarnings("unused")
    public void unregister() {
        schemas.remove(id);
    }

    public static void register(@NotNull KinematicEntitySchema schema) {
        if (schemas.containsKey(schema.id)) {
            throw new Exceptions.IdConflictException(schema.id);
        }

        schemas.put(schema.id, schema);
    }

    public static KinematicEntitySchema get(@NotNull String id) {
        return schemas.get(id);
    }

    public static boolean isRegistered(@NotNull String id) {
        return get(id) != null;
    }

    public static boolean isRegistered(@NotNull KinematicEntitySchema schema) {
        return get(schema.id) != null;
    }

    public static @NotNull Set<String> registeredSchemas() {
        return schemas.keySet();
    }

    public static @NotNull Set<String> registeredSchemasByAddon(@NotNull KinematicAddon addonClass) {
        String addon = addonClass.getClass().getSimpleName().toLowerCase();
        return schemas.entrySet()
                .stream()
                .filter(schema -> schema.getValue().addonName.equals(addon))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }
}
