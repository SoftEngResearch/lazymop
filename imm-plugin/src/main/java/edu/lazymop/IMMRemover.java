package edu.lazymop;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.lazymop.deinstrumentation.IMMTransformer;
import edu.lazymop.deinstrumentation.Utils;
import edu.lazymop.types.IMMData;
import org.apache.maven.model.Build;
import org.apache.maven.plugin.logging.Log;

// For single version or for incremental IMM, after running TinyMOP
public class IMMRemover {

    public static String buildPath = "";
    public static String classesPath = "";
    public static String testClassesPath = "";

    private static Set<String> transformed = new HashSet<>();

    public static void removeIMMs(boolean disableMixedIMM, File agentPath, List<String> classpath, boolean replace,
                                  boolean disableLoopRemover, boolean disableInitRemover, Build build, Log log) {
        classesPath = build.getOutputDirectory();
        testClassesPath = build.getTestOutputDirectory();

        Map<String, List<String>> specsExcludedMethods = new HashMap<>();  // map spec to methods to exclude for that spec
        for (Map.Entry<String, Map<String, Map<String, Integer>>> entry : IMMData.statistics.entrySet()) {
            String method = entry.getKey();
            if (method.contains("<clinit>") || method.contains("IMM_") || method.startsWith("ASMAccessorImpl")
                    || method.endsWith(":0")) {
                log.warn("Skip " + method + " because we cannot de-instrument this method.");
                continue;
            }
            if (disableInitRemover){
                if (method.contains("<init>")){
                    log.warn("Skip " + method + " because disableInitRemover is true so we cannot " +
                            "de-instrument this method.");
                    continue;
                }
            }

            boolean notSupportSpecExclusion = method.contains("<init>"); // no pointcut to exclude init
            Map<String, Map<String, Integer>> specsToStatus = entry.getValue();

            boolean isIMM = false;
            boolean shouldTransform = false;
            boolean needExclude = false;
            boolean isViolating = false;
            Set<String> excludedSpecs = new HashSet<>();
            for (Map.Entry<String, Map<String, Integer>> innerEntry : specsToStatus.entrySet()) {
                // isLoopExcludeSpec is true if spec is a non-iterator related spec that support loop de-instrumentation.
                String spec = innerEntry.getKey();
                boolean isLoopExcludeSpec = spec.endsWith("UnsafeIterator");
                if (isLoopExcludeSpec && innerEntry.getValue().get("isMethodViolating") > 0) {
                    isViolating = true;
                }

                if (
                        innerEntry.getValue().get("totalNumberOfNonIsolatedTraces") > 0 ||  // contains non-isolated traces
                        innerEntry.getValue().get("totalNumberOfIsolatedTraces") == 1   // has single trace
                ) {
                    // not IMM
                    if (!notSupportSpecExclusion && !disableMixedIMM) {
                        specsExcludedMethods.computeIfAbsent(spec, k -> new ArrayList<>()).add(method);
                        excludedSpecs.add(spec);
                    }
                    if (isLoopExcludeSpec) {
                        // Since this spec contains non-isolated traces, we cannot add @NoMonitoringSpec
                        log.debug("Spec " + spec + " is not an IMM. Will not add @NoMonitoringSpec.");
                        isViolating = true;
                    } else {
                        log.debug("Spec " + spec + " is not an IMM.");
                    }
                    needExclude = true;
                } else if (innerEntry.getValue().get("totalNumberOfUniqueIsolatedTraces") > 1) {
                    // This is a potential IMM. We need this block because we need to set isIMM to true.
                    // contains multiple unique traces, we exclude them, but @NoMonitoring will suppress this exclusion.
                    isIMM = true;

                    // not IMM
                    if (!notSupportSpecExclusion && !disableMixedIMM) {
                        specsExcludedMethods.computeIfAbsent(spec, k -> new ArrayList<>()).add(method);
                        excludedSpecs.add(spec);
                    }
                    needExclude = true;
                    log.debug("Spec " + spec + " contains multiple unique traces. We will exclude them unless " +
                            "@NoMonitoring is present");
                } else {
                    isIMM = true;
                    shouldTransform = true; // at least one spec considers this method as an IMM, so we transform
                }
            }

            if (needExclude) {
                if (notSupportSpecExclusion || disableMixedIMM) {
                    log.warn("Cannot partially exclude method " + method + ", so it cannot be an IMM.");
                    isIMM = false;
                }
            }

            if (!isIMM) {
                log.warn("Skip " + method + " because it is not an IMM.");
                continue;
            }

            // De-instrument `method`
            if (shouldTransform) {
                // At least one spec thinks this is an IMM
                log.info("De-instrumenting " + method);
            } else {
                log.info("De-instrumenting " + method + " for for-loop related specs");
            }

            IMMTransformer.violating = isViolating;
            IMMTransformer.excludedSpecs = excludedSpecs;
            if (isViolating) {
                log.info("Will not de-instrument certain spec because this method contains violation or " +
                        "certain specs are non-isolated.");
            }

            try {
                transformMethod(method, classpath, replace, log, shouldTransform);
            } catch (Exception exception) {
                log.error("Unable to de-instrument " + method);
                log.error(exception);
            }
        }
        if (replace) {
            // Replace is true means it is before running test
            excludeMethods(specsExcludedMethods, agentPath, build, disableLoopRemover);
        } else {
            log.debug("Copying original bytecode classes and test-classes");
            // Copy original bytecode file to artifact, we need this to (1) restore file and (2) check if class changed
            FileUtil.copyDirectory(Paths.get(classesPath),
                    Paths.get(FileUtil.getOriginalBytecodeProject() + File.separator + "classes"));
            FileUtil.copyDirectory(Paths.get(testClassesPath),
                    Paths.get(FileUtil.getOriginalBytecodeProject() + File.separator + "test-classes"));
        }
    }

    private static void transformMethod(String method, List<String> classpath, boolean replace, Log log, boolean transform)
            throws IOException {
        // if replace is true, we copy modified bytecode to both target and artifact
        // otherwise, we only copy it to artifact
        int lastDot = method.lastIndexOf('.');
        String classType = method.substring(0, lastDot);
        String className = classType.replace(".", "/") + ".class";
        String methodName = method.substring(lastDot + 1);
        // Check if method is in classes
        Path classFile = Paths.get(classesPath, className);
        if (Files.exists(classFile)) {
            log.debug("Found file " + classFile);
            // Copy original bytecode file to artifact, we need this to (1) restore file and (2) check if class changed
//            FileUtil.copy(classFile, Paths.get(FileUtil.getOriginalBytecodeProject() + File.separator + "classes"
//                    + File.separator + className), true);

            Path destPath = Paths.get(FileUtil.getTransformedBytecodeProject() + File.separator + "classes"
                    + File.separator + className);
            if (!replace && transformed.contains(className)) {
                processBytecode(false, classType, methodName, Files.exists(destPath) ? destPath : classFile, destPath, log, transform);
            }  else {
                processBytecode(replace, classType, methodName, classFile, destPath, log, transform);
                transformed.add(className);
            }

            if (transform) {
                IMMData.deIMMClasses.add(classType);
            }
            return;
        }

        // Check if method is in test-classes
        Path testClassFile = Paths.get(testClassesPath, className);
        if (Files.exists(testClassFile)) {
            log.debug("Found test file " + testClassFile);
            // Copy original bytecode file to artifact, we need this to (1) restore file and (2) check if class changed
//            FileUtil.copy(testClassFile, Paths.get(FileUtil.getOriginalBytecodeProject() + File.separator + "test-classes"
//                    + File.separator + className), true);

            Path destPath = Paths.get(FileUtil.getTransformedBytecodeProject() + File.separator + "test-classes"
                    + File.separator + className);
            if (!replace && transformed.contains(className)) {
                processBytecode(false, classType, methodName, Files.exists(destPath) ? destPath : testClassFile, destPath, log,
                        transform);
            } else {
                processBytecode(replace, classType, methodName, testClassFile, destPath, log, transform);
                transformed.add(className);
            }

            if (transform) {
                IMMData.deIMMClasses.add(classType);
            }
            return;
        }

        // Check if method is in jar
        log.debug(method + " is probably in a library, searching " + className + " in jars");
        String previousJar = IMMData.classToJar.get(className);
        if (previousJar != null && classpath.contains(previousJar)) {
            Path previousJarPath = Paths.get(previousJar);
            Path destPath = Paths.get(FileUtil.getTransformedBytecodeLibrary() + File.separator
                    + previousJarPath.getFileName() + File.separator + className);
            if (!replace && transformed.contains(className) && Files.exists(destPath)) {
                // Partially transformed
                log.debug("Found PREVIOUS jar file " + previousJar + " with class name " + className);
                IMMTransformer.processBytecode(Files.newInputStream(destPath), classType, methodName, destPath.toString(),
                        "replace", "", log, transform);

                if (transform) {
                    IMMData.deIMMClasses.add(classType);
                }
                return;
            } else if (IMMTransformer.modifyLibraryJar(className, replace ? "replace" : destPath.toString(), classType,
                    methodName, previousJar, log, transform)) {
                // Transforming first time
                log.debug("Found PREVIOUS jar file " + previousJar + " with class name " + className);

                // Copy original jar file to artifact, we need this to (1) restore file and (2) check if class changed
                FileUtil.copy(previousJarPath, Paths.get(FileUtil.getOriginalBytecodeLibrary() + File.separator
                        + previousJarPath.getFileName()), false);
                transformed.add(className);

                if (transform) {
                    IMMData.deIMMClasses.add(classType);
                }
                return;
            }
        }

        // Unable to find jar using classToJar, need to search jar one by one.
        for (String potentialJar : classpath) {
            if (!potentialJar.endsWith(".jar")) {
                continue;
            }
            Path potentialJarPath = Paths.get(potentialJar);

            String dest = FileUtil.getTransformedBytecodeLibrary() + File.separator
                    + potentialJarPath.getFileName() + File.separator + className;
            if (IMMTransformer.modifyLibraryJar(className, replace ? "replace" : dest, classType, methodName, potentialJar,
                    log, transform)) {
                log.debug("Found jar file " + potentialJar + " with class name " + className);
                IMMData.classToJar.put(className, potentialJar);

                // Copy original jar file to artifact, we need this to (1) restore file and (2) check if class changed
                FileUtil.copy(potentialJarPath, Paths.get(FileUtil.getOriginalBytecodeLibrary() + File.separator
                        + potentialJarPath.getFileName()), false);
                transformed.add(className);

                if (transform) {
                    IMMData.deIMMClasses.add(classType);
                }
                return;
            }
        }
    }

    private static void processBytecode(boolean replace, String className, String methodName, Path classFile, Path destPath, Log log,
                                        boolean transform) throws IOException {
        if (replace) {
            // Replace target, also save to artifact dir
            IMMTransformer.processBytecode(Files.newInputStream(classFile), className, methodName, classFile.toString(),
                    "replace", "", log, transform);
            FileUtil.copy(classFile, destPath, true);
        } else {
            // Save directly to artifact dir
            IMMTransformer.processBytecode(Files.newInputStream(classFile), className, methodName, classFile.toString(),
                    destPath.toString(), "", log, transform);
        }
    }

    public static void excludeMethods(Map<String, List<String>> specsExcludedMethods, File agentPath, Build build, boolean disableLoopRemover) {
        String buildPath = build.getDirectory();

        Set<String> iteratorRelatedSpecs = new HashSet<>(Arrays.asList("IteratorHasNext", "IteratorRemoveOnce",
                "ListIteratorRemoveOnce", "ListIteratorSet", "ListIteratorhasNextPrevious"));
        Set<String> nonIteratorRelatedSpecs = new HashSet<>(Arrays.asList("CollectionUnsafeIterator",
                "MapUnsafeIterator", "ArrayDequeUnsafeIterator", "NavigableMapUnsafeIterator"));
        if (disableLoopRemover) {
            // Don't de-instrument spec related methods
            iteratorRelatedSpecs = new HashSet<>();
            nonIteratorRelatedSpecs = new HashSet<>();
        }

        try {
            Files.createDirectories(Paths.get(buildPath, "mop"));
            Set<String> specs = new HashSet<>();

            List<String> allAJFiles = new ArrayList<>();
            for (Map.Entry<String, List<String>> entry : specsExcludedMethods.entrySet()) {
                String specName = entry.getKey();
                String ajFile = buildPath + File.separator + "mop"  + File.separator + specName + "BaseAspect.aj";
                PrintWriter writer = new PrintWriter(ajFile, "UTF-8");
                if (!IMMData.containLoopClasses.isEmpty() && iteratorRelatedSpecs.contains(specName)) {
                    writer.print(Utils.getSpecFile(specName, entry.getValue(), true, false));
                    iteratorRelatedSpecs.remove(specName);
                } else if (!IMMData.containLoopClasses.isEmpty() && nonIteratorRelatedSpecs.contains(specName)) {
                    writer.print(Utils.getSpecFile(specName, entry.getValue(), false, true));
                    nonIteratorRelatedSpecs.remove(specName);
                } else {
                    writer.print(Utils.getSpecFile(specName, entry.getValue(), false, false));
                }
                writer.close();

                allAJFiles.add(ajFile);
                specs.add(specName);
            }

            if (!IMMData.containLoopClasses.isEmpty()) {
                for (String specName : iteratorRelatedSpecs) {
                    String ajFile = buildPath + File.separator + "mop" + File.separator + specName + "BaseAspect.aj";
                    PrintWriter writer = new PrintWriter(ajFile, "UTF-8");
                    writer.print(Utils.getSpecFile(specName, null, true, false));
                    writer.close();
                    allAJFiles.add(ajFile);
                    specs.add(specName);
                }

                for (String specName : nonIteratorRelatedSpecs) {
                    String ajFile = buildPath + File.separator + "mop" + File.separator + specName + "BaseAspect.aj";
                    PrintWriter writer = new PrintWriter(ajFile, "UTF-8");
                    writer.print(Utils.getSpecFile(specName, null, false, true));
                    writer.close();
                    allAJFiles.add(ajFile);
                    specs.add(specName);
                }
            }

            if (!Utils.compileBaseAspect(allAJFiles)) {
                throw new RuntimeException("Unable to compile BaseAspect");
            }

            if (!Utils.updateBaseAspectInJar(agentPath, buildPath + File.separator + "mop",
                    specs)) {
                throw new RuntimeException("Unable to update agent");
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
            throw new RuntimeException(ioe);
        }
    }
}
