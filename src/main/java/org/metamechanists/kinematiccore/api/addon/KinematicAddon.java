package org.metamechanists.kinematiccore.api.addon;

import org.metamechanists.kinematiccore.KinematicCore;


@SuppressWarnings({"InterfaceMayBeAnnotatedFunctional", "unused"})
public interface KinematicAddon {
    String name();
    default void cleanup() {
        try {
            KinematicCore.cleanup(this);
        } catch (NoClassDefFoundError e) {
            // Kinematic already disabled so should have saved addon data already
        }
    }
}
