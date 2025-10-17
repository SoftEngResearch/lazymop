package edu.lazymop;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import edu.lazymop.types.Event;
import edu.lazymop.types.SpecTraces;
import edu.lazymop.types.Trace;

public class HotMethodStatisticsReporter extends HotMethodReporter {

    private int totalNumberOfRelatedTraces = 0;
    private int totalNumberOfUniqueRelatedTraces = 0;

    private int totalNumberOfRelatedEvents = 0;
    private int totalNumberOfUniqueRelatedEvents = 0;

    private int totalNumberOfIsolatedTraces = 0;
    private int totalNumberOfUniqueIsolatedTraces = 0;

    private int totalNumberOfNonIsolatedTraces = 0;
    private int totalNumberOfUniqueNonIsolatedTraces = 0;

    private int totalNumberOfIsolatedEvents = 0;
    private int totalNumberOfUniqueIsolatedEvents = 0;

    private int totalNumberOfNonIsolatedEvents = 0;
    private int totalNumberOfUniqueNonIsolatedEvents = 0;

    private int isMethodViolating = 0;


    public HotMethodStatisticsReporter(SpecTraces specTraces) {
        super(specTraces);
    }

    @Override
    public boolean isAllIsolated(String method) {
        clearStats();

        // `relatedLocations` is locations that are in `method`
        Set<Integer> relatedLocations = this.methodToRelatedLocation.getOrDefault(method, new HashSet<>());
        if (relatedLocations.isEmpty()) {
            // don't bother, this spec doesn't even have event in `method`
            return true;
        }

        if (!Collections.disjoint(specTraces.violatingLocations, relatedLocations)) {
            // locations that have violation intercepts locations that are in `method` is not empty
            // That mean this method contains violation
            isMethodViolating = 1;
        }

        boolean allIsolated = true;
        // Find traces that are related to `method`
        for (Trace trace : specTraces.traces) {
            // `trace.relatedLocations` is a set of events' location
            if (!Collections.disjoint(trace.relatedLocations, relatedLocations)) {
                // at least one event in `trace` is in `relatedLocations`
                // therefore this `trace` is related
                totalNumberOfRelatedTraces += trace.traceFrequency;
                totalNumberOfUniqueRelatedTraces += 1;

                // Check if trace is isolated - a trace is isolated if all events occurred within the relatedLocations
                // so related locations in trace is a subset of relatedLocations
                boolean isolated = true;
                for (Event event : trace.events) {
                    if (relatedLocations.contains(event.eventLocation)) {
                        totalNumberOfRelatedEvents += event.eventFrequency * trace.traceFrequency;
                        totalNumberOfUniqueRelatedEvents += event.eventFrequency;
                    } else {
                        isolated = false;
                    }
                }

                if (isolated) {
                    totalNumberOfIsolatedTraces += trace.traceFrequency;
                    totalNumberOfUniqueIsolatedTraces += 1;

                    for (Event event : trace.events) {
                        totalNumberOfIsolatedEvents += trace.traceFrequency * event.eventFrequency;
                        totalNumberOfUniqueIsolatedEvents += event.eventFrequency;
                    }
                } else {
                    totalNumberOfNonIsolatedTraces += trace.traceFrequency;
                    totalNumberOfUniqueNonIsolatedTraces += 1;

                    for (Event event : trace.events) {
                        totalNumberOfNonIsolatedEvents += trace.traceFrequency * event.eventFrequency;
                        totalNumberOfUniqueIsolatedEvents += event.eventFrequency;
                    }
                    allIsolated = false;
                }
            }
        }

        return allIsolated;
    }

    public void clearStats() {
        totalNumberOfRelatedTraces = 0;
        totalNumberOfUniqueRelatedTraces = 0;

        totalNumberOfRelatedEvents = 0;
        totalNumberOfUniqueRelatedEvents = 0;

        totalNumberOfIsolatedTraces = 0;
        totalNumberOfUniqueIsolatedTraces = 0;

        totalNumberOfNonIsolatedTraces = 0;
        totalNumberOfUniqueNonIsolatedTraces = 0;

        totalNumberOfIsolatedEvents = 0;
        totalNumberOfUniqueIsolatedEvents = 0;

        totalNumberOfNonIsolatedEvents = 0;
        totalNumberOfUniqueNonIsolatedEvents = 0;

        isMethodViolating = 0;
    }

    public int getTotalNumberOfRelatedTraces() {
        return totalNumberOfRelatedTraces;
    }

    public int getTotalNumberOfUniqueRelatedTraces() {
        return totalNumberOfUniqueRelatedTraces;
    }

    public int getTotalNumberOfIsolatedTraces() {
        return totalNumberOfIsolatedTraces;
    }

    public int getTotalNumberOfUniqueIsolatedTraces() {
        return totalNumberOfUniqueIsolatedTraces;
    }

    public int getTotalNumberOfNonIsolatedTraces() {
        return totalNumberOfNonIsolatedTraces;
    }

    public int getTotalNumberOfUniqueNonIsolatedTraces() {
        return totalNumberOfUniqueNonIsolatedTraces;
    }

    public int getTotalNumberOfIsolatedEvents() {
        return totalNumberOfIsolatedEvents;
    }

    public int getTotalNumberOfUniqueIsolatedEvents() {
        return totalNumberOfUniqueIsolatedEvents;
    }

    public int getTotalNumberOfNonIsolatedEvents() {
        return totalNumberOfNonIsolatedEvents;
    }

    public int getTotalNumberOfUniqueNonIsolatedEvents() {
        return totalNumberOfUniqueNonIsolatedEvents;
    }

    public int getTotalNumberOfRelatedEvents() {
        return totalNumberOfRelatedEvents;
    }

    public int getTotalNumberOfUniqueRelatedEvents() {
        return totalNumberOfUniqueRelatedEvents;
    }

    public int getIsMethodViolating() {
        return isMethodViolating;
    }
}
