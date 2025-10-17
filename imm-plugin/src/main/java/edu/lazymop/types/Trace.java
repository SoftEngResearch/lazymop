package edu.lazymop.types;

import java.util.Arrays;
import java.util.Set;

public class Trace {
    public int traceFrequency;
    public Event[] events;
    public Set<Integer> relatedLocations;

    public Trace(int frequency, Event[] events, Set<Integer> relatedLocations) {
        this.traceFrequency = frequency;
        this.events = events;
        this.relatedLocations = relatedLocations;
    }

    @Override
    public String toString() {
        return "Trace{frequency=" + traceFrequency + ", events=" + Arrays.toString(events) + ", relatedLocations="
                + relatedLocations + "}";
    }
}
