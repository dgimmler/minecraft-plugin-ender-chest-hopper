package com.danielgimmler.enderChestHopper.db.playerConfig;

import com.danielgimmler.enderChestHopper.EnderChestHopper;
import com.danielgimmler.enderChestHopper.db.ConfigManagerBase;
import com.danielgimmler.enderChestHopper.db.enderChestLocation.EnderChestLocation;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.*;

public class PlayerConfigManager extends ConfigManagerBase {

    private final Map<UUID, PlayerConfig> playerConfigs;

    public PlayerConfigManager(EnderChestHopper main) {
        super(main, "playerConfig.yml");

        this.playerConfigs = new HashMap<>();
        loadPlayerConfigs();
    }

    // GETTERS
    // -----------------------------------------------------------------------------------------------------------------

    public PlayerConfig getPlayerConfig(UUID playerId) { return playerConfigs.get(playerId); }
    public PlayerConfig getPlayerConfig(Player player) { return getPlayerConfig(player.getUniqueId()); }

    // SETTERS
    // -----------------------------------------------------------------------------------------------------------------

    public void createPlayerConfig(Player player) { playerConfigs.put(player.getUniqueId(), new PlayerConfig(main, this, player)); }
    public void createPlayerConfig(UUID playerId) { playerConfigs.put(playerId, new PlayerConfig(main, this, playerId)); }

    public void loadPlayerConfigs() {
        if (!fileExists()) return;

        YamlConfiguration file = getYamlFile();

        for (String k : file.getKeys(false)) {
            Player player = Bukkit.getPlayer(UUID.fromString(k));
            if (player != null)
                createPlayerConfig(player);
            else
                createPlayerConfig(UUID.fromString(k));
        }
    }

    public void loadPlayerConfig(Player player) {
        if (!playerConfigIsSet(player)) return;

        playerConfigs.get(player.getUniqueId()).loadPlayerConfig(player);
    }

    public void removeChestFromPlayerConfigs(EnderChestLocation chest, Player player) {
        for ( UUID i : playerConfigs.keySet()) {
            try {
                playerConfigs.get(i).removeChestFromPlayerConfig(chest, player);
            } catch (IOException e) {
                main.logger.severe("Unable to remove chest from player " + playerConfigs.get(i).getPlayerName() + " config. Already removed?");
            }
        }
    }

    // HELPERS
    // -----------------------------------------------------------------------------------------------------------------

    public boolean playerConfigIsSet(UUID playerId) { return playerConfigs.containsKey(playerId); }
    public boolean playerConfigIsSet(Player player) { return playerConfigIsSet(player.getUniqueId()); }
}
