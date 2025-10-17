package edu.lazymop.tinymop.monitoring.monitors;

import com.github.oxo42.stateless4j.StateMachine;
import com.github.oxo42.stateless4j.StateMachineConfig;
import com.github.oxo42.stateless4j.delegates.Action2;
import com.github.oxo42.stateless4j.transitions.Transition;
import edu.lazymop.tinymop.specparser.monitoring.RuntimeMonitor;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IteratorHasNextMonitor extends RuntimeMonitor {

    private enum State {

        VIOLATION,
        S0_S1,
        S2
    }

    private enum Event {

        HASNEXTTRUE,
        HASNEXTFALSE,
        NEXT
    }

    private StateMachine<State, Event> automaton;

    private StateMachineConfig<State, Event> config;

    private static List<Event> trace;

    private static String specName;

    public static VerdictCategory verdict;

    public IteratorHasNextMonitor(String specName) {
        this.specName = specName;
        this.trace = new ArrayList<>();
        this.automaton = buildAutomaton();
        this.verdict = VerdictCategory.DONTKNOW;
    }

    private StateMachine<State, Event> buildAutomaton() {
        this.config = new StateMachineConfig<>();
        this.config.configure(State.VIOLATION).ignore(Event.HASNEXTTRUE).ignore(Event.HASNEXTFALSE).ignore(Event.NEXT).onEntry(IteratorHasNextMonitor::handler);
        this.config.configure(State.S0_S1).permit(Event.NEXT, State.VIOLATION).permit(Event.HASNEXTTRUE, State.S2).permitReentry(Event.HASNEXTFALSE);
        this.config.configure(State.S2).permit(Event.NEXT, State.S0_S1).permitReentry(Event.HASNEXTTRUE).permit(Event.HASNEXTFALSE, State.S0_S1);
        return new StateMachine<>(State.S0_S1, this.config);
    }

    private static void handler(Transition<State, Event> transition) {
        System.out.println("Trace: " + trace + " violated specification " + specName + " on transition " + transition.getSource() + " - " + transition.getTrigger() + " -> " + transition.getDestination());
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
        this.automaton = buildAutomaton();
    }

    public void toDot() throws IOException {
        FileOutputStream fos = new FileOutputStream("/tmp/monitor.dot");
        this.config.generateDotFileInto(fos, true);
    }
}
