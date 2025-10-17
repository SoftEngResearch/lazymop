package edu.lazymop.tinymop.specparser;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

import com.runtimeverification.rvmonitor.logicrepository.LogicException;
import com.runtimeverification.rvmonitor.util.RVMException;
import edu.lazymop.util.Logger;

public class Main {
    public static boolean onDemandSync = true;
    private static final Logger LOGGER = Logger.getGlobal();

    // mvn compile exec:java -Dexec.mainClass="edu.lazymop.tinymop.specparser.Main" -Dexec.args="output props"
    public static void main(String[] args) {
        /*
        Usage: <output directory> <props directory> [on-demand synchronization: true]
         */
        LOGGER.log(Level.INFO, "Initializing Spec Parser...");
        SpecParser parser = new SpecParser(false);
        if (args.length < 2) {
            LOGGER.log(Level.SEVERE, "Missing output directory or spec directory");
            System.exit(1);
        }

        if (args.length >= 3 && args[2].equals("false")) {
            LOGGER.log(Level.WARNING, "On-demand synchronization is disabled!");
            Main.onDemandSync = false;
        }

        File outputDirectory = new File(args[0]);
        validate(outputDirectory, false);
        parser.setOutputDirectory(outputDirectory);
        LOGGER.log(Level.INFO, "Initializing Spec Parser... DONE");
        LOGGER.log(Level.INFO, "Locating spec files...");
        // check spec directory
        File specDirectory = new File(args[1]);
        validate(specDirectory, true);
        List<String> specFileNames = Arrays.stream(specDirectory.list())
                .filter(i -> i.endsWith(".mop")).collect(Collectors.toList()); // only want files end with .mop
        LOGGER.log(Level.INFO, "spec files: " + specFileNames);
        LOGGER.log(Level.INFO, "Locating spec files... DONE");
        LOGGER.log(Level.INFO, "Parsing spec files...");
        List<File> specFiles = new ArrayList<>();
        for (File specFile : Arrays.stream(specDirectory.listFiles())
                .filter(i -> i.getName().endsWith(".mop")).collect(Collectors.toList())) {
            specFiles.add(specFile);
        }
        try {
            parser.parseMultipleSpecs(specFiles);
            parser.getMonitorsAndManagers(specFiles);
        } catch (RVMException rvme) {
            LOGGER.log(Level.SEVERE, rvme.getMessage());
            rvme.printStackTrace();
        } catch (LogicException le) {
            LOGGER.log(Level.SEVERE, le.getMessage());
            le.printStackTrace();
        }
        LOGGER.log(Level.INFO, "Parsing spec files... DONE");
    }

    private static void validate(File dir, boolean checkEmpty) {
        if (!dir.exists() || !dir.isDirectory()) {
            LOGGER.log(Level.INFO, "Provided directory does not exist or is not a directory: " + dir);
            System.exit(1);
        }

        if (checkEmpty && dir.list().length == 0) {
            LOGGER.log(Level.INFO, "Provided directory is empty: " + dir);
            System.exit(1);
        }
    }
}
