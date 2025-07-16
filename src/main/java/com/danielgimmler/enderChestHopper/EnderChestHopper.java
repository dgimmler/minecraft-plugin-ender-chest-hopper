package com.danielgimmler.enderChestHopper;

import com.danielgimmler.enderChestHopper.listeners.EnderChestListener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public final class EnderChestHopper extends JavaPlugin {

    public File file;
    public Logger logger;

    @Override
    public void onEnable() {
        this.logger = new Logger(this, "EnderChestHopper");

        logger.info(
                "\n\n--------------------------------------------------------------------------------" +
                "\nEnder Chest Hoppers" +
                "\n--------------------------------------------------------------------------------\n"
        );

        this.file = ensureEnderChestLocationFile();

        this.getServer().getPluginManager().registerEvents(new EnderChestListener(this), this);
    }

    public File ensureEnderChestLocationFile() {
        if (file != null) return file;

        this.file = new File(getDataFolder(), "enderChestLocations.yml");

        if (file.exists()) return file;

        try {
            file.createNewFile();
        } catch(IOException e) {
            logger.severe("Can't load enderChestLocations.yml file. Error:");
            logger.severe(e.getMessage());

            return file;
        }

        return file;
    }
}
