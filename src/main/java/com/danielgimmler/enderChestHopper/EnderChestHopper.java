package com.danielgimmler.enderChestHopper;

import com.danielgimmler.enderChestHopper.db.enderChestLocation.EnderChestLocationManager;
import com.danielgimmler.enderChestHopper.db.playerConfig.PlayerConfigManager;
import com.danielgimmler.enderChestHopper.instance.ChunkLoader.ChunkManager;
import com.danielgimmler.enderChestHopper.instance.gui.EnderChestGuiManager;
import com.danielgimmler.enderChestHopper.instance.transferManager.TransferManager;
import com.danielgimmler.enderChestHopper.listeners.EnderChestListener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public final class EnderChestHopper extends JavaPlugin {

    public Logger logger;
    private EnderChestLocationManager enderChestLocations;
    private TransferManager transferManager;
    private PlayerConfigManager playerConfigManager;
    private EnderChestGuiManager guiManager;
    private ChunkManager chunkManager;

    public TransferManager getTransferManager() { return transferManager; }
    public PlayerConfigManager getPlayerConfigManager() { return playerConfigManager; }
    public EnderChestGuiManager getGuiManager() { return guiManager;}
    public ChunkManager getChunkManager() { return chunkManager; }

    @Override
    public void onEnable() {
        this.logger = new Logger(this);
        this.logger.setLogLevel(Level.FINEST);

        logger.info(
                "\n\n--------------------------------------------------------------------------------" +
                "\nEnder Chest Hoppers" +
                "\n--------------------------------------------------------------------------------\n"
        );

//        this.file = ensureEnderChestLocationFile();
        this.enderChestLocations = new EnderChestLocationManager(this);
        this.playerConfigManager = new PlayerConfigManager(this);
        this.guiManager = new EnderChestGuiManager(this);
        this.transferManager = new TransferManager(this);
        this.chunkManager = new ChunkManager(this);

        this.getServer().getPluginManager().registerEvents(new EnderChestListener(this), this);
    }

    public EnderChestLocationManager getEnderChestLocationManager() { return this.enderChestLocations; }

    public boolean isPaper() {
        try {
            Class.forName("com.destroystokyo.paper.PaperConfig");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

}
