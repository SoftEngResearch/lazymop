package edu.lazymop.tinymop.specparser.slicing.component;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.runtimeverification.rvmonitor.java.rvj.parser.ast.ImportDeclaration;
import edu.lazymop.tinymop.specparser.monitoring.MonitorGenerationUtil;
import edu.lazymop.tinymop.specparser.slicing.SlicerGenerationUtil;

public class Import extends Component {

    public Import(SlicerGenerationUtil slicerGenUtil, CompilationUnit code, ClassOrInterfaceDeclaration klass) {
        super(slicerGenUtil, code, klass);
    }

    @Override
    public void add() {
        String rvmRT = "com.runtimeverification.rvmonitor.java.rt";
        code.addImport("java.lang.ref.WeakReference");
        code.addImport("java.util.concurrent.locks.ReentrantLock");
        code.addImport("java.util.*");

        code.addImport(rvmRT + ".*");
        code.addImport(rvmRT + ".ref.*");
        code.addImport(rvmRT + ".table.*");
        code.addImport(rvmRT + ".tablebase.AbstractIndexingTree");
        code.addImport(rvmRT + ".tablebase.SetEventDelegator");
        code.addImport(rvmRT + ".tablebase.TableAdopter.Tuple2");
        code.addImport(rvmRT + ".tablebase.TableAdopter.Tuple3");
        code.addImport(rvmRT + ".tablebase.IDisableHolder");
        code.addImport(rvmRT + ".tablebase.IMonitor");
        code.addImport(rvmRT + ".tablebase.DisableHolder");
        code.addImport(rvmRT + ".tablebase.TerminatedMonitorCleaner");

        String monitoring = "edu.lazymop.tinymop.monitoring";
        code.addImport(monitoring + ".MonitorManager");
        code.addImport(monitoring + ".datastructure.Trie");
        code.addImport(monitoring + ".util.SpecializedSlicingAlgorithmUtil");
        code.addImport(monitoring + ".GlobalMonitorManager");

        // Imports in the .mop file
        for (ImportDeclaration imp : slicerGenUtil.getImports()) {
            code.addImport(imp.getName().toString(), imp.isStatic(), imp.isAsterisk());
        }
    }
}
