package org.metamechanists.kinematiccore.api;

import lombok.Getter;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.metamechanists.kinematiccore.KinematicCore;

import java.lang.reflect.Constructor;


@Getter
public class KinematicEntitySchema {
    private final String id;
    private final Class<? extends KinematicEntity<?>> kinematicClass;
    private final Class<? extends Entity> entityClass;
    private final Constructor<? extends KinematicEntity<?>> constructor;

    public KinematicEntitySchema(
            @NotNull String id,
            @NotNull Class<? extends KinematicAddon> addonClass,
            @NotNull Class<? extends KinematicEntity<?>> kinematicClass,
            @NotNull Class<? extends Entity> entityClass
    ) {
        this.id = addonClass.getName() + id.toLowerCase();
        this.kinematicClass = kinematicClass;
        this.entityClass = entityClass;

        try {
            constructor = kinematicClass.getConstructor(StateReader.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(id + " must implement a constructor like public SomeKinematicEntity(StateReader reader) { ... }");
        }

        constructor.setAccessible(true);
    }
}
