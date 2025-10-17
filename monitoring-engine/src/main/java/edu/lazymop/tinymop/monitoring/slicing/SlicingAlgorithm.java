package edu.lazymop.tinymop.monitoring.slicing;

import edu.lazymop.tinymop.monitoring.MonitorManager;

public interface SlicingAlgorithm {

    void collectStatistics(String specName);

    void monitorSlices(String specName, MonitorManager monitorManager);
}
