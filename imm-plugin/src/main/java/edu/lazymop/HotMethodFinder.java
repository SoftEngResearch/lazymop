package edu.lazymop;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.lazymop.types.Event;
import edu.lazymop.types.SpecTraces;
import edu.lazymop.types.Trace;

/**
 * The HotMethodFinder class provides methods to find the methods with the most events.
 * It processes a list of SpecTraces and counts the events for each method.
 */
public class HotMethodFinder {

    /**
     * Gets the methods with the most events from the given list of SpecTraces.
     *
     * @param specTracesList the list of SpecTraces to process
     * @return a map of method locations to their event counts
     */
    public static Map<String, Integer> getMethodsWithMostEvents(List<SpecTraces> specTracesList) {
        Map<String, Integer> methodEventCounter = new HashMap<>();

        for (SpecTraces specTraces : specTracesList) {
            Map<Integer, Integer> specMethodEventCounter = getSpecMethodEventCounter(specTraces);

            // specMethodEventCounter is the counter for individual spec, we need to merge it with methodEventCounter
            for (Map.Entry<Integer, Integer> entry : specMethodEventCounter.entrySet()) {
                String location = specTraces.locationIDMap.get(entry.getKey());
                methodEventCounter.put(location, methodEventCounter.getOrDefault(location, 0) + entry.getValue());
            }
        }

        return methodEventCounter;
    }

    /**
     * Gets the event counter for each method in the given SpecTraces.
     *
     * @param specTraces the SpecTraces to process
     * @return a map of method location IDs to their event counts
     */
    private static Map<Integer, Integer> getSpecMethodEventCounter(SpecTraces specTraces) {
        Map<Integer, Integer> specMethodEventCounter = new HashMap<>();
        for (Trace trace : specTraces.traces) {
            for (Event event : trace.events) {
                // add eventFreq to event.eventLocation counter
                specMethodEventCounter.put(
                        event.eventLocation,
                        specMethodEventCounter.getOrDefault(event.eventLocation, 0)
                                + trace.traceFrequency * event.eventFrequency
                );
            }
        }
        return specMethodEventCounter;
    }
}
