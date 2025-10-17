package edu.lazymop;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import edu.lazymop.types.IMMData;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.classworlds.realm.ClassRealm;

@Mojo(name = "incremental-run", requiresDirectInvocation = true, requiresDependencyResolution = ResolutionScope.TEST)
@Execute(phase = LifecyclePhase.TEST, lifecycle = "incremental-run")
public class IMMIncrementalRunMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Parameter(property = "tracesDir")
    private String tracesDir;

    @Parameter(property = "stats", defaultValue = "false")
    private boolean stats;

    @Parameter(property = "agentPath")
    private File agentPath;

    @Parameter(property = "disableMixed")
    private boolean disableMixedIMM;

    @Parameter(property = "disableLoopRemover")
    private boolean disableLoopRemover;

    @Parameter(property = "disableInitRemover")
    private boolean disableInitRemover;

    @Parameter(defaultValue = "${session}", readonly = true)
    private MavenSession session;

    @Component
    private PluginDescriptor descriptor;

    private List<String> classpath;

    public void execute() throws MojoExecutionException {
        long testEndTime = System.currentTimeMillis();
        getLog().info("===== Time Data =====");
        getLog().info("Locator: " + IMMData.timeToRunLocator + " ms");
        getLog().info("Remover: " + IMMData.timeToRunRemover + " ms");
        getLog().info("Total: " + (IMMData.timeToRunLocator + IMMData.timeToRunRemover) + " ms");
        getLog().info("Test time: " + (testEndTime - IMMData.testStartTime) + " ms");

        restoreUninstrumentedFiles();
        long restorerEndTime = System.currentTimeMillis();

        // if it is a new run (i.e., IMMData.statistics is null), then we use all traces, otherwise we only use new traces
        IMMLocator.identifyIMMs(tracesDir, stats, IMMData.statistics != null, getLog());
        long locatorEndTime = System.currentTimeMillis();

        // Perform de-instrumentation
        getClasspath();
        IMMRemover.removeIMMs(disableMixedIMM, agentPath, classpath, false, disableLoopRemover, disableInitRemover,
                project.getBuild(), getLog());
        IMMData.writeClassToJarToFile();
        long removerEndTime = System.currentTimeMillis();

        getLog().info("Restoring time: " + (restorerEndTime - testEndTime) + " ms");
        getLog().info("Re-locator time: " + (locatorEndTime - restorerEndTime) + " ms");
        getLog().info("Re-Remover time: " + (removerEndTime - locatorEndTime) + " ms");
    }

    private void restoreUninstrumentedFiles() {
        try {
            for (String className : IMMData.transformedClasses) {
                // Copy original classes from artifact back to target/classes
                Path classFile = Paths.get(project.getBuild().getOutputDirectory() , className);
                getLog().debug("Restoring " + classFile);

                FileUtil.copy(Paths.get(FileUtil.getOriginalBytecodeProject() + File.separator + "classes", className),
                        classFile, false);
            }

            for (String className : IMMData.transformedTestClasses) {
                // Copy original test-classes from artifact back to target/test-classes
                Path classFile = Paths.get(project.getBuild().getTestOutputDirectory() , className);
                getLog().debug("Restoring " + classFile);

                FileUtil.copy(Paths.get(FileUtil.getOriginalBytecodeProject() + File.separator + "test-classes", className),
                        classFile, false);
            }

            for (String jar : IMMData.transformedLibraryClasses) {
                // Copy original jar from artifact back to m2
                Path jarPath = Paths.get(jar);
                getLog().debug("Restoring " + jar);

                FileUtil.copy(Paths.get(FileUtil.getOriginalBytecodeLibrary() + File.separator + jarPath.getFileName()),
                        jarPath, false);
            }
        } catch (IOException ioe) {
            getLog().error("Unable to restore original files.");
            throw new RuntimeException(ioe);
        }
    }

    public void getClasspath() {
        try {
            classpath = project.getTestClasspathElements();

            ClassRealm realm = descriptor.getClassRealm();

            for (String path : classpath) {
                getLog().debug("Injecting path " + path);
                File elementFile = new File(path);
                realm.addURL(elementFile.toURI().toURL());
            }
        } catch (DependencyResolutionRequiredException | MalformedURLException exception) {
            throw new RuntimeException(exception);
        }
    }
}