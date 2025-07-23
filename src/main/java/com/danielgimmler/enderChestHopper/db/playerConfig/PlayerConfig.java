package com.danielgimmler.enderChestHopper.db.playerConfig;

import com.danielgimmler.enderChestHopper.EnderChestHopper;
import com.danielgimmler.enderChestHopper.db.enderChestLocation.EnderChestLocation;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerConfig {

    private EnderChestHopper main;
    private PlayerConfigManager mgr;

    private Player player;
    private boolean hopperTransfersOn;
    private String playerName;

    private Map<String, EnderChestConfig> chests;

    // CONSTRUCTORS
    // -----------------------------------------------------------------------------------------------------------------

    public PlayerConfig(EnderChestHopper main, PlayerConfigManager mgr, Player player, boolean hopperTransfersOn) {
        this.main = main;
        this.mgr = mgr;

        this.player = player;
        this.chests = new HashMap<>();

        if (!loadPlayerConfig()) {
            this.hopperTransfersOn = hopperTransfersOn;
            this.playerName = player.getName();
        }

        System.out.println("playerName: " + this.playerName);
        System.out.println("hopper enabled: " + this.hopperTransfersOn);

        savePlayerConfig();
    }

    public PlayerConfig(EnderChestHopper main, PlayerConfigManager mgr, Player player) {
        this(main, mgr, player, false);
    }

    public PlayerConfig(EnderChestHopper main, PlayerConfigManager mgr, UUID playerId) {
        this.main = main;
        this.mgr = mgr;

        this.chests = new HashMap<>();

        if (!loadPlayerConfig(playerId)) {
            this.hopperTransfersOn = false;
            this.playerName = "";
        }

        savePlayerConfig(playerId);
    }

    public boolean isEnderChestConfigSet(Location location) { return chests.containsKey(new EnderChestLocation(main, location)); }
    public boolean isEnderChestConfigSet(EnderChestLocation chest) {
        System.out.println(chests);
        return chests.containsKey(chest.getKey());
    }

    public String getPlayerName() {
        return playerName;
    }

    public void listChests(Player player) {
        player.sendMessage(Component.text("You have the following chests configured:"));
        player.sendMessage(Component.text(""));

        for (String k : chests.keySet()) {
            EnderChestConfig chest = chests.get(k);

            player.sendMessage(Component.text("ID: " + chest.getName()));
            player.sendMessage(Component.text("Hopper Enabled: " + chest.isHopperEnabled()));
            player.sendMessage(Component.text(""));
        }
    }

    // SETTERS
    // -----------------------------------------------------------------------------------------------------------------

    public void setPlayer(Player player) {
        this.player = player;
        this.playerName = player.getName();
    }

    public void setEnderChestConfig(String chestKey) { setEnderChestConfig(chestKey, chestKey, false); }
    public void setEnderChestConfig(String chestKey, boolean hopperEnabled) { setEnderChestConfig(chestKey, chestKey, hopperEnabled); }
    public void setEnderChestConfig(Location location) { setEnderChestConfig(new EnderChestLocation(main, location));}
    public void setEnderChestConfig(Location location, String chestName) { setEnderChestConfig(new EnderChestLocation(main, location), chestName, false); }
    public void setEnderChestConfig(Location location, boolean hopperEnabled) {
        EnderChestLocation chest = new EnderChestLocation(main, location);
        setEnderChestConfig(chest, chest.getKey(), hopperEnabled);
        savePlayerConfig();
    }
    public void setEnderChestConfig(EnderChestLocation chest) { setEnderChestConfig(chest, chest.getKey(), false); }
    public void setEnderChestConfig(EnderChestLocation chest, String chestName) { setEnderChestConfig(chest, chestName, false); }
    public void setEnderChestConfig(EnderChestLocation chest, boolean hopperEnabled) { setEnderChestConfig(chest, chest.getKey(), hopperEnabled); }
    public void setEnderChestConfig(EnderChestLocation chest, String chestName, boolean hopperEnabled) { setEnderChestConfig(chest.getKey(), chestName, hopperEnabled); }
    public void setEnderChestConfig(String chestKey, String chestName, boolean hopperEnabled) {
        this.chests.put(chestKey, new EnderChestConfig(chestKey, chestName, hopperEnabled));
        savePlayerConfig();
    }

    // PUBLIC
    // -----------------------------------------------------------------------------------------------------------------

    public boolean areHopperTransfersOn() { return this.hopperTransfersOn; }

    public boolean isHopperEnabledForChest(EnderChestLocation chest) {
        String k = chest.getKey();

        if (!chests.containsKey(k)) return false;
        if (!areHopperTransfersOn()) return false;
        return chests.get(k).isHopperEnabled();
    }

    public void toggleHopperTransfers() {
        if (!hopperTransfersOn) {
            sendPlayerMessage(
                Component.text("Turned hopper transfers ON. Ender Chests will now transfer to hoppers if configured to do so.")
                .color(NamedTextColor.GREEN));

            this.hopperTransfersOn = true;
            main.getTransferManager().handleTransfer(player);
        } else {
            sendPlayerMessage(
                Component.text("Turned hopper transfers OFF. Ender Chests will not transfer to hoppers even if configured to do so.")
                .color(NamedTextColor.RED));

            this.hopperTransfersOn = false;
        }

        savePlayerConfig();
    }

    public void toggleHopperTransfersForChest(EnderChestLocation chestLocation, UUID playerId) {
        String k = chestLocation.getKey();
        EnderChestConfig chestConfig = chests.get(k);

        if (chestConfig == null) {
            chests.put(k, new EnderChestConfig(k, false));
            savePlayerConfig(playerId);

            return;
        }

        if (!chestConfig.isHopperEnabled()) {
            sendPlayerMessage(
                Component.text("Turned hopper transfers ON for this chest. Ender Chest will now transfer to hoppers if you have Ender Chest transfers enabled globally.")
                    .color(NamedTextColor.GREEN));

            chestConfig.enableHopper();
            main.getTransferManager().handleTransfer(player);
        } else {
            sendPlayerMessage(
                Component.text("Turned hopper transfers OFF for this chest. Ender Chest will not transfer to hoppers even if you have Ender Chest transfers enabled globally.")
                    .color(NamedTextColor.RED));

            chestConfig.disableHopper();
        }

        savePlayerConfig(playerId);
    }

    public void renameEnderChest(String chestKey, String chestName, UUID playerId) {
        if (chests.containsKey(chestKey))
            chests.get(chestKey).setName(chestName);
        else
            chests.put(chestKey, new EnderChestConfig(chestKey, chestName));

        savePlayerConfig(playerId);
    }

    public boolean loadPlayerConfig() { return loadPlayerConfig(player);  }
    public boolean loadPlayerConfig(Player player) {
        if (player == null) {
            main.logger.severe("Unable to load player config, player id null");

            return false;
        }

        return loadPlayerConfig(player.getUniqueId());
    }
    public boolean loadPlayerConfig(UUID playerId) {
        if (!mgr.fileExists()) return false;

        YamlConfiguration file = mgr.getYamlFile();
        if (!file.isConfigurationSection(playerId.toString())) return false;

        this.hopperTransfersOn = file.getBoolean("" + playerId + ".hopperTransfersOn", false);
        this.playerName = file.getString("" + playerId + ".playerName");

        if (!file.isConfigurationSection(playerId + ".chests"))
            return true;

        ConfigurationSection chestConfigs = mgr.getYamlFile().getConfigurationSection(playerId.toString() + ".chests");
        for ( String k : chestConfigs.getKeys(false)) {
            ConfigurationSection chest = chestConfigs.getConfigurationSection(k);
            if (chest == null) continue;

            chests.put(k, new EnderChestConfig(k, chest.getString("name"), chest.getBoolean("hopperEnabled")));
        }

        return true;
    }

    private void savePlayerConfig() { savePlayerConfig(player.getUniqueId()); }
    private void savePlayerConfig(UUID playerId)  {
        if (!mgr.fileExists()) return;

        String playerName = this.playerName;
        if (playerName == null && player != null)
            playerName = player.getName();

        System.out.println("Player name: " + playerName);

        // log player config in config file
        YamlConfiguration file = mgr.getYamlFile();
        file.set(playerId + ".hopperTransfersOn", hopperTransfersOn);
        file.set(playerId + ".playerName", playerName);

        // add chests section if not there already
        String chestsPath = playerId + ".chests";
        if (!file.isConfigurationSection(chestsPath))
            file.createSection(chestsPath);

        // log chest config
        for (String k : chests.keySet()) {
            EnderChestConfig chest = chests.get(k);

            file.set(chestsPath + "." + chest.getKey() + ".name", chest.getName());
            file.set(chestsPath + "." + chest.getKey() + ".hopperEnabled", chest.isHopperEnabled());
        }

        try {
            file.save(mgr.getFile());
            main.logger.info("Saved Player config to file");
        } catch (IOException ex) {
            String errorMsg = "Error Saving player config to file. Ender Chest configurations may not behave as expected";

            sendPlayerMessage(
                Component.text(errorMsg + " Error has been logged.")
                .color(NamedTextColor.RED));
            main.logger.severe(errorMsg + " Error: " + ex.getMessage());
        }
    }

    public void removePlayerConfig() throws IOException { removePlayerConfig(player.getUniqueId()); }
    public void removePlayerConfig(UUID playerId) throws IOException {
        if (!mgr.fileExists()) return;

        YamlConfiguration file = mgr.getYamlFile();
        String k = playerId.toString();

        if (file.contains(k)) {
            file.set(k, null);
            file.save(mgr.getFile());
        }
    }

    public void removeChestFromPlayerConfig(EnderChestLocation chest) throws IOException {
        if (player == null) {
            main.logger.severe("Player not found");

            return;
        }

        removeChestFromPlayerConfig(chest, player.getUniqueId().toString());
    }
    public void removeChestFromPlayerConfig(EnderChestLocation chest, String playerId) throws IOException {
        String k = chest.getKey();
        chests.remove(k);

        if (!mgr.fileExists()) return;
        YamlConfiguration file = mgr.getYamlFile();

        ConfigurationSection chestConfigs = file.getConfigurationSection(playerId + ".chests");
        if (chestConfigs == null) return;

        if (chestConfigs.isConfigurationSection(k)) {
            chestConfigs.set(k, null);
            file.save(mgr.getFile());
        }
    }

    // HELPERS
    // -----------------------------------------------------------------------------------------------------------------
    private void sendPlayerMessage(Component msg) {
        if (player != null)
            player.sendMessage(msg);
    }
}
