package edu.lazymop.tinymop.specparser.slicing.component;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.CastExpr;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.SuperExpr;
import com.github.javaparser.ast.expr.ThisExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.ThrowStmt;
import com.github.javaparser.ast.stmt.TryStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.VoidType;
import com.runtimeverification.rvmonitor.java.rvj.output.monitor.MonitorFeatures;
import com.runtimeverification.rvmonitor.java.rvj.parser.ast.rvmspec.EventDefinition;
import com.runtimeverification.rvmonitor.java.rvj.parser.ast.rvmspec.RVMParameter;
import com.runtimeverification.rvmonitor.java.rvj.parser.ast.rvmspec.RVMParameters;
import edu.lazymop.tinymop.specparser.slicing.SlicerGenerationUtil;

public class BindingClass extends Component {
    public BindingClass(SlicerGenerationUtil slicerGenUtil, CompilationUnit code,
                                 ClassOrInterfaceDeclaration klass) {
        super(slicerGenUtil, code, klass);
    }

    // Similar to JavaMOP BaseMonitor's toString method

    private void generateBasicMethods(ClassOrInterfaceDeclaration klass, MonitorFeatures feature) {
        BlockStmt cloneBody = new BlockStmt();
        MethodDeclaration cloneMethod = new MethodDeclaration(Modifier.createModifierList(Modifier.Keyword.PROTECTED),
                new ClassOrInterfaceType(null, "Object"), "clone")
                .setBody(cloneBody);

        // Clone monitor (and update current node's monitors count)
        cloneBody.addStatement(new TryStmt()
                .setTryBlock(new BlockStmt()
                        // XXXMonitor ret = (XXXMonitor) super.clone();
                        .addStatement(
                                new AssignExpr(new VariableDeclarationExpr(
                                        new ClassOrInterfaceType(null, getSliceClassName()), "ret"),
                                        new CastExpr(
                                                new ClassOrInterfaceType(null, getSliceClassName()),
                                                new MethodCallExpr(new SuperExpr(), "clone")
                                        ),
                                        AssignExpr.Operator.ASSIGN
                                )
                        )
                        // ret.node.monitor += 1;
                        .addStatement(new AssignExpr(
                                new FieldAccessExpr(new FieldAccessExpr(new NameExpr("ret"), "node"), "monitors"),
                                new IntegerLiteralExpr("1"), AssignExpr.Operator.PLUS))
                        // return ret;
                        .addStatement(new ReturnStmt(new NameExpr("ret")))

                )
                .setCatchClauses(new NodeList<>(
                        new CatchClause(new Parameter(
                                new ClassOrInterfaceType(null, "CloneNotSupportedException"), "e"),
                                new BlockStmt().addStatement(new ThrowStmt(
                                        new ObjectCreationExpr(null,
                                                new ClassOrInterfaceType(null, "InternalError"),
                                                new NodeList<>(
                                                        new MethodCallExpr(new NameExpr("e"), "toString")
                                                )
                                        )
                                ))
                        ))
                )
        );
        klass.addMember(cloneMethod);

        MethodDeclaration getStateMethod = new MethodDeclaration(
                Modifier.createModifierList(Modifier.Keyword.PUBLIC, Modifier.Keyword.FINAL),
                PrimitiveType.intType(), "getState")
                .addAnnotation(new MarkerAnnotationExpr("Override"))
                .setBody(new BlockStmt().addStatement(new ReturnStmt(new NameExpr("state"))));
        klass.addMember(getStateMethod);

        if (feature.isTimeTrackingNeeded()) {
            MethodDeclaration getTauMethod = new MethodDeclaration(
                    Modifier.createModifierList(Modifier.Keyword.PUBLIC, Modifier.Keyword.FINAL),
                    PrimitiveType.longType(), "getTau")
                    .addAnnotation(new MarkerAnnotationExpr("Override"))
                    .setBody(new BlockStmt().addStatement(new ReturnStmt(new NameExpr("tau"))));
            klass.addMember(getTauMethod);

            MethodDeclaration getDisableMethod = new MethodDeclaration(
                    Modifier.createModifierList(Modifier.Keyword.PUBLIC, Modifier.Keyword.FINAL),
                    PrimitiveType.longType(), "getDisable")
                    .addAnnotation(new MarkerAnnotationExpr("Override"))
                    .setBody(new BlockStmt().addStatement(new ReturnStmt(new NameExpr("disable"))));
            klass.addMember(getDisableMethod);

            MethodDeclaration setDisableMethod = new MethodDeclaration(
                    Modifier.createModifierList(Modifier.Keyword.PUBLIC, Modifier.Keyword.FINAL),
                    new VoidType(), "setDisable")
                    .addAnnotation(new MarkerAnnotationExpr("Override"))
                    .addParameter(PrimitiveType.longType(), "value")
                    // this.disable = value;
                    .setBody(new BlockStmt().addStatement(
                            new ExpressionStmt(
                                    new AssignExpr(
                                            new FieldAccessExpr(new ThisExpr(), "disable"),
                                            new NameExpr("value"),
                                            AssignExpr.Operator.ASSIGN
                                    )
                            )
                    ));
            klass.addMember(setDisableMethod);
        }

        MethodDeclaration getTerminateInternalMethod = new MethodDeclaration(
                Modifier.createModifierList(Modifier.Keyword.PROTECTED),
                new VoidType(), "terminateInternal")
                .addAnnotation(new MarkerAnnotationExpr("Override"))
                .addParameter(PrimitiveType.intType(), "i");
        klass.addMember(getTerminateInternalMethod);

        BlockStmt seeMethodBody = new BlockStmt();
        MethodDeclaration seeMethod = new MethodDeclaration(
                Modifier.createModifierList(Modifier.Keyword.PRIVATE),
                new VoidType(), "see")
                .addParameter(PrimitiveType.intType(), "eID")
                .setBody(seeMethodBody);

        // this.monitor -= 1;
        seeMethodBody.addStatement(new ExpressionStmt(
                new AssignExpr(
                        new FieldAccessExpr(new NameExpr("node"), "monitors"),
                        new IntegerLiteralExpr("1"),
                        AssignExpr.Operator.MINUS
                )
        ));

        // node = node.getNextNodeAfterSeeingEvent(eID);
        seeMethodBody.addStatement(new ExpressionStmt(
                new AssignExpr(
                        new NameExpr("node"),
                        new MethodCallExpr(new NameExpr("node"), "getNextNodeAfterSeeingEvent",
                                new NodeList<>(new NameExpr("eID"))),
                        AssignExpr.Operator.ASSIGN
                )
        ));

        // this.monitor += 1;
        seeMethodBody.addStatement(new ExpressionStmt(
                new AssignExpr(
                        new FieldAccessExpr(new NameExpr("node"), "monitors"),
                        new IntegerLiteralExpr("1"),
                        AssignExpr.Operator.PLUS
                )
        ));
        klass.addMember(seeMethod);

        for (EventDefinition event : slicerGenUtil.getEvents()) {
            generateEventMethod(klass, event);
        }
    }

    private void generateEventMethod(ClassOrInterfaceDeclaration klass, EventDefinition event) {
        MethodDeclaration method = new MethodDeclaration(
                Modifier.createModifierList(Modifier.Keyword.PUBLIC, Modifier.Keyword.FINAL),
                PrimitiveType.booleanType(), event.getId());
        for (RVMParameter parameter : event.getParameters()) {
            method.addParameter(new Parameter(new ClassOrInterfaceType().setName(parameter.getType().toString()),
                    parameter.getName()));
        }
        method.addParameter(new Parameter(new PrimitiveType(PrimitiveType.Primitive.INT), "event"));

        // this adds condition or code defined in the spec file
        BlockStmt body = StaticJavaParser.parseBlock("{" + event.getAction() + "}");

        body.accept(new SlicerGenerationUtil.RecursiveBlockFlattener(), null);
        body.addStatement(new MethodCallExpr("see", new NameExpr("event")));

        // return true;
        body.addStatement(new ReturnStmt(new BooleanLiteralExpr(true)));
        method.setBody(body);
        klass.addMember(method);
    }

    public void generateConstructor(ClassOrInterfaceDeclaration klass, MonitorFeatures feature) {
        ConstructorDeclaration constructor = new ConstructorDeclaration()
                .setName((slicerGenUtil.getSpecName() + "Monitor"))
                .setModifiers(Modifier.Keyword.PUBLIC);

        if (feature.isTimeTrackingNeeded()) {
            constructor
                    .addParameter(new Parameter(new PrimitiveType(PrimitiveType.Primitive.LONG), "tau"));
        }

        RVMParameters params;
        if (feature.isNonFinalWeakRefsInMonitorNeeded()
                || feature.isFinalWeakRefsInMonitorNeeded()) {
            params = slicerGenUtil.eventHandlerUtil.getRVMonitorSpec().getParameters();
        } else {
            params = feature.getRememberedParameters();
        }

        // handle more parameters (e.g., Map_UnsafeIteratorMonitor)
        for (RVMParameter param : params) {
            constructor.addParameter(new Parameter(new ClassOrInterfaceType(null, "CachedWeakReference"),
                   "RVMRef_" + param.getName()));
        }

        BlockStmt constructorBody = new BlockStmt();
        constructor
                .addParameter(new Parameter(new ClassOrInterfaceType(null, "Trie.Node"), "node"))
                .setBody(constructorBody);

        if (feature.isTimeTrackingNeeded()) {
            constructorBody.addStatement(new AssignExpr(
                    new FieldAccessExpr(new ThisExpr(), "tau"),
                    new NameExpr("tau"),
                    AssignExpr.Operator.ASSIGN
            ));
        }

        for (RVMParameter param : params) {
            constructorBody.addStatement(new AssignExpr(
                    new FieldAccessExpr(new ThisExpr(), "RVMRef_" + param.getName()),
                    new NameExpr("RVMRef_" + param.getName()),
                    AssignExpr.Operator.ASSIGN
            ));
        }

        constructorBody.addStatement(new AssignExpr(
                new FieldAccessExpr(new ThisExpr(), "node"),
                new NameExpr("node"),
                AssignExpr.Operator.ASSIGN
        ));
        constructorBody.addStatement(new AssignExpr(
                new FieldAccessExpr(new FieldAccessExpr(new ThisExpr(), "node"), "monitors"),
                new IntegerLiteralExpr("1"),
                AssignExpr.Operator.PLUS
        ));
        constructorBody.addStatement(new AssignExpr(
                new NameExpr("state"),
                new IntegerLiteralExpr("0"),
                AssignExpr.Operator.ASSIGN
        ));

        klass.addMember(constructor);
    }

    @Override
    public void add() {
        MonitorFeatures feature = slicerGenUtil.eventHandlerUtil.monitors
                .get(slicerGenUtil.eventHandlerUtil.getRVMonitorSpec()).getFeatures();

        ClassOrInterfaceDeclaration bindingClass = code.addClass(getSliceClassName())
                .setPublic(false)
                .addExtendedType("com.runtimeverification.rvmonitor.java.rt.tablebase.AbstractSynchronizedMonitor")
                .addImplementedType("Cloneable")
                .addImplementedType("com.runtimeverification.rvmonitor.java.rt.RVMObject");


        if (feature.isTimeTrackingNeeded()) {
            if (feature.isDisableHolderNeeded()) {
                bindingClass.addImplementedType("I" + slicerGenUtil.getSpecName() + "Monitor");
            } else {
                bindingClass.addImplementedType("com.runtimeverification.rvmonitor.java.rt.tablebase.IDisableHolder");
            }
        }

        // int state;
        bindingClass.addField(PrimitiveType.intType(), "state");

        // Trie.Node node;
        bindingClass.addField("Trie.Node", "node");

        if (feature.isTimeTrackingNeeded()) {
            // private final long tau;
            bindingClass.addField(PrimitiveType.longType(), "tau")
                    .addModifier(Modifier.Keyword.PRIVATE, Modifier.Keyword.FINAL);

            // private final long disable = -1;
            bindingClass.addFieldWithInitializer(PrimitiveType.longType(), "disable",
                    new IntegerLiteralExpr("-1")).addModifier(Modifier.Keyword.PRIVATE);
        }

        // Add user defined fields to class (CSC)
        for (FieldDeclaration field : slicerGenUtil.getFieldsFromString(slicerGenUtil.eventHandlerUtil.getRVMonitorSpec()
                .getDeclarationsStr())) {
            bindingClass.addMember(field);
        }

        // e.g., WeakReference Ref_t = null;
        for (RVMParameter p : slicerGenUtil.eventHandlerUtil.getRVMonitorSpec().getVarsToSave()) {
            bindingClass.addFieldWithInitializer("WeakReference", "Ref_" + p.getName(), new NullLiteralExpr());
        }

        boolean generalcase = feature.isNonFinalWeakRefsInMonitorNeeded() || feature.isFinalWeakRefsInMonitorNeeded();
        RVMParameters needed = feature.getRememberedParameters();
        RVMParameters parameters = slicerGenUtil.eventHandlerUtil.getRVMonitorSpec().getParameters();
        for (RVMParameter param : parameters) {
            if (generalcase || needed.contains(param)) {
                bindingClass.addField("CachedWeakReference", "RVMRef_" + param.getName());
            }
            // else param.getName() was suppressed
        }
        /*
         boolean generalcase = features.isNonFinalWeakRefsInMonitorNeeded()
                    || features.isFinalWeakRefsInMonitorNeeded();
            RVMParameters needed = features.getRememberedParameters();
            for (RVMParameter param : parameters) {
                if (generalcase || needed.contains(param)) {
                    if (!features.isNonFinalWeakRefsInMonitorNeeded())
                        ret += "final ";
                    ret += getRefType(param) + " " + references.get(param)
                            + ";";
                } else
                    ret += "// " + references.get(param)
                    + " was suppressed to reduce memory overhead";
                ret += "\n";
            }
         */

        generateConstructor(bindingClass, feature);
        generateBasicMethods(bindingClass, feature);
    }
}
