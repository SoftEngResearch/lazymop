package edu.lazymop.tinymop.monitoring.valg;

/** Exposes and controls a monitor's private trace identity for its Valg agent. */
public interface ValgTrace {
    Object getValgTraceIdentity();

    default void disableValgTraceRecording() {}
}
