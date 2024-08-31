package org.metamechanists.cybernetics;

import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;


import javax.annotation.Nonnull;

public class Cybernetics extends JavaPlugin implements SlimefunAddon {
    @Getter
    private static Cybernetics instance;

    @Override
    public void onEnable() {
        instance = this;
    }

    @Override
    public void onDisable() {}

    @Nonnull
    @Override
    public JavaPlugin getJavaPlugin() {
        return this;
    }

    @Nonnull
    @Override
    public String getBugTrackerURL() {
        return "";
    }
}
