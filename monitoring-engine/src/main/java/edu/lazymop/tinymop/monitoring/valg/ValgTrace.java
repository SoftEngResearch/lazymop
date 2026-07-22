package edu.lazymop.tinymop.monitoring.valg;

/** Exposes a monitor's current LazyMOP trace to its Valg agent. */
public interface ValgTrace {
    Object getValgTraceIdentity();
}
