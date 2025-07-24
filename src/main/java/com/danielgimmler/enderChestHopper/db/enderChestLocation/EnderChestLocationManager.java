package com.danielgimmler.enderChestHopper.db.enderChestLocation;

import com.danielgimmler.enderChestHopper.EnderChestHopper;
import com.danielgimmler.enderChestHopper.db.ConfigManagerBase;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.IOException;
import java.util.*;

public class EnderChestLocationManager extends ConfigManagerBase {

    private final Map<String, EnderChestLocation> enderChests;

    public EnderChestLocationManager(EnderChestHopper main) {
        super(main, "enderChestLocations.yml");

        this.enderChests = new HashMap<>();
    }

    public void setEnderChestLocation(EnderChestLocation chest) { enderChests.put(chest.getKey(), chest); }

    public List<EnderChestLocation> getEnderChests() throws IOException {
        if (!fileExists()) throw new IllegalStateException("Yaml file not found!");

        YamlConfiguration file = getYamlFile();

        List<EnderChestLocation> list = new ArrayList<>();
        for (String k : file.getKeys(false))
            list.add(EnderChestLocation.getEnderChestLocation(main, k));

        return list;
    }

    public boolean enderChestIsSaved(Location location) {
        return enderChests.get(new EnderChestLocation(main, location).getKey()) != null;
    }

    public void removeEnderChestLocation(EnderChestLocation chest) throws IOException {
        chest.removeLocation();

        this.enderChests.remove(chest.getKey());
    }
}
