package edu.lazymop.tinymop.specparser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;

import com.google.common.collect.Sets;
import com.runtimeverification.rvmonitor.java.rvj.RVMNameSpace;
import com.runtimeverification.rvmonitor.java.rvj.SpecExtractor;
import com.runtimeverification.rvmonitor.java.rvj.parser.ast.RVMSpecFile;
import com.runtimeverification.rvmonitor.java.rvj.parser.ast.rvmspec.RVMParameter;
import com.runtimeverification.rvmonitor.java.rvj.parser.ast.rvmspec.RVMonitorSpec;
import com.runtimeverification.rvmonitor.logicrepository.LogicException;
import com.runtimeverification.rvmonitor.util.RVMException;
import com.runtimeverification.rvmonitor.util.Tool;
import edu.lazymop.util.Logger;
import edu.lazymop.util.Writer;

/**
 * This class parses the .rvm file that javamop produces.
 */
public class RVMParser {

    public static StandaloneRVMProcessor processor;
    private static final Logger LOGGER = Logger.getGlobal();

    private static final String bottomKey = "zzzbottom";
    private static final Map<String, Integer> bottomBinding = Collections.singletonMap(bottomKey, -1);

    /**
     * Process a specification file to generate a runtime monitor file. The file
     * argument should be an initialized file object. The location argument
     * should contain the original file name, But it may have a different
     * directory.
     *
     * @param file a File object containing the specification file
     */
    public static void processSpecFile(File file, File outputDir, boolean isVerbose)
            throws RVMException, LogicException {
        RVMSpecFile spec = getRVMSpecFile(file);
        if (outputDir == null) {
            ArrayList<File> specList = new ArrayList<>();
            specList.add(file);
            outputDir = getTargetDir(specList);
        }
        String outputName = Tool.getFileName(file.getAbsolutePath());
        if (isVerbose) {
            writeLessInformativeSets(spec, outputName);
        }
        processor = new StandaloneRVMProcessor(outputName, isVerbose);
        String output = processor.process(spec);
        // writeCombinedOutputFile(output, outputName, outputDir, isVerbose);
    }

    private static RVMSpecFile getRVMSpecFile(File file) throws RVMException {
        RVMNameSpace.init();
        String specStr = SpecExtractor.process(file); // read the spec into a String object
        RVMSpecFile spec = SpecExtractor.parse(specStr);
        return spec;
    }

    protected static void writeLessInformativeSets(RVMSpecFile spec, String outputName) {
        for (RVMonitorSpec specification : spec.getSpecs()) {
            Set<RVMParameter> parameters = new HashSet<>();
            for (RVMParameter parameter : specification.getParameters()) {
                parameters.add(parameter);
            }
            Set<Set<RVMParameter>> powerSet = Sets.powerSet(parameters);
            Map<Set<String>, TreeSet<Map<String, Integer>>> lessInfo = new HashMap<>();
            for (Set<RVMParameter> subset : powerSet) {
                Map<String, Integer> binding = toBinding(subset);
                TreeSet<Map<String, Integer>> lessInformative  = getLessInformative(binding);
                lessInfo.put(binding.keySet(), lessInformative);
            }
            Writer.persistLessInformative(outputName, lessInfo);
        }
    }

    private static Map<String, Integer> toBinding(Set<RVMParameter> subset) {
        Map<String, Integer> binding = new HashMap<>();
        for (RVMParameter parameter : subset) {
            binding.put(parameter.getType().toString(), 0);
        }
        return binding;
    }

    public static TreeSet<Map<String, Integer>> getLessInformative(Map<String, Integer> eventBinding) {
        TreeSet<Map<String, Integer>> lessInformative = new TreeSet<>(new BindingComparator());
        if (eventBinding != null) {
            Set<String> keys = eventBinding.keySet();
            int size = keys.size();
            for (int i = 1; i < size; i++) {
                Set<Set<String>> combinations = Sets.combinations(keys, i);
                for (Set<String> combination : combinations) {
                    Map<String, Integer> binding = new TreeMap<>();
                    for (String key : combination) {
                        binding.put(key, eventBinding.get(key));
                    }
                    lessInformative.add(binding);
                }
            }
        }
        // "bottom" is always less informative, unless the binding itself is "bottom" in which case the initialization step
        // of the algorithm ensures that this method is never called.
        lessInformative.add(bottomBinding);
        return lessInformative;
    }

    /**
     * The target directory for outputting the results produced from some
     * .rvm files. If the input files are all in the same directory,
     * return that directory. Otherwise, return the current directory.
     *
     * @param specFiles
     *            The specification files used in the input.
     * @return The place to put the output files.
     */
    private static File getTargetDir(ArrayList<File> specFiles) {

        boolean sameDir = true;
        File parentFile = null;

        for (File file : specFiles) {
            if (parentFile == null) {
                parentFile = file.getAbsoluteFile().getParentFile();
            } else {
                if (!file.getAbsoluteFile().getParentFile().equals(parentFile)) {
                    sameDir = false;
                    break;
                }
            }
        }

        if (sameDir) {
            return parentFile;
        } else {
            return new File(".");
        }
    }

    /**
     * Write an output file with the given content and name.
     *
     * @param outContent The text to write into the file.
     * @param outName    The name of the output being written.
     * @param outDir The directory to write the monitoring code to.
     */
    protected static void writeCombinedOutputFile(String outContent, String outName, File outDir, boolean isVerbose)
            throws RVMException {
        if (outContent == null || outContent.length() == 0) {
            return;
        }
        try (FileWriter writer = new FileWriter(outDir.getAbsolutePath()
                + File.separator + outName + "RuntimeMonitor.java")) {
            writer.write(outContent);
        } catch (IOException ioe) {
            throw new RVMException(ioe.getMessage());
        }

        if (isVerbose) {
            LOGGER.log(Level.INFO, outName + "RuntimeMonitor.java is generated.\n");
        }
    }

    static class BindingComparator implements Comparator<Map<String, Integer>> {
        @Override
        public int compare(Map<String, Integer> map1, Map<String, Integer> map2) {
            return map1.toString().compareTo(map2.toString());
        }
    }
}
