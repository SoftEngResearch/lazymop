package edu.lazymop.tinymop.specparser.slicing.component;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import edu.lazymop.tinymop.specparser.slicing.SlicerGenerationUtil;

public abstract class Component {

    protected SlicerGenerationUtil slicerGenUtil;
    protected CompilationUnit code;
    protected ClassOrInterfaceDeclaration klass;
    protected String fileName;

    public Component(SlicerGenerationUtil slicerGenUtil, CompilationUnit code, ClassOrInterfaceDeclaration klass) {
        this.slicerGenUtil = slicerGenUtil;
        this.code = code;
        this.klass = klass;
        this.fileName = slicerGenUtil.getSpecName();
    }

    public abstract void add();

    public String getSliceClassName() {
        return fileName + "Monitor";
    }

    public String getSliceInterfaceClassName() {
        return "I" + fileName + "Monitor";
    }

    public String getSliceSetClassName() {
        return fileName + "Monitor_Set";
    }
}
