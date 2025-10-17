package edu.lazymop.tinymop.specparser.slicing;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.printer.PrettyPrinter;
import com.github.javaparser.printer.configuration.PrettyPrinterConfiguration;
import com.runtimeverification.rvmonitor.logicrepository.parser.logicrepositorysyntax.LogicRepositoryType;
import edu.lazymop.tinymop.specparser.monitoring.MonitorGenerationUtil;
import edu.lazymop.tinymop.specparser.slicing.component.BindingClass;
import edu.lazymop.tinymop.specparser.slicing.component.Component;
import edu.lazymop.tinymop.specparser.slicing.component.EventHandler;
import edu.lazymop.tinymop.specparser.slicing.component.Field;
import edu.lazymop.tinymop.specparser.slicing.component.Import;
import edu.lazymop.tinymop.specparser.slicing.component.MonitorDisableHolderClass;
import edu.lazymop.tinymop.specparser.slicing.component.MonitorInterfaceClass;
import edu.lazymop.tinymop.specparser.slicing.component.MonitorSetClass;

public class SlicerGenerator {

    private SlicerGenerationUtil slicerGenUtil;
    private LogicRepositoryType monitorData;
    private String fileName;

    public SlicerGenerator(SlicerGenerationUtil slicerGenUtil, LogicRepositoryType monitorData) {
        this.slicerGenUtil = slicerGenUtil;
        this.monitorData = monitorData;
        this.fileName = slicerGenUtil.getSpecName().replace("_", "");
    }

    public String generateSlicerCode() {
        CompilationUnit code = new CompilationUnit();
        code.setPackageDeclaration("edu.lazymop.tinymop.monitoring.slicing.algod");
        ClassOrInterfaceDeclaration klass = code.addClass(fileName);

        Component imports = new Import(slicerGenUtil, code, klass);
        imports.add();

        Component fields = new Field(slicerGenUtil, code, klass);
        fields.add();

        Component handlers = new EventHandler(slicerGenUtil, code, klass);
        handlers.add();

        Component set = new MonitorSetClass(slicerGenUtil, code, klass);
        set.add();

        Component interf = new MonitorInterfaceClass(slicerGenUtil, code, klass);
        interf.add();

        Component holder = new MonitorDisableHolderClass(slicerGenUtil, code, klass);
        holder.add();

        Component binding = new BindingClass(slicerGenUtil, code, klass);
        binding.add();

        final String out = new PrettyPrinter(new PrettyPrinterConfiguration()
                .setEndOfLineCharacter("\n")
                .setMaxEnumConstantsToAlignHorizontally(1)
        ).print(code);
        return out;
    }
}
