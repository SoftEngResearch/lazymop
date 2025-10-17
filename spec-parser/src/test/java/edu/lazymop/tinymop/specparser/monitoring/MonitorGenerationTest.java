package edu.lazymop.tinymop.specparser.monitoring;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import edu.lazymop.util.Logger;
import edu.lazymop.util.Writer;
import org.junit.BeforeClass;
import org.junit.Test;

public class MonitorGenerationTest {

    private static final Logger LOGGER = Logger.getGlobal();

    @BeforeClass
    public static void setUp() {
        LOGGER.setLoggingLevel(Level.INFO);
    }

    @Test
    public void testIHNMonitorGeneration() throws RVMException, LogicException {
        String specName = "Iterator_HasNext";
        String fileName = specName.replace("_", "");
        String outPath = System.getProperty("java.io.tmpdir") + File.separator + fileName + "Monitor.java";
        LOGGER.log(Level.INFO, "Path to Monitor: " + "outPath");
        generateMonitorCode(specName, outPath);
        // TODO: this assertion is flaky, and needs to be debugged
        // assertTrue(FileUtils.contentEquals(TestUtil.getResourceFile(specName + "Monitor.java"), new File(outPath)));
        checkCompilation(fileName, outPath);
    }

    @Test
    public void testCSCMonitorGeneration() throws RVMException, LogicException {
        String specName = "Collections_SynchronizedCollection";
        String fileName = specName.replace("_", "");
        String outPath = System.getProperty("java.io.tmpdir") + File.separator + fileName + "Monitor.java";
        generateMonitorCode(specName, outPath);
//        assertTrue(FileUtils.contentEquals(TestUtil.getResourceFile(specName + "Monitor.java"),
//                 new File(outPath)));
        checkCompilation(fileName, outPath);
    }

    @Test
    public void testBAOMonitorGeneration() throws RVMException, LogicException {
        String specName = "ByteArrayOutputStream_FlushBeforeRetrieve";
        String fileName = specName.replace("_", "");
        String outPath = System.getProperty("java.io.tmpdir") + File.separator + fileName + "Monitor.java";
        generateMonitorCode(specName, outPath);
        // TODO: this assertion is flaky, and needs to be debugged (somehow, testCSCMonitorGeneration seems not flaky)
//        assertTrue(FileUtils.contentEquals(TestUtil.getResourceFile(specName + "Monitor.java"),
//                new File(outPath)));
        checkCompilation(fileName, outPath);
    }

    @Test
    public void testMapUnsafeMonitorGeneration() throws RVMException, LogicException {
        String specName = "Map_UnsafeIterator";
        String fileName = specName.replace("_", "");
        String outPath = System.getProperty("java.io.tmpdir") + File.separator + fileName + "Monitor.java";
        generateMonitorCode(specName, outPath);
        // TODO: is this assertion flaky?
//        assertTrue(FileUtils.contentEquals(TestUtil.getResourceFile(specName + "Monitor.java"),
//                new File(outPath)));
        checkCompilation(fileName, outPath);
    }

    // We have transition out of violating state
    @Test
    public void testMultipleCloseMonitorGeneration() throws RVMException, LogicException {
        String specName = "Closeable_MultipleClose";
        String fileName = specName.replace("_", "");
        String outPath = System.getProperty("java.io.tmpdir") + File.separator + fileName + "Monitor.java";
        generateMonitorCode(specName, outPath);
        checkCompilation(fileName, outPath);
    }

    // InputStream_ReadAheadLimit does not have the right default state because JavaMOP minimized the FSM
    // So states S0 and S1 become S0_S1
    @Test
    public void testInputStreamReadAheadLimitMonitorGeneration() throws RVMException, LogicException {
        String specName = "InputStream_ReadAheadLimit";
        String fileName = specName.replace("_", "");
        String outPath = System.getProperty("java.io.tmpdir") + File.separator + fileName + "Monitor.java";
        generateMonitorCode(specName, outPath);
        checkCompilation(fileName, outPath);
    }

    // We have multiple transitions out of violating state
    @Test
    public void testReuseSocketMonitorGeneration() throws RVMException, LogicException {
        String specName = "Socket_ReuseSocket";
        String fileName = specName.replace("_", "");
        String outPath = System.getProperty("java.io.tmpdir") + File.separator + fileName + "Monitor.java";
        generateMonitorCode(specName, outPath);
        checkCompilation(fileName, outPath);
    }

    // Use @err, not @match or @violation
    @Test
    public void testPrematureStartMonitorGeneration() throws RVMException, LogicException {
        String specName = "ShutdownHook_PrematureStart";
        String fileName = specName.replace("_", "");
        String outPath = System.getProperty("java.io.tmpdir") + File.separator + fileName + "Monitor.java";
        generateMonitorCode(specName, outPath);
        checkCompilation(fileName, outPath);
    }

    // single monitor spec
    @Test
    public void testMCRSlicerGeneration() throws RVMException, LogicException {
        String specName = "Math_ContendedRandom";
        String fileName = specName.replace("_", "");
        String outPath = System.getProperty("java.io.tmpdir") + File.separator + fileName + "Monitor.java";
        generateMonitorCode(specName, outPath);
        checkCompilation(fileName, outPath);
    }

    public void checkCompilation(String specName, String outPath) {
        String classpath = System.getProperty("java.class.path");
        File expectedMonitorFile = TestUtil.getResourceFile(specName + "Monitor.java");
        File compilationDestination = new File(System.getProperty("java.io.tmpdir") + File.separator + "out");
        if (expectedMonitorFile != null) {
            StringBuffer expected = getCompilationCommandBuffer(compilationDestination, classpath, expectedMonitorFile);
            // does the expected monitor code compile?
            checkMonitorCodeCompilation(expected);
        } else {
            LOGGER.log(Level.WARNING, "Skip compiling " + specName);
        }

        StringBuffer generated = getCompilationCommandBuffer(compilationDestination, classpath, new File(outPath));
        // does the generated monitor code compile?
        checkMonitorCodeCompilation(generated);
    }

    private static void checkMonitorCodeCompilation(StringBuffer sb) {
        String compile = sb.toString();
        LOGGER.log(Level.FINE, compile);
        int exitSignal = 1;
        try {
            exitSignal = runProcess(compile);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        } catch (InterruptedException ie) {
            throw new RuntimeException(ie);
        }
        assertEquals(0, exitSignal);
    }

    private static StringBuffer getCompilationCommandBuffer(File destination, String classpath, File monitorFile) {
        destination.mkdirs();
        StringBuffer sb = new StringBuffer();
        sb.append("javac -cp ");
        sb.append(classpath);
        sb.append(" -d " + destination.getPath());
        sb.append(" " + monitorFile.getPath());
        return sb;
    }

    private static int runProcess(String command) throws IOException, InterruptedException {
        Process pro = Runtime.getRuntime().exec(command);
        LOGGER.log(Level.FINE, "\n\n");
        LOGGER.log(Level.INFO, " stdout:" + streamToString(pro.getInputStream()) + "\n\n");
        LOGGER.log(Level.INFO," stderr:" + streamToString(pro.getErrorStream()) + "\n\n");
        pro.waitFor();
        LOGGER.log(Level.INFO," exitValue() " + pro.exitValue() + "\n\n");
        return pro.exitValue();
    }

    private static String streamToString(InputStream inputStream) {
        StringBuffer sb  = new StringBuffer();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            int read;
            while ((read = br.read()) != -1) {
                sb.append((char) read);
            }
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
        return sb.toString();
    }

    private static void generateMonitorCode(String specName, String outPath) throws LogicException, RVMException {
        File resourceFile = TestUtil.getResourceFile(specName + ".mop");
        LOGGER.log(Level.INFO, "MOP file for monitor code generation: " + resourceFile.getPath());
        SpecParser parser = new SpecParser(false);
        parser.setOutputDirectory(new File(System.getProperty("java.io.tmpdir")));
        parser.parseSingleSpec(resourceFile);
        File rvmFile = new File(resourceFile.getPath().replace(".mop", ".rvm"));
        RVMParser.processSpecFile(rvmFile, parser.getOutputDirectory(), Main.options.verbose);
        StandaloneRVMProcessor processor = RVMParser.processor;
        MonitorGenerationUtil monGenUtil = new MonitorGenerationUtil(processor.getName(), processor.getMonitorData());
        try (BufferedWriter writer = Writer.getWriter(outPath)) {
            writer.write(new MonitorGenerator(monGenUtil,
                    System.getProperty("java.io.tmpdir") + File.separator + "monitor.dot").generateMonitorCode());
        } catch (IOException ioe) {
            LOGGER.log(Level.INFO, "ERROR during monitor code generation: " + ioe.getMessage() + "\n");
        }
    }
}
