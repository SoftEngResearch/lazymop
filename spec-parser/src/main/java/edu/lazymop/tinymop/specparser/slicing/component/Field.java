package edu.lazymop.tinymop.specparser.slicing.component;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.type.PrimitiveType;
import com.runtimeverification.rvmonitor.java.rvj.output.combinedoutputcode.newindexingtree.IndexingDeclNew;
import com.runtimeverification.rvmonitor.java.rvj.output.combinedoutputcode.newindexingtree.IndexingTreeInterface;
import edu.lazymop.tinymop.specparser.monitoring.MonitorGenerationUtil;
import edu.lazymop.tinymop.specparser.slicing.SlicerGenerationUtil;

public class Field extends Component {

    public Field(SlicerGenerationUtil slicerGenUtil, CompilationUnit code, ClassOrInterfaceDeclaration klass) {
        super(slicerGenUtil, code, klass);
    }

    @Override
    public void add() {
        klass.addFieldWithInitializer("Trie", "trie",
                new ObjectCreationExpr().setType("Trie"),
                Modifier.Keyword.PRIVATE, Modifier.Keyword.STATIC);

        klass.addFieldWithInitializer("ReentrantLock", "lock",
                new ObjectCreationExpr().setType("ReentrantLock"),
                Modifier.Keyword.PRIVATE, Modifier.Keyword.STATIC, Modifier.Keyword.FINAL);

        klass.addFieldWithInitializer(PrimitiveType.longType(),  fileName + "_timestamp",
                new IntegerLiteralExpr("1"),
                Modifier.Keyword.PRIVATE, Modifier.Keyword.STATIC);

        klass.addFieldWithInitializer(PrimitiveType.booleanType(),  "activated",
                new BooleanLiteralExpr(false),
                Modifier.Keyword.PRIVATE, Modifier.Keyword.STATIC);

        String specName = slicerGenUtil.getSpecName();
        IndexingDeclNew decl = slicerGenUtil.eventHandlerUtil.indexingTreeManager
                .getIndexingDecl(slicerGenUtil.eventHandlerUtil.getRVMonitorSpec());
        for (FieldDeclaration field : slicerGenUtil.getFieldsFromString(decl.toString())) {
            for (VariableDeclarator variable : field.getVariables()) {
                if (variable.getNameAsString().contains("cachekey") || variable.getNameAsString().contains("cacheval")) {
                    variable.setName(variable.getNameAsString().replace(specName + "_", ""));
                }

                if (variable.getInitializer().isPresent()) {
                    if (variable.getInitializer().get().isObjectCreationExpr()) {
                        ObjectCreationExpr expression = variable.getInitializer().get().asObjectCreationExpr();
                        if (expression.getTypeAsString().equals(slicerGenUtil.getSpecName() + "Monitor")) {
                            expression.addArgument("trie.root");
                        }
                    }
                }
            }

            klass.addMember(field);
        }
    }
}
