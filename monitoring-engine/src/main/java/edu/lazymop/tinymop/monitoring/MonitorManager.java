package edu.lazymop.tinymop.monitoring;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import edu.lazymop.tinymop.monitoring.slicing.SlicingAlgorithm;
import edu.lazymop.tinymop.monitoring.valg.ValgRuntime;
import edu.lazymop.tinymop.specparser.monitoring.RuntimeMonitor;

public abstract class MonitorManager {
    public Map<Integer, String> locationsMapping;
    public boolean[] locationsInChangedClasses;
    protected SlicingAlgorithm algo;

    protected Set<RuntimeMonitor> monitors;

    protected String specName;
    protected String valgSpecName;

    public MonitorManager(String specName) {
        this.specName = specName;
        monitors = new HashSet<>();
        locationsMapping = new ConcurrentHashMap<>();
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
        locationsMapping.putIfAbsent(id, location);
        if (valgSpecName != null) {
            ValgRuntime.registerLocation(valgSpecName, id, location);
        }
        if (fromChangedClass) {
            locationsInChangedClasses[id] = true;
        }
    }
}
