package edu.lazymop.tinymop.specparser.slicing.component;

import com.github.javaparser.ast.ArrayCreationLevel;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.ArrayAccessExpr;
import com.github.javaparser.ast.expr.ArrayCreationExpr;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ThisExpr;
import com.github.javaparser.ast.expr.UnaryExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.VoidType;
import com.runtimeverification.rvmonitor.java.rvj.parser.ast.rvmspec.EventDefinition;
import com.runtimeverification.rvmonitor.java.rvj.parser.ast.rvmspec.RVMParameter;
import edu.lazymop.tinymop.specparser.slicing.SlicerGenerationUtil;

/**
 * The MonitorSetClass is responsible for generating the monitor set class.
 * This class extends the AbstractMonitorSet class and provides methods for handling events.
 */
public class MonitorSetClass extends Component {
    public MonitorSetClass(SlicerGenerationUtil slicerGenUtil, CompilationUnit code, ClassOrInterfaceDeclaration klass) {
        super(slicerGenUtil, code, klass);
    }

    public void getHandlerBody(EventDefinition event, MethodDeclaration method) {
        BlockStmt blockStmt = new BlockStmt();

        // int _current_iteration = 0;
        VariableDeclarationExpr loopVar = new VariableDeclarationExpr(
                new VariableDeclarator(new PrimitiveType(PrimitiveType.Primitive.INT), "_current_iteration",
                        new IntegerLiteralExpr("0"))
        );
        // _current_iteration < this.size
        BinaryExpr condition = new BinaryExpr(
                new NameExpr("_current_iteration"),
                new FieldAccessExpr(new ThisExpr(), "size"),
                BinaryExpr.Operator.LESS
        );

        // _current_iteration++;
        UnaryExpr increment = new UnaryExpr(new NameExpr("_current_iteration"), UnaryExpr.Operator.POSTFIX_INCREMENT);
        BlockStmt forBody = new BlockStmt();
        ForStmt forLoop = new ForStmt(new NodeList<>(loopVar), condition, new NodeList<>(increment), forBody);

        NodeList<Expression> arguments = new NodeList<>();
        for (RVMParameter parameter : event.getParameters()) {
            arguments.add(new NameExpr(parameter.getName()));
        }
        arguments.add(new NameExpr("event"));
        MethodCallExpr callSlice = new MethodCallExpr(
                // elements[_current_iteration]
                new ArrayAccessExpr(new NameExpr("elements"), new NameExpr("_current_iteration")),
                event.getId(),
                arguments
        );
        forBody.addStatement(callSlice);
        blockStmt.addStatement(forLoop);
        method.setBody(blockStmt);
    }

    public void addHandler(ClassOrInterfaceDeclaration monitorSetClass, EventDefinition event) {
        MethodDeclaration method = new MethodDeclaration(
                Modifier.createModifierList(Modifier.Keyword.PUBLIC),
                new VoidType(),
                event.getId());
        for (RVMParameter parameter : event.getParameters()) {
            method.addParameter(new Parameter(new ClassOrInterfaceType().setName(parameter.getType().toString()),
                    parameter.getName()));
        }
        method.addParameter(new Parameter(new PrimitiveType(PrimitiveType.Primitive.INT), "event"));

        getHandlerBody(event, method);
        monitorSetClass.addMember(method);
    }

    @Override
    public void add() {
        String className = slicerGenUtil.getSpecName() + "Monitor_Set";
        String monitorClassName = slicerGenUtil.getSpecName() + "Monitor";
        ClassOrInterfaceDeclaration monitorSetClass = code.addClass(className)
                .setPublic(false);

        // Extends
        ClassOrInterfaceType superClass = new ClassOrInterfaceType(null,
                "com.runtimeverification.rvmonitor.java.rt.tablebase.AbstractMonitorSet");
        superClass.setTypeArguments(new ClassOrInterfaceType(null, monitorClassName));
        monitorSetClass.addExtendedType(superClass);

        // Constructor
        BlockStmt constructorBlock = new BlockStmt();
        ConstructorDeclaration constructor = new ConstructorDeclaration()
                .setName(className)
                .setModifiers(Modifier.Keyword.PUBLIC)
                .setBody(constructorBlock);
        monitorSetClass.addMember(constructor);

        // this.size = 0;
        ExpressionStmt sizeAssignment = new ExpressionStmt(
                new AssignExpr(
                        new FieldAccessExpr(new ThisExpr(), "size"),
                        new IntegerLiteralExpr("0"),
                        AssignExpr.Operator.ASSIGN
                )
        );
        constructorBlock.addStatement(sizeAssignment);

        // this.elements = new XXXMonitor[4];
        ArrayCreationExpr arrayCreationExpr = new ArrayCreationExpr(
                new ClassOrInterfaceType(null, monitorClassName),
                new NodeList<>(new ArrayCreationLevel().setDimension(new IntegerLiteralExpr("4"))),
                null);
        ExpressionStmt elementsAssignment = new ExpressionStmt(
                new AssignExpr(
                        new FieldAccessExpr(new ThisExpr(), "elements"),
                        arrayCreationExpr,
                        AssignExpr.Operator.ASSIGN
                )
        );
        constructorBlock.addStatement(elementsAssignment);

        // Event handlers
        for (EventDefinition event : slicerGenUtil.getEvents()) {
            addHandler(monitorSetClass, event);
        }
    }
}
