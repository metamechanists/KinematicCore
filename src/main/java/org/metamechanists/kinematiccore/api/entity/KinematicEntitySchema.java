package org.metamechanists.kinematiccore.api.entity;

import lombok.Getter;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.metamechanists.kinematiccore.api.Exceptions;
import org.metamechanists.kinematiccore.api.addon.KinematicAddon;
import org.metamechanists.kinematiccore.internal.entity.EntitySchemas;
import org.metamechanists.kinematiccore.api.state.StateReader;

import java.lang.reflect.Constructor;
import java.util.UUID;


@Getter
public class KinematicEntitySchema {
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

        EntitySchemas.register(this);
    }

    @SuppressWarnings("unused")
    public void unregister() {
        EntitySchemas.unregister(id);
    }

    public static KinematicEntitySchema get(String id) {
        return EntitySchemas.schema(id);
    }
}
