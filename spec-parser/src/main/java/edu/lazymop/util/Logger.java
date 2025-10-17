package edu.lazymop.util;

/**
 * Logger used in all tinymop code.
 */
import java.io.PrintStream;
import java.util.logging.Level;

public class Logger {

    private static final Logger INSTANCE = new Logger();
    private PrintStream out = System.out;
    private Level level = Level.CONFIG;

    public void setLoggingLevel(Level level) {
        this.level = level;
    }

    public Level getLoggingLevel() {
        return this.level;
    }

    public static Logger getGlobal() {
        return Logger.INSTANCE;
    }

    /**
     * Log a message using this Logger.
     *
     * @param lev The logging level.
     * @param msg The message to log.
     * @param thr The throwable to log in case of an exception.
     */
    public void log(Level lev, String msg, Throwable thr) {
        if (lev.intValue() < this.level.intValue()) {
            return;
        }
        this.out.println(lev.toString() + ":" + msg);
        this.out.println(thr);
    }

    /**
     * Log a message using this Logger.
     *
     * @param lev The logging level.
     * @param msg The message to log.
     */
    public void log(Level lev, String msg) {
        if (lev.intValue() < this.level.intValue()) {
            return;
        }
        this.out.println(lev.toString() + ":" + msg);
    }
}