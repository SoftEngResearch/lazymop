// CHECKSTYLE:OFF
package edu.lazymop.deinstrumentation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import edu.lazymop.FileUtil;
import edu.lazymop.types.IMMData;
import org.apache.maven.plugin.logging.Log;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

public class IMMTransformer {

    public static Map<String, Boolean> methodToForEachLoop = new HashMap<>();
    public static boolean violating = true;
    public static Set<String> excludedSpecs = null;

    public static boolean modifyLibraryJar(String inputFile, String outputFile, String className, String methodName, String jarPath, Log log,
                                           boolean transform) throws IOException {
        try (JarFile jarFile = new JarFile(jarPath)) {
            Enumeration<JarEntry> entries = jarFile.entries();
            JarEntry directEntry = jarFile.getJarEntry(inputFile);
            if (directEntry != null) {
                log.debug("Found file directly " + inputFile + " in " + jarPath);
                Patcher.patchJar(jarPath);
                processBytecode(jarFile.getInputStream(directEntry), className, methodName, inputFile, outputFile, jarPath, log,
                        transform);
                return true;
            }

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String filename = entry.getName();
                if (filename.endsWith(".class")) {
                    if (filename.equals(inputFile)) {
                        log.debug("Found file " + filename + " in " + jarPath);
                        Patcher.patchJar(jarPath);
                        processBytecode(jarFile.getInputStream(entry), className, methodName, inputFile, outputFile, jarPath, log,
                                transform);
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public static void processBytecode(InputStream inputStream, String className, String methodName, String inputFile, String outputFile,
                                        String jarPath, Log log, boolean transform) throws IOException {
        log.debug("Finding all method signatures related to " + methodName);
        ClassReader classReader = new ClassReader(inputStream);

        String[] methodAndLoc = methodName.split(":");
        String targetMethodName = methodAndLoc[0];
        int targetMethodLOC = -1;
        String targetDesc = "";
        if (methodAndLoc.length >= 2) {
            String[] desc = methodAndLoc[1].split("#");
            if (desc.length < 2) {
                targetMethodLOC = Integer.parseInt(methodAndLoc[1]);
            } else {
                targetMethodLOC = Integer.parseInt(desc[0]);
                targetDesc = desc[1];
            }
        }
        boolean checkForEachLoop = !methodToForEachLoop.containsKey(targetMethodName);

        MethodLocatorClassVisitor locatorVisitor = new MethodLocatorClassVisitor(
                targetMethodName, targetMethodLOC, targetDesc, checkForEachLoop
        );
        classReader.accept(locatorVisitor, ClassReader.SKIP_FRAMES);

        Set<String> matchingSignatures = locatorVisitor.matchingSignatures;
        Set<String> potentialMatchingSignatures = locatorVisitor.potentialMatchingSignatures;
        int majorVersion = locatorVisitor.majorVersion;
        Map<String, Boolean> signatureToForLoop = locatorVisitor.signatureToForLoop;

        if (matchingSignatures.isEmpty()) {
            if (potentialMatchingSignatures.isEmpty()) {
                if (locatorVisitor.allSignatures.size() == 1) {
                    matchingSignatures = locatorVisitor.allSignatures;
                    log.warn("Cannot find method with matching line: " + methodName
                            + ", but can find ONE method with desc: " + matchingSignatures);
                } else if (locatorVisitor.allSignatures.isEmpty()) {
                    throw new RuntimeException("Cannot find method with matching line: " + methodName);
                } else {
                    throw new RuntimeException("Cannot find method with matching line: " + methodName
                            + ", there are multiple methods");
                }
            } else {
                log.warn("Cannot find method with matching line: " + methodName
                        + ", but can find method with line " + targetMethodLOC);
                matchingSignatures = potentialMatchingSignatures;
            }
        }

        log.debug("Found signatures " + matchingSignatures + " with location " + targetMethodLOC
                + ", transforming method " + methodName);
        if (checkForEachLoop) {
            if (signatureToForLoop.isEmpty()) {
                // Not using iterator/ForEach loop
                methodToForEachLoop.put(targetMethodName, false);
                log.debug("Method " + targetMethodName + " doesn't use iterator or for-each loop");
            } else {
                boolean hasOnlyForEachLoop = true;
                for (Boolean useForEachLoop : signatureToForLoop.values()) {
                    if (!useForEachLoop) {
                        // at least one method with this name used iterator outside ForEach
                        hasOnlyForEachLoop = false;
                        break;
                    }
                }
                if (hasOnlyForEachLoop) {
                    IMMData.containLoopClasses.add(className);
                }

                methodToForEachLoop.put(targetMethodName, hasOnlyForEachLoop);
                log.debug("Method " + targetMethodName + " uses iterator " + (hasOnlyForEachLoop ? "only" : "but not")
                        + " for for-each loop");
            }
        }

        System.out.println("RESULT: " + methodToForEachLoop.getOrDefault(targetMethodName, false));

        // Cloning the method or add annotation to skip instrumentation
        boolean isInterface = (classReader.getAccess() & Opcodes.ACC_INTERFACE) != 0;
        String interfaceID = "IMM" + methodName.hashCode();
        ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS);
        DuplicateIMMClassVisitor classVisitor = new DuplicateIMMClassVisitor(classWriter, targetMethodName,
                targetMethodLOC, true, !isInterface ? "" : interfaceID, matchingSignatures,
                methodToForEachLoop.getOrDefault(targetMethodName, false), transform);
        classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES);

        // Adding guards to method
        byte[] bytes = classWriter.toByteArray();
        if (transform) {
            ClassReader classReader2 = new ClassReader(bytes);
            ClassWriter classWriter2 = majorVersion < 50 ? new ClassWriter(classReader2, 0) : new ClassWriter(
                    classReader2, ClassWriter.COMPUTE_FRAMES);
            DuplicateIMMClassVisitor classVisitor2 = new DuplicateIMMClassVisitor(classWriter2, targetMethodName,
                    targetMethodLOC, false, !isInterface ? "" : interfaceID, matchingSignatures,
                    methodToForEachLoop.getOrDefault(targetMethodName, false), transform);
            classReader2.accept(classVisitor2, ClassReader.SKIP_FRAMES);
            bytes = classWriter2.toByteArray();

            if (isInterface) {
                // Generate class to store interface variables in method
                // input is an interface, we cannot add flag to the interface, so we add flag to another file

                int classLast = classVisitor.className.lastIndexOf('/');
                ClassWriter classWriter3 = new ClassWriter(0);
                DuplicateIMMInterfaceClassVisitor classVisitor3 = new DuplicateIMMInterfaceClassVisitor(classWriter3,
                        classVisitor.IMMFlagName, targetMethodLOC);
                classVisitor3.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC,
                        classLast < 0 ? interfaceID : (classVisitor.className.substring(0, classLast) + "/" + interfaceID),
                        null, "java/lang/Object", null);
                classVisitor3.visitEnd();

                int inputLast = inputFile.lastIndexOf('/');
                String newInputFile = inputFile.equals("replace") ? inputFile : (inputLast < 0 ? (interfaceID + ".class") :
                        (inputFile.substring(0, inputLast) + "/" + interfaceID + ".class"));

                int outputLast = outputFile.lastIndexOf('/');
                String newOutputFile = outputFile.equals("replace") ? outputFile : (outputLast < 0 ? (interfaceID + ".class") :
                        (outputFile.substring(0, outputLast) + "/" + interfaceID + ".class"));

                // input is newInputFile, output is newOutputFile
                // TODO: need to record this!
                writeToFile(classWriter3.toByteArray(), newInputFile, newOutputFile, jarPath, log);
            }
        }

        writeToFile(bytes, inputFile, outputFile, jarPath, log);
    }

    private static void writeToFile(byte[] bytes, String inputFile, String outputFile, String jarPath, Log log)
            throws IOException {
        if (outputFile.equals("replace")) {
            if (!jarPath.isEmpty()) {
                // Replace file inside jar
                log.debug("Writing file (input is " + inputFile + ", output is in jar)");
                File file = new File("tmp.class");
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(bytes);
                fos.close();

                try (FileSystem fs = FileSystems.newFileSystem(URI.create("jar:file:" + jarPath),
                        new HashMap<String, String>())) {
                    Path newFile = Paths.get("tmp.class");
                    Path oldFile = fs.getPath(inputFile);
                    FileUtil.copy(newFile, oldFile, false);
                }

                file.delete();
                return;
            }

            outputFile = inputFile;
        }
        log.debug("Writing file (input is " + inputFile + ", output is " + outputFile + ")");
        Files.createDirectories(Paths.get(outputFile).getParent());

        File file = new File(outputFile);
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(bytes);
        fos.close();
    }
}
