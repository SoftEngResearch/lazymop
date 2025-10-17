package edu.lazymop.tinymop.specparser.slicing;

import java.util.HashSet;
import java.util.Set;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.ThisExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExplicitConstructorInvocationStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.VoidType;
import com.github.javaparser.printer.PrettyPrinter;
import com.github.javaparser.printer.configuration.PrettyPrinterConfiguration;
import com.runtimeverification.rvmonitor.java.rvj.parser.ast.ImportDeclaration;
import com.runtimeverification.rvmonitor.java.rvj.parser.ast.rvmspec.EventDefinition;
import com.runtimeverification.rvmonitor.java.rvj.parser.ast.rvmspec.RVMParameter;
import com.runtimeverification.rvmonitor.logicrepository.parser.logicrepositorysyntax.LogicRepositoryType;

/**
 * The MonitorManagerGenerator class is responsible for generating the monitor manager class.
 * This class uses JavaParser to generate the necessary code for the monitor manager.
 */
public class MonitorManagerGenerator {
    private SlicerGenerationUtil slicerGenUtil;
    private LogicRepositoryType monitorData;
    private String specName;
    private String fileName;

    /**
     * Constructs a MonitorManagerGenerator with the given SlicerGenerationUtil and LogicRepositoryType.
     *
     * @param slicerGenUtil the SlicerGenerationUtil used for generating the monitor manager
     * @param monitorData the LogicRepositoryType representing the monitor data
     */
    public MonitorManagerGenerator(SlicerGenerationUtil slicerGenUtil, LogicRepositoryType monitorData) {
        this.slicerGenUtil = slicerGenUtil;
        this.monitorData = monitorData;
        this.specName = slicerGenUtil.getSpecName().replace("_", "");
        this.fileName = specName + "MonitorManager";
    }

    /**
     * Adds the necessary import statements to the given CompilationUnit.
     * It includes imports from the mop file as well as TinyMOP's classes
     *
     * @param code the CompilationUnit to add the import statements to
     */
    private void addImport(CompilationUnit code) {
        String monitoring = "edu.lazymop.tinymop.monitoring";

        for (ImportDeclaration imp : slicerGenUtil.getImports()) {
            code.addImport(imp.getName().toString(), imp.isStatic(), imp.isAsterisk());
        }

        code.addImport(monitoring + ".GlobalMonitorManager");
        code.addImport(monitoring + ".MonitorManager");
        code.addImport(monitoring + ".monitors." + specName + "Monitor");
        code.addImport(monitoring + ".slicing.algod." + specName);
        code.addImport("edu.lazymop.tinymop.specparser.monitoring.RuntimeMonitor");
    }

    /**
     * Generates the createMonitor method for the monitor manager class.
     *
     * @param klass the ClassOrInterfaceDeclaration representing the monitor manager class
     */
    private void generateCreateMonitor(ClassOrInterfaceDeclaration klass) {
        MethodDeclaration method = new MethodDeclaration(
                Modifier.createModifierList(Modifier.Keyword.PUBLIC),
                new ClassOrInterfaceType(null, "RuntimeMonitor"), "createMonitor")
                .addAnnotation(new MarkerAnnotationExpr("Override"))
                .setBody(new BlockStmt().addStatement(
                        new ReturnStmt(new ObjectCreationExpr().setType(specName + "Monitor")
                                .addArgument(new NameExpr("specName")))
                ));
        klass.addMember(method);
    }

    /**
     * Generates the getManagerInstance method for the monitor manager class.
     *  if (managerInstance == null) {
     *      managerInstance = new SPECMonitorManager();
     *      GlobalMonitorManager.registerManager(managerInstance);
     *  }
     *  return managerInstance;
     *
     * @param klass the ClassOrInterfaceDeclaration representing the monitor manager class
     */
    private void generateGetManagerInstance(ClassOrInterfaceDeclaration klass) {
        BlockStmt body = new BlockStmt();
        MethodDeclaration method = new MethodDeclaration(
                Modifier.createModifierList(Modifier.Keyword.PUBLIC, Modifier.Keyword.STATIC),
                new ClassOrInterfaceType(null, fileName), "getManagerInstance")
                .setBody(body);

        BinaryExpr ifCondition = new BinaryExpr(new NameExpr("managerInstance"), new NullLiteralExpr(),
                BinaryExpr.Operator.EQUALS);
        BlockStmt ifBody = new BlockStmt();
        body.addStatement(new IfStmt().setCondition(ifCondition).setThenStmt(ifBody));
        ifBody.addStatement(new AssignExpr(new NameExpr("managerInstance"),
                new ObjectCreationExpr().setType(new ClassOrInterfaceType(null, fileName)),
                AssignExpr.Operator.ASSIGN));
        ifBody.addStatement(
                new MethodCallExpr(new NameExpr("GlobalMonitorManager"), "registerManager")
                        .addArgument(new NameExpr("managerInstance")));
        body.addStatement(new ReturnStmt(new NameExpr("managerInstance")));
        klass.addMember(method);
    }

    /**
     * Generates the collectStatistics method for the monitor manager class.
     * @Override protected void collectStatistics() {}
     *
     * @param klass the ClassOrInterfaceDeclaration representing the monitor manager class
     */
    private void generateCollectStatistics(ClassOrInterfaceDeclaration klass) {
        MethodDeclaration method = new MethodDeclaration(
                Modifier.createModifierList(Modifier.Keyword.PROTECTED),
                new VoidType(), "collectStatistics")
                .addAnnotation(new MarkerAnnotationExpr("Override"));
        klass.addMember(method);
    }

    /**
     * Generates the monitorSlices method for the monitor manager class.
     *
     * @param klass the ClassOrInterfaceDeclaration representing the monitor manager class
     */
    private void generateMonitorSlices(ClassOrInterfaceDeclaration klass) {
        MethodDeclaration method = new MethodDeclaration(
                Modifier.createModifierList(Modifier.Keyword.PROTECTED),
                new VoidType(), "monitorSlices")
                .addAnnotation(new MarkerAnnotationExpr("Override"))
                .setBody(new BlockStmt().addStatement(
                        new MethodCallExpr(new NameExpr(specName), "monitorSlices")
                                .addArgument(new FieldAccessExpr(new ThisExpr(), "specName"))
                                .addArgument(new ThisExpr())
                        )
                );
        klass.addMember(method);
    }

    /**
     * Generates an event method for the given event and adds it to the monitor manager class.
     * SPEC.EVENT(OBJECT, (location << 4) | 1); means event e1
     *
     * @param klass the ClassOrInterfaceDeclaration representing the monitor manager class
     * @param event the EventDefinition representing the event
     * @param eventID the ID of the event
     */
    private void generateEventMethod(ClassOrInterfaceDeclaration klass, EventDefinition event, int eventID) {
        MethodDeclaration method = new MethodDeclaration(
                Modifier.createModifierList(Modifier.Keyword.PUBLIC),
                new VoidType(),
                event.getId() + "Event");
        for (RVMParameter parameter : event.getParameters()) {
            method.addParameter(new Parameter(new ClassOrInterfaceType().setName(parameter.getType().toString()),
                    parameter.getName()));
        }
        method.addParameter(new Parameter(PrimitiveType.intType(), "event"));
        method.addParameter(new Parameter(PrimitiveType.booleanType(), "isCreationEvent"));

        MethodCallExpr methodCall = new MethodCallExpr(new NameExpr(specName), event.getId());
        for (RVMParameter parameter : event.getParameters()) {
            methodCall.addArgument(parameter.getName());
        }


        // event << 4
        BinaryExpr shiftExpr = new BinaryExpr(
                new NameExpr("event"),
                new IntegerLiteralExpr("4"),
                BinaryExpr.Operator.LEFT_SHIFT
        );

        // (event << 4) | 1
        BinaryExpr orExpr = new BinaryExpr(
                shiftExpr,
                new IntegerLiteralExpr(String.valueOf(eventID)),
                BinaryExpr.Operator.BINARY_OR
        );
        methodCall.addArgument(orExpr);


        method.setBody(new BlockStmt().addStatement(methodCall));
        klass.addMember(method);
    }

    private void generateEvents(ClassOrInterfaceDeclaration klass) {
        int eventID = 0;
        Set<String> processedEvents = new HashSet<>();

        for (EventDefinition event : slicerGenUtil.getEvents()) {
            if (!processedEvents.contains(event.getId())) {
                eventID += 1;
                processedEvents.add(event.getId());
            }
            generateEventMethod(klass, event, eventID);
        }
    }

    public String generateManagerCode() {
        CompilationUnit code = new CompilationUnit();
        code.setPackageDeclaration("edu.lazymop.tinymop.monitoring.monitorsmanager");
        ClassOrInterfaceDeclaration klass = code.addClass(fileName)
                .addExtendedType("MonitorManager");

        addImport(code);
        klass.addPrivateField(fileName, "managerInstance").setStatic(true);

        ConstructorDeclaration constructor = new ConstructorDeclaration()
                .setName(fileName)
                .setModifiers(Modifier.Keyword.PUBLIC)
                .setBody(new BlockStmt().addStatement(new ExplicitConstructorInvocationStmt().setThis(false)
                        .addArgument(new StringLiteralExpr(specName))));
        klass.addMember(constructor);

        generateCreateMonitor(klass);
        generateGetManagerInstance(klass);
        generateCollectStatistics(klass);
        generateMonitorSlices(klass);
        generateEvents(klass);

        final String out = new PrettyPrinter(new PrettyPrinterConfiguration()
                .setEndOfLineCharacter("\n")
                .setMaxEnumConstantsToAlignHorizontally(1)
        ).print(code);
        return out;
    }
}
