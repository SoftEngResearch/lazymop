package edu.lazymop.tinymop.specparser.monitoring;

import java.io.IOException;
import java.util.List;

/**
 * All Monitors that are derived from specs must implement this interface.
 */
public abstract class RuntimeMonitor implements Cloneable {

    public abstract VerdictCategory runAutomatonOnStrings(List<String> trace);

    public abstract void reset();

    public abstract void toDot() throws IOException;

    public enum VerdictCategory {
        VIOLATING,
        VALIDATING,
        DONTKNOW,
    }
}
