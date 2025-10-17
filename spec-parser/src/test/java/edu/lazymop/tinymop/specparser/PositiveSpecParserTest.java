package edu.lazymop.tinymop.specparser;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import com.runtimeverification.rvmonitor.logicrepository.LogicException;
import com.runtimeverification.rvmonitor.util.RVMException;
import edu.lazymop.util.Logger;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class PositiveSpecParserTest {

    static SpecParser parser;

    private File mopFile;
    private File rvmFile;
    private File aspectFile;
    private File javaFile;
    private String specName;

    public PositiveSpecParserTest(String specName) {
        this.specName = specName;
        this.mopFile = TestUtil.getResourceFile(specName + ".mop");
        this.rvmFile = TestUtil.getResourceFile(specName + ".rvm");
        this.aspectFile = TestUtil.getResourceFile(specName + "MonitorAspect.aj");
        this.javaFile = TestUtil.getResourceFile(specName + "RuntimeMonitor.java");
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { "Iterator_HasNext"},
                { "Collections_SynchronizedCollection" },
                { "ByteArrayOutputStream_FlushBeforeRetrieve" },
                { "Arrays_MutuallyComparable" }
        });
    }

    @Test
    public void testParsing() throws IOException, RVMException, LogicException {
        parser.parseSingleSpec(mopFile);
        File outputDirectory = parser.getOutputDirectory();
        assertTrue(FileUtils.contentEquals(rvmFile,
                new File(outputDirectory, specName + ".rvm")));
        assertTrue(FileUtils.contentEquals(aspectFile,
                new File(outputDirectory, specName + "MonitorAspect.aj")));
        assertTrue(FileUtils.contentEquals(javaFile,
                new File(outputDirectory, specName + "RuntimeMonitor.java")));
    }

    @Before
    public void loadReasources() {
        parser = new SpecParser(false);
        parser.setOutputDirectory(new File(mopFile.getParent()));
    }
}
