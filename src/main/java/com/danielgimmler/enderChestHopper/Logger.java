package com.danielgimmler.enderChestHopper;

import java.util.logging.Level;

public class Logger {
    public final java.util.logging.Logger baseLogger;
    private final String prefix;

    public Logger(EnderChestHopper main) {
        this.baseLogger = main.getLogger();
        this.prefix = "";
    }

    public Logger(EnderChestHopper main, String prefix) {
        this.baseLogger = main.getLogger();
        this.prefix = "[" + prefix + "] ";
    }

    public void setLogLevel(Level level) { this.baseLogger.setLevel(level); }

    public void finer(String msg) { trace(msg); }
    public void finest(String msg) { trace(msg); }
    public void trace(String msg) { baseLogger.finest(msg); }

    public void config(String msg) { debug(msg); }
    public void fine(String msg) { debug(msg); }
    public void debug(String msg) { baseLogger.fine(msg); }

    public void info(String msg) {
        baseLogger.info(prefix + msg);
    }

    public void warn(String msg) { warning(msg); }
    public void warning(String msg) {
        baseLogger.warning(prefix + msg);
    }

    public void error(String msg) { severe(msg); }
    public void severe(String msg) {
        baseLogger.severe(prefix + msg);
    }

    public void log(Level level, String msg) {
        baseLogger.log(level, prefix + msg);
    }
}
