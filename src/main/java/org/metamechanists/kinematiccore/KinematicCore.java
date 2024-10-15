package org.metamechanists.kinematiccore;

import co.aikar.commands.PaperCommandManager;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import org.metamechanists.kinematiccore.internal.addon.AddonLifecycle;
import org.metamechanists.kinematiccore.internal.addon.AddonStorage;
import org.metamechanists.kinematiccore.internal.entity.EntityListener;
import org.metamechanists.kinematiccore.internal.entity.EntityStorage;
import org.metamechanists.kinematiccore.api.addon.KinematicAddon;
import org.metamechanists.kinematiccore.internal.entity.EntityTicker;
import org.metamechanists.kinematiccore.internal.command.KinematicCommand;
import org.metamechanists.kinematiccore.internal.storage.EntityStorageListener;


public class KinematicCore extends JavaPlugin implements KinematicAddon {
    @Getter
    private static KinematicCore instance;

    @Override
    public void onEnable() {
        instance = this;
        AddonStorage.init();
        AddonLifecycle.init();
        EntityStorage.init();
        EntityStorageListener.init();
        EntityTicker.init();
        EntityListener.init();
        PaperCommandManager manager = new PaperCommandManager(this);
        manager.enableUnstableAPI("help");
        manager.registerCommand(new KinematicCommand());
    }

    @Override
    public void onDisable() {
        AddonLifecycle.cleanup();
        EntityStorage.getInstance().close();
    }

    @Override
    public String name() {
        return "KinematicCore";
    }
}
