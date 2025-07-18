package com.danielgimmler.enderChestHopper.db;

import com.danielgimmler.enderChestHopper.EnderChestHopper;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EnderChestLocationManager {

    private EnderChestHopper main;

    private File file;
    private Map<String, EnderChestLocation> enderChests;

    public EnderChestLocationManager(EnderChestHopper main) {
        this.main = main;

        this.file = ensureEnderChestLocationFile();
    }

    private File ensureEnderChestLocationFile() {
        if (file != null) return file;

        this.file = new File(main.getDataFolder(), "enderChestLocations.yml");

        if (file.exists()) return file;

        try {
            file.createNewFile();
        } catch(IOException e) {
            main.logger.severe("Can't load enderChestLocations.yml file. Error:");
            main.logger.severe(e.getMessage());

            return file;
        }

        return file;
    }

    public File getFile() { return this.file; }
    public YamlConfiguration getYamlFile() { return YamlConfiguration.loadConfiguration(getFile()); }
    public void setEnderChestLocation(EnderChestLocation chest) { enderChests.put(chest.getKey(), chest); }

    public boolean fileExists() {
        if (getFile() == null) return false;

        return getFile().exists();
    }

    public List<EnderChestLocation> getEnderChests() throws IOException {
        String errorMsg = "Yaml file not found!";
        if (this.file == null) throw new IllegalStateException(errorMsg);
        if (!this.file.exists()) throw new IllegalStateException(errorMsg);

        YamlConfiguration file = YamlConfiguration.loadConfiguration(this.file);
        Set<String> keys = file.getKeys(false);

        List<EnderChestLocation> list = new ArrayList<>();
        for (String k : keys)
            list.add(EnderChestLocation.getEnderChestLocation(main, k));

        return list;
    }

}
