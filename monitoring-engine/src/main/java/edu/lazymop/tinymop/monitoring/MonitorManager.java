package edu.lazymop.tinymop.monitoring;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.lazymop.tinymop.monitoring.slicing.SlicingAlgorithm;
import edu.lazymop.tinymop.specparser.monitoring.RuntimeMonitor;

public abstract class MonitorManager {
    public HashMap<Integer, String> locationsMapping;
    public boolean[] locationsInChangedClasses;
    protected SlicingAlgorithm algo;

    protected Set<RuntimeMonitor> monitors;

    protected String specName;

    public MonitorManager(String specName) {
        this.specName = specName;
        monitors = new HashSet<>();
        locationsMapping = new HashMap<>();
        locationsInChangedClasses = new boolean[100000];
    }

    // can collect statistics about the monitoring process for this spec
    protected void collectStatistics() {
        algo.collectStatistics(specName);
    }

    protected void monitorSlices() {
        algo.monitorSlices(specName, this);
    }

    // creates a singleton Manager for a spec
    public abstract RuntimeMonitor createMonitor();

    // can run traces on monitors
    public RuntimeMonitor.VerdictCategory runTraceOnMonitor(RuntimeMonitor monitor, List<String> trace) {
        return monitor.runAutomatonOnStrings(trace);
    }

    public void notifyMapping(int id, String location, boolean fromChangedClass) {
        locationsMapping.put(id, location);
        if (fromChangedClass) {
            locationsInChangedClasses[id] = true;
        }
    }
}
