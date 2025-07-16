package com.danielgimmler.enderChestHopper.listeners;

import com.danielgimmler.enderChestHopper.EnderChestHopper;
import com.danielgimmler.enderChestHopper.db.EnderChestLocation;
import com.danielgimmler.enderChestHopper.manager.TransferManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitTask;

import java.io.IOException;
import java.util.*;

public class EnderChestListener implements Listener {
    protected EnderChestHopper main;
    private final Map<UUID, BukkitTask> debouncedTransfers;
    private final long DEBOUNCE_DELAY_TICKS;
    private TransferManager transferManager;

    public EnderChestListener(EnderChestHopper main) {
        this.main = main;
        this.debouncedTransfers = new HashMap<>();
        this.DEBOUNCE_DELAY_TICKS = 10L; // 0.5 seconds
        this.transferManager = new TransferManager(main);
    }

    @EventHandler
    public void onHopperTransfer(InventoryMoveItemEvent e) {
        if (e.getInitiator().getType() != InventoryType.HOPPER)
            return;

        for (Player player : Bukkit.getOnlinePlayers())
            transferManager.handleTransfer(player);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        // begin any transfers for any chests user has
        transferManager.handleTransfer(e.getPlayer());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (!player.isOnline()) return;

        Inventory clicked = e.getInventory();
        if (!clicked.equals(player.getEnderChest())) return;

        transferManager.handleTransfer(player);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        Location blockLocation = e.getBlock().getLocation();

        if (e.getBlockPlaced().getType() == Material.ENDER_CHEST) {
            handleEnderChestPlace(e, blockLocation);
        } else if (e.getBlockPlaced().getType() == Material.HOPPER) {
            handleHopperPlace(e, blockLocation);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Location blockLocation = e.getBlock().getLocation();

        if (e.getBlock().getType() == Material.ENDER_CHEST) {
            handleEnderChestBreak(blockLocation);
        } else if (e.getBlock().getType() == Material.HOPPER) {
            handleHopperBreak(blockLocation);
        }
    }

    // HELPER FUNCTIONS
    // -----------------------------------------------------------------------------------------------------------------

    private void toggleHopperPlace(Location blockLocation, boolean hopperValue) throws IOException {
        Location above = blockLocation.clone().add(0, 1, 0);
        EnderChestLocation chest = new EnderChestLocation(main, above);

        chest.updateHopperValue(hopperValue);
    }

    public static boolean isEnderChestAbove(Location location) {
        Location above = location.clone().add(0, 1, 0);
        Block blockAbove = above.getBlock();

        return blockAbove.getType() == Material.ENDER_CHEST;
    }

    // CHEST PLACE FUNCTIONS
    // -----------------------------------------------------------------------------------------------------------------

    private void handleEnderChestPlace(BlockPlaceEvent e, Location blockLocation) {
        try {
            EnderChestLocation chest = new EnderChestLocation(main, blockLocation);
            chest.saveLocation();
        } catch (IOException ex) {
            main.logger.severe("Unable to save block location");
            main.logger.severe(ex.getMessage());

            e.getPlayer().sendMessage(ChatColor.RED + "Unable to place log location of Ender Chest, attached hopper may not work as expected. Error logged.");
            e.setCancelled(true);
        }
    }

    private void handleHopperPlace(BlockPlaceEvent e, Location blockLocation) {
        if (!isEnderChestAbove(blockLocation)) return;

        try {
            toggleHopperPlace(blockLocation, true);
            // begin any transfers for any chests user has
            transferManager.handleTransfer(e.getPlayer());
        } catch(IOException ex) {
            main.logger.severe("Unable to update hopper status for ender chest");
            main.logger.severe(ex.getMessage());

            e.getPlayer().sendMessage(ChatColor.RED + "Unable to find log location of Ender Chest, attached hopper may not work as expected. Error logged.");
            e.setCancelled(true);
        }
    }

    // CHEST BREAK FUNCTIONS
    // -----------------------------------------------------------------------------------------------------------------

    private void handleEnderChestBreak(Location blockLocation) {
        EnderChestLocation chest = new EnderChestLocation(main, blockLocation);

        try {
            chest.removeLocation();
        } catch (IOException ex) {
            main.logger.severe("Unable to remove Ender Chest from log file. Already removed?");
            main.logger.severe(ex.getMessage());
        }
    }

    private void handleHopperBreak(Location blockLocation) {
        if (!isEnderChestAbove(blockLocation)) return;

        try {
            toggleHopperPlace(blockLocation, false);
        } catch(IOException ex) {
            main.logger.severe("Unable to update hopper status for ender chest");
            main.logger.severe(ex.getMessage());
        }
    }
}
