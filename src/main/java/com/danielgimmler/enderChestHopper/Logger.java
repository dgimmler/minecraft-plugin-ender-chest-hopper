package com.danielgimmler.enderChestHopper;

import java.util.logging.Level;

public class Logger {
    public final java.util.logging.Logger baseLogger;
    private final String prefix;

    public Logger(EnderChestHopper main, String prefix) {
        this.baseLogger = main.getLogger();
        this.prefix = "[" + prefix + "] ";
    }

    public void info(String msg) {
        baseLogger.info(prefix + msg);
    }

    public void warning(String msg) {
        baseLogger.warning(prefix + msg);
    }

    public void severe(String msg) {
        baseLogger.severe(prefix + msg);
    }

    public void log(Level level, String msg) {
        baseLogger.log(level, prefix + msg);
    }
}
