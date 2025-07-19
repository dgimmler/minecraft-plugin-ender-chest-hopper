package com.danielgimmler.enderChestHopper.db.playerConfig;

import com.danielgimmler.enderChestHopper.EnderChestHopper;
import com.danielgimmler.enderChestHopper.db.enderChestLocation.EnderChestLocation;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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

    private Map<String, EnderChestLocation> chests;

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

            savePlayerConfig();
        }
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

            savePlayerConfig(playerId);
        }
    }

    // SETTERS
    // -----------------------------------------------------------------------------------------------------------------

    public void setPlayer(Player player) {
        this.player = player;
        this.playerName = player.getName();
    }

    // PUBLIC
    // -----------------------------------------------------------------------------------------------------------------

    public boolean areHopperTransfersOn() { return this.hopperTransfersOn; }

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

        this.hopperTransfersOn = mgr.getYamlFile().getBoolean("" + playerId + ".hopperTransfersOn", false);
        this.playerName = mgr.getYamlFile().getString("" + playerId + ".playerName");

        return true;
    }

    private void savePlayerConfig() { savePlayerConfig(player.getUniqueId()); }
    private void savePlayerConfig(UUID playerId)  {
        if (!mgr.fileExists()) return;

        String playerName = this.playerName;
        if (player != null)
            playerName = player.getName();

        // log player config in config file
        YamlConfiguration file = mgr.getYamlFile();
        file.set("" + playerId, Map.ofEntries(
            Map.entry("hopperTransfersOn", hopperTransfersOn),
            Map.entry("playerName", playerName)
        ));

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

    // HELPERS
    // -----------------------------------------------------------------------------------------------------------------
    private void sendPlayerMessage(Component msg) {
        if (player != null)
            player.sendMessage(msg);
    }
}
