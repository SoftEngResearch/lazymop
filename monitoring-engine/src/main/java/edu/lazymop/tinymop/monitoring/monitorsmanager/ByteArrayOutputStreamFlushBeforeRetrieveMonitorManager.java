package edu.lazymop.tinymop.monitoring.monitorsmanager;

/*
MOP FILE's IMPORT HERE
 */
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import edu.lazymop.tinymop.monitoring.GlobalMonitorManager;
import edu.lazymop.tinymop.monitoring.MonitorManager;
import edu.lazymop.tinymop.monitoring.monitors.ByteArrayOutputStreamFlushBeforeRetrieveMonitor;
import edu.lazymop.tinymop.specparser.monitoring.RuntimeMonitor;

public class ByteArrayOutputStreamFlushBeforeRetrieveMonitorManager extends MonitorManager {
    private static ByteArrayOutputStreamFlushBeforeRetrieveMonitorManager managerInstance;

    protected ByteArrayOutputStreamFlushBeforeRetrieveMonitorManager() {
        super("ByteArrayOutputStream_FlushBeforeRetrieve");
    }

    @Override
    public RuntimeMonitor createMonitor() {
        return new ByteArrayOutputStreamFlushBeforeRetrieveMonitor(specName);
    }

    public static ByteArrayOutputStreamFlushBeforeRetrieveMonitorManager getManagerInstance() {
        if (managerInstance == null) {
            managerInstance = new ByteArrayOutputStreamFlushBeforeRetrieveMonitorManager();
            GlobalMonitorManager.registerManager(managerInstance);
        }
        return managerInstance;
    }

    @Override
    protected void collectStatistics() {}

    public void writeEvent(OutputStream outputStream, int location, boolean isCreationEvent) {

    }

    public void flushEvent(OutputStream outputStream, int location, boolean isCreationEvent) {

    }

    public void closeEvent(OutputStream outputStream, int location, boolean isCreationEvent) {

    }

    public void tobytearrayEvent(ByteArrayOutputStream baos, int location, boolean isCreationEvent) {

    }

    public void tostringEvent(ByteArrayOutputStream baos, int location, boolean isCreationEvent) {

    }

    public void outputstreaminitEvent(ByteArrayOutputStream baos, OutputStream outputStream,
                                      int location, boolean isCreationEvent) {

    }
}
