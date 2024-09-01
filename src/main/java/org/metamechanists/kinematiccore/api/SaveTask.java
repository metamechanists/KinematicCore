package org.metamechanists.kinematiccore.api;

import org.bukkit.Bukkit;
import org.metamechanists.kinematiccore.KinematicCore;


public class SaveTask implements Runnable {
    private static final int INTERVAL = 60;

    @Override
    public void run() {

    }

    public static void init() {
        Bukkit.getServer().getScheduler().runTaskTimer(KinematicCore.getInstance(), new TickerTask(), 0, INTERVAL);
    }
}
