package edu.lazymop.tinymop.specparser.slicing.component.handler;

import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.runtimeverification.rvmonitor.java.rvj.output.combinedoutputcode.event.itf.WeakReferenceVariables;
import com.runtimeverification.rvmonitor.java.rvj.output.combinedoutputcode.newindexingtree.IndexingTreeImplementation;
import com.runtimeverification.rvmonitor.java.rvj.output.combinedoutputcode.newindexingtree.IndexingTreeInterface;
import com.runtimeverification.rvmonitor.java.rvj.output.combinedoutputcode.newindexingtree.IndexingTreeQueryResult;
import com.runtimeverification.rvmonitor.java.rvj.parser.ast.rvmspec.RVMParameters;

public class CustomIndexingTreeQueryResult extends IndexingTreeQueryResult {

    private final IndexingTreeImplementation.Access access;

    public CustomIndexingTreeQueryResult(IndexingTreeInterface tree, WeakReferenceVariables weakrefs,
                                         RVMParameters params, IndexingTreeImplementation.Access access, String prefix) {
        super(tree, weakrefs, params, access, prefix);
        this.access = access;
    }

    /**
     * Generates the declaration code for indexing trees and adds it to the given block statement.
     *
     * @param blockStmt the BlockStmt to add the declaration code to
     */
    public void getDeclarationCode(BlockStmt blockStmt) {
        if (getLastMapRef() != null) {
            blockStmt.addStatement(
                    new ExpressionStmt(new VariableDeclarationExpr(new VariableDeclarator(
                            new ClassOrInterfaceType().setName(getLastMapRef().getVariable().getType().getJavaType()),
                            getLastMapRef().getVariable().getName(),
                            new NullLiteralExpr()
                    )))
            );
        }

        blockStmt.addStatement(
                new ExpressionStmt(new VariableDeclarationExpr(new VariableDeclarator(
                        new ClassOrInterfaceType().setName(getEntryRef().getVariable().getType().getJavaType()),
                        getEntryRef().getVariable().getName(),
                        new NullLiteralExpr()
                )))
        );

        if (this.access == IndexingTreeImplementation.Access.Set && getSetRef() != null) {
            blockStmt.addStatement(
                    new ExpressionStmt(new VariableDeclarationExpr(new VariableDeclarator(
                            new ClassOrInterfaceType().setName(getSetRef().getVariable().getType().getJavaType()),
                            getSetRef().getVariable().getName(),
                            new NullLiteralExpr()
                    )))
            );
        }

        if (this.access == IndexingTreeImplementation.Access.Leaf && getLeafRef() != null) {
            blockStmt.addStatement(
                    new ExpressionStmt(new VariableDeclarationExpr(new VariableDeclarator(
                            new ClassOrInterfaceType().setName(getLeafRef().getVariable().getType().getJavaType()),
                            getLeafRef().getVariable().getName(),
                            new NullLiteralExpr()
                    )))
            );
        }
    }
}
