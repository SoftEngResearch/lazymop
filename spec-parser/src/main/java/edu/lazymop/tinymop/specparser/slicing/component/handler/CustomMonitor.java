package edu.lazymop.tinymop.specparser.slicing.component.handler;

import java.util.ArrayList;
import java.util.List;

import com.runtimeverification.rvmonitor.java.rvj.output.OptimizedCoenableSet;
import com.runtimeverification.rvmonitor.java.rvj.output.monitor.MonitorFeatures;
import com.runtimeverification.rvmonitor.java.rvj.output.monitor.SuffixMonitor;
import com.runtimeverification.rvmonitor.java.rvj.parser.ast.rvmspec.RVMParameters;
import com.runtimeverification.rvmonitor.java.rvj.parser.ast.rvmspec.RVMonitorSpec;
import com.runtimeverification.rvmonitor.util.RVMException;

public class CustomMonitor extends SuffixMonitor  {
    private MonitorFeatures features;

    public CustomMonitor(String outputName, RVMonitorSpec rvmSpec, OptimizedCoenableSet coenableSet, boolean isOutermost)
            throws RVMException {
        super(outputName, rvmSpec, coenableSet, isOutermost);
        this.features = new CustomMonitorFeatures(rvmSpec.getParameters());
    }

    @Override
    public MonitorFeatures getFeatures() {
        return features;
    }

    public class CustomMonitorFeatures extends MonitorFeatures {

        private List<RVMParameters> allPrms = new ArrayList<>();
        private boolean needsNonFinalWeakRefsInMonitor = false;

        public CustomMonitorFeatures(RVMParameters specParams) {
            super(specParams);
        }

        public void addRelatedEvent(RVMParameters parameters) {
            allPrms.add(parameters);
        }

        @Override
        public boolean isNonFinalWeakRefsInMonitorNeeded() {
            if (!this.isStabilized()) {
                throw new IllegalAccessError();
            } else {
                return this.needsNonFinalWeakRefsInMonitor;
            }
        }

        @Override
        public void onCodeGenerationPass1Completed() {
            super.onCodeGenerationPass1Completed(); // to modify the stabilized variable

            boolean allcovered = true;
            RVMParameters needed = this.getRememberedParameters();
            for (RVMParameters prms : this.allPrms) {
                if (!prms.contains(needed)) {
                    allcovered = false;
                    break;
                }
            }

            if (!allcovered) {
                this.needsNonFinalWeakRefsInMonitor = true;
            }
        }
    }

}
