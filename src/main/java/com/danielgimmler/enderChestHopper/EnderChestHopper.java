package com.danielgimmler.enderChestHopper;

import com.danielgimmler.enderChestHopper.db.EnderChestLocationManager;
import com.danielgimmler.enderChestHopper.instance.gui.EnderChestGui;
import com.danielgimmler.enderChestHopper.instance.playerConfig.PlayerConfig;
import com.danielgimmler.enderChestHopper.instance.transferManager.TransferManager;
import com.danielgimmler.enderChestHopper.listeners.EnderChestListener;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class EnderChestHopper extends JavaPlugin {

    public Logger logger;
    private EnderChestLocationManager enderChestLocations;
    private TransferManager transferManager;
    private Map<UUID, EnderChestGui> gui;
    private Map<UUID, PlayerConfig> playerConfigMap;

    public TransferManager getTransferManager() { return transferManager; }

    public EnderChestGui getPlayerGui(UUID playerId) { return gui.get(playerId); }
    public EnderChestGui getPlayerGui(Player player) { return gui.get(player.getUniqueId()); }
    public PlayerConfig getPlayerConfig(UUID playerId) { return playerConfigMap.get(playerId); }
    public PlayerConfig getPlayerConfig(Player player) { return getPlayerConfig(player.getUniqueId()); }

    public boolean playerGuiIsSet(UUID playerId) { return gui.containsKey(playerId); }
    public boolean playerGuiIsSet(Player player) { return playerGuiIsSet(player.getUniqueId()); }
    public boolean playerConfigIsSet(UUID playerId) { return playerConfigMap.containsKey(playerId); }
    public boolean playerConfigIsSet(Player player) { return playerConfigIsSet(player.getUniqueId()); }

    public void createPlayerGui(Player player) { gui.put(player.getUniqueId(), new EnderChestGui(this, player)); }
    public void createPlayerConfig(Player player) { playerConfigMap.put(player.getUniqueId(), new PlayerConfig(this, player)); }

    @Override
    public void onEnable() {
        this.logger = new Logger(this, "EnderChestHopper");

        logger.info(
                "\n\n--------------------------------------------------------------------------------" +
                "\nEnder Chest Hoppers" +
                "\n--------------------------------------------------------------------------------\n"
        );

//        this.file = ensureEnderChestLocationFile();
        this.enderChestLocations = new EnderChestLocationManager(this);
        this.transferManager = new TransferManager(this);
        this.gui = new HashMap<>();
        this.playerConfigMap = new HashMap<>();

        this.getServer().getPluginManager().registerEvents(new EnderChestListener(this), this);
    }

    public EnderChestLocationManager getEnderChestLocationManager() { return this.enderChestLocations; }
}
