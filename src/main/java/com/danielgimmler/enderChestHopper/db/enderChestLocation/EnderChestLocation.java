package com.danielgimmler.enderChestHopper.db.enderChestLocation;

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
    public EnderChestHopper main;

    private final String world;
    private final int x;
    private final int y;
    private final int z;
    private final boolean hasHopper;
    private final Location location;

    // CONSTRUCTORS
    // -----------------------------------------------------------------------------------------------------------------

    public EnderChestLocation(EnderChestHopper main, String world, int x, int y, int z, boolean hasHopper) {
        this.main = main;

        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.hasHopper = hasHopper;
        this.location = new Location(Bukkit.getWorld(world), x, y, z);

        main.logger.debug("Saved ender chest at location: " + x + " " + y + " " + z + " in world " + world + ". Key: " + world + x + y + z);
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

    // GETTERS
    // -----------------------------------------------------------------------------------------------------------------
    public String getKey() { return getKey(world, x, y, z); }
    public static String getKey(String world, int x, int y, int z) { return world + x + y + z; }

    public boolean isHopperBelow() { return this.hasHopper; }
    public static boolean isHopperBelow(Location location) {
        Location below = location.clone().subtract(0, 1, 0);
        Block blockBelow = below.getBlock();

        return blockBelow.getType() == Material.HOPPER;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getZ() { return z; }
    public Location getLocation() { return location; }

    // HELPERS
    // -----------------------------------------------------------------------------------------------------------------

    public boolean chestSaved() { return chestSaved(main.getEnderChestLocationManager(), getKey()); }
    public static boolean chestSaved(EnderChestLocationManager mgr, String k) { return mgr.getYamlFile().contains(k); }

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
        YamlConfiguration file = main.getEnderChestLocationManager().getYamlFile();

        String k = getKey(world, x, y, z);
        if (!chestSaved(main.getEnderChestLocationManager(), k)) return null;

        int storedX = file.getInt(k + ".x");
        int storedY = file.getInt(k + ".y");
        int storedZ = file.getInt(k + ".z");
        boolean hasHopper = file.getBoolean(k + ".hopper", false);

        return new EnderChestLocation(main, world, storedX, storedY, storedZ, hasHopper);
    }

    public static EnderChestLocation getEnderChestLocation(EnderChestHopper main, String k) throws IOException {
        String errorMsg = "Yaml file not found!";
        if (!main.getEnderChestLocationManager().fileExists()) throw new IllegalStateException(errorMsg);

        YamlConfiguration file = main.getEnderChestLocationManager().getYamlFile();
        if (!chestSaved(main.getEnderChestLocationManager(), k)) throw new IllegalStateException("Ender chest not found at key: " + k);

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
        EnderChestLocationManager mgr = main.getEnderChestLocationManager();
        if (!mgr.fileExists()) return;

        // log location in config file
        YamlConfiguration file = mgr.getYamlFile();
        file.set("" + world + x + y + z, Map.ofEntries(
                Map.entry("x", x),
                Map.entry("y", y),
                Map.entry("z", z),
                Map.entry("world", world),
                Map.entry("hopper", hasHopper)
        ));

        file.save(mgr.getFile());
        main.logger.debug("Saved location of Ender Chest to file");
    }

    public void removeLocation() throws IOException {
        EnderChestLocationManager mgr = main.getEnderChestLocationManager();
        if (!mgr.fileExists()) return;

        YamlConfiguration file = mgr.getYamlFile();
        String k = getKey();

        if (file.contains(k)) {
            file.set(k, null);
            file.save(mgr.getFile());
        } else {
            throw new IllegalStateException("Ender chest not found: " + x + " " + y + " " + z + " in world " + world + ". Key: " + k);
        }
    }

    /*
        We only ever need to update the hopper value. All other values affect the coordinates, meaning the ender chest
        will be destroyed and replaced, and the oonBlockPlace and onBlockRemove events will handle it
     */
    public void updateHopperValue(boolean newHopperValue) throws IOException {
        EnderChestLocationManager mgr = main.getEnderChestLocationManager();
        if (!mgr.fileExists()) return;

        YamlConfiguration file = mgr.getYamlFile();
        String k = getKey();

        if (file.contains(k)) {
            file.set(k + ".hopper", newHopperValue);
            file.save(mgr.getFile());
        } else {
            throw new IllegalStateException("Ender chest not found: " + x + " " + y + " " + z + " in world " + world + ". Key: " + k);
        }

        if (newHopperValue)
            main.getChunkManager().addHopperChunk(location.getChunk());
        else
            main.getChunkManager().removeHopperChunk(location.getChunk());
    }
}
