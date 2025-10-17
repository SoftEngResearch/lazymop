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

public class ByteArrayOutputStreamFlushBeforeRetrieveMonitor extends RuntimeMonitor {

    private enum State {

        OUTPUTSTREAMCREATED_WRITING,
        INITIAL,
        CLOSED,
        FLUSHED,
        FAIL
    }

    private enum Event {

        OUTPUTSTREAMINIT,
        WRITE,
        FLUSH,
        CLOSE,
        TOBYTEARRAY,
        TOSTRING
    }

    private StateMachine<State, Event> automaton;

    private StateMachineConfig<State, Event> config;

    private static List<Event> trace;

    private static String specName;

    public static VerdictCategory verdict;

    public ByteArrayOutputStreamFlushBeforeRetrieveMonitor(String specName) {
        this.specName = specName;
        this.trace = new ArrayList<>();
        this.automaton = buildAutomaton();
        this.verdict = VerdictCategory.DONTKNOW;
    }

    private StateMachine<State, Event> buildAutomaton() {
        this.config = new StateMachineConfig<>();
        this.config.configure(State.OUTPUTSTREAMCREATED_WRITING).permit(Event.FLUSH, State.FLUSHED).permitReentry(Event.WRITE).permit(Event.CLOSE, State.CLOSED);
        this.config.configure(State.INITIAL).permit(Event.OUTPUTSTREAMINIT, State.OUTPUTSTREAMCREATED_WRITING);
        this.config.configure(State.CLOSED).permitReentry(Event.TOBYTEARRAY).permitReentry(Event.TOSTRING);
        this.config.configure(State.FLUSHED).permitReentry(Event.TOBYTEARRAY).permitReentry(Event.FLUSH).permit(Event.WRITE, State.OUTPUTSTREAMCREATED_WRITING).permit(Event.CLOSE, State.CLOSED).permitReentry(Event.TOSTRING);
        StateMachine<State, Event> stateEventStateMachine = new StateMachine<>(State.INITIAL, this.config);
        stateEventStateMachine.onUnhandledTrigger(new Action2<State, Event>() {

            @Override()
            public void doIt(State state, Event event) {
                handler(new Transition<>(state, State.FAIL, event));
            }
        });
        return stateEventStateMachine;
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
