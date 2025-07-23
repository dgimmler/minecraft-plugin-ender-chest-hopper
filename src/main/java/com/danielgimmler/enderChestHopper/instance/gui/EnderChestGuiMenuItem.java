package com.danielgimmler.enderChestHopper.instance.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum EnderChestGuiMenuItem {
    /*
        [0,  1,  2,  3,  4,  5,  6,  7,  8
         9,  10, 11, 12, 13, 14, 15, 16, 17,
         18, 19, 20, 21, 22, 23, 24, 25, 26]

         [X, -,  -,  -,  -,  -,  -,  -,  -,
          -, 0,  H,  C,  R,  F,  L,  0,  -,
          -, -,  -,  -,  -,  -,  -,  -,  -]
     */
    // NOTE: a slot of -1 means that this item appears in the GUI multiple times
    BORDER(-1, Material.LIME_STAINED_GLASS_PANE, NamedTextColor.GRAY, "", new ArrayList<>()),  // - - border
    EMPTY_SLOT(-1, Material.AIR, NamedTextColor.GRAY, "", new ArrayList<>()),                  // 0 - empty slot
    CLOSE(0, Material.BARRIER, NamedTextColor.RED, "Close", new ArrayList<>()),                // X - closes GUI
    CLOSE_ANVIL(0, Material.BARRIER,
        NamedTextColor.RED,
        "Close",
        Arrays.asList("Cancel and close")),                                                              // XA - Anvil GUI: closes GUI
    CONFIRM(0, Material.LIME_CONCRETE,
        NamedTextColor.GREEN,
        "Confirm",
        Arrays.asList("Confirm name and close")),                                                        // CA - Anvil GUI: Confirms rename
    TOGGLE_HOPPER(11, Material.HOPPER,
        NamedTextColor.BLUE,
    "Toggle Hopper Transfers",
        Arrays.asList(
            "Toggle hopper transfers on or off",
            "OFF - No Ender Chest will transfer inventory into any hopper (default)",
            "ON  - Named Ender chests will transfer to hoppers if chest toggle is also ON")),            // H - turn all hopper transfers on or off
    TOGGLE_CHEST(12, Material.ENDER_CHEST,
        NamedTextColor.BLUE,
    "Toggle This Chest",
        Arrays.asList(
            "Toggle hopper transfers on or off on this chest only",
            "OFF - No Ender Chest will transfer inventory into any hopper (default)",
            "ON  - Ender Chest will transfer to hoppers general toggle is also ON")),                    // C - turn hopper transfers on specific chest on or off
    RENAME_CHEST(13, Material.BIRCH_SIGN,
        NamedTextColor.WHITE,
    "Rename this Chest",
        Arrays.asList("Give this chest a unique, human-readable identifier")),                           // R - rename specific chest
    FORGET_CHEST(14, Material.TNT,
        NamedTextColor.RED,
    "Forget this chest",
        Arrays.asList(
            "Remove this chest from your list of known chests",
            "No hopper transfers will occur on this chest",
            "It will be as if you never found it")),                                                     // F - remove chest from player configuration and memory
    LIST_CHESTS(15, Material.BOOK,
        NamedTextColor.GRAY,
    "List known chests",
        Arrays.asList("Display all named chested and their configurations known to you"));               // L - list all chests players have configured

    private final int slot;
    private final Material material;
    private Component title;
    private List<Component> lore;

    EnderChestGuiMenuItem(int slot, Material material, NamedTextColor color, String title, List<String> lore) {
        this.slot = slot;
        this.material = material;
        this.title = Component.text(title).color(color);

        List<Component> loreComponents = new ArrayList<>();
        for (String line : lore)
            loreComponents.add(Component.text(line).color(NamedTextColor.GRAY));

        this.lore = loreComponents;
    }

    public int getSlot() { return slot; }
    public Material getMaterial() { return material; }
    public Component getTitle() { return title; }
    public List<Component> getLore() { return lore; }

    public static EnderChestGuiMenuItem fromSlot(int slot) {
        for (EnderChestGuiMenuItem s : values())
            if (s.getSlot() == slot) return s;

        return null;
    }

    public static EnderChestGuiMenuItem fromMaterial(Material material) {
        for (EnderChestGuiMenuItem s : values())
            if (s.getMaterial() == material) return s;

        return null;
    }

    public static EnderChestGuiMenuItem fromTitle(String title) {
        for (EnderChestGuiMenuItem s : values())
            if (s.getTitle().toString() == title) return s;

        return null;
    }

    public static EnderChestGuiMenuItem fromTitle(Component title) {
        for (EnderChestGuiMenuItem s : values())
            if (s.getTitle() == title) return s;

        return null;
    }
}

