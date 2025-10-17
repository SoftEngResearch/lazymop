// CHECKSTYLE:OFF
package edu.lazymop.tinymop.specparser.slicing.component;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.VoidType;
import com.runtimeverification.rvmonitor.java.rvj.output.monitor.MonitorFeatures;
import com.runtimeverification.rvmonitor.java.rvj.output.monitor.SuffixMonitor;
import com.runtimeverification.rvmonitor.java.rvj.parser.ast.rvmspec.EventDefinition;
import com.runtimeverification.rvmonitor.java.rvj.parser.ast.rvmspec.RVMParameter;
import edu.lazymop.tinymop.specparser.slicing.SlicerGenerationUtil;
import edu.lazymop.tinymop.specparser.slicing.component.handler.EventMethodBody;

public class EventHandler extends Component {
    private boolean isSecondPass = false;
    /**
     * @param slicerGenUtil the SlicerGenerationUtil used for generating code
     * @param code the CompilationUnit representing the code
     * @param klass the ClassOrInterfaceDeclaration representing the class
     */
    public EventHandler(SlicerGenerationUtil slicerGenUtil, CompilationUnit code, ClassOrInterfaceDeclaration klass) {
        super(slicerGenUtil, code, klass);
    }

    /**
     * Generates the body of the handler method for an activated event call.
     *
     * @param event the EventDefinition representing the event
     * @return the BlockStmt representing the body of the handler method
     */
    public BlockStmt getHandlerBodyActivated(EventDefinition event) {
        BlockStmt blockStmt = new BlockStmt();

        EventMethodBody body = new EventMethodBody(event, blockStmt, slicerGenUtil.eventHandlerUtil, isSecondPass);
        body.getWeakReferenceInit();
        body.getMatchedIndexingTreeInit();
        body.getCache();
        body.getMonitorCreation();

        // Update cache while updating slices
        // Otherwise, it is possible that we don't have a monitor
        body.updateSlices();

//        blockStmt.accept(new SlicerGenerationUtil.RecursiveBlockFlattener(), null);
        return blockStmt;
    }

    /**
     * Generates the body of the handler method for the given event and sets it
     * to the provided MethodDeclaration.
     *
     * @param event the EventDefinition representing the event
     * @param method the MethodDeclaration to set the body to
     */
    public void getHandlerBody(EventDefinition event, MethodDeclaration method) {
        BlockStmt blockStmt = new BlockStmt();
        if (event.isStartEvent()) {
            AssignExpr active = new AssignExpr(new NameExpr("activated"), new BooleanLiteralExpr(true), AssignExpr.Operator.ASSIGN);
            blockStmt.addStatement(active);
        }

        EventHandlerUtil.addLock(blockStmt);
        if (event.isStartEvent()) {
            blockStmt.addStatement(getHandlerBodyActivated(event));
        } else {
            blockStmt.addStatement(new IfStmt(new NameExpr("activated"), getHandlerBodyActivated(event), null));
        }
        EventHandlerUtil.addUnlock(blockStmt);

        method.setBody(blockStmt);
    }

    /**
     * Adds a handler method for the given event to the class.
     *
     * @param event the EventDefinition representing the event
     */
    public void addHandler(EventDefinition event, boolean add) {
        MethodDeclaration method = new MethodDeclaration(
                Modifier.createModifierList(Modifier.Keyword.PUBLIC, Modifier.Keyword.STATIC),
                new VoidType(),
                event.getId());
        for (RVMParameter parameter : event.getParameters()) {
            method.addParameter(new Parameter(new ClassOrInterfaceType().setName(parameter.getType().toString()),
                    parameter.getName()));
        }
        method.addParameter(new Parameter(new PrimitiveType(PrimitiveType.Primitive.INT), "event"));

        getHandlerBody(event, method);

        if (add) {
            klass.addMember(method);
        }
    }

    /**
     * Add monitorSlices method
     * public static void monitorSlices(String specName, MonitorManager monitorManager) {
     *     SpecializedSlicingAlgorithmUtil.monitorSlices(specName, monitorManager, trie.root);
     * }
     */
    private void addMonitorSlices() {
        MethodDeclaration method = new MethodDeclaration(
                Modifier.createModifierList(Modifier.Keyword.PUBLIC, Modifier.Keyword.STATIC),
                new VoidType(), "monitorSlices"
        )
                .addParameter(new Parameter(new ClassOrInterfaceType(null, "String"), "specName"))
                .addParameter(new Parameter(new ClassOrInterfaceType(null, "MonitorManager"), "monitorManager"));

        method.setBody(new com.github.javaparser.ast.stmt.BlockStmt(new NodeList<>(
                new ExpressionStmt(new MethodCallExpr(
                        new NameExpr("SpecializedSlicingAlgorithmUtil"),
                        "monitorSlices",
                        new NodeList<>(new NameExpr("specName"), new NameExpr("monitorManager"),
                                new FieldAccessExpr(new NameExpr("trie"), "root"))
                ))
        )));
        klass.addMember(method);
    }

    /**
     * Adds handler methods for all events defined in the spec.
     */
    @Override
    public void add() {
        for (EventDefinition event : slicerGenUtil.getEvents()) {
            addHandler(event, false);
        }

        for (SuffixMonitor monitor : this.slicerGenUtil.eventHandlerUtil.monitors.values()) {
            MonitorFeatures features = monitor.getFeatures();
            features.onCodeGenerationPass1Completed();
        }

        isSecondPass = true;

        for (EventDefinition event : slicerGenUtil.getEvents()) {
            addHandler(event, true);
        }

        addMonitorSlices();

    }
}
