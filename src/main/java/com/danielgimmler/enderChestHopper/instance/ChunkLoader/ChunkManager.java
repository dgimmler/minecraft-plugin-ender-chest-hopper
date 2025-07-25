package com.danielgimmler.enderChestHopper.instance.ChunkLoader;

import com.danielgimmler.enderChestHopper.EnderChestHopper;
import org.bukkit.Chunk;
import org.bukkit.block.Hopper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ChunkManager {

    private EnderChestHopper main;
    private Map<String, Chunk> hopperChunks;

    public ChunkManager(EnderChestHopper main) {
        this.main = main;
        this.hopperChunks = new HashMap<>();

        try {
            getHopperChunksFromFile();
            loadHopperChunks();
        } catch (IOException e) {
            main.logger.severe("Error loading hopper chunks. Error: " + e.getMessage());
        }
    }

    private void getHopperChunksFromFile() throws IOException { for (Hopper h : main.getEnderChestLocationManager().getEnderChestHoppers()) addHopperChunk(h.getChunk()); }
    public void loadHopperChunks() { for (String k : hopperChunks.keySet()) loadChunk(k); }
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
        if (!hopperChunks.containsKey(k)) {
            hopperChunks.put(k, c);
            loadChunk(c);
        }
    }


    public void removeHopperChunk(long k) { removeHopperChunk(getChunkKey(k)); }
    public void removeHopperChunk(String k) { if(getChunk(k) != null) removeHopperChunk(getChunk(k)); }
    public void removeHopperChunk(Chunk c) {
        removeHopperChunk(getChunkKey(c));
        c.setForceLoaded(false);
    }

}
