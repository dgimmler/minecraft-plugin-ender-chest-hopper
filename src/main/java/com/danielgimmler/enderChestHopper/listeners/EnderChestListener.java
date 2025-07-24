package com.danielgimmler.enderChestHopper.listeners;

import com.danielgimmler.enderChestHopper.EnderChestHopper;
import com.danielgimmler.enderChestHopper.db.enderChestLocation.EnderChestLocation;
import com.danielgimmler.enderChestHopper.db.playerConfig.PlayerConfig;
import com.danielgimmler.enderChestHopper.instance.gui.EnderChestGui;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;

import java.io.IOException;
import java.util.*;

public class EnderChestListener implements Listener {
    protected EnderChestHopper main;

    public EnderChestListener(EnderChestHopper main) {
        this.main = main;
    }

    @EventHandler
    public void onHopperTransfer(InventoryMoveItemEvent e) {
        if (e.getInitiator().getType() != InventoryType.HOPPER)
            return;

        for (Player player : Bukkit.getOnlinePlayers())
            main.getTransferManager().handleTransfer(player);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();

        // create and store player Config
        if (!main.getPlayerConfigManager().playerConfigIsSet(player))
            main.getPlayerConfigManager().createPlayerConfig(player);
        else {
            main.getPlayerConfigManager().getPlayerConfig(player).setPlayer(player);
            main.getPlayerConfigManager().loadPlayerConfig(player);
        }

        // create and store gui for player
        if (!main.getGuiManager().playerGuiIsSet(player))
            main.getGuiManager().createPlayerGui(player);

        // begin any transfers for any chests user has
        main.getTransferManager().handleTransfer(player);
    }

    @EventHandler
    public void onHopperInteract(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (!player.isOnline()) return;

        Inventory clicked = e.getInventory();
        if (!clicked.equals(player.getEnderChest())) return;

        main.getTransferManager().handleTransfer(player);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        Location blockLocation = e.getBlock().getLocation();

        if (e.getBlockPlaced().getType() == Material.ENDER_CHEST) {
            if (!handleEnderChestPlace(e.getPlayer(), blockLocation))
                e.setCancelled(true);
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

    @EventHandler
    public void onEnderChestRightClick(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        UUID playerId = player.getUniqueId();

        // only handle this event if a player left-clicked an ender chest
        if (!player.isSneaking()) return;
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (e.getClickedBlock() == null || e.getClickedBlock().getType() != Material.ENDER_CHEST) return;
        if (e.getItem() != null) return;

        e.setCancelled(true);

        Location chestLocation = e.getClickedBlock().getLocation();

        // save chest location if it hasn't been logged yet
        if (!main.getEnderChestLocationManager().enderChestIsSaved(chestLocation)) {
            if (!handleEnderChestPlace(e.getPlayer(), chestLocation))
                e.setCancelled(true);
        }

        // add chest to player config if not there already
        PlayerConfig playerConfig = main.getPlayerConfigManager().getPlayerConfig(player);
        EnderChestLocation chest = new EnderChestLocation(main,chestLocation);
        if (!playerConfig.isEnderChestConfigSet(chest))
            playerConfig.setEnderChestConfig(chest);

        // open gui for player
        EnderChestGui playerGui =  main.getGuiManager().getPlayerGui(playerId);

        playerGui.setLastClickedEnderChest(e.getClickedBlock().getLocation());
        playerGui.openGui();
    }

    @EventHandler
    public void onGuiInteract(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (!player.isOnline()) return;
        if (e.getInventory() != main.getGuiManager().getPlayerGui(player).playerGui) return;
        if (e.getCurrentItem() == null)  return;

        e.setCancelled(true);
        main.getGuiManager().getPlayerGui(player).handleItemClick(e.getRawSlot());
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

    private boolean handleEnderChestPlace(Player player, Location blockLocation) {
        try {
            EnderChestLocation chest = new EnderChestLocation(main, blockLocation);
            chest.saveLocation();

            return true;
        } catch (IOException ex) {
            main.logger.severe("Unable to save block location");
            main.logger.severe(ex.getMessage());

            player.sendMessage(ChatColor.RED + "Unable to place log location of Ender Chest, attached hopper may not work as expected. Error logged.");

            return false;
        }
    }

    private void handleHopperPlace(BlockPlaceEvent e, Location blockLocation) {
        if (!isEnderChestAbove(blockLocation)) return;

        try {
            toggleHopperPlace(blockLocation, true);
            // begin any transfers for any chests user has
            main.getTransferManager().handleTransfer(e.getPlayer());
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
            main.getEnderChestLocationManager().removeEnderChestLocation(chest);
        } catch (IOException ex) {
            main.logger.severe("Unable to remove Ender Chest from log file. Already removed?");
            main.logger.severe(ex.getMessage());
        }

        main.getPlayerConfigManager().removeChestFromPlayerConfigs(chest);
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
