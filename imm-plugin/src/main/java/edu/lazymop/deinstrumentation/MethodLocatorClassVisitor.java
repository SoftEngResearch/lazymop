// CHECKSTYLE:OFF
package edu.lazymop.deinstrumentation;

import static org.eclipse.sisu.space.asm.Opcodes.ASM6;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class MethodLocatorClassVisitor extends ClassVisitor {

    public String targetMethodName;
    public int targetMethodLOC = -1;
    public String targetDesc = "";
    public int majorVersion = 0;
    public boolean checkForEachLoop = false;

    public boolean onlyForLoop = true;
    public int iteratorState = 0;

    public Set<String> matchingSignatures = new HashSet<>();
    public Set<String> potentialMatchingSignatures = new HashSet<>();
    public Set<String> allSignatures = new HashSet<>();
    public Map<String, Boolean> signatureToForLoop = new HashMap<>();


    protected MethodLocatorClassVisitor(String targetMethodName, int targetMethodLOC, String targetDesc, boolean checkForEachLoop) {
        super(Opcodes.ASM6);
        this.targetMethodName = targetMethodName;
        this.targetMethodLOC = targetMethodLOC;
        this.targetDesc = targetDesc;

        this.checkForEachLoop = checkForEachLoop;
    }

    @Override
    public void visit(int version, int access, String name, String signature,
                      String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        this.majorVersion = version & 0xFFFF;
//        System.out.println("Bytecode major version: " + majorVersion);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc,
                                     String signature, String[] exceptions) {
        MethodVisitor methodVisitor = super.visitMethod(access, name, desc, signature, exceptions);
        if ((access & Opcodes.ACC_BRIDGE) != 0) {
            return methodVisitor;
        }

        if (name.equals(this.targetMethodName)) {
            allSignatures.add(desc);

            return new MethodVisitor(ASM6) {
                @Override
                public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
                    super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);

                    if (!onlyForLoop || !checkForEachLoop) {
                        return;
                    }

                    if (iteratorState == 0 || iteratorState == 8) {
                        if (opcode == Opcodes.INVOKEINTERFACE || opcode == Opcodes.INVOKEVIRTUAL) {
                            if (name.equals("iterator") && descriptor.equals("()Ljava/util/Iterator;")) {
                                iteratorState = 1;
                            }
                        }
                    } else if (iteratorState == 3) {
                        if (opcode == Opcodes.INVOKEINTERFACE && owner.equals("java/util/Iterator") && name.equals("hasNext") && descriptor.equals("()Z")) {
                            iteratorState = 4;
                        } else {
                            onlyForLoop = false;
                        }
                    } else if (iteratorState == 6) {
                        if (opcode == Opcodes.INVOKEINTERFACE && owner.equals("java/util/Iterator") && name.equals("next") && descriptor.equals("()Ljava/lang/Object;")) {
                            iteratorState = 7;
                        } else {
                            onlyForLoop = false;
                        }
                    } else {
                        onlyForLoop = false;
                    }
                }

                @Override
                public void visitJumpInsn(int opcode, Label label) {
                    super.visitJumpInsn(opcode, label);

                    if (!onlyForLoop || !checkForEachLoop) {
                        return;
                    }

                    if (iteratorState == 4) {
                        if (opcode == Opcodes.IFEQ) {
                            iteratorState = 5;
                        } else {
                            onlyForLoop = false;
                        }
                    } else if (iteratorState != 0 && iteratorState != 8) {
                        onlyForLoop = false;
                    }
                }

                @Override
                public void visitTypeInsn(int opcode, String type) {
                    super.visitTypeInsn(opcode, type);

                    if (!onlyForLoop || !checkForEachLoop) {
                        return;
                    }

                    if (iteratorState == 7) {
                        if (opcode == Opcodes.CHECKCAST) {
                            iteratorState = 8;
                        } else {
                            onlyForLoop = false;
                        }
                    } else if (iteratorState != 0 && iteratorState != 8) {
                        onlyForLoop = false;
                    }
                }

                @Override
                public void visitVarInsn(int opcode, int varIndex) {
                    super.visitVarInsn(opcode, varIndex);

                    if (!onlyForLoop || !checkForEachLoop) {
                        return;
                    }

                    if (iteratorState == 1) {
                        if (opcode == Opcodes.ASTORE) {
                            iteratorState = 2;
                        } else {
                            onlyForLoop = false;
                        }
                    } else if (iteratorState == 2) {
                        if (opcode == Opcodes.ALOAD) {
                            iteratorState = 3;
                        } else {
                            onlyForLoop = false;
                        }
                    } else if (iteratorState == 5) {
                        if (opcode == Opcodes.ALOAD) {
                            iteratorState = 6;
                        } else {
                            onlyForLoop = false;
                        }
                    } else if (iteratorState != 0 && iteratorState != 8) {
                        onlyForLoop = false;
                    }
                }

                @Override
                public void visitEnd() {
                    super.visitEnd();
                    if (checkForEachLoop) {
                        if (onlyForLoop) {
                            if (iteratorState == 8) { // has ForEach loop
                                signatureToForLoop.put(desc, true);
                                System.out.println("FOR EACH LOOP");
                            }
                        } else {
                            // Use iterator outside ForEach loop
                            signatureToForLoop.put(desc, false);
                        }
                    }

                    iteratorState = 0;
                    onlyForLoop = true;
                }

                @Override
                public void visitLineNumber(int line, Label start) {
                    super.visitLineNumber(line, start);
                    if (!targetDesc.isEmpty()) {
                        if (targetDesc.equals(desc)) {
//                            System.out.println("Method " + name + " found target desc: " + desc + " with new line number " + line);
                            matchingSignatures.add(desc);
                        }
                    } else if (line == targetMethodLOC) {
//                        System.out.println("Method " + name + " found target line number " + line + " with desc: " + desc);
                        matchingSignatures.add(desc);
                    }
                }
            };
        }

        return new MethodVisitor(ASM6) {
            @Override
            public void visitLineNumber(int line, Label start) {
                super.visitLineNumber(line, start);
                if (line == targetMethodLOC) {
//                    System.out.println("Method " + name + " found target line number " + line + " with desc: " + desc);
//                    System.err.println("Method " + name + " is not " + targetMethodName + ", but have right line number");
                    potentialMatchingSignatures.add(desc);
                    targetMethodName = name;
                }
            }
        };
    }
}
