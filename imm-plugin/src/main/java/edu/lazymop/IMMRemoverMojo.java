package edu.lazymop;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.lazymop.deinstrumentation.IMMTransformer;
import edu.lazymop.deinstrumentation.Utils;
import edu.lazymop.types.IMMData;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.classworlds.realm.ClassRealm;

@Mojo(name = "remove", requiresDirectInvocation = true, requiresDependencyResolution = ResolutionScope.TEST)
public class IMMRemoverMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Parameter(property = "agentPath")
    private File agentPath;

    @Parameter(property = "disableMixed")
    private boolean disableMixedIMM;

    @Parameter(defaultValue = "${session}", readonly = true)
    private MavenSession session;

    @Component
    private PluginDescriptor descriptor;

    private List<String> classpath;

    public void execute() throws MojoExecutionException {
        long start = System.currentTimeMillis();
        if (agentPath == null || !agentPath.isFile()) {
            throw new MojoExecutionException("agentPath: path to agent file is missing or is invalid");
        }

        FileUtil.setArtifactDir(project.getBasedir().getAbsolutePath());
        IMMData.readClassToJarToFile();

        if (IMMData.statistics == null) {
            throw new MojoExecutionException("Unable to find IMMs. Are you using the right goal?");
        }

        getClasspath();
        IMMRemover.removeIMMs(disableMixedIMM, agentPath, classpath, true, false, false,
                project.getBuild(), getLog());

        long end = System.currentTimeMillis();
        IMMData.timeToRunRemover = end - start;
        getLog().info("Time took " + IMMData.timeToRunLocator + " ms");

        IMMData.testStartTime = System.currentTimeMillis();
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
