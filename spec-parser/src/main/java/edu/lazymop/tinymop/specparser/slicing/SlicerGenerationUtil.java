package edu.lazymop.tinymop.specparser.slicing;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import com.runtimeverification.rvmonitor.java.rvj.output.RVMVariable;
import com.runtimeverification.rvmonitor.java.rvj.parser.ast.ImportDeclaration;
import com.runtimeverification.rvmonitor.java.rvj.parser.ast.RVMSpecFile;
import com.runtimeverification.rvmonitor.java.rvj.parser.ast.rvmspec.EventDefinition;
import com.runtimeverification.rvmonitor.java.rvj.parser.ast.rvmspec.PropertyAndHandlers;
import com.runtimeverification.rvmonitor.java.rvj.parser.ast.rvmspec.RVMonitorSpec;
import com.runtimeverification.rvmonitor.logicrepository.parser.logicrepositorysyntax.LogicRepositoryType;
import com.runtimeverification.rvmonitor.util.RVMException;
import edu.lazymop.tinymop.specparser.monitoring.MonitorGenerationUtil;
import edu.lazymop.tinymop.specparser.slicing.component.EventHandlerUtil;

public class SlicerGenerationUtil extends MonitorGenerationUtil {

    public EventHandlerUtil eventHandlerUtil;
    private RVMSpecFile rvmSpecFile;
    private RVMonitorSpec rvmMonitorSpec;



    public SlicerGenerationUtil(String name, RVMSpecFile rvmSpecFile, LogicRepositoryType monitorData) {
        super(name, monitorData);
        this.rvmSpecFile = rvmSpecFile;

        if (rvmSpecFile.getSpecs().size() != 1) {
            throw new RuntimeException("SlicerGenerationUtil unable to find the right RVMonitorSpec in RVMSpecFile"
                    + "because there are " + rvmSpecFile.getSpecs().size() + " specs in RVMSpecFile");
        }

        this.rvmMonitorSpec = rvmSpecFile.getSpecs().get(0);
        try {
            this.eventHandlerUtil = new EventHandlerUtil(rvmSpecFile);
        } catch (RVMException rvmException) {
            throw new RuntimeException(rvmException);
        }
    }

    public List<EventDefinition> getEvents() {
        return this.rvmMonitorSpec.getEvents();
    }

    public List<ImportDeclaration> getImports() {
        return this.rvmSpecFile.getImports();
    }

    public RVMSpecFile getRvmSpecFile() {
        return rvmSpecFile;
    }

    public List<FieldDeclaration> getFieldsFromString(String inputFieldsString) {
        // TODO: this is a super ugly way to add user defined fields to bindingClass
        return StaticJavaParser.parse("class TMP{" + inputFieldsString + "}")
                .getClassByName("TMP").get().getFields();
    }

    /**
     * A recursive visitor that removes redundant blocks from the given BlockStmt.
     * It traverses the block statement, flattening any nested blocks and removing
     * empty blocks, to simplify the overall structure of the code.
     */
    public static class RecursiveBlockFlattener extends ModifierVisitor<Void> {
        @Override
        public Visitable visit(BlockStmt block, Void arg) {
            super.visit(block, arg);

            NodeList<Statement> newStatements = new NodeList<>();
            for (Statement stmt : block.getStatements()) {
                if (stmt instanceof BlockStmt) {
                    stmt = (BlockStmt) visit((BlockStmt) stmt, arg);
                    newStatements.addAll(((BlockStmt) stmt).getStatements());
                } else {
                    newStatements.add(stmt);
                }
            }

            // Remove empty blocks
            newStatements.removeIf(stmt -> stmt instanceof BlockStmt && ((BlockStmt) stmt).isEmpty());
            block.setStatements(newStatements);
            return block;
        }
    }

    public String getManagerFilename() {
        return specName.replace("_", "") + "MonitorManager.java";
    }

    public String getSlicerFilename() {
        return specName.replace("_", "") + ".java";
    }
}
