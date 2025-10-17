// Based on JavaMOP's com.runtimeverification.rvmonitor.java.rvj.output.combinedoutputcode.CombinedOutput

package edu.lazymop.tinymop.specparser.slicing.component;

import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;

import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.UnaryExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.WhileStmt;
import com.runtimeverification.rvmonitor.java.rvj.output.CoEnableSet;
import com.runtimeverification.rvmonitor.java.rvj.output.EnableSet;
import com.runtimeverification.rvmonitor.java.rvj.output.OptimizedCoenableSet;
import com.runtimeverification.rvmonitor.java.rvj.output.combinedoutputcode.indexingtree.IndexingTreeManager;
import com.runtimeverification.rvmonitor.java.rvj.output.combinedoutputcode.indexingtree.reftree.RefTree;
import com.runtimeverification.rvmonitor.java.rvj.output.monitor.SuffixMonitor;
import com.runtimeverification.rvmonitor.java.rvj.output.monitorset.MonitorSet;
import com.runtimeverification.rvmonitor.java.rvj.parser.ast.RVMSpecFile;
import com.runtimeverification.rvmonitor.java.rvj.parser.ast.rvmspec.EventDefinition;
import com.runtimeverification.rvmonitor.java.rvj.parser.ast.rvmspec.PropertyAndHandlers;
import com.runtimeverification.rvmonitor.java.rvj.parser.ast.rvmspec.RVMParameter;
import com.runtimeverification.rvmonitor.java.rvj.parser.ast.rvmspec.RVMParameterSet;
import com.runtimeverification.rvmonitor.java.rvj.parser.ast.rvmspec.RVMParameters;
import com.runtimeverification.rvmonitor.java.rvj.parser.ast.rvmspec.RVMonitorSpec;
import com.runtimeverification.rvmonitor.util.RVMException;
import edu.lazymop.tinymop.specparser.Main;
import edu.lazymop.tinymop.specparser.slicing.component.handler.CustomMonitor;

public class EventHandlerUtil {

    public final IndexingTreeManager indexingTreeManager;

    public TreeMap<RVMonitorSpec, MonitorSet> monitorSets = new TreeMap<>();
    public TreeMap<RVMonitorSpec, SuffixMonitor> monitors = new TreeMap<>();
    public HashSet<RVMParameter> disableParams = new HashSet<>();

    private RVMSpecFile rvmSpecFile;
    private RVMonitorSpec rvMonitorSpec;
    private List<RVMonitorSpec> specs;
    private TreeMap<RVMonitorSpec, EnableSet> enableSets = new TreeMap<>();


    public EventHandlerUtil(RVMSpecFile rvmSpecFile) throws RVMException {
        this.rvmSpecFile = rvmSpecFile;
        this.specs = rvmSpecFile.getSpecs();
        this.rvMonitorSpec = this.specs.get(0);

        for (RVMonitorSpec rvmSpec : rvmSpecFile.getSpecs()) {
            EnableSet enableSet = new EnableSet(rvmSpec.getEvents(), rvmSpec.getParameters());
            CoEnableSet coenableSet = new CoEnableSet(rvmSpec.getEvents(), rvmSpec.getParameters());

            for (PropertyAndHandlers prop : rvmSpec.getPropertiesAndHandlers()) {
                enableSet.add(new EnableSet(prop, rvmSpec.getEvents(), rvmSpec.getParameters()));
                coenableSet.add(new CoEnableSet(prop, rvmSpec.getEvents(), rvmSpec.getParameters()));
            }

            OptimizedCoenableSet optimizedCoenableSet = new OptimizedCoenableSet(coenableSet);

            enableSets.put(rvmSpec, enableSet);

            CustomMonitor monitor = new CustomMonitor(rvmSpec.getName(), rvmSpec, optimizedCoenableSet, true);

            monitors.put(rvmSpec, monitor);

            monitorSets.put(rvmSpec, new MonitorSet(rvmSpec, monitor));

            for (EventDefinition event : rvmSpec.getEvents()) {
                RVMParameters eventParams = event.getRVMParametersOnSpec();
                RVMParameterSet enable = enableSet.getEnable(
                        event.getId());

                for (RVMParameters enableEntity : enable) {
                    if (enableEntity.size() == 0 && !rvmSpec.hasNoParamEvent()) {
                        continue;
                    }
                    if (enableEntity.contains(eventParams)) {
                        continue;
                    }

                    RVMParameters unionOfEnableEntityAndParam = RVMParameters
                            .unionSet(enableEntity, eventParams);

                    for (RVMParameter p : unionOfEnableEntityAndParam) {
                        if (!enableEntity.contains(p)) {
                            disableParams.add(p);
                        }
                    }
                }
            }
        }

        this.indexingTreeManager = new IndexingTreeManager("Monitor", this.specs,
                this.monitorSets, this.monitors, this.enableSets);

        for (MonitorSet monitorSet : monitorSets.values()) {
            monitorSet.setMonitorLock("THISISAPLACEHOLDER1");
            monitorSet.setIndexingTreeManager(indexingTreeManager);
        }

        TreeMap<String, RefTree> refTrees = indexingTreeManager.refTrees;

        for (SuffixMonitor monitor : monitors.values()) {
            monitor.setRefTrees(refTrees);
        }
    }

    public RVMonitorSpec getRVMonitorSpec() {
        return rvMonitorSpec;
    }

    /*
     * Add lock
     * while (!lock.tryLock()) {
     *   Thread.yield();
     * }
     */
    public static void addLock(BlockStmt statement) {
        MethodCallExpr tryLockCall = new MethodCallExpr(new NameExpr("lock"), "tryLock");
        UnaryExpr condition = new UnaryExpr(tryLockCall, UnaryExpr.Operator.LOGICAL_COMPLEMENT);

        MethodCallExpr yieldCall = new MethodCallExpr(new NameExpr("Thread"), "yield");
        BlockStmt whileBody = new BlockStmt().addStatement(new ExpressionStmt(yieldCall));

        WhileStmt whileStmt = new WhileStmt(condition, whileBody);

        if (Main.onDemandSync) {
            IfStmt ifStmt = new IfStmt().setCondition(
                            new BinaryExpr(
                                    new FieldAccessExpr(new NameExpr("GlobalMonitorManager"), "isMultiThreaded"),
                                    new BooleanLiteralExpr(true),
                                    BinaryExpr.Operator.EQUALS)
                    )
                    .setThenStmt(whileStmt);
            statement.addStatement(ifStmt);
        } else {
            statement.addStatement(whileStmt);
        }
    }

    public static void addUnlock(BlockStmt statement) {
        MethodCallExpr unlockCall = new MethodCallExpr(new NameExpr("lock"), "unlock");
        if (Main.onDemandSync) {
            IfStmt ifStmt = new IfStmt().setCondition(
                            new BinaryExpr(
                                    new FieldAccessExpr(new NameExpr("GlobalMonitorManager"), "isMultiThreaded"),
                                    new BooleanLiteralExpr(true),
                                    BinaryExpr.Operator.EQUALS)
                    )
                    .setThenStmt(new ExpressionStmt(unlockCall));
            statement.addStatement(ifStmt);
        } else {
            statement.addStatement(unlockCall);
        }
    }
}
