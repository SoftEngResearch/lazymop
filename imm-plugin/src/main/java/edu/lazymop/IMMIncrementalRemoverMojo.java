package edu.lazymop;

import static org.twdata.maven.mojoexecutor.MojoExecutor.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.lazymop.types.IMMData;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.classworlds.realm.ClassRealm;

// For incremental IMM, before running TinyMOP
@Mojo(name = "incremental-remove", requiresDirectInvocation = true, requiresDependencyResolution = ResolutionScope.TEST)
public class IMMIncrementalRemoverMojo extends AbstractMojo {

    private static Set<String> changedClasses;
    private static Set<String> changedTestClasses;
    private static Set<String> changedJarClasses;


    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Parameter(property = "agentPath")
    private File agentPath;

    @Parameter(property = "disableMixed")
    private boolean disableMixedIMM;

    @Parameter(property = "eMOP")
    private boolean eMOP;

    @Parameter(property = "cleanBytecode")
    private boolean cleanBytecode;

    @Parameter(property = "disableLoopRemover")
    private boolean disableLoopRemover;

    @Parameter(property = "disableInitRemover")
    private boolean disableInitRemover;

    @Parameter(defaultValue = "${session}", readonly = true)
    private MavenSession session;

    @Component
    private PluginDescriptor descriptor;

    @Component
    private BuildPluginManager manager;

    private List<String> classpath;

    // No need to transform method if we already transformed the class
    private final Map<String, Boolean> transformedClasses = new HashMap<>();

    public void execute() throws MojoExecutionException {
        long start = System.currentTimeMillis();
        if (agentPath == null || !agentPath.isFile()) {
            throw new MojoExecutionException("agentPath: path to agent file is missing or is invalid");
        }

        FileUtil.setArtifactDir(project.getBasedir().getAbsolutePath());
        IMMData.readStatisticsFromFile();
        IMMData.readClassToJarToFile();

        Set<String> newClasspath = new HashSet<>();

        try {
            classpath = project.getTestClasspathElements();

            ClassRealm realm = descriptor.getClassRealm();

            for (String path : classpath) {
                getLog().debug("Injecting path " + path);
                File elementFile = new File(path);
                realm.addURL(elementFile.toURI().toURL());
                if (path.endsWith(".jar")) {
                    newClasspath.add(path);
                }
            }
        } catch (DependencyResolutionRequiredException | MalformedURLException exception) {
            throw new RuntimeException(exception);
        }
        Set<String> oldClasspath = IMMData.getPreviousClasspathAndUpdate(newClasspath);

        runEMOP();

        if (IMMData.statistics == null) {
            getLog().info("First time running... Will not perform de-instrumentation.");
            IMMData.testStartTime = System.currentTimeMillis();
            return;
        } else if (oldClasspath == null) {
            throw new RuntimeException("Unable to read old classpath");
        }

        getChangedFiles(newClasspath, oldClasspath);

        if (IMMData.changedClasses.isEmpty()) {
            getLog().warn("No changed classes. Skip running test.");
            System.exit(0);
        }

        Map<String, List<String>> specsExcludedMethods = new HashMap<>();  // map spec to methods to exclude for that spec
        for (Map.Entry<String, Map<String, Map<String, Integer>>> entry : IMMData.statistics.entrySet()) {
            String method = entry.getKey();
            if (method.contains("<clinit>") || method.contains("IMM_") || method.startsWith("ASMAccessorImpl")
                    || method.endsWith(":0")) {
                getLog().warn("Skip " + method + " because we cannot de-instrument this method.");
                continue;
            }
            if (disableInitRemover) {
                if (method.contains("<init>")){
                    getLog().warn("Skip " + method + " because disableInitRemover is true so we cannot " +
                            "de-instrument this method.");
                    continue;
                }
            }

            boolean notSupportSpecExclusion = method.contains("<init>");
            Map<String, Map<String, Integer>> specsToStatus = entry.getValue();
            List<String> specToExcludeThisMethod = new ArrayList<>();

            boolean isIMM = false;
            boolean shouldTransform = false;
            boolean needExclude = false;
            for (Map.Entry<String, Map<String, Integer>> innerEntry : specsToStatus.entrySet()) {
                String spec = innerEntry.getKey();

                if (
                        innerEntry.getValue().get("totalNumberOfNonIsolatedTraces") > 0 ||  // contains non-isolated traces
                        innerEntry.getValue().get("totalNumberOfIsolatedTraces") == 1   // has single trace
                ) {
                    // not IMM
                    if (!notSupportSpecExclusion && !disableMixedIMM) {
                        specToExcludeThisMethod.add(spec);
                    }
                    needExclude = true;
                    getLog().debug("Spec " + spec + " is not an IMM.");
                } else if (innerEntry.getValue().get("totalNumberOfUniqueIsolatedTraces") > 1) {
                    // This is a potential IMM. We need this block because we need to set isIMM to true.
                    // contains multiple unique traces, we exclude them, but @NoMonitoring will suppress this exclusion.
                    isIMM = true;

                    if (!notSupportSpecExclusion && !disableMixedIMM) {
                        specToExcludeThisMethod.add(spec);
                    }
                    needExclude = true;
                    getLog().debug("Spec " + spec + " contains multiple unique traces. We will exclude them unless " +
                            "@NoMonitoring is present");
                } else {
                    isIMM = true;
                    shouldTransform = true; // at least one spec considers this method as an IMM, so we transform
                }
            }

            if (needExclude) {
                if (notSupportSpecExclusion || disableMixedIMM) {
                    getLog().warn("Cannot partially exclude method " + method + ", so it cannot be an IMM.");
                    isIMM = false;
                }
            }

            if (!isIMM) {
                getLog().warn("Skip " + method + " because it is not an IMM.");
                continue;
            }

            // De-instrument `method`
            if (shouldTransform) {
                getLog().info("De-instrumenting " + method);
            } else {
                getLog().info("De-instrumenting " + method + " for for-loop related specs");
            }
            try {
                if (transformMethod(method)) {
                    // Only modify spec's BaseAspect to exclude method if we transformed the class.
                    for (String spec : specToExcludeThisMethod) {
                        specsExcludedMethods.computeIfAbsent(spec, k -> new ArrayList<>()).add(method);
                    }
                }
            } catch (IOException exception) {
                getLog().error("Unable to de-instrument " + method);
                getLog().error(exception);
            }
        }


        IMMRemover.excludeMethods(specsExcludedMethods, agentPath, project.getBuild(), disableLoopRemover);
        getLog().info("Need to modify specs: " + specsExcludedMethods);

        long end = System.currentTimeMillis();
        IMMData.timeToRunRemover = end - start;
        getLog().info("Time took " + IMMData.timeToRunRemover + " ms");

        IMMData.testStartTime = System.currentTimeMillis();
    }

    private void runEMOP() throws MojoExecutionException {
        if (eMOP) {
            getLog().info("Running eMOP");
            // Use eMOP plugin by the author of https://github.com/SoftEngResearch/emop/
            executeMojo(
                    plugin(
                            groupId("edu.cornell"),
                            artifactId("emop-maven-plugin"),
                            version("1.0-SNAPSHOT")
                    ),
                    goal("monitor"),
                    configuration(),
                    executionEnvironment(
                            project,
                            session,
                            manager
                    )
            );
        }
    }

    private boolean transformMethod(String method) throws IOException {
        int lastDot = method.lastIndexOf('.');
        String classType = method.substring(0, lastDot);
        String className = classType.replace(".", "/") + ".class";
        String methodName = method.substring(lastDot + 1);

        if (transformedClasses.containsKey(className)) {
            if (!transformedClasses.get(className)) {
                getLog().warn("This previously identified IMM is in a changed class. Will not perform de-instrumentation.");
                return false;
            }
            return IMMData.deIMMClasses.contains(classType); // instead of returning true, we return whether we transformed
        } else if (changedClasses.contains(className) || changedTestClasses.contains(className)
                || changedJarClasses.contains(className)) {
            getLog().warn("This previously identified IMM is in a changed class. Will not perform de-instrumentation.");
            IMMData.containLoopClasses.remove(classType);
            IMMData.deIMMClasses.remove(classType);
            transformedClasses.put(className, false);
            return false;
        }
        getLog().info("Reuse " + className + " (" + method + ") previous transformed bytecode.");

        Path classFile = Paths.get(project.getBuild().getOutputDirectory() , className);
        if (Files.exists(classFile)) {
            Path oldFile = Paths.get(FileUtil.getTransformedBytecodeProject() + File.separator + "classes", className);

            if (!Files.exists(oldFile)) {
                getLog().error("This previously identified IMM, but cannot find the transformed bytecode file anymore");
                transformedClasses.put(className, false);
                IMMData.containLoopClasses.remove(classType);
                return false;
            }

            getLog().debug("Found file " + className + " from disk " + oldFile);
            FileUtil.copy(oldFile, classFile, false); // copy previously transformed code to target
            transformedClasses.put(className, true);
            IMMData.transformedClasses.add(className); // we need to copy original bytecode back to target later
            return IMMData.deIMMClasses.contains(classType); // instead of returning true, we return whether we transformed
        }

        Path testClassFile = Paths.get(project.getBuild().getTestOutputDirectory() , className);
        if (Files.exists(testClassFile)) {
            Path oldFile = Paths.get(FileUtil.getTransformedBytecodeProject() + File.separator + "test-classes", className);

            if (!Files.exists(oldFile)) {
                getLog().error("This previously identified IMM, but cannot find the transformed bytecode file anymore");
                transformedClasses.put(className, false);
                IMMData.containLoopClasses.remove(classType);
                return false;
            }

            getLog().debug("Found file " + className + " from disk " + oldFile);
            FileUtil.copy(oldFile, testClassFile, false); // copy previously transformed code to target
            transformedClasses.put(className, true);
            IMMData.transformedTestClasses.add(className); // we need to copy original bytecode back to target later
            return IMMData.deIMMClasses.contains(classType); // instead of returning true, we return whether we transformed
        }

        getLog().info(method + " is probably in a library, searching " + className + " in jars");

        String previousJar = IMMData.classToJar.get(className);
        if (previousJar != null) {
            Path oldFile = Paths.get(FileUtil.getTransformedBytecodeLibrary() + File.separator
                    + Paths.get(previousJar).getFileName() + File.separator + className);

            if (!Files.exists(oldFile)) {
                getLog().error("This previously identified IMM, but cannot find the transformed bytecode file anymore");
                transformedClasses.put(className, false);
                IMMData.containLoopClasses.remove(classType);
                return false;
            }

            getLog().info("Found file " + className + " from disk " + oldFile);
            FileUtil.copyToJar(previousJar, oldFile, className);
            transformedClasses.put(className, true);
            IMMData.transformedLibraryClasses.add(previousJar);
            return IMMData.deIMMClasses.contains(classType); // instead of returning true, we return whether we transformed
        }

        getLog().error("Unable to find method " + method + " in project!");
        return false;
    }

    private void getChangedFiles(Set<String> newClasspath, Set<String> oldClasspath) {
        changedClasses = FileUtil.getChangedClasses(
                Paths.get(project.getBuild().getOutputDirectory()),
                Paths.get(FileUtil.getOriginalBytecodeProject() + File.separator + "classes"),
                cleanBytecode
        );
        getLog().debug("changedClasses: " + changedClasses.toString());

        changedTestClasses = FileUtil.getChangedClasses(
                Paths.get(project.getBuild().getTestOutputDirectory()),
                Paths.get(FileUtil.getOriginalBytecodeProject() + File.separator + "test-classes"),
                cleanBytecode
        );
        getLog().debug("changedTestClasses: " + changedTestClasses.toString());

        getLog().debug("Old classpath: " + oldClasspath.toString());
        getLog().debug("New classpath: " + newClasspath.toString());
        changedJarClasses = new HashSet<>();
        for (String newJar : newClasspath) {
            if (!oldClasspath.contains(newJar)) {
                // newJar is new, assume they are all changed for now
                Set<String> filesInJar = FileUtil.listFilesInJar(newJar);
                for (String file : filesInJar) {
                    changedJarClasses.add(file);
                    IMMData.classToJar.put(file, newJar);
                }
            }
        }
        getLog().debug("changedJarClasses: " + changedJarClasses.toString());

        String changedFile = System.getenv("TINYMOP_CHANGED_CLASSES");
        if (changedFile != null) {
            try (FileWriter fw = new FileWriter(changedFile, false)) {
                for (String line : changedClasses) {
                    String klass = line.substring(0, line.lastIndexOf(".")).replace("/", ".");
                    fw.write(klass + System.lineSeparator());
                    IMMData.changedClasses.add(klass);
                }

                for (String line : changedTestClasses) {
                    String klass = line.substring(0, line.lastIndexOf(".")).replace("/", ".");
                    fw.write(klass + System.lineSeparator());
                    IMMData.changedClasses.add(klass);
                }

                for (String line : changedJarClasses) {
                    String klass = line.substring(0, line.lastIndexOf(".")).replace("/", ".");
                    fw.write(klass + System.lineSeparator());
                    IMMData.changedClasses.add(klass);
                }
            } catch (IOException ignored) {
                // Nothing we can do
            }
        }
    }
}
