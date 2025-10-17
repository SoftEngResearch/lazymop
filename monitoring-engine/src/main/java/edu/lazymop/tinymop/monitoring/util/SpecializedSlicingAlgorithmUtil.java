package edu.lazymop.tinymop.monitoring.util;

import static edu.lazymop.tinymop.monitoring.util.SlicingUtil.getMonitorableTrace;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import edu.lazymop.tinymop.monitoring.GlobalMonitorManager;
import edu.lazymop.tinymop.monitoring.MonitorManager;
import edu.lazymop.tinymop.monitoring.datastructure.Pair;
import edu.lazymop.tinymop.monitoring.datastructure.Trie;
import edu.lazymop.tinymop.specparser.monitoring.RuntimeMonitor;

public class SpecializedSlicingAlgorithmUtil {

    public static void monitorSlices(String specName, MonitorManager monitorManager, Trie.Node root) {
        try {
            long begin = System.nanoTime();
            long checkpoint = System.nanoTime();
            long collectTime = 0;

            String tmp = System.getenv("TINYMOP_COLLECT_TRACES");
            boolean collect = tmp == null || tmp.equals("1");

            List<String> traces = new ArrayList<>();
            List<String> tracesFromChangedClasses = new ArrayList<>();
            int totalBindings = 0;
            int totalNewBindings = 0;
            Map<RuntimeMonitor.VerdictCategory, Set<String>> verdicts = new HashMap<>();
            Stack<Pair> stack = new Stack<>();
            stack.add(new Pair(root, null));

            // Need to map monitorableTrace from ID back to event
            while (!stack.isEmpty()) {
                Pair obj = stack.pop();
                if (obj.node.event > 0) {
                    int result = obj.node.event;
                    int eventName = result & 15;
                    int location = result >> 4;
                    String event = "E" + eventName + "~" + location;
                    if (obj.events == null) {
                        obj.events = new ArrayList<>();
                        obj.events.add(event);
                    } else {
                        obj.events.add(event);
                    }
                    if (monitorManager.locationsInChangedClasses[location]) {
                        obj.fromChangedClasses = true;
                    }
                }

                if (obj.node.monitors > 0 && obj.events != null) {
                    // need to check obj.events != null, because specs like ShutdownHookLateRegister can have monitor
                    // without event
                    collectTime += (System.nanoTime() - checkpoint);
                    RuntimeMonitor monitor = monitorManager.createMonitor();
                    List<String> monitorableTrace = getMonitorableTrace(obj.events);
                    RuntimeMonitor.VerdictCategory verdict;
                    try {
                        verdict = monitorManager.runTraceOnMonitor(monitor, monitorableTrace);
                    } catch (IllegalStateException ex) {
                        verdict = RuntimeMonitor.VerdictCategory.VIOLATING;
                    }

                    if (!verdicts.containsKey(verdict)) {
                        verdicts.put(verdict, new HashSet<>());
                    }
                    String traceInString = compactTrace(obj.node.monitors, obj.events);
                    verdicts.get(verdict).add(traceInString);
                    if (collect) {
                        if (obj.fromChangedClasses) {
                            tracesFromChangedClasses.add(traceInString);
                            totalNewBindings += obj.node.monitors;
                        } else {
                            traces.add(traceInString);
                        }

                        totalBindings += obj.node.monitors;
                    }
                    checkpoint = System.nanoTime();
                }

                if (obj.events == null) {
                    for (Trie.Node child : obj.node.children.values()) {
                        stack.add(new Pair(child, null));
                    }
                } else {
                    if (obj.node.children.size() > 1) {
                        for (Trie.Node child : obj.node.children.values()) {
                            stack.add(new Pair(child, new ArrayList<>(obj.events)));
                        }
                    } else if (obj.node.children.size() == 1) {
                        for (Trie.Node child : obj.node.children.values()) {
                            // Don't need to duplicate events because no one is using it
                            stack.add(new Pair(child, obj.events));
                        }
                    }
                }
            }

            try (FileWriter fw = new FileWriter(getViolationFile(specName), false)) {
                for (RuntimeMonitor.VerdictCategory verdict : verdicts.keySet()) {
                    if (verdict.equals(RuntimeMonitor.VerdictCategory.VIOLATING)) {
                        for (String trace : verdicts.get(verdict)) {
                            fw.write(trace + System.lineSeparator());
                        }
                    }
                }
                long end = System.nanoTime();
                fw.write("MONITORING TIME: " + (end - begin) + System.lineSeparator());
                fw.write("COLLECTING TIME: " + collectTime + System.lineSeparator());
            } catch (IOException ioe) {
                try (FileWriter fw = new FileWriter(getErrorFile(specName), false)) {
                    fw.write(ioe + "\n");
                    fw.write(ioe.getMessage() + "\n");
                    fw.write(ioe.toString());
                } catch (IOException ignored) {
                    // Nothing we can do here
                }
            }

            if (collect) {
                if (!traces.isEmpty()) {
                    try (FileWriter fw = new FileWriter(getTracesFile(specName), false)) {
                        for (String trace : traces) {
                            fw.write(trace + System.lineSeparator());
                        }

                        fw.write("Total " + totalBindings + " bindings" + System.lineSeparator());
                    } catch (IOException ioe) {
                        try (FileWriter fw = new FileWriter(getErrorFile(specName), false)) {
                            fw.write(ioe + "\n");
                            fw.write(ioe.getMessage() + "\n");
                            fw.write(ioe.toString());
                        } catch (IOException ignored) {
                            // Nothing we can do here
                        }
                    }
                }

                if (!tracesFromChangedClasses.isEmpty()) {
                    try (FileWriter fw = new FileWriter(getTracesFromChangedClassFile(specName), false)) {
                        for (String trace : tracesFromChangedClasses) {
                            fw.write(trace + System.lineSeparator());
                        }

                        fw.write("Total " + totalNewBindings + " bindings" + System.lineSeparator());
                    } catch (IOException ioe) {
                        try (FileWriter fw = new FileWriter(getErrorFile(specName), false)) {
                            fw.write(ioe + "\n");
                            fw.write(ioe.getMessage() + "\n");
                            fw.write(ioe.toString());
                        } catch (IOException ignored) {
                            // Nothing we can do here
                        }
                    }
                }

                // Dump location map
                try (FileWriter fw = new FileWriter(getLocationFile(specName), false)) {
                    for (Map.Entry<Integer, String> entry : monitorManager.locationsMapping.entrySet()) {
                        fw.write(entry.getKey() + " " + entry.getValue() + System.lineSeparator());
                    }
                } catch (IOException ioe) {
                    try (FileWriter fw = new FileWriter(getErrorFile(specName), false)) {
                        fw.write(ioe + "\n");
                        fw.write(ioe.getMessage() + "\n");
                        fw.write(ioe.toString());
                    } catch (IOException ignored) {
                        // Nothing we can do here
                    }
                }
            }
        } catch (Exception ex) {
            try (FileWriter fw = new FileWriter(getErrorFile(specName), false)) {
                fw.write(ex + "\n");
                fw.write(ex.getMessage() + "\n");
                fw.write(Arrays.toString(ex.getStackTrace()));
            } catch (IOException ignored) {
                // Nothing we can do here
            }
        }
    }

    private static String compactTrace(int frequency, List<String> events) {
        if (events.isEmpty()) {
            return frequency + " []";
        }

        StringBuilder builder = new StringBuilder();
        builder.append(frequency).append(" [");
        String lastEvent = events.get(0);
        int lastEventFrequency = 1;
        boolean firstEvent = true;
        for (String event : events) {
            if (firstEvent) {
                firstEvent = false;
                continue;
            }
            if (event.equals(lastEvent)) {
                lastEventFrequency += 1;
            } else {
                if (lastEventFrequency > 1) {
                    builder.append(lastEvent).append("x").append(lastEventFrequency).append(", ");
                } else {
                    builder.append(lastEvent).append(", ");
                }

                lastEvent = event;
                lastEventFrequency = 1;
            }
        }

        if (lastEventFrequency > 1) {
            builder.append(lastEvent).append("x").append(lastEventFrequency);
        } else {
            builder.append(lastEvent);
        }

        builder.append("]");
        return builder.toString();
    }

    private static File getViolationFile(String specName) {
        return new File(getDirectory() + File.separator + specName + "-violations").getAbsoluteFile();
    }

    private static File getTracesFile(String specName) {
        return new File(getDirectory() + File.separator + specName + "-traces").getAbsoluteFile();
    }

    private static File getTracesFromChangedClassFile(String specName) {
        return new File(getDirectory() + File.separator + specName + "-changedTraces").getAbsoluteFile();
    }

    private static File getLocationFile(String specName) {
        return new File(getDirectory() + File.separator + specName + "-locations").getAbsoluteFile();
    }

    private static File getErrorFile(String specName) {
        return new File(getDirectory() + File.separator + specName + "-error").getAbsoluteFile();
    }

    public static File getTestFile() {
        return new File(getDirectory() + File.separator + "test-id.txt").getAbsoluteFile();
    }

    public static String getDirectory() {
        String dbPath = System.getenv("TINYMOP_TRACEDB_PATH");
        if (dbPath == null) {
            return System.getProperty("user.dir") + File.separator + GlobalMonitorManager.uuid;
        } else {
            return dbPath + File.separator + GlobalMonitorManager.uuid;
        }
    }
}
