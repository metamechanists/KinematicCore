package org.metamechanists.kinematiccore.internal.entity;

import org.jetbrains.annotations.NotNull;
import org.metamechanists.kinematiccore.api.Exceptions;
import org.metamechanists.kinematiccore.api.addon.KinematicAddon;
import org.metamechanists.kinematiccore.api.entity.KinematicEntitySchema;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


@SuppressWarnings("unused")
public final class EntitySchemas {
    private static final Map<String, KinematicEntitySchema> schemas = new ConcurrentHashMap<>();

    private EntitySchemas() {}

    public static void register(@NotNull KinematicEntitySchema schema) {
        if (schemas.containsKey(schema.getId())) {
            throw new Exceptions.IdConflictException(schema.getId());
        }

        schemas.put(schema.getId(), schema);
    }

    public static void unregister(@NotNull String id) {
        schemas.remove(id);
    }

    public static KinematicEntitySchema schema(@NotNull String id) {
        return schemas.get(id);
    }

    public static boolean isRegistered(@NotNull String id) {
        return schema(id) != null;
    }

    public static boolean isRegistered(@NotNull KinematicEntitySchema schema) {
        return schema(schema.getId()) != null;
    }

    public static @NotNull Set<String> registeredSchemas() {
        return schemas.keySet();
    }

    public static @NotNull Set<String> registeredSchemasByAddon(@NotNull KinematicAddon addonClass) {
        String addon = addonClass.getClass().getSimpleName().toLowerCase();
        return schemas.entrySet()
                .stream()
                .filter(schema -> schema.getValue().getAddonName().equals(addon))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }
}
