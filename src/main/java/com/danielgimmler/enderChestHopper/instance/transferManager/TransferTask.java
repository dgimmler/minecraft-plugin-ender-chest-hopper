package com.danielgimmler.enderChestHopper.instance.transferManager;

import com.danielgimmler.enderChestHopper.EnderChestHopper;
import com.danielgimmler.enderChestHopper.db.EnderChestLocation;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.HashMap;

public class TransferTask {
    public final EnderChestHopper main;

    public EnderChestLocation chest;
    public Inventory chestInv;
    public Inventory hopperInv;

    public TransferTask(EnderChestHopper main, EnderChestLocation chest, Inventory chestInv) throws IOException {
        this.main = main;

        this.chest = chest;
        this.chestInv = chestInv;
        this.hopperInv = chest.getHopper().getInventory();
    }

    public void reloadEnderChestInventory(Player player) {
        this.chestInv = player.getEnderChest();
    }

    public int getFirstFilledSlot(Player player) {
        reloadEnderChestInventory(player);

        if (chestIsEmpty())
            return 0;

        for (int slot = 0; slot < chestInv.getSize(); slot++) {
            ItemStack item = chestInv.getItem(slot);

            if (item != null && item.getType() != Material.AIR)
                return slot;
        }

        return 0; // return 0 if all slots are empty, we'll check separately if chest is empty
    }

    public boolean run(int slot) {
        // get items in slot
        ItemStack chestStack = chestInv.getItem(slot);
        if (chestStack == null || chestStack.getType() == Material.AIR) return true;

        // save current item count
        int originalAmount = chestStack.getAmount();
        int remaining = 1; // used to determine how many items to move at once

        // create a new itemstack for the item with a size of 1
        ItemStack oneItemStack = chestStack.clone();
        oneItemStack.setAmount(1);

        // Try to merge this 1 item into existing stacks
        int afterMerge = mergeWithExistingStacks(oneItemStack, hopperInv, remaining);

        // If still not placed, try empty slot
        if (afterMerge > 0)
            afterMerge = insertIntoEmptySlots(oneItemStack, hopperInv, afterMerge);

        // If at least 1 was moved, subtract from source
        if (afterMerge < 1) {
            chestStack.setAmount(originalAmount - 1);
            if (chestStack.getAmount() <= 0) chestInv.clear(slot);
            else chestInv.setItem(slot, chestStack);

            // Play transfer sound
            Location soundLocation = hopperInv.getLocation().add(0.5, 0.5, 0.5);
            hopperInv.getLocation().getWorld().playSound(soundLocation, Sound.BLOCK_DISPENSER_DISPENSE, 0.5F, 1.0F);

            return true;
        }

        // return false to indicate no items were transferred
        return false;
    }

    private int mergeWithExistingStacks(ItemStack chestStack, Inventory hopperInv, int remaining) {
        for (int slot = 0; slot < hopperInv.getSize(); slot++) {
            ItemStack hopperStack = hopperInv.getItem(slot);
            if (hopperStack == null || hopperStack.getType() != chestStack.getType() || !hopperStack.isSimilar(chestStack))
                continue;

            int space = hopperStack.getMaxStackSize() - hopperStack.getAmount();
            if (space > 0) {
                int toTransfer = Math.min(space, remaining);
                hopperStack.setAmount(hopperStack.getAmount() + toTransfer);
                remaining -= toTransfer;

                if (remaining <= 0)
                    return remaining;
            }
        }

        return remaining;
    }

    private int insertIntoEmptySlots(ItemStack fromStack, Inventory hopperInv, int remaining) {
        if (remaining <= 0) return 0;

        ItemStack remainingStack = fromStack.clone();
        remainingStack.setAmount(remaining);
        HashMap<Integer, ItemStack> leftovers = hopperInv.addItem(remainingStack);

        if (leftovers.containsKey(0))
            return leftovers.get(0).getAmount();
        else
            return 0;
    }

    public boolean chestIsEmpty() {
        if (chestInv == null)
            return true;

        for (ItemStack i : chestInv.getContents()) {
            if (i != null && i.getType() != Material.AIR)
                return false;
        }

        return true;
    }

    public boolean chestSlotIsEmpty(int slot) {
        if (chestIsEmpty())
            return true;

        if (slot > chestInv.getMaxStackSize())
            return true;

        ItemStack slotItem = chestInv.getItem(slot);
        if (slotItem == null)
            return true;

        return slotItem.getAmount() < 1;
    }
}
