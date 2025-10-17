package edu.lazymop.tinymop.specparser.slicing.component.handler;

import java.util.Map;

import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.runtimeverification.rvmonitor.java.rvj.output.codedom.helper.CodeVariable;
import com.runtimeverification.rvmonitor.java.rvj.output.combinedoutputcode.event.itf.WeakReferenceVariables;
import com.runtimeverification.rvmonitor.java.rvj.output.combinedoutputcode.indexingtree.IndexingTreeManager;
import com.runtimeverification.rvmonitor.java.rvj.parser.ast.rvmspec.RVMParameter;
import com.runtimeverification.rvmonitor.java.rvj.parser.ast.rvmspec.RVMParameters;

public class CustomWeakReferenceVariables extends WeakReferenceVariables {
    public CustomWeakReferenceVariables(IndexingTreeManager trees, RVMParameters params) {
        super(trees, params);
    }

    /**
     * Generates the declaration code for weak references and adds it to the given block statement.
     *
     * @param blockStmt the BlockStmt to add the declaration code to
     */
    public void getDeclarationCode(BlockStmt blockStmt) {
        for (Map.Entry<RVMParameter, CodeVariable> entry : getMapping().entrySet()) {
            blockStmt.addStatement(new ExpressionStmt(new VariableDeclarationExpr(new VariableDeclarator(
                    new ClassOrInterfaceType().setName(entry.getValue().getType().getJavaType()),
                    entry.getValue().getName(), new NullLiteralExpr()))));
        }
    }
}
