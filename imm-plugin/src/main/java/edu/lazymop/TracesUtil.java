package edu.lazymop;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.lazymop.types.Event;
import edu.lazymop.types.SpecTraces;
import edu.lazymop.types.Trace;
import org.apache.maven.plugin.logging.Log;

public class TracesUtil {

    private static Map<Integer, String> locationIDMapCache = null;

    /**
     * Reads trace file and location file from the traces directory.
     *
     * @param tracesDir the directory containing the trace files
     * @throws IOException if an I/O error occurs
     */
    public static List<SpecTraces> readDirectory(String tracesDir, boolean newOnly, Log log) throws IOException {
        List<SpecTraces> specTracesList = new ArrayList<>();
        File[] files = new File(tracesDir).listFiles();
        if (files == null || files.length > 1) {
            throw new IOException("Only supports one group of traces");
        } else if (files.length == 0) {
            log.warn("Cannot find traces");
            return specTracesList;
        }

        String tracesPath = files[0].getPath();

        for (String spec : SpecsUtil.getSpecs()) {
            String specName = spec.replace("_", "");
            File tracesFile = new File(tracesPath, specName + (newOnly ? "-changedTraces" : "-traces"));
            File locationsFile = new File(tracesPath, specName + "-locations");
            File violationsFile = new File(tracesPath, specName + "-violations");

            locationIDMapCache = null;

            if (tracesFile.isFile() && locationsFile.isFile()) {
                // isLoopExcludeSpec is true if spec is a non-iterator related spec that support loop de-instrumentation.
                boolean isLoopExcludeSpec = specName.endsWith("UnsafeIterator");
                specTracesList.add(readTraces(specName, tracesFile, locationsFile, isLoopExcludeSpec ?
                        getViolatingLocation(violationsFile) : new HashSet<>()));
            }
        }

        return specTracesList;
    }

    public static Set<Integer> getViolatingLocation(File violationsFile) throws IOException {
        if (violationsFile.isFile()) {
            Set<Integer> locations = new HashSet<>();
            try (BufferedReader br = new BufferedReader(new FileReader(violationsFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.charAt(0) == 'M' ) {
                        // traces end here
                        break;
                    }

                    int spaceIndex = line.indexOf(' ');
                    String[] eventsList = line.substring(spaceIndex + 2, line.length() - 1).split(", "); // remove []

                    for (String eventStr : eventsList) {
                        int xxIndex = eventStr.indexOf('x');
                        int tildeIndex = eventStr.indexOf('~');
                        if (xxIndex > 0) {
                            // two or more events (e.g., E1~2x3)
                            // Integer.parseInt(eventStr.substring(tildeIndex + 1, xIndex)) gives you 2 as int (location id)
                            int location = Integer.parseInt(eventStr.substring(tildeIndex + 1, xxIndex));
                            locations.add(location);
                        } else {
                            // just one event (e.g., E1~2)
                            // Integer.parseInt(eventStr.substring(tildeIndex + 1)) gives you 2 as int (location id)
                            int location = Integer.parseInt(eventStr.substring(tildeIndex + 1));
                            locations.add(location);
                        }
                    }
                }
            }
            return locations;
        }
        return new HashSet<>();
    }

    public static SpecTraces readTraces(String specName, File tracesFile, File locationsFile, Set<Integer> violationsLoc)
            throws IOException {
        List<Trace> traces = new ArrayList<>();
        Set<Integer> allRelatedLocations = new HashSet<>();
        try (BufferedReader br = new BufferedReader(new FileReader(tracesFile))) {
            String line;
            String nextLine = br.readLine();
            // we use this instead of (line = br.readLine()) != null because we need two read two lines
            // we stop processing if the current line is the last line (i.e., nextLine is null) because last line is
            // statistic, not a trace.
            while ((line = nextLine) != null) {
                nextLine = br.readLine();
                if (nextLine == null) {
                    break;
                }

                // split location ID and location string into two parts, then add to hashmap so that key is id.
                int spaceIndex = line.indexOf(' ');
                int frequency = Integer.parseInt(line.substring(0, spaceIndex));
                String[] eventsList = line.substring(spaceIndex + 2, line.length() - 1).split(", "); // remove []
                Event[] events = new Event[eventsList.length];
                Set<Integer> relatedLocations = new HashSet<>();

                int index = 0; // we need index to add element to `events`
                for (String eventStr : eventsList) {
                    int xxIndex = eventStr.indexOf('x');
                    int tildeIndex = eventStr.indexOf('~');
                    if (xxIndex > 0) {
                        // two or more events (e.g., E1~2x3)
                        // eventStr.substring(0, tildeIndex) gives you E1 (event name)
                        // Integer.parseInt(eventStr.substring(tildeIndex + 1, xIndex)) gives you 2 as int (location id)
                        // Integer.parseInt(eventStr.substring(xIndex + 1)) gives you 3 as int (frequency)
                        int location = Integer.parseInt(eventStr.substring(tildeIndex + 1, xxIndex));
                        events[index] = new Event(eventStr.substring(0, tildeIndex), location,
                                Integer.parseInt(eventStr.substring(xxIndex + 1)));
                        relatedLocations.add(location);
                        allRelatedLocations.add(location);
                    } else {
                        // just one event (e.g., E1~2)
                        // eventStr.substring(0, tildeIndex) gives you E1 (event name)
                        // Integer.parseInt(eventStr.substring(tildeIndex + 1)) gives you 2 as int (location id)
                        int location = Integer.parseInt(eventStr.substring(tildeIndex + 1));
                        events[index] = new Event(eventStr.substring(0, tildeIndex), location, 1);
                        relatedLocations.add(location);
                        allRelatedLocations.add(location);
                    }

                    index += 1;
                }
                traces.add(new Trace(frequency, events, relatedLocations));
            }
        }

        Map<Integer, String> locationIDMap = readLocations(locationsFile);
        return new SpecTraces(specName, traces, locationIDMap, allRelatedLocations, violationsLoc);
    }


    /**
     * Reads the location file and maps short location IDs to their actual locations.
     *
     * @param locationsFile the file containing the location mappings
     * @return a map of short location IDs to actual locations
     * @throws IOException if an I/O error occurs
     */
    public static Map<Integer, String> readLocations(File locationsFile) throws IOException {
        if (locationIDMapCache != null) {
            return locationIDMapCache;
        }

        Map<Integer, String> locationIDMap = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(locationsFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                // split location ID and location string into two parts, then add to hashmap so that key is id.
                // location[0] + ":" + location[3] to keep only the method name and the method starting line
                int spaceIndex = line.indexOf(' ');
                String[] location = line.substring(spaceIndex + 1).split(":");
                locationIDMap.put(Integer.parseInt(line.substring(0, spaceIndex)), location[0] + ":" + location[3]);
            }
        }

        locationIDMapCache = locationIDMap;
        return locationIDMap;
    }
}
