// CHECKSTYLE:OFF
package edu.lazymop.deinstrumentation;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class DuplicateIMMMethodVisitor extends MethodVisitor {

    private final String className;
    private final String superName;
    private final String targetMethodName;
    private final int targetMethodLOC;
    private final String methodName;
    private final String desc;
    private int access;
    private MethodVisitor newMethodVisitor;
    private boolean firstVisit;
    private boolean visitedSuper = true; // if targetMethodName is <init>, we don't want to clone call to super();
    private boolean insertedGuard = false;

    private boolean CLONE_METHOD = true; // debug flag

    private Map<Label, Label> labelMap = new HashMap<>();
    private int index;
    private String IMMFlagName;
    private String IMMMethodName;
    private String IMMFlagClass; // empty if it is not an interface, else it is a random string to store flags
    private boolean skipLoop = false;


    public DuplicateIMMMethodVisitor(int api, MethodVisitor mv, String className, String superName, String methodName, int targetMethodLOC,
                                     String desc, int access, MethodVisitor newMethodVisitor, boolean firstVisit,
                                     String IMMFlagClass, boolean skipLoop, boolean transform) {
        super(api, mv);
        this.className= className;
        this.superName = superName;
        this.targetMethodName = methodName;
        this.targetMethodLOC = targetMethodLOC;
        this.methodName = methodName.equals("<init>") ? "init" : methodName;
        this.desc = desc;
        this.access = access;
        this.newMethodVisitor = newMethodVisitor;
        this.firstVisit = firstVisit;

        if (targetMethodName.equals("<init>")) {
            visitedSuper = false;
        }

        IMMMethodName = "IMM_" + this.methodName + "_" + className.replace('$', '_').replace('/', '_');
        IMMFlagName = IMMMethodName + "_visited";

        if (IMMFlagClass.isEmpty()) {
            this.IMMFlagClass = IMMFlagClass;
        } else {
            // Keep package, but change className to IMMFlagClass
            int classLast = className.lastIndexOf('/');
            this.IMMFlagClass = classLast < 0 ? IMMFlagClass : (className.substring(0, classLast) + "/" + IMMFlagClass);
        }
        this.skipLoop = skipLoop;
        this.CLONE_METHOD = transform;

//        System.out.println(">> In Method Visitor for " + className + ", " + methodName + desc + ". ID: "
//                + this.targetMethodLOC);
    }

    @Override
    public void visitMultiANewArrayInsn(String descriptor, int numDimensions) {
        super.visitMultiANewArrayInsn(descriptor, numDimensions);
        if (firstVisit && CLONE_METHOD)
            newMethodVisitor.visitMultiANewArrayInsn(descriptor, numDimensions);
    }

    @Override
    public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
        super.visitLookupSwitchInsn(dflt, keys, labels);
        if (firstVisit && CLONE_METHOD) {
            Label[] cloneLabels = new Label[labels.length];
            for (int i = 0; i < labels.length; i++) {
                cloneLabels[i] = getLabel(labels[i]);
            }

            newMethodVisitor.visitLookupSwitchInsn(getLabel(dflt), keys, cloneLabels);
        }
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle,
                                       Object... bootstrapMethodArguments) {
        super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
        if (firstVisit && CLONE_METHOD)
            newMethodVisitor.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
    }

    @Override
    public void visitIntInsn(int opcode, int operand) {
        super.visitIntInsn(opcode, operand);
        if (firstVisit && CLONE_METHOD)
            newMethodVisitor.visitIntInsn(opcode, operand);
    }

    @Override
    public void visitAttribute(Attribute attribute) {
        super.visitAttribute(attribute);
        if (firstVisit && CLONE_METHOD)
            newMethodVisitor.visitAttribute(attribute);
    }

    @Override
    public void visitAnnotableParameterCount(int parameterCount, boolean visible) {
        super.visitAnnotableParameterCount(parameterCount, visible);
        if (firstVisit && CLONE_METHOD)
            newMethodVisitor.visitAnnotableParameterCount(parameterCount, visible);
    }

    @Override
    public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
        super.visitTableSwitchInsn(min, max, dflt, labels);
        if (firstVisit && CLONE_METHOD) {
            Label[] cloneLabels = new Label[labels.length];
            for (int i = 0; i < labels.length; i++) {
                cloneLabels[i] = getLabel(labels[i]);
//                System.out.println("Get label " + labels[i] + " -> " + cloneLabels[i]);
            }
            Label cloneDflt = getLabel(dflt);
//            System.out.println("Get label " + dflt + " -> " + cloneDflt);

            newMethodVisitor.visitTableSwitchInsn(min, max, cloneDflt, cloneLabels);
        }
    }

    @Override
    public void visitParameter(String name, int access) {
        super.visitParameter(name, access);
        if (firstVisit && visitedSuper && CLONE_METHOD)
            newMethodVisitor.visitParameter(name, access);
    }

    @Override
    public void visitTypeInsn(int opcode, String type) {
        super.visitTypeInsn(opcode, type);
//        System.out.println(opcode);
        if (firstVisit && visitedSuper && CLONE_METHOD)
            newMethodVisitor.visitTypeInsn(opcode, type);
    }

    @Override
    public void visitFrame(int type, int numLocal, Object[] local, int numStack, Object[] stack) {
        super.visitFrame(type, numLocal, local, numStack, stack);
        if (firstVisit && CLONE_METHOD)
            newMethodVisitor.visitFrame(type, numLocal, local, numStack, stack);
    }

    @Override
    public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
        super.visitTryCatchBlock(start, end, handler, type);
        if (firstVisit && CLONE_METHOD)
            newMethodVisitor.visitTryCatchBlock(getLabel(start), getLabel(end), getLabel(handler), type);
    }

    @Override
    public void visitCode() {
        if (!firstVisit) {
            if (!targetMethodName.equals("<init>")) {
                insertFlagGuard();
            }
            super.visitCode();
        } else {
            if (skipLoop) {
                AnnotationVisitor av = super.visitAnnotation("Lmop/NoMonitoringLoop;", true);
                av.visitEnd();
                if (!IMMTransformer.violating) {
                    System.out.println("Injecting NoMonitoringSpec");
                    av = super.visitAnnotation("Lmop/NoMonitoringSpec;", true);
                    av.visitEnd();
                }
            }

            super.visitCode();
            if (CLONE_METHOD) {
                if (skipLoop) {
                    AnnotationVisitor av = newMethodVisitor.visitAnnotation("Lmop/NoMonitoringLoop;", true);
                    av.visitEnd();
                    if (!IMMTransformer.violating) {
                        System.out.println("Injecting NoMonitoringSpec in clone");
                        av = newMethodVisitor.visitAnnotation("Lmop/NoMonitoringSpec;", true);
                        av.visitEnd();
                    }
                }

                for (String specToExclude : IMMTransformer.excludedSpecs) {
                    AnnotationVisitor av = newMethodVisitor.visitAnnotation("Lmop/DoMonitoring" + specToExclude + ";",
                            true);
                    av.visitEnd();
                }

                newMethodVisitor.visitCode();
            }
        }
    }

    public void insertFlagGuard() {
        if (insertedGuard)
            // We should only insert this guard once, at the top of a regular method, or after super() if targetMethodName is a constructor
            return;
        insertedGuard = true;

//        System.out.println("Inserting guard...");
        mv.visitFieldInsn(Opcodes.GETSTATIC, IMMFlagClass.isEmpty() ? className : IMMFlagClass, IMMFlagName + "_"
                + this.targetMethodLOC, "Z");
        Label labelContinue = new Label();
        mv.visitJumpInsn(Opcodes.IFEQ, labelContinue);

        boolean isStatic = (Opcodes.ACC_STATIC & this.access) != 0;
        boolean isInterface = !IMMFlagClass.isEmpty();
        int localIndex = isStatic ? 0 : 1; // for non-static, 0 is 'this'

        Type[] argumentTypes = Type.getArgumentTypes(desc);
        if (!isStatic) {
            mv.visitVarInsn(Opcodes.ALOAD, 0);
        }

        for (Type type : argumentTypes) {
            switch (type.getSort()) {
                case Type.BOOLEAN:
                case Type.BYTE:
                case Type.CHAR:
                case Type.SHORT:
                case Type.INT:
                    mv.visitVarInsn(Opcodes.ILOAD, localIndex);
                    break;
                case Type.FLOAT:
                    mv.visitVarInsn(Opcodes.FLOAD, localIndex);
                    break;
                case Type.LONG:
                    mv.visitVarInsn(Opcodes.LLOAD, localIndex);
                    localIndex++;
                    break;
                case Type.DOUBLE:
                    mv.visitVarInsn(Opcodes.DLOAD, localIndex);
                    localIndex++;
                    break;
                case Type.OBJECT:
                case Type.ARRAY:
                    mv.visitVarInsn(Opcodes.ALOAD, localIndex);
                    break;
            }
            localIndex++;
        }

        if (isStatic) {
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, className, IMMMethodName + "_" + this.targetMethodLOC, desc, false);
        } else if (isInterface) {
            mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, className, IMMMethodName + "_" + this.targetMethodLOC, desc,
                    true);
        } else {
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, className, IMMMethodName + "_" + this.targetMethodLOC, desc, false);
        }
//        mv.visitInsn(Opcodes.POP);
        Type returnType = Type.getReturnType(desc);
        switch (returnType.getSort()) {
            case Type.VOID:
                mv.visitInsn(Opcodes.RETURN);
                break;
            case Type.BOOLEAN:
            case Type.BYTE:
            case Type.CHAR:
            case Type.SHORT:
            case Type.INT:
                mv.visitInsn(Opcodes.IRETURN);
                break;
            case Type.FLOAT:
                mv.visitInsn(Opcodes.FRETURN);
                break;
            case Type.LONG:
                mv.visitInsn(Opcodes.LRETURN);
                break;
            case Type.DOUBLE:
                mv.visitInsn(Opcodes.DRETURN);
                break;
            case Type.OBJECT:
            case Type.ARRAY:
                mv.visitInsn(Opcodes.ARETURN);
                break;
        }
//            mv.visitInsn(Opcodes.ARETURN);
        mv.visitLabel(labelContinue);
    }

    @Override
    public void visitLineNumber(int line, Label start) {
        super.visitLineNumber(line, start);
//        if (visitedSuper && CLONE_METHOD)
//            super.visitLineNumber(line, start);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
        if (!firstVisit) {
            if (targetMethodName.equals("<init>") && opcode == Opcodes.INVOKESPECIAL) {
                if (owner.equals(superName) || owner.equals(className)) {
                    insertFlagGuard();
                }
            }
        } else {
            // start cloning after invokespecial
            if (targetMethodName.equals("<init>") && opcode == Opcodes.INVOKESPECIAL && !visitedSuper) {
                if (owner.equals(superName) || owner.equals(className)) {
                    visitedSuper = true;
                }
            } else if (visitedSuper && CLONE_METHOD) {
                newMethodVisitor.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
            }
        }
    }

    @Override
    public void visitInsn(int opcode) {
        // Source: https://stackoverflow.com/a/71030033

        if (!firstVisit) {
            if ((opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN) || opcode == Opcodes.ATHROW) {
//            System.out.println("> " + opcode);
//                mv.visitInsn(Opcodes.DUP);
                mv.visitInsn(Opcodes.ICONST_1);
                mv.visitFieldInsn(Opcodes.PUTSTATIC, IMMFlagClass.isEmpty() ? className : IMMFlagClass, IMMFlagName + "_"
                        + this.targetMethodLOC, "Z");
            }

            super.visitInsn(opcode);
        } else {
            super.visitInsn(opcode);
            if (CLONE_METHOD && visitedSuper)
                newMethodVisitor.visitInsn(opcode);
        }
    }

    @Override
    public void visitVarInsn(int opcode, int var) {
        super.visitVarInsn(opcode, var);
        if (firstVisit && visitedSuper && CLONE_METHOD)
            newMethodVisitor.visitVarInsn(opcode, var);
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
        super.visitFieldInsn(opcode, owner, name, descriptor);
        if (firstVisit && visitedSuper && CLONE_METHOD)
            newMethodVisitor.visitFieldInsn(opcode, owner, name, descriptor);
    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {
        super.visitJumpInsn(opcode, label);
        if (firstVisit && CLONE_METHOD)
            newMethodVisitor.visitJumpInsn(opcode, getLabel(label));
    }

    @Override
    public void visitLabel(Label label) {
        super.visitLabel(label);
//        System.out.println(label);
        if (firstVisit && CLONE_METHOD) {
            newMethodVisitor.visitLabel(getLabel(label));
        }
    }

    @Override
    public void visitLdcInsn(Object value) {
        super.visitLdcInsn(value);
        if (firstVisit && CLONE_METHOD)
            newMethodVisitor.visitLdcInsn(value);
    }

    @Override
    public void visitIincInsn(int var, int increment) {
        super.visitIincInsn(var, increment);
        if (firstVisit && CLONE_METHOD)
            newMethodVisitor.visitIincInsn(var, increment);
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        super.visitMaxs(maxStack + 1, maxLocals);
        if (firstVisit && CLONE_METHOD)
            newMethodVisitor.visitMaxs(maxStack + 1, maxLocals);
    }

    @Override
    public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {
        super.visitLocalVariable(name, descriptor, signature, start, end, index);
//        newMethodVisitor.visitLocalVariable(name, descriptor, signature, start, end, index);
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
        if (firstVisit && CLONE_METHOD)
            newMethodVisitor.visitEnd();

    }

    public Label getLabel(Label originalLabel) {
        if (labelMap.containsKey(originalLabel)) {
//            System.out.println("Getting existing label " + originalLabel + " -> " + labelMap.get(originalLabel));
            return labelMap.get(originalLabel);
        }

        Label newLabel = new Label();
        labelMap.put(originalLabel, newLabel);
//        System.out.println("Creating new label " + originalLabel + " -> " + newLabel);
        return newLabel;
    }
}
