package com.danielgimmler.enderChestHopper.instance.gui;

import com.danielgimmler.enderChestHopper.EnderChestHopper;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EnderChestGuiManager {

    private EnderChestHopper main;
    private final Map<UUID, EnderChestGui> playerGuis;

    public EnderChestGuiManager(EnderChestHopper main) {
        this.main = main;
        playerGuis = new HashMap<>();
    }

    // GETTERS
    // -----------------------------------------------------------------------------------------------------------------

    public EnderChestGui getPlayerGui(UUID playerId) { return playerGuis.get(playerId); }
    public EnderChestGui getPlayerGui(Player player) { return playerGuis.get(player.getUniqueId()); }

    // SETTERS
    // -----------------------------------------------------------------------------------------------------------------

    public void createPlayerGui(Player player) { playerGuis.put(player.getUniqueId(), new EnderChestGui(main, player)); }

    // HELPERS
    // -----------------------------------------------------------------------------------------------------------------

    public boolean playerGuiIsSet(UUID playerId) { return playerGuis.containsKey(playerId); }
    public boolean playerGuiIsSet(Player player) { return playerGuiIsSet(player.getUniqueId()); }

}
