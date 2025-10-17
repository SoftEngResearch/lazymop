package edu.lazymop.tinymop.monitoring.monitors;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.github.oxo42.stateless4j.StateMachine;
import com.github.oxo42.stateless4j.StateMachineConfig;
import com.github.oxo42.stateless4j.transitions.Transition;
import edu.lazymop.tinymop.specparser.monitoring.RuntimeMonitor;

public class CollectionsSynchronizedCollectionMonitor extends RuntimeMonitor {

    private enum State {

        S3,
        S0,
        S1,
        S2
    }

    private enum Event {

        E1, // SYNC
        E2, // SYNCCREATEITER
        E3, // ASYNCCREATEITER
        E4 // ACCESSITER
    }

    private static List<Event> trace;

    private static String specName;

    private static VerdictCategory verdict;

    private StateMachine<State, Event> automaton;

    private StateMachineConfig<State, Event> config;

    public CollectionsSynchronizedCollectionMonitor(String specName) {
        this.specName = specName;
        this.trace = new ArrayList<>();
        this.automaton = buildAutomaton();
        this.verdict = VerdictCategory.DONTKNOW;
    }

    public CollectionsSynchronizedCollectionMonitor clone() {
        CollectionsSynchronizedCollectionMonitor monitor = null;
        try {
            monitor = (CollectionsSynchronizedCollectionMonitor) super.clone();
        } catch (CloneNotSupportedException cnse) {
            cnse.printStackTrace();
        }
        return monitor;
    }

    private StateMachine<State, Event> buildAutomaton() {
        this.config = new StateMachineConfig<>();
        this.config.configure(State.S3)
                .ignore(Event.E1)
                .ignore(Event.E2)
                .ignore(Event.E3)
                .ignore(Event.E4)
                .onEntry(CollectionsSynchronizedCollectionMonitor::handler);
        this.config.configure(State.S0)
                .permit(Event.E1, State.S1)
                .ignore(Event.E4)
                .ignore(Event.E3)
                .ignore(Event.E2);
        this.config.configure(State.S1)
                .permit(Event.E2, State.S2)
                .permit(Event.E3, State.S3);
        this.config.configure(State.S2)
                .permit(Event.E4, State.S3);
        return new StateMachine<>(State.S0, this.config);
    }

    private static void handler(Transition<State, Event> transition) {
        // CHECKSTYLE:OFF
        System.out.println("Trace: " + trace + " violated specification " + specName + " on transition "
                + transition.getSource() + " - " + transition.getTrigger() + " -> " + transition.getDestination());
        // CHECKSTYLE:OFF
        verdict = VerdictCategory.VIOLATING;
    }

    public VerdictCategory runAutomatonOnStrings(List<String> trace) {
        this.trace = new ArrayList<>();
        for (String event : trace) {
            Event currentEvent = Event.valueOf(event.toUpperCase());
            this.trace.add(currentEvent);
            this.automaton.fire(currentEvent);
        }
        return this.verdict;
    }

    public void runAutomatonOnEvents(List<Event> trace) {
        this.trace = trace;
        for (Event event : trace) {
            this.automaton.fire(event);
        }
    }

    public void reset() {
        this.trace = new ArrayList<>();
        this.automaton = new StateMachine<>(State.S0, this.config);
        this.verdict = VerdictCategory.DONTKNOW;
    }

    public void toDot() throws IOException {
        FileOutputStream fos = new FileOutputStream("/tmp/monitor.dot");
        this.config.generateDotFileInto(fos, true);
    }
}
