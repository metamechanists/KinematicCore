package org.metamechanists.kinematiccore;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import org.metamechanists.kinematiccore.api.EntityStorage;
import org.metamechanists.kinematiccore.api.TickerTask;


public class KinematicCore extends JavaPlugin {
    @Getter
    private static KinematicCore instance;

    @Override
    public void onEnable() {
        instance = this;
        EntityStorage.init();
        TickerTask.init();
    }

    @Override
    public void onDisable() {
        EntityStorage.cleanup();
    }
}
