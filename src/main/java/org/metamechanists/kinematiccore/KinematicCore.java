package org.metamechanists.kinematiccore;

import co.aikar.commands.PaperCommandManager;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import org.metamechanists.kinematiccore.api.storage.EntityStorage;
import org.metamechanists.kinematiccore.api.KinematicAddon;
import org.metamechanists.kinematiccore.api.entity.KinematicEntityTicker;
import org.metamechanists.kinematiccore.command.KinematicCommand;


public class KinematicCore extends JavaPlugin implements KinematicAddon {
    @Getter
    private static KinematicCore instance;

    @Override
    public void onEnable() {
        instance = this;
        EntityStorage.init();
        KinematicEntityTicker.init();
        PaperCommandManager manager = new PaperCommandManager(this);
        manager.enableUnstableAPI("help");
        manager.registerCommand(new KinematicCommand());
    }

    @Override
    public void onDisable() {
        EntityStorage.close();
    }
}
