package com.danielgimmler.enderChestHopper.instance.transferManager;

import com.danielgimmler.enderChestHopper.EnderChestHopper;
import com.danielgimmler.enderChestHopper.db.enderChestLocation.EnderChestLocation;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.IOException;
import java.util.*;

public class TransferManager {
    public EnderChestHopper main;

    private final Map<UUID, BukkitTask> debouncedTransfers;
    private final Set<UUID> currentlyProcessing;
    private final long DEBOUNCE_DELAY_TICKS;
    private final long TRANSFER_INTERVAL_TICKS;

    public TransferManager(EnderChestHopper main) {
        this.main = main;

        this.debouncedTransfers = new HashMap<>();
        this.currentlyProcessing = new HashSet<>();
        this.DEBOUNCE_DELAY_TICKS = 10L; // 0.5 seconds
        this.TRANSFER_INTERVAL_TICKS = 8L; // mimic hopper tick rate
    }

    // SCHEDULE FUNCTIONS
    // -----------------------------------------------------------------------------------------------------------------

    public void handleTransfer(Player player, String calledFrom) {
        main.logger.debug("handleTransfer called from: " + calledFrom);
        UUID playerId = player.getUniqueId();

        // Avoid collisions by not running additional tasks
        if (debouncedTransfers.containsKey(playerId))
            return;

        // Schedule a new task with debounce
        BukkitTask task = main.getServer().getScheduler().runTaskLater(main, () -> {
            // ensure new tasks can be added once this one starts
            if (debouncedTransfers.get(playerId) != null) debouncedTransfers.get(playerId).cancel();
            debouncedTransfers.remove(playerId);

            // Prevent overlapping transfers and ignore empty chests
            if (currentlyProcessing.contains(playerId) || !playerHasEnderChest(player)) return;

            currentlyProcessing.add(playerId);
            try {
                scheduleTransferWithDelay(player);
            } catch (IOException ex) {
                main.logger.severe("Failed to initiate hopper transfer: " + ex.getMessage());
                currentlyProcessing.remove(playerId);
            }
        }, DEBOUNCE_DELAY_TICKS);

        if (playerHasEnderChest(player))
            debouncedTransfers.put(playerId, task);
    }

    private void scheduleTransferWithDelay(Player player) throws IOException {
        List<EnderChestLocation> chests = getEnderChestsWithHopper();
        Inventory enderChestInv = player.getEnderChest();

        // create a task for each ender chest
        List<TransferTask> transferTasks = new ArrayList<>();
        for (EnderChestLocation chest : chests)
            transferTasks.add(new TransferTask(main, chest, enderChestInv));


        UUID playerId = player.getUniqueId();
        String playerName = player.getName();

        // The runnable will run once every interval until cancelled.
        // consider it a while loop with a sleep between each loop
        new BukkitRunnable() {
            int taskIndex = 0;
            int chestSlot = 0;

            @Override
            public void run() {
                TransferTask task = null;
                try {
                    task = transferTasks.get(taskIndex);
                } catch(IndexOutOfBoundsException ex) {
                    cancelTask(this, task, playerId, "Error running task. Error: " + ex.getMessage());

                    return;
                }
                if (task == null) return;

                // cancel runnable player is offline, has hopper transfers toggled off or task list is empty (processed)
                if (!player.isOnline() || !currentlyProcessing.contains(playerId)) {
                    cancelTask(this, task, playerId, "Player " + playerName + " is offline. Cancelling ender chest hopper.");

                    return;
                } else if (!main.getPlayerConfigManager().getPlayerConfig(player).isHopperEnabledForChest(task.getChest())) {
                    cancelTask(this, task, playerId, "Player " + playerName + " toggled hopper transfers off. Cancelling ender chest hopper.");

                    return;
                } else if (transferTasks.isEmpty()) {
                    cancelTask(this, task, playerId, "Transfer tasks for player " + playerName + " are complete. Cancelling ender chest hopper.");

                    return;
                }

                task.reloadHopperChunks();
                task.reloadEnderChestInventory(player);

                // cancel runnable if hopper is removed
                try {
                    task.getChest().getHopper();
                } catch(IllegalStateException ex) {
                    cancelTask(this, task, playerId, "Ender chest for player " + playerName + " has been removed. Cancelling ender chest hopper.");

                    return;
                }

                // cancel runnable if chest inv is depleted.
                if (task.chestIsEmpty()){
                    cancelTask(this, task, playerId, "Ender chest for player " + playerName + " is empty. Cancelling ender chest hopper.");

                    return;
                }

                boolean transferred = task.run(chestSlot);

                if (!transferred) {
                    main.logger.debug("hopper is full, trying next one if available...");
                    // try next hopper if this hopper is full
                    taskIndex++;

                    if (taskIndex >= transferTasks.size())
                        cancelTask(this, task, playerId, "No further ender chest with hopper available. Cancelling ender chest hopper.");
                } else if (chestSlot >= enderChestInv.getMaxStackSize() || chestSlot < 0) {
                    main.logger.debug("Looped through all chest slots, going back to first slot...");
                    // loop back to first filled slot if all item stacks finish
                    chestSlot=0;
                } else if (task.chestSlotIsEmpty(chestSlot)) {
                    // go to next chest slot if item stack depleted
                    chestSlot=task.getFirstFilledSlot(player);
                    main.logger.debug("Chest slot is empty, trying next slot: " + chestSlot);
                }
            }
        }.runTaskTimer(main, 0L, TRANSFER_INTERVAL_TICKS); // sleep by set ticks before running again
    }

    // HELPER FUNCTIONS
    // -----------------------------------------------------------------------------------------------------------------

    public void cancelTask(BukkitRunnable runnable, TransferTask task, UUID playerId, String msg) {
        main.logger.trace(msg);

        // cancel runnable
        runnable.cancel();
        currentlyProcessing.remove(playerId);

        // cancel processing transfer
        if (debouncedTransfers.get(playerId) != null) debouncedTransfers.get(playerId).cancel();
        debouncedTransfers.remove(playerId);

        // cancel force-loaded chunks
        if (task != null) task.unloadHopperChunks();

        main.logger.trace("active transfers after remove: " + debouncedTransfers);
    }

    public boolean playerHasEnderChest(Player player) {
        Inventory chestInv = player.getEnderChest();

        if (chestInv == null)
            return false;

        for (ItemStack i : chestInv.getContents()) {
            if (i != null && i.getType() != Material.AIR)
                return true;
        }

        return false;
    }

    public List<EnderChestLocation> getEnderChestsWithHopper() throws IOException {
        List<EnderChestLocation> list = new ArrayList<>();
        for (EnderChestLocation chest : main.getEnderChestLocationManager().getEnderChests()) {
            if (chest.isHopperBelow())
                list.add(chest);
        }

        return list;
    }
}

