package edu.lazymop.tinymop.monitoring.monitorsmanager;

/*
MOP FILE's IMPORT HERE
 */
import java.util.Iterator;

import com.runtimeverification.rvmonitor.java.rt.RuntimeOption;
import edu.lazymop.tinymop.monitoring.GlobalMonitorManager;
import edu.lazymop.tinymop.monitoring.MonitorManager;
import edu.lazymop.tinymop.monitoring.monitors.IteratorHasNextMonitor;
import edu.lazymop.tinymop.monitoring.slicing.algod.IteratorHasNext;

public class IteratorHasNextMonitorManager extends MonitorManager {
    private static IteratorHasNextMonitorManager managerInstance;

    public IteratorHasNextMonitorManager() {
        super("Iterator_HasNext");
    }

    @Override
    public IteratorHasNextMonitor createMonitor() {
        return new IteratorHasNextMonitor(specName);
    }

    public static IteratorHasNextMonitorManager getManagerInstance() {
        if (managerInstance == null) {
            managerInstance = new IteratorHasNextMonitorManager();
            GlobalMonitorManager.registerManager(managerInstance);
            RuntimeOption.enableFineGrainedLock(true);
        }
        return managerInstance;
    }

    @Override
    protected void collectStatistics() {}

    @Override
    protected void monitorSlices() {
        IteratorHasNext.monitorSlices(this.specName, this);
    }

    public void nextEvent(Iterator iterator, int location, boolean isCreationEvent) {
        IteratorHasNext.next(iterator, (location << 4) | 3); // event E3
    }

    public void hasnexttrueEvent(Iterator iterator, boolean bool, int location, boolean isCreationEvent) {
        IteratorHasNext.hasnexttrue(iterator, bool, (location << 4) | 1); // event E1
    }

    public void hasnextfalseEvent(Iterator iterator, boolean bool, int location, boolean isCreationEvent) {
        IteratorHasNext.hasnextfalse(iterator, bool, (location << 4) | 2); // event E2
    }
}
