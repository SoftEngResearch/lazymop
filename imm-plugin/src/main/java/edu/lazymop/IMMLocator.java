package edu.lazymop;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import edu.lazymop.types.Event;
import edu.lazymop.types.IMMData;
import edu.lazymop.types.SpecTraces;
import edu.lazymop.types.Trace;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

public class IMMLocator {
    public static void identifyIMMs(String tracesDir, boolean stats, boolean newOnly, Log log)
            throws MojoExecutionException {
        try {
            List<SpecTraces> specTracesList = TracesUtil.readDirectory(tracesDir, newOnly, log);

            int totalTraces = 0;
            int totalEvents = 0;
            for (SpecTraces specTraces : specTracesList) {
                int specTotalTraces = 0;
                int specTotalEvents = 0;

                for (Trace trace : specTraces.traces) {
                    totalTraces += trace.traceFrequency;
                    specTotalTraces += trace.traceFrequency;
                    for (Event event : trace.events) {
                        totalEvents += event.eventFrequency * trace.traceFrequency;
                        specTotalEvents += event.eventFrequency * trace.traceFrequency;
                    }
                }

                if (specTotalTraces > 0 || specTotalEvents > 0) {
                    log.debug(specTraces.specName);
                    log.debug("Traces: " + specTotalTraces + ", events: " + specTotalEvents);
                }
            }

            log.info("Total " + totalTraces + " traces and " + totalEvents + " events");

            Map<String, Integer> hotMethods =  HotMethodFinder.getMethodsWithMostEvents(specTracesList);

            if (stats) {
                getIsolatedMethodsStats(specTracesList, hotMethods, log);
            } else {
                getIsolatedMethods(specTracesList, hotMethods, log);
            }

        } catch (Exception exception) {
            exception.printStackTrace();
            throw new MojoExecutionException(exception);
        }
    }

    private static void getIsolatedMethods(List<SpecTraces> specTracesList, Map<String, Integer> hotMethods, Log log) {
        /*
         * methodsIsolationStatus is a [method: [spec: boolean]] map
         * for example, {methodA: {spec1: true, spec2: true}, methodB: {spec2: true, spec4: false}}
         * means all traces in methodA are isolated, and traces from spec4 in methodB are not isolated
         */
        Map<String, Map<String, Boolean>> methodsIsolationStatus = new HashMap<>();

        for (SpecTraces specTraces : specTracesList) {
            HotMethodReporter reporter = new HotMethodReporter(specTraces);

            for (String hotMethod : hotMethods.keySet()) {
                if (reporter.hasTraces(hotMethod)) {
                    methodsIsolationStatus.computeIfAbsent(hotMethod, k -> new HashMap<>())
                            .put(specTraces.specName, reporter.isAllIsolated(hotMethod));
                }
            }
        }

        log.info(methodsIsolationStatus.toString());
        IMMData.methodsIsolationStatus = methodsIsolationStatus;
    }

    private static void getIsolatedMethodsStats(List<SpecTraces> specTracesList, Map<String, Integer> hotMethods, Log log) {
        /*
         * methodsIsolationStatus is a [method: [spec: boolean]] map
         * for example, {methodA: {spec1: true, spec2: true}, methodB: {spec2: true, spec4: false}}
         * means all traces in methodA are isolated, and traces from spec4 in methodB are not isolated
         */
        Map<String, Integer> sortedHotMethods = hotMethods.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        log.debug("# of events\tmethod name");
        for (Map.Entry<String, Integer> hotMethod : sortedHotMethods.entrySet()) {
            log.debug(hotMethod.getValue() + "\t\t" + hotMethod.getKey());
        }

        Map<String, Map<String, Boolean>> methodsIsolationStatus = new HashMap<>();
        Map<String, Map<String, Map<String, Integer>>> stats = new HashMap<>(); // map method to spec to stats

        for (SpecTraces specTraces : specTracesList) {
            HotMethodStatisticsReporter reporter = new HotMethodStatisticsReporter(specTraces);

            for (String hotMethod : hotMethods.keySet()) {
                if (reporter.hasTraces(hotMethod)) {
                    methodsIsolationStatus.computeIfAbsent(hotMethod, k -> new HashMap<>())
                            .put(specTraces.specName, reporter.isAllIsolated(hotMethod));
                    Map<String, Integer> statsMap = stats.computeIfAbsent(hotMethod, k -> new HashMap<>())
                            .computeIfAbsent(specTraces.specName, k -> new HashMap<>());

                    statsMap.put("totalNumberOfRelatedTraces", reporter.getTotalNumberOfRelatedTraces());
                    statsMap.put("totalNumberOfUniqueRelatedTraces", reporter.getTotalNumberOfUniqueRelatedTraces());

                    statsMap.put("totalNumberOfIsolatedTraces", reporter.getTotalNumberOfIsolatedTraces());
                    statsMap.put("totalNumberOfUniqueIsolatedTraces", reporter.getTotalNumberOfUniqueIsolatedTraces());

                    statsMap.put("totalNumberOfNonIsolatedTraces", reporter.getTotalNumberOfNonIsolatedTraces());
                    statsMap.put("totalNumberOfUniqueNonIsolatedTraces", reporter.getTotalNumberOfUniqueNonIsolatedTraces());

                    statsMap.put("totalNumberOfIsolatedEvents", reporter.getTotalNumberOfIsolatedEvents());
                    statsMap.put("totalNumberOfUniqueIsolatedEvents", reporter.getTotalNumberOfUniqueIsolatedEvents());

                    statsMap.put("totalNumberOfNonIsolatedEvents", reporter.getTotalNumberOfNonIsolatedEvents());
                    statsMap.put("totalNumberOfUniqueNonIsolatedEvents", reporter.getTotalNumberOfUniqueNonIsolatedEvents());

                    statsMap.put("totalNumberOfRelatedEvents", reporter.getTotalNumberOfRelatedEvents());
                    statsMap.put("totalNumberOfUniqueRelatedEvents", reporter.getTotalNumberOfUniqueRelatedEvents());

                    statsMap.put("isMethodViolating", reporter.getIsMethodViolating());
                }
            }
        }


        for (String hotMethod : sortedHotMethods.keySet()) {
            log.debug("");
            log.debug(">>>>>>>>>>>>>>> " + hotMethod + " <<<<<<<<<<<<<<<");
            Map<String, Integer> result = new HashMap<>();

            if (!stats.containsKey(hotMethod)) {
                log.error(hotMethod + " is in methods list, but it is not in stats.");
                continue;
            }

            for (Map<String, Integer> statsMap : stats.get(hotMethod).values()) {
                for (Map.Entry<String, Integer> entry : statsMap.entrySet()) {
                    result.put(entry.getKey(), result.getOrDefault(entry.getKey(), 0) + entry.getValue());
                }
            }

            log.debug(result.toString());
            for (Map.Entry<String, Map<String, Integer>> entry : stats.get(hotMethod).entrySet()) {
                log.debug("===== " + entry.getKey() + " =====");
                log.debug(entry.getValue().toString());
            }
        }

        log.debug(methodsIsolationStatus.toString());
        IMMData.methodsIsolationStatus = methodsIsolationStatus;
        IMMData.sortedHotMethods = sortedHotMethods;

        if (IMMData.statistics == null) {
            IMMData.statistics = stats;
            IMMData.writeStatisticsToFile();
        } else {
            // merge stats with IMMData.statistics
            // first, remove all changed methods from IMMData.statistics
            // then, insert new IMM data (stats) to IMMData
            IMMData.statistics.keySet().removeIf(method ->
                    IMMData.changedClasses.contains(method.substring(0, method.lastIndexOf('.'))));

            for (Map.Entry<String, Map<String, Map<String, Integer>>> entry : stats.entrySet()) {
                String method = entry.getKey();
                IMMData.statistics.put(method, entry.getValue());
            }

            log.debug("Final IMMData methods: " + IMMData.statistics.keySet());
            IMMData.writeStatisticsToFile(); // this should only contain all methods
            IMMData.statistics = stats; // this should only contain new methods, we need this to de-instrument next
        }
    }
}
