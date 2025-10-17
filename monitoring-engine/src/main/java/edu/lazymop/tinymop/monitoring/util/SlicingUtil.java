package edu.lazymop.tinymop.monitoring.util;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import edu.lazymop.util.Logger;

public class SlicingUtil {
    protected static Logger LOGGER = Logger.getGlobal();

    // TODO: process edu.lazymop.tinymop.monitoring.datastructure.Event objects instead of splitting strings
    public static List<String> getMonitorableTrace(List<String> rawTrace) {
        long begin = System.nanoTime();
        List<String> refinedTrace = new ArrayList<>();
        for (String event : rawTrace) {
            refinedTrace.add(event.split("~")[0]);
        }
        long end = System.nanoTime();
        LOGGER.log(Level.FINEST, "GETMONITORABLE: " + (end - begin));
        return refinedTrace;
    }
}
