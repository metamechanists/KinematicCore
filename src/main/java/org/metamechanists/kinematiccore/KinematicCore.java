package org.metamechanists.kinematiccore;

import org.bukkit.plugin.java.JavaPlugin;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import org.metamechanists.kinematiccore.api.KinematicEntity;
import org.metamechanists.kinematiccore.api.TestEntity;

import java.io.File;
import java.util.UUID;


public class KinematicCore extends JavaPlugin {
    private DB db;
    private HTreeMap<UUID, KinematicEntity> entities;

    @Override
    public void onEnable() {
        getDataFolder().mkdir();

        db = DBMaker.fileDB(new File(getDataFolder(), "data.mapdb"))
                .closeOnJvmShutdown()
                .fileMmapEnableIfSupported()
                .transactionEnable()
                .make();

        entities = db.hashMap("entities", Serializer.UUID, Serializer.JAVA)
                .createOrOpen();

        entities.put(new UUID(1, 2), new TestEntity());
    }

    @Override
    public void onDisable() {
        db.close();
    }
}
