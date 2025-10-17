package edu.lazymop.types;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class SpecTraces {
    private static int nextSpecID = 1;

    public int specID;
    public String specName;
    public List<Trace> traces;
    public Map<Integer, String> locationIDMap;
    public Set<Integer> relatedLocations;

    public Set<Integer> violatingLocations;

    public SpecTraces(String specName, List<Trace> traces, Map<Integer, String> locationIDMap,
                      Set<Integer> relatedLocations, Set<Integer> violatingLocations) {
        this.specID = nextSpecID;
        this.specName = specName;
        this.traces = traces;
        this.locationIDMap = locationIDMap;
        this.relatedLocations = relatedLocations;
        this.violatingLocations = violatingLocations;

        nextSpecID += 1;
    }

    @Override
    public String toString() {
        return "SpecTraces{specID=" + specID + ", specName=" + specName
                + "\ntraces: " + traces
                + "\nlocationIDMap=" + locationIDMap
                + "\nrelatedLocation=" + relatedLocations + "}";
    }
}
