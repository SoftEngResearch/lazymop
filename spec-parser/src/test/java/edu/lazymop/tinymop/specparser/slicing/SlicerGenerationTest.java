// CHECKSTYLE:OFF
package edu.lazymop.tinymop.specparser.slicing;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;

import com.runtimeverification.rvmonitor.java.rvj.Main;
import com.runtimeverification.rvmonitor.logicrepository.LogicException;
import com.runtimeverification.rvmonitor.util.RVMException;
import edu.lazymop.tinymop.specparser.RVMParser;
import edu.lazymop.tinymop.specparser.SpecParser;
import edu.lazymop.tinymop.specparser.StandaloneRVMProcessor;
import edu.lazymop.tinymop.specparser.TestUtil;
import edu.lazymop.tinymop.specparser.monitoring.MonitorGenerationUtil;
import edu.lazymop.util.Logger;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class SlicerGenerationTest {

    private static final Logger LOGGER = Logger.getGlobal();

    @BeforeClass
    public static void setUp() {
        LOGGER.setLoggingLevel(Level.INFO);
    }

    @Test
    public void testCSCSlicerGeneration() throws RVMException, LogicException {
        String specName = "Collections_SynchronizedCollection";
        String outPath = System.getProperty("java.io.tmpdir") + File.separator + specName.replace("_", "") + ".java";
        generateSlicerCode(specName, outPath);
    }

    @Test
    public void testITNSlicerGeneration() throws RVMException, LogicException {
        String specName = "Iterator_HasNext";
        String outPath = System.getProperty("java.io.tmpdir") + File.separator + specName.replace("_", "") + ".java";
        generateSlicerCode(specName, outPath);
    }

    @Test
    public void testMUISlicerGeneration() throws RVMException, LogicException {
        String specName = "Map_UnsafeIterator";
        String outPath = System.getProperty("java.io.tmpdir") + File.separator + specName.replace("_", "") + ".java";
        generateSlicerCode(specName, outPath);
    }

    // single monitor spec
    @Test
    public void testMCRSlicerGeneration() throws RVMException, LogicException {
        String specName = "Math_ContendedRandom";
        String outPath = System.getProperty("java.io.tmpdir") + File.separator + specName.replace("_", "") + ".java";
        generateSlicerCode(specName, outPath);
    }

    public static BufferedWriter getWriter(String filePath) {
        Path path = Paths.get(filePath);
        BufferedWriter writer = null;
        try {
            if (path.getParent() != null && !Files.exists(path.getParent())) {
                Files.createDirectories(path.getParent());
            }
            writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return writer;
    }

    private static void generateSlicerCode(String specName, String outPath) throws LogicException, RVMException {
        File resourceFile = TestUtil.getResourceFile(specName + ".mop");
        LOGGER.log(Level.INFO, "MOP file for slicer code generation: " + resourceFile.getPath());
        SpecParser parser = new SpecParser(false);
        parser.setOutputDirectory(new File(System.getProperty("java.io.tmpdir")));
        parser.parseSingleSpec(resourceFile);
        File rvmFile = new File(resourceFile.getPath().replace(".mop", ".rvm"));
        RVMParser.processSpecFile(rvmFile, parser.getOutputDirectory(), Main.options.verbose);
        StandaloneRVMProcessor processor = RVMParser.processor;
       try {
           SlicerGenerationUtil slicerGenUtil = new SlicerGenerationUtil(processor.getName(),
                   processor.getRvmSpecFile(), processor.getMonitorData());
           try (BufferedWriter writer = getWriter(outPath)) {
               writer.write(new SlicerGenerator(slicerGenUtil, processor.getMonitorData()).generateSlicerCode());
           } catch (IOException ioe) {
               LOGGER.log(Level.INFO, "ERROR during slicer code generation: " + ioe.getMessage() + "\n");
           }
       } catch (Exception exception) {
           exception.printStackTrace();
       }
    }
}
