package edu.lazymop.tinymop.instrumentation;

import java.util.logging.Level;

import edu.lazymop.util.Logger;

/**
 * Hello world.
 */
public class App {

    private static final Logger LOGGER = Logger.getGlobal();

    public static void main(String[] args) {
        LOGGER.log(Level.INFO, "Hello World!");
    }
}
