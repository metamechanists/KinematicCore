package org.metamechanists.kinematiccore;

import co.aikar.commands.PaperCommandManager;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.metamechanists.kinematiccore.api.EntityStorage;
import org.metamechanists.kinematiccore.api.KinematicAddon;
import org.metamechanists.kinematiccore.api.TestListener;
import org.metamechanists.kinematiccore.api.TickerTask;
import org.metamechanists.kinematiccore.command.KinematicCommand;


public class KinematicCore extends JavaPlugin implements KinematicAddon {
    @Getter
    private static KinematicCore instance;

    @Override
    public void onEnable() {
        instance = this;
        EntityStorage.init();
        TickerTask.init();
        Bukkit.getServer().getPluginManager().registerEvents(new TestListener(), this);
        PaperCommandManager manager = new PaperCommandManager(this);
        manager.enableUnstableAPI("help");
        manager.registerCommand(new KinematicCommand());
    }

    @Override
    public void onDisable() {
        EntityStorage.close();
    }
}
