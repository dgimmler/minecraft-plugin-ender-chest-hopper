package com.danielgimmler.enderChestHopper.instance.playerConfig;

import com.danielgimmler.enderChestHopper.EnderChestHopper;
import com.danielgimmler.enderChestHopper.db.EnderChestLocation;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class PlayerConfig {

    private EnderChestHopper main;

    private Player player;
    private Map<String, EnderChestLocation> chests;
    private boolean hopperTransfersOn;

    public PlayerConfig(EnderChestHopper main, Player player) {
        this.main = main;

        this.player = player;
        this.chests = new HashMap<>();
        this.hopperTransfersOn = false;
    }

    public boolean areHopperTransfersOn() { return this.hopperTransfersOn; }

    public void toggleHopperTransfers() {
        if (!hopperTransfersOn) {
            player.sendMessage(
                Component.text("Turned hopper transfers ON. Ender Chests will now transfer to hoppers if configured to do so.")
                .color(NamedTextColor.GREEN));

            this.hopperTransfersOn = true;
            main.getTransferManager().handleTransfer(player);
        } else {
            player.sendMessage(
                Component.text("Turned hopper transfers OFF. Ender Chests will not transfer to hoppers even if configured to do so.")
                .color(NamedTextColor.RED));

            this.hopperTransfersOn = false;
        }
    }
}
