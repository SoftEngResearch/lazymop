package edu.lazymop;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.lazymop.types.SpecTraces;
import edu.lazymop.types.Trace;

public class HotMethodReporter {

    public final SpecTraces specTraces;
    public final Map<String, Set<Integer>> methodToRelatedLocation;

    public HotMethodReporter(SpecTraces specTraces) {
        this.specTraces = specTraces;
        this.methodToRelatedLocation = new HashMap<>();

        getRelatedLocation();
    }

    public boolean hasTraces(String method) {
        return this.methodToRelatedLocation.containsKey(method);
    }

    public boolean isAllIsolated(String method) {
        // `relatedLocations` is locations that are in `method`
        Set<Integer> relatedLocations = this.methodToRelatedLocation.getOrDefault(method, new HashSet<>());
        if (relatedLocations.isEmpty()) {
            // don't bother, this spec doesn't even have event in `method`
            return true;
        }

        // find traces that are related to `method`
        for (Trace trace : specTraces.traces) {
            // `trace.relatedLocations` is a set of events' location
            if (!Collections.disjoint(trace.relatedLocations, relatedLocations)) {
                // at least one event in `trace` is in `relatedLocations`
                // therefore this `trace` is related

                // check if trace is isolated - a trace is isolated if all events occurred within the relatedLocations
                // so related locations in trace is a subset of relatedLocations
                if (!relatedLocations.containsAll(trace.relatedLocations)) {
                    // not isolated, some location id in trace.relatedLocations is not in relatedLocations
                    return false;
                }
            }
        }
        return true;
    }


    public void getRelatedLocation() {
        for (Map.Entry<Integer, String> entry : specTraces.locationIDMap.entrySet()) {
            // key is location short ID
            // val is location
            if (!specTraces.relatedLocations.contains(entry.getKey())) {
                // no event uses this location, so we can skip it
                // note: it is possible that no event uses the location but the location is in the file because
                // tinymop will still save location from non-creation events to the locations file
                continue;
            }

            methodToRelatedLocation.computeIfAbsent(entry.getValue(), k -> new HashSet<>()).add(entry.getKey());
        }
    }
}
