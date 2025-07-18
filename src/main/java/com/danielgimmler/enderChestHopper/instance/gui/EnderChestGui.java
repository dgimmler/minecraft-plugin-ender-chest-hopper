package com.danielgimmler.enderChestHopper.instance.gui;

import com.danielgimmler.enderChestHopper.EnderChestHopper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class EnderChestGui {

    private EnderChestHopper main;
    private Player player;
    public Inventory playerGui;

    public EnderChestGui(EnderChestHopper main, Player player) {
        this.main = main;

        this.player = player;
        this.playerGui = Bukkit.createInventory(player, InventoryType.ENDER_CHEST, Component.text("Ender Chest Settings")
            .color(NamedTextColor.GRAY));

        setMenuItem(playerGui, EnderChestGuiMenuItem.CLOSE);
        setMenuItem(playerGui, EnderChestGuiMenuItem.TOGGLE_HOPPER);
        setMenuItem(playerGui, EnderChestGuiMenuItem.TOGGLE_CHEST);
        setMenuItem(playerGui, EnderChestGuiMenuItem.RENAME_CHEST);
        setMenuItem(playerGui, EnderChestGuiMenuItem.FORGET_CHEST);
        setMenuItem(playerGui, EnderChestGuiMenuItem.LIST_CHESTS);

        // FRAME
        ItemStack frame = new ItemStack(EnderChestGuiMenuItem.BORDER.getMaterial());
        for (int i: new int[]{1,2,3,4,5,6,7,8,9,17,18,19,20,21,22,23,24,25,26})
            playerGui.setItem(i, frame);
    }

    private void setMenuItem(Inventory gui, EnderChestGuiMenuItem i) {
        Map.Entry<Integer, ItemStack> item = getMenuItem(i);
        gui.setItem(item.getKey(), item.getValue());
    }

    private Map.Entry<Integer, ItemStack> getMenuItem(EnderChestGuiMenuItem i) {
        ItemStack item = new ItemStack(i.getMaterial());

        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.displayName(i.getTitle());
        itemMeta.lore(i.getLore());
        item.setItemMeta(itemMeta);

        return new AbstractMap.SimpleEntry<>(i.getSlot(), item);
    }

    public void openGui() {
        player.openInventory(playerGui);
    }

    public void handleItemClick(int slot) {
        EnderChestGuiMenuItem item = EnderChestGuiMenuItem.fromSlot(slot);
        if (item == null) return;

        switch (item) {
            case CLOSE:
                // handle close
                break;

            case TOGGLE_HOPPER:
                main.getPlayerConfig(player).toggleHopperTransfers();

                break;

            case TOGGLE_CHEST:
                player.sendMessage(
                    Component.text("Chest toggle not implemented")
                    .color(NamedTextColor.RED));

                break;

            case RENAME_CHEST:
                player.sendMessage(
                    Component.text("Rename chest not implemented")
                    .color(NamedTextColor.RED));

                break;

            case FORGET_CHEST:
                player.sendMessage(
                    Component.text("Forget chest not implemented")
                    .color(NamedTextColor.RED));

                break;

            case LIST_CHESTS:
                player.sendMessage(
                    Component.text("List chests not implemented")
                    .color(NamedTextColor.RED));

                break;

            default:
                return;
        }

        player.closeInventory();
    }
}
