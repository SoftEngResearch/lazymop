package edu.lazymop.tinymop.specparser.slicing.component;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExplicitConstructorInvocationStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.type.PrimitiveType;
import edu.lazymop.tinymop.specparser.slicing.SlicerGenerationUtil;

/**
 * The MonitorDisableHolderClass class is responsible for generating the disable holder class
 * for the monitor. It extends the DisableHolder class and implements the IMonitor interface.
 */
public class MonitorDisableHolderClass extends Component {
    public MonitorDisableHolderClass(SlicerGenerationUtil slicerGenUtil, CompilationUnit code,
                                     ClassOrInterfaceDeclaration klass) {
        super(slicerGenUtil, code, klass);
    }

    @Override
    public void add() {
        ClassOrInterfaceDeclaration disableHolder = code.addClass(slicerGenUtil.getSpecName() + "DisableHolder")
                .setPublic(false)
                .addImplementedType("I" + slicerGenUtil.getSpecName() + "Monitor")
                .addExtendedType("DisableHolder");

        ConstructorDeclaration constructor = new ConstructorDeclaration()
                .setName(disableHolder.getName())
                .setModifiers(Modifier.Keyword.PUBLIC)
                .addParameter(new Parameter(new PrimitiveType(PrimitiveType.Primitive.LONG), "tau"));

        BlockStmt body = new BlockStmt();
        ExplicitConstructorInvocationStmt superCall = new ExplicitConstructorInvocationStmt().setThis(false)
                .addArgument("tau");
        body.addStatement(superCall);
        constructor.setBody(body);
        disableHolder.addMember(constructor);

        // isTerminated
        MethodDeclaration isTerminatedMethod = new MethodDeclaration(
                Modifier.createModifierList(Modifier.Keyword.PUBLIC, Modifier.Keyword.FINAL),
                PrimitiveType.booleanType(), "isTerminated")
                .addAnnotation(new MarkerAnnotationExpr("Override"))
                .setBody(new BlockStmt().addStatement(new ReturnStmt(new BooleanLiteralExpr(false))));
        disableHolder.addMember(isTerminatedMethod);

        // getLastEvent
        MethodDeclaration getLastEventMethod = new MethodDeclaration(
                Modifier.createModifierList(Modifier.Keyword.PUBLIC),
                PrimitiveType.intType(), "getLastEvent")
                .addAnnotation(new MarkerAnnotationExpr("Override"))
                .setBody(new BlockStmt().addStatement(new ReturnStmt(new IntegerLiteralExpr("-1"))));
        disableHolder.addMember(getLastEventMethod);

        // getState
        MethodDeclaration getStateMethod = new MethodDeclaration(
                Modifier.createModifierList(Modifier.Keyword.PUBLIC),
                PrimitiveType.intType(), "getState")
                .addAnnotation(new MarkerAnnotationExpr("Override"))
                .setBody(new BlockStmt().addStatement(new ReturnStmt(new IntegerLiteralExpr("-1"))));
        disableHolder.addMember(getStateMethod);
    }
}
