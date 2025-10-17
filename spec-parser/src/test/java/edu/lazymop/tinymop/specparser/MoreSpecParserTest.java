package edu.lazymop.tinymop.specparser;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import com.runtimeverification.rvmonitor.logicrepository.LogicException;
import com.runtimeverification.rvmonitor.util.RVMException;
import edu.lazymop.util.Logger;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class MoreSpecParserTest {
    private static final Logger LOGGER = Logger.getGlobal();
    SpecParser parser;

    @Before
    public void setup() {
        parser = new SpecParser(false);
        parser.setOutputDirectory(new File(System.getProperty("java.io.tmpdir") + File.separator));
    }

    @After
    public void teardown() {
        parser = null;
        RVMParser.processor = null;
    }

    //TODO: this test is a polluter, when it runs before tests that use Iterarator_HasNext correctly, it pollutes the
    //      state and makes them fail.
    @Test(expected = com.runtimeverification.rvmonitor.logicrepository.LogicException.class)
    @Ignore
    public void testForLogicException() throws RVMException, LogicException {
        File badSpecFile = TestUtil.getResourceFile("NullIterator_HasNext.mop");
        parser.parseSingleSpec(badSpecFile);
    }

    @Test
    public void testWithVerbose() throws IOException {
        parser = new SpecParser(true);
        File iteratorHasNextSpecFile = TestUtil.getResourceFile("Iterator_HasNext.mop");
        parser.parseSingleSpec(iteratorHasNextSpecFile);
        assertTrue(FileUtils.contentEquals(TestUtil.getResourceFile("Iterator_HasNext.rvm"),
                new File(parser.getOutputDirectory(), "Iterator_HasNext.rvm")));
    }

    @Test
    public void testIHNSpecQuietly() throws IOException {
        File iteratorHasNextSpecFile = TestUtil.getResourceFile("Iterator_HasNext.mop");
        parser.parseSingleSpec(iteratorHasNextSpecFile);
        File file2 = new File(parser.getOutputDirectory(), "Iterator_HasNext.rvm");
        File resourceFile = TestUtil.getResourceFile("Iterator_HasNext.rvm");
        assertTrue(FileUtils.contentEquals(resourceFile, file2));
    }

    @Test
    public void testAMCSpecQuietly() throws IOException {
        File amcSpecFile = TestUtil.getResourceFile("Arrays_MutuallyComparable.mop");
        parser.parseSingleSpec(amcSpecFile);
        assertTrue(FileUtils.contentEquals(TestUtil.getResourceFile("Arrays_MutuallyComparable.rvm"),
                new File(parser.getOutputDirectory(), "Arrays_MutuallyComparable.rvm")));
    }

    @Test
    public void testBAOSpecQuietly() throws IOException {
        File baosSpecFile = TestUtil.getResourceFile("ByteArrayOutputStream_FlushBeforeRetrieve.mop");
        parser.parseSingleSpec(baosSpecFile);
        assertTrue(FileUtils.contentEquals(TestUtil.getResourceFile("ByteArrayOutputStream_FlushBeforeRetrieve.rvm"),
                new File(parser.getOutputDirectory(), "ByteArrayOutputStream_FlushBeforeRetrieve.rvm")));
    }

    @Test
    public void testCSCpecQuietly() throws IOException {
        File cscSpecFile = TestUtil.getResourceFile("Collections_SynchronizedCollection.mop");
        parser.parseSingleSpec(cscSpecFile);
        assertTrue(FileUtils.contentEquals(TestUtil.getResourceFile("Collections_SynchronizedCollection.rvm"),
                new File(parser.getOutputDirectory(), "Collections_SynchronizedCollection.rvm")));
    }

    @Test
    public void testMultipleSpecFiles() throws RVMException, LogicException, IOException {
        List<File> mopFiles = new ArrayList<>();
        String[] specNames = new String[]{"Iterator_HasNext",
            "Arrays_MutuallyComparable",
            "ByteArrayOutputStream_FlushBeforeRetrieve",
            "Collections_SynchronizedCollection"};
        for (String specName : specNames) {
            mopFiles.add(TestUtil.getResourceFile(specName + ".mop"));
        }
        parser.parseMultipleSpecs(mopFiles);
        File outDir = parser.getOutputDirectory();
        for (String specName : specNames) {
            String rvmName = specName + ".rvm";
            assertTrue(rvmName + " doesn't match",
                    FileUtils.contentEquals(TestUtil.getResourceFile(rvmName), new File(outDir, rvmName)));
            String aspectName = specName + "MonitorAspect.aj";
            File resourceFile = TestUtil.getResourceFile(aspectName);
            File file2 = new File(outDir, aspectName);
            LOGGER.log(Level.INFO, "Trigger CI?");
            LOGGER.log(Level.INFO, "R: " + resourceFile.getAbsolutePath());
            LOGGER.log(Level.INFO, "F: " + file2.getAbsolutePath());
            assertTrue(aspectName + " doesn't match", FileUtils.contentEquals(resourceFile, file2));
        }
    }
}
