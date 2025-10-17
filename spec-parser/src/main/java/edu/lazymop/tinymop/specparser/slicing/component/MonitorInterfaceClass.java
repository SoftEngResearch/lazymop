package edu.lazymop.tinymop.specparser.slicing.component;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import edu.lazymop.tinymop.specparser.slicing.SlicerGenerationUtil;

/**
 * The MonitorInterfaceClass is responsible for generating the monitor interface.
 * This interface extends the IMonitor and IDisableHolder interfaces.
 */
public class MonitorInterfaceClass extends Component {
    public MonitorInterfaceClass(SlicerGenerationUtil slicerGenUtil, CompilationUnit code,
                                 ClassOrInterfaceDeclaration klass) {
        super(slicerGenUtil, code, klass);
    }

    @Override
    public void add() {
        code.addClass("I" + slicerGenUtil.getSpecName() + "Monitor")
                .setPublic(false)
                .setName("I" + slicerGenUtil.getSpecName() + "Monitor")
                .setInterface(true)
                .setExtendedTypes(new NodeList<>(new ClassOrInterfaceType(null, "IMonitor"),
                        new ClassOrInterfaceType(null, "IDisableHolder")));
    }
}
