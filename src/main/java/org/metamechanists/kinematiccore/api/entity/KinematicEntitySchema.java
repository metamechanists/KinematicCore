package org.metamechanists.kinematiccore.api.entity;

import lombok.Getter;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.metamechanists.kinematiccore.api.Exceptions;
import org.metamechanists.kinematiccore.api.KinematicAddon;
import org.metamechanists.kinematiccore.api.storage.StateReader;

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
        this.id = addonClass.getName() +":" + id.toLowerCase();
        this.kinematicClass = kinematicClass;
        this.entityClass = entityClass;

        try {
            constructor = kinematicClass.getConstructor(StateReader.class);
        } catch (NoSuchMethodException e) {
            throw new Exceptions.MissingConstructorException(id);
        }

        constructor.setAccessible(true);
    }
}
