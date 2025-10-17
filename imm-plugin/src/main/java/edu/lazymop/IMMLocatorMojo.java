package edu.lazymop;

import java.io.File;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import edu.lazymop.types.Event;
import edu.lazymop.types.IMMData;
import edu.lazymop.types.SpecTraces;
import edu.lazymop.types.Trace;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

@Mojo(name = "find", requiresDirectInvocation = true, requiresDependencyResolution = ResolutionScope.TEST)
public class IMMLocatorMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Parameter(property = "tracesDir")
    private String tracesDir;

    @Parameter(property = "stats", defaultValue = "false")
    private boolean stats;

    @Parameter(defaultValue = "${session}", readonly = true)
    private MavenSession session;

    public void execute() throws MojoExecutionException {
        long start = System.currentTimeMillis();
        if (tracesDir == null || !new File(tracesDir).isDirectory()) {
            throw new MojoExecutionException("tracesDir: path to traces is not a valid directory");
        }

        FileUtil.setArtifactDir(project.getBasedir().getAbsolutePath());

        IMMLocator.identifyIMMs(tracesDir, stats, false, getLog());

        long end = System.currentTimeMillis();
        IMMData.timeToRunLocator = end - start;
        getLog().info("Time took " + IMMData.timeToRunLocator + " ms");
    }
}
