package com.danielgimmler.enderChestHopper.instance.ChunkLoader;

import com.danielgimmler.enderChestHopper.EnderChestHopper;
import com.danielgimmler.enderChestHopper.db.enderChestLocation.EnderChestLocation;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Hopper;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ChunkManager {

    private EnderChestHopper main;
    private Map<String, Chunk> hopperChunks;
    private boolean enabled;
    private int defaultChunkDepth;

    public ChunkManager(EnderChestHopper main) {
        this.main = main;
        this.hopperChunks = new HashMap<>();
        this.enabled = true;
        this.defaultChunkDepth = getChunkDepth();

        try {
            getHopperChunksFromFile();
            loadHopperChunks();
        } catch (IOException e) {
            main.logger.severe("Error loading hopper chunks. Error: " + e.getMessage());
        }
    }

    private int getChunkDepth() {
        if (main.isPaper()) return Bukkit.getSimulationDistance();

        return Bukkit.getViewDistance();
    }

    private int getChunkDepth(World world) {
        if (main.isPaper()) return world.getSimulationDistance();

        return world.getViewDistance();
    }


    // PUBLIC
    // -----------------------------------------------------------------------------------------------------------------

    private void getHopperChunksFromFile() throws IOException {
        for (Hopper h : main.getEnderChestLocationManager().getEnderChestHoppers()) {
            for (Chunk c : getAdjacentChunks(h.getChunk()))
                addHopperChunk(c);
        }
    }

    public void loadHopperChunks() {
        for (String k : hopperChunks.keySet())
            loadChunk(k);
    }

    public void loadChunk(long k) { loadChunk(String.valueOf(k)); }
    public void loadChunk(String k) { loadChunk(hopperChunks.get(k)); }
    public void loadChunk(Chunk chunk) {
        if (chunk != null) {
            if (!chunk.isLoaded())
                chunk.load();

            chunk.setForceLoaded(true); // keep the chunk loaded
        }
    }

    // GETTERS
    // -----------------------------------------------------------------------------------------------------------------

    public String getChunkKey(long l) { return String.valueOf(l); }
    public String getChunkKey(Chunk c) { return String.valueOf(c.getChunkKey()); }

    public Chunk getChunk(long l) { return getChunk(getChunkKey(l)); }
    public Chunk getChunk(String k) { return hopperChunks.get(k); }

    // SETTERS
    // -----------------------------------------------------------------------------------------------------------------

    public void addHopperChunk(Chunk c) { addHopperChunk(c, getChunkKey(c)); }
    public void addHopperChunk(Chunk c, String k) {
        if (hopperChunks.containsKey(k)) return;

        hopperChunks.put(k, c);
        loadChunk(c);
    }

    public void addHopperChunkWithDepth(Chunk chunk) {
        if (hopperChunks.containsKey(getChunkKey(chunk))) return;

        for (Chunk c : getAdjacentChunks(chunk))
            addHopperChunk(c);
    }

    public void removeHopperChunk(long k) { removeHopperChunk(getChunkKey(k)); }
    public void removeHopperChunk(String k) { if(getChunk(k) != null) removeHopperChunk(getChunk(k)); }
    public void removeHopperChunk(Chunk c) {
        if (!hopperChunks.containsKey(getChunkKey(c))) return;

        hopperChunks.remove(getChunkKey(c));
        c.setForceLoaded(false);
    }

    public void removeHopperChunkWithDepth(Chunk chunk) {
        if (!hopperChunks.containsKey(getChunkKey(chunk))) return;

        for (Chunk c : getAdjacentChunks(chunk))
            removeHopperChunk(c);
    }

    // HELPERS
    // -----------------------------------------------------------------------------------------------------------------

    private Set<Chunk> getAdjacentChunks(Chunk center) {
        int n = getChunkDepth(center.getWorld());

        Set<Chunk> chunks = new HashSet<>();
        World world = center.getWorld();
        int centerX = center.getX();
        int centerZ = center.getZ();

        for (int dx = -n; dx <= n; dx++) {
            for (int dz = -n; dz <= n; dz++) {
                Chunk chunk = world.getChunkAt(centerX + dx, centerZ + dz);
                chunks.add(chunk);
            }
        }

        return chunks;
    }

    private boolean isEnderChestWithHopperInChunk(Chunk chunk) {
        try {
            for (EnderChestLocation chest : main.getEnderChestLocationManager().getEnderChests()) {
                if (!chest.getLocation().getChunk().equals(chunk)) continue;
                if (!chest.isHopperBelow()) continue;

                return true;
            }
        } catch (IOException ex) {
            main.logger.severe("Error checking if chunk has ender chest. Error: " + ex.getMessage());

            return false;
        }

        return false;
    }
}
