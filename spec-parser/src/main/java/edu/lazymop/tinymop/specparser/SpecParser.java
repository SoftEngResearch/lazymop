package edu.lazymop.tinymop.specparser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import com.runtimeverification.rvmonitor.java.rvj.Main;
import com.runtimeverification.rvmonitor.java.rvj.RVMOptions;
import com.runtimeverification.rvmonitor.logicrepository.LogicException;
import com.runtimeverification.rvmonitor.util.RVMException;
import edu.lazymop.tinymop.specparser.monitoring.MonitorGenerationUtil;
import edu.lazymop.tinymop.specparser.monitoring.MonitorGenerator;
import edu.lazymop.tinymop.specparser.slicing.MonitorManagerGenerator;
import edu.lazymop.tinymop.specparser.slicing.SlicerGenerationUtil;
import edu.lazymop.tinymop.specparser.slicing.SlicerGenerator;
import edu.lazymop.util.AspectJPrinter;
import edu.lazymop.util.Logger;
import edu.lazymop.util.Writer;
import javamop.JavaMOPMain;
import javamop.JavaMOPOptions;
import javamop.output.AspectJCode;
import javamop.util.MOPException;
import javamop.util.Tool;

/*
Input: a JavaMOP spec
Outputs: instrumentation code (aspectj for now) and an automaton class
 */
public class SpecParser {

    private static final Logger LOGGER = Logger.getGlobal();
    private File outputDirectory;

    public SpecParser() {
        Main.options = new RVMOptions();
        JavaMOPMain.options = new JavaMOPOptions();
        JavaMOPMain.options.emop = true;
        setOutputDirectory(new File(System.getProperty("java.io.tmpdir")));
    }

    public SpecParser(boolean isVerbose) {
        this();
        Main.options.verbose = isVerbose;
    }

    protected static File getFileFromResources(String fileName) {
        URL url = Thread.currentThread().getContextClassLoader().getResource(fileName);
        return new File(url.getPath());
    }

    /**
     * Invokes the RVMParser on a single .mop file.
     *
     * @param mopFile Represents the .mop file.
     */
    public void parseSingleSpec(File mopFile) {
        File baseAspect = getFileFromResources("BaseAspect.aj");
        JavaMOPMain.options.baseAspect = baseAspect;
        try {
            String outputPath = getOutputDirectory().getAbsolutePath();
            if (!outputPath.endsWith("/")) {
                outputPath = outputPath + "/"; // JavaMOP's bug, path must ends with /
            }
            JavaMOPMain.prepare(mopFile.getPath().split(" "));
            AspectJCode ajCode = JavaMOPMain.processSpecFileWithReturn(
                    mopFile,
                    outputPath,
                    baseAspect
            );
            String ajCodeString = AspectJPrinter.printAspectJCode(ajCode);
            Writer.write(Tool.changeIndentation(ajCodeString, "", "\t"),
                    getOutputDirectory(), mopFile.getName().replace("_", ""));
        } catch (MOPException mopException) {
            LOGGER.log(Level.INFO, "[MOPException] RV Monitor is unable to process: "
                    + mopFile + " because: \n" + mopException.getMessage());
            mopException.printStackTrace();
        } catch (IOException ioException) {
            LOGGER.log(Level.INFO, "[MOPException] RV Monitor is unable to process: "
                    + mopFile + " because: \n" + ioException.getMessage());
            ioException.printStackTrace();
        }
    }

    /**
     * Invokes the RVMParser on multiple .mop files.
     *
     * @param mopFiles A list of .mop files.
     */
    public void parseMultipleSpecs(List<File> mopFiles) throws RVMException, LogicException {
        for (File mopFile : mopFiles) {
            LOGGER.log(Level.INFO, "Parsing spec file... " + mopFile.getAbsolutePath());
            parseSingleSpec(mopFile);
        }
    }

    public void getMonitorsAndManagers(List<File> mopFiles) throws RVMException, LogicException {
        for (File mopFile : mopFiles) {
            LOGGER.log(Level.INFO, "Generating code for spec file... " + mopFile.getAbsolutePath());

            String filename = mopFile.getName().replace(".mop", ".rvm");
            File rvmFile = new File(getOutputDirectory(), filename);
            RVMParser.processSpecFile(rvmFile, getOutputDirectory(), Main.options.verbose);
            StandaloneRVMProcessor processor = RVMParser.processor;

            // Generate monitors
            MonitorGenerationUtil monGenUtil = new MonitorGenerationUtil(processor.getName(), processor.getMonitorData());
            try (BufferedWriter writer = Writer.getWriter(
                    new File(getOutputDirectory(), monGenUtil.getFilename()).toString())) {
                writer.write(new MonitorGenerator(monGenUtil,
                        System.getProperty("java.io.tmpdir") + File.separator + "monitor.dot").generateMonitorCode());
            } catch (IOException ioe) {
                LOGGER.log(Level.INFO, "ERROR during monitor code generation: " + ioe.getMessage() + "\n");
            }

            // Generate monitor managers
            SlicerGenerationUtil slicerGenUtil = new SlicerGenerationUtil(processor.getName(),
                    processor.getRvmSpecFile(), processor.getMonitorData());
            try (BufferedWriter writer = Writer.getWriter(
                    new File(getOutputDirectory(), slicerGenUtil.getManagerFilename()).toString())) {
                writer.write(new MonitorManagerGenerator(slicerGenUtil, processor.getMonitorData()).generateManagerCode());
            } catch (IOException ioe) {
                LOGGER.log(Level.INFO, "ERROR during monitor manager code generation: " + ioe.getMessage() + "\n");
            }

            // Generate slicers
            try (BufferedWriter writer = Writer.getWriter(
                    new File(getOutputDirectory(), slicerGenUtil.getSlicerFilename()).toString())) {
                writer.write(new SlicerGenerator(slicerGenUtil, processor.getMonitorData()).generateSlicerCode());
            } catch (IOException ioe) {
                LOGGER.log(Level.INFO, "ERROR during slicer code generation: " + ioe.getMessage() + "\n");
            }
        }
    }

    public File getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }
}
