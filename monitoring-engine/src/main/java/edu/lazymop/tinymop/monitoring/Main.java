package edu.lazymop.tinymop.monitoring;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import edu.lazymop.tinymop.specparser.monitoring.RuntimeMonitor;
import edu.lazymop.util.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class Main {
    private static final Logger LOGGER = Logger.getGlobal();
    private static Set<String> violationMessages = new HashSet<>();

    // mvn compile exec:java -Dexec.mainClass="edu.lazymop.tinymop.monitoring.Main" -Dexec.args="violations-file locations-file spec"
    public static void main(String[] args) throws IOException {
        if (args.length < 3) {
            LOGGER.log(Level.SEVERE, "Missing arguments violation file, location file, or spec name");
            System.exit(1);
        }

        if (!Files.exists(Paths.get(args[0]))) {
            LOGGER.log(Level.SEVERE, "Missing violation file");
            System.exit(1);
        }
        if (!Files.exists(Paths.get(args[1]))) {
            LOGGER.log(Level.SEVERE, "Missing location file");
            System.exit(1);
        }

        Map<String, String> locationsMap = readLocations(new File(args[1]));
        try (BufferedReader br = new BufferedReader(new FileReader(args[0]))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("MONITORING")) {
                    break;
                }


                String spec = args[2].replace("_", "");
                String monitorClassName = "edu.lazymop.tinymop.monitoring.monitors." + spec + "Monitor";
                Class<?> monitorClass = Class.forName(monitorClassName);
                Constructor<?> constructor = monitorClass.getConstructor(String.class);
                Object monitorInstance = constructor.newInstance(spec);


                int spaceIndex = line.indexOf(' ');
                String[] eventsList = line.substring(spaceIndex + 2, line.length() - 1).split(", "); // remove []
                for (String eventStr : eventsList) {
                    int xxIndex = eventStr.indexOf('x');
                    int tildeIndex = eventStr.indexOf('~');
                    if (xxIndex > 0) {
                        // two or more events (e.g., E1~2x3)
                        // eventStr.substring(0, tildeIndex) gives you E1 (event name)
                        // Integer.parseInt(eventStr.substring(tildeIndex + 1, xIndex)) gives you 2 as int (location id)
                        // Integer.parseInt(eventStr.substring(xIndex + 1)) gives you 3 as int (frequency)
                        int location = Integer.parseInt(eventStr.substring(tildeIndex + 1, xxIndex));
                        String eventName = eventStr.substring(0, tildeIndex);
                        int frequency = Integer.parseInt(eventStr.substring(xxIndex + 1));

                        for (int i = 0; i < frequency; i++) {
                            fire(locationsMap, monitorClassName, monitorClass, monitorInstance, location, eventName);
                        }
                    } else {
                        // just one event (e.g., E1~2)
                        // eventStr.substring(0, tildeIndex) gives you E1 (event name)
                        // Integer.parseInt(eventStr.substring(tildeIndex + 1)) gives you 2 as int (location id)
                        int location = Integer.parseInt(eventStr.substring(tildeIndex + 1));
                        String eventName = eventStr.substring(0, tildeIndex);

                        fire(locationsMap, monitorClassName, monitorClass, monitorInstance, location, eventName);
                    }
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        for (String location : violationMessages) {
            LOGGER.log(Level.WARNING, "Violation on line " + location);
        }
    }

    private static void fire(Map<String, String> locationsMap, String monitorClassName, Class<?> monitorClass,
                             Object monitorInstance, int location, String eventName) throws Exception {
        try {
            Field automatonField = monitorClass.getDeclaredField("automaton");
            automatonField.setAccessible(true);
            Object automatonInstance = automatonField.get(monitorInstance);

            Class<?> eventEnumClass = Class.forName(monitorClassName + "$Event");
            Method valueOfMethod = eventEnumClass.getDeclaredMethod("valueOf", String.class);
            valueOfMethod.setAccessible(true);
            Object eventInstance = valueOfMethod.invoke(null, eventName);
            Method fireMethod = automatonInstance.getClass().getDeclaredMethod("fire", Object.class);
            fireMethod.setAccessible(true);
            fireMethod.invoke(automatonInstance, eventInstance);
            Field verdictField = monitorClass.getDeclaredField("verdict");
            verdictField.setAccessible(true);
            Object verdictInstance = verdictField.get(monitorInstance);
            if (verdictInstance.equals(RuntimeMonitor.VerdictCategory.VIOLATING)) {
                violationMessages.add(locationsMap.getOrDefault(String.valueOf(location), "Unknown"));
            }
        } catch (IllegalStateException | InvocationTargetException ignored) {
            violationMessages.add(locationsMap.getOrDefault(String.valueOf(location), "Unknown"));
        }
    }

    public static Map<String, String> readLocations(File locationsFile) throws IOException {
        Map<String, String> locationIDMap = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(locationsFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                int spaceIndex = line.indexOf(' ');
                String[] location = line.substring(spaceIndex + 1).split(":");
                locationIDMap.put(line.substring(0, spaceIndex), location[0] + ":" + location[2]);
            }
        }
        return locationIDMap;
    }
}
