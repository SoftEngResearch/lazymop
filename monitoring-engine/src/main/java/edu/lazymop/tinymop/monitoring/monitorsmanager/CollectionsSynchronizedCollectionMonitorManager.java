package edu.lazymop.tinymop.monitoring.monitorsmanager;

/*
MOP FILE's IMPORT HERE
 */
import java.util.Collection;
import java.util.Iterator;

import edu.lazymop.tinymop.monitoring.GlobalMonitorManager;
import edu.lazymop.tinymop.monitoring.MonitorManager;
import edu.lazymop.tinymop.monitoring.monitors.CollectionsSynchronizedCollectionMonitor;
import edu.lazymop.tinymop.monitoring.slicing.algod.CollectionsSynchronizedCollection;
import edu.lazymop.tinymop.specparser.monitoring.RuntimeMonitor;

public class CollectionsSynchronizedCollectionMonitorManager extends MonitorManager {
    private static CollectionsSynchronizedCollectionMonitorManager managerInstance;

    protected CollectionsSynchronizedCollectionMonitorManager() {
        super("Collections_SynchronizedCollection");
    }

    @Override
    public RuntimeMonitor createMonitor() {
        return new CollectionsSynchronizedCollectionMonitor(specName);
    }

    public static CollectionsSynchronizedCollectionMonitorManager getManagerInstance() {
        if (managerInstance == null) {
            managerInstance = new CollectionsSynchronizedCollectionMonitorManager();
            GlobalMonitorManager.registerManager(managerInstance);
        }
        return managerInstance;
    }

    @Override
    protected void collectStatistics() {}

    @Override
    protected void monitorSlices() {
        CollectionsSynchronizedCollection.monitorSlices(this.specName, this);
    }

    public void accessIterEvent(Iterator iterator, int location, boolean isCreationEvent) {
        CollectionsSynchronizedCollection.accessIter(iterator, (location << 4) | 4); // event E4
    }

    public void syncEvent(Collection collection, int location, boolean isCreationEvent) {
        CollectionsSynchronizedCollection.sync(collection, (location << 4) | 1); // event E1
    }

    public void syncCreateIterEvent(Collection collection, Iterator iterator,
                                    int location, boolean isCreationEvent) {
        CollectionsSynchronizedCollection.syncCreateIter(collection, iterator, (location << 4) | 2); // event E2
    }

    public void asyncCreateIterEvent(Collection collection, Iterator iterator,
                                     int location, boolean isCreationEvent) {
        CollectionsSynchronizedCollection.asyncCreateIter(collection, iterator, (location << 4) | 3); // event E3
    }
}
