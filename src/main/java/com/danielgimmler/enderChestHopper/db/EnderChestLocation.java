package com.danielgimmler.enderChestHopper.db;

import com.danielgimmler.enderChestHopper.EnderChestHopper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Hopper;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.IOException;
import java.util.Map;

public class EnderChestLocation {
    EnderChestHopper main;

    public final String world;
    public final int x;
    public final int y;
    public final int z;
    public final boolean hasHopper;

    // CONSTRUCTORS
    // -----------------------------------------------------------------------------------------------------------------

    public EnderChestLocation(EnderChestHopper main, String world, int x, int y, int z, boolean hasHopper) {
        this.main = main;

        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.hasHopper = hasHopper;

        main.logger.info("Saved ender chest at location: " + x + " " + y + " " + z + " in world " + world + ". Key: " + world + x + y + z);
    }

    public EnderChestLocation(EnderChestHopper main, Location blockLocation) {
        this(
            main,
            blockLocation.getWorld().getName(),
            blockLocation.getBlockX(),
            blockLocation.getBlockY(),
            blockLocation.getBlockZ(),
            isHopperBelow(blockLocation)
        );
    }

    // HELPERS
    // -----------------------------------------------------------------------------------------------------------------

    public static boolean isHopperBelow(Location location) {
        Location below = location.clone().subtract(0, 1, 0);
        Block blockBelow = below.getBlock();

        return blockBelow.getType() == Material.HOPPER;
    }

    public Hopper getHopper() throws IllegalStateException {
        if (!hasHopper) throw new IllegalStateException("Ender chest has no hopper attached");

        World world = Bukkit.getWorld(this.world);
        Location location = new Location(world, x, y, z);
        Location hopperLocation = location.clone().subtract(0, 1, 0);

        Block block = hopperLocation.getBlock();
        if (block.getType() != Material.HOPPER)
            throw new IllegalStateException("Expected hopper below ender chest at " + hopperLocation);

        BlockState state = block.getState();
        if (!(state instanceof Hopper))
            throw new IllegalStateException("Block state is not a Hopper at " + hopperLocation);

        return (Hopper) state;
    }

    public static EnderChestLocation getEnderChestLocation(EnderChestHopper main, String world, int x, int y, int z) {
        YamlConfiguration file = YamlConfiguration.loadConfiguration(main.file);

        String k = world + x + y + z;
        if (!file.contains(k)) return null;

        int storedX = file.getInt(k + ".x");
        int storedY = file.getInt(k + ".y");
        int storedZ = file.getInt(k + ".z");
        boolean hasHopper = file.getBoolean(k + ".hopper", false);

        return new EnderChestLocation(main, world, storedX, storedY, storedZ, hasHopper);
    }

    public static EnderChestLocation getEnderChestLocation(EnderChestHopper main, String k) throws IOException {
        String errorMsg = "Yaml file not found!";
        if (main.file == null) throw new IllegalStateException(errorMsg);
        if (!main.file.exists()) throw new IllegalStateException(errorMsg);

        YamlConfiguration file = YamlConfiguration.loadConfiguration(main.file);
        if (!file.contains(k)) throw new IllegalStateException("Ender chest not found at key: " + k);

        String world = file.getString(k + ".world");
        int x = file.getInt(k + ".x");
        int y = file.getInt(k + ".y");
        int z = file.getInt(k + ".z");
        boolean hasHopper = file.getBoolean(k + ".hopper", false);

        return new EnderChestLocation(main, world, x, y, z, hasHopper);
    }

    // FILE MANAGEMENT
    // -----------------------------------------------------------------------------------------------------------------

    public void saveLocation() throws IOException {
        if (main.file == null) return;
        if (!main.file.exists()) return;

        // log location in config file
        YamlConfiguration file = YamlConfiguration.loadConfiguration(main.file);

        file.set("" + world + x + y + z, Map.ofEntries(
                Map.entry("x", x),
                Map.entry("y", y),
                Map.entry("z", z),
                Map.entry("world", world),
                Map.entry("hopper", hasHopper)
        ));

        try {
            file.save(main.file);
            main.logger.info("Saved location of Ender Chest to file");
        } catch (IOException ex) {
            throw ex;
        }
    }

    public void removeLocation() throws IOException {
        if (main.file == null) return;
        if (!main.file.exists()) return;

        YamlConfiguration file = YamlConfiguration.loadConfiguration(main.file);

        String k = world + x + y + z;

        if (file.contains(k)) {
            file.set(k, null);
            file.save(main.file);
        } else {
            throw new IllegalStateException("Ender chest not found: " + x + " " + y + " " + z + " in world " + world + ". Key: " + k);
        }
    }

    /*
        We only ever need to update the hopper value. All other values affect the coordinates, meaning the ender chest
        will be destroyed and replaced, and the oonBlockPlace and onBlockRemove events will handle it
     */
    public void updateHopperValue(boolean newHopperValue) throws IOException {
        if (main.file == null) return;
        if (!main.file.exists()) return;

        YamlConfiguration file = YamlConfiguration.loadConfiguration(main.file);

        String k = world + x + y + z; // Must exactly match the original save format

        if (file.contains(k)) {
            file.set(k + ".hopper", newHopperValue);
            file.save(main.file);
        } else {
            throw new IllegalStateException("Ender chest not found: " + x + " " + y + " " + z + " in world " + world + ". Key: " + k);
        }
    }
}
