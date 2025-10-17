package edu.lazymop;

import edu.lazymop.types.IMMData;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

@Mojo(name = "run", requiresDirectInvocation = true, requiresDependencyResolution = ResolutionScope.TEST)
@Execute(phase = LifecyclePhase.TEST, lifecycle = "run")
public class IMMRunMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;
    @Parameter(defaultValue = "${session}", readonly = true)
    private MavenSession session;

    public void execute() throws MojoExecutionException {
        long testEndTime = System.currentTimeMillis();
        getLog().info("===== Time Data =====");
        getLog().info("Locator: " + IMMData.timeToRunLocator + " ms");
        getLog().info("Remover: " + IMMData.timeToRunRemover + " ms");
        getLog().info("Total: " + (IMMData.timeToRunLocator + IMMData.timeToRunRemover) + " ms");
        getLog().info("Test time: " + (testEndTime - IMMData.testStartTime) + " ms");
    }
}
