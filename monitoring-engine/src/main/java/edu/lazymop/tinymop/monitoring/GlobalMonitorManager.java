package edu.lazymop.tinymop.monitoring;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import edu.lazymop.tinymop.monitoring.util.SpecializedSlicingAlgorithmUtil;

public class GlobalMonitorManager {

    public static int currentRunningTest = 0;
    public static int nextTestID = 1;
    public static Map<Integer, Integer> locationToTestID = new HashMap<>();
    public static Set<String> changedMethods = null;
    public static boolean changedMethodsInit = false;
    public static String uuid = "";
    public static Map<Integer, String> testIDToName = new HashMap<>();
    public static boolean isMultiThreaded = false;
    private static Set<MonitorManager> registeredManagers;

    // initialized before any monitoring starts
    public static void initialize() {
        registeredManagers = new HashSet<>();
    }

    // registers all spec-specific MonitorManagers
    public static boolean registerManager(MonitorManager manager) {
        if (registeredManagers == null) {
            initialize();

            /*
            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    // This shutdown hook is used to save testIDToName to disk
                    try (FileWriter fw = new FileWriter(SpecializedSlicingAlgorithmUtil.getTestFile(), false)) {
                        for (Map.Entry<Integer, String> entry : testIDToName.entrySet()) {
                            fw.write(entry.getKey() + " " + entry.getValue() + System.lineSeparator());
                        }
                    } catch (IOException ignored) {
                        // Nothing we can do here
                    }
                }
            });
             */

            uuid = UUID.randomUUID().toString();
            try {
                Files.createDirectories(Paths.get(SpecializedSlicingAlgorithmUtil.getDirectory()));
            } catch (IOException ignored) {
                // Nothing we can do here
            }
        }

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                manager.monitorSlices();
                manager.collectStatistics();
            }
        });

        return registeredManagers.add(manager);
    }

    public static void addRunningTest(String testName, int locationID) {
        currentRunningTest = nextTestID;
        locationToTestID.put(locationID, currentRunningTest);
        testIDToName.put(currentRunningTest, testName);

        nextTestID += 1;
    }

    public static void updateRunningTest(int locationID) {
        currentRunningTest = locationToTestID.getOrDefault(locationID, 0);
    }

    public static boolean isChangedMethods(String klass) {
        if (!changedMethodsInit) {
            changedMethodsInit = true;

            String changedFile = System.getenv("TINYMOP_CHANGED_CLASSES");
            changedMethods = new HashSet<>();

            if (changedFile == null) {
                return false;
            }

            try {
                try (BufferedReader br = new BufferedReader(new FileReader(changedFile))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        if (!line.isEmpty()) {
                            changedMethods.add(line);
                        }
                    }
                }
            } catch (IOException ioe) {
                return false;
            }
        }

        return changedMethods.contains(klass);
    }
}
