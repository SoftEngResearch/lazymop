// CHECKSTYLE:OFF
package edu.lazymop.deinstrumentation;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import static org.objectweb.asm.Opcodes.ASM6;

public class DuplicateIMMInterfaceClassVisitor extends ClassVisitor {

    public String className = "";
    public String IMMFlagName;
    public int targetMethodLOC;

    public DuplicateIMMInterfaceClassVisitor(ClassVisitor classVisitor, String IMMFlagName, int targetMethodLOC) {
        super(ASM6, classVisitor);
        this.IMMFlagName = IMMFlagName;
        this.targetMethodLOC = targetMethodLOC;
    }

    @Override
    public void visit(int version, int access, String name, String signature,
                      String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        className = name;
    }

    @Override
    public void visitEnd() {
        // Source: https://stackoverflow.com/a/14757540
//        System.out.println("INTERFACE: Adding flag after visit");
        cv.visitField(Opcodes.ACC_STATIC, IMMFlagName + "_" + targetMethodLOC, "Z", null, null).visitEnd();

        MethodVisitor mv = cv.visitMethod(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
        if (mv != null) {
            mv.visitCode();

            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitFieldInsn(Opcodes.PUTSTATIC, className, IMMFlagName + "_" + targetMethodLOC, "Z");

            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(1, 0); // Specifies the maximum stack size and number of local variables
            mv.visitEnd();
        }

        super.visitEnd();
    }
}
