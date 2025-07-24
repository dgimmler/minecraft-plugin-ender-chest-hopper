package com.danielgimmler.enderChestHopper.db;

import com.danielgimmler.enderChestHopper.EnderChestHopper;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class ConfigManagerBase {

    protected EnderChestHopper main;

    protected File file;
    private final String fileName;

    // CONSTRUCTORS
    // -----------------------------------------------------------------------------------------------------------------

    public ConfigManagerBase(EnderChestHopper main, String fileName) {
        this.main = main;
        this.fileName = fileName;
        this.file = ensureFile();
    }

    private File ensureFile() {
        if (file != null) return file;

        File dataFolder = main.getDataFolder();
        if (!dataFolder.exists())
            dataFolder.mkdirs(); // Ensure the plugin folder exists

        this.file = new File(dataFolder, fileName);
        if (file.exists()) return file;

        try {
            file.createNewFile();
        } catch(IOException e) {
            main.logger.severe("Can't load " + dataFolder.getName() + "/" + fileName + " file. Error:");
            main.logger.severe(e.getMessage());

            return file;
        }

        return file;
    }

    // GETTERS
    // -----------------------------------------------------------------------------------------------------------------

    public File getFile() { return this.file; }
    public YamlConfiguration getYamlFile() { return YamlConfiguration.loadConfiguration(getFile()); }

    // HELPERS
    // -----------------------------------------------------------------------------------------------------------------

    public boolean fileExists() {
        if (getFile() == null) return false;

        return getFile().exists();
    }
}
