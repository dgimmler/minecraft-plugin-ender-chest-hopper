package com.danielgimmler.enderChestHopper.db.playerConfig;

public class EnderChestConfig {

    private final String chestKey;
    private String name;
    private boolean hopperEnabled;

    public EnderChestConfig(String chestKey) { this(chestKey, chestKey, false); }
    public EnderChestConfig(String chestKey, String chestName) { this(chestKey, chestName, false); }
    public EnderChestConfig(String chestKey, boolean hopperEnabled) { this(chestKey, chestKey, hopperEnabled); }
    public EnderChestConfig(String chestKey, String chestName, boolean hopperEnabled) {
        this.chestKey = chestKey;
        setName(chestName);
        setHopperEnabled(hopperEnabled);
    }

    // GETTERS
    // -----------------------------------------------------------------------------------------------------------------

    public String getKey() { return chestKey; }
    public String getName() { return name; }
    public boolean isHopperEnabled() { return hopperEnabled; }

    // SETTERS
    // -----------------------------------------------------------------------------------------------------------------

    public void setName(String name) { this.name = name; }
    public void setHopperEnabled(boolean hopperEnabled) { this.hopperEnabled = hopperEnabled; }
    public void enableHopper() { setHopperEnabled(true); }
    public void disableHopper() { setHopperEnabled(false); }
    public void toggleHopper() { setHopperEnabled(!isHopperEnabled()); }
}
