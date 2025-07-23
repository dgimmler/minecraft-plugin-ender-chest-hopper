package com.danielgimmler.enderChestHopper.instance.gui;

import com.danielgimmler.enderChestHopper.EnderChestHopper;
import com.danielgimmler.enderChestHopper.db.enderChestLocation.EnderChestLocation;
import com.danielgimmler.enderChestHopper.db.playerConfig.EnderChestConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.function.Consumer;

public class EnderChestGui {

    private EnderChestHopper main;
    private Player player;
    private EnderChestLocation lastClickedEnderChest;
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

    public void setLastClickedEnderChest(Location location) { setLastClickedEnderChest(new EnderChestLocation(main, location)); }
    public void setLastClickedEnderChest(EnderChestLocation chest) { this.lastClickedEnderChest = chest; }
    private void unsetLastClickedEnderChestLocation() { this.lastClickedEnderChest = null; }

    private void setMenuItem(Inventory gui, EnderChestGuiMenuItem i) {
        Map.Entry<Integer, ItemStack> item = getMenuItem(i);
        gui.setItem(item.getKey(), item.getValue());
    }

    private Map.Entry<Integer, ItemStack> getMenuItem(EnderChestGuiMenuItem i) {
        return new AbstractMap.SimpleEntry<>(i.getSlot(), getMenuItemstack(i));
    }

    private ItemStack getMenuItemstack(EnderChestGuiMenuItem i) {
        ItemStack item = new ItemStack(i.getMaterial());

        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.displayName(i.getTitle());
        itemMeta.lore(i.getLore());
        item.setItemMeta(itemMeta);

        return item;
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
                main.getPlayerConfigManager().getPlayerConfig(player).toggleHopperTransfers();

                break;

            case TOGGLE_CHEST:
                player.sendMessage(
                    Component.text("Chest toggle not implemented")
                    .color(NamedTextColor.RED));

                break;

            case RENAME_CHEST:
                if (lastClickedEnderChest != null)
                    getAnvilGuiTextInput(player, chestName -> {
                        main.getPlayerConfigManager().getPlayerConfig(player).renameEnderChest(lastClickedEnderChest.getKey(), chestName, player.getUniqueId());
                        unsetLastClickedEnderChestLocation();
                    });
                else {
                    String errorMsg = "Unable to establish Ender Chest Location";
                    player.sendMessage(
                        Component.text(errorMsg + ". Error has been logged.")
                        .color(NamedTextColor.RED));
                    main.logger.severe("Error for player " + player.getName() + ": " + errorMsg);
                }

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

    public void getAnvilGuiTextInput(Player player, Consumer<String> callback) {
        Bukkit.getScheduler().runTask(main, () -> {
            new AnvilGUI.Builder()
                .plugin(main)
                .title("Ender Chest Name")
                .text("Enter name here...")
                .itemLeft(getMenuItemstack(EnderChestGuiMenuItem.CLOSE_ANVIL))
                .itemOutput(getMenuItemstack(EnderChestGuiMenuItem.CONFIRM))
                .onClick((slot, snapshot) -> {
                    if (slot != AnvilGUI.Slot.OUTPUT)
                        return List.of(AnvilGUI.ResponseAction.close());

                    String input = snapshot.getText();
                    callback.accept(input); // return result to caller
                    return List.of(AnvilGUI.ResponseAction.close());
                })
                .open(player);
        });
    }

}
