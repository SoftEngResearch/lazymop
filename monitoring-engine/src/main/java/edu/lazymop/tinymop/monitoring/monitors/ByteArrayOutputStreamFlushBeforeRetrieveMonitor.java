package edu.lazymop.tinymop.monitoring.monitors;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.github.oxo42.stateless4j.StateMachine;
import com.github.oxo42.stateless4j.StateMachineConfig;
import com.github.oxo42.stateless4j.delegates.Action2;
import com.github.oxo42.stateless4j.transitions.Transition;
import edu.lazymop.tinymop.specparser.monitoring.RuntimeMonitor;

public class ByteArrayOutputStreamFlushBeforeRetrieveMonitor extends RuntimeMonitor {

    private enum State {

        WRITING_OUTPUTSTREAMCREATED,
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

    private static List<Event> trace;

    private static String specName;

    private static VerdictCategory verdict;

    private StateMachine<State, Event> automaton;

    private StateMachineConfig<State, Event> config;

    public ByteArrayOutputStreamFlushBeforeRetrieveMonitor(String specName) {
        this.specName = specName;
        this.trace = new ArrayList<>();
        this.automaton = buildAutomaton();
        this.verdict = VerdictCategory.DONTKNOW;
    }

    public ByteArrayOutputStreamFlushBeforeRetrieveMonitor clone() {
        ByteArrayOutputStreamFlushBeforeRetrieveMonitor monitor = null;
        try {
            monitor = (ByteArrayOutputStreamFlushBeforeRetrieveMonitor) super.clone();
        } catch (CloneNotSupportedException cnse) {
            cnse.printStackTrace();
        }
        return monitor;
    }

    private StateMachine<State, Event> buildAutomaton() {
        this.config = new StateMachineConfig<>();
        this.config.configure(State.WRITING_OUTPUTSTREAMCREATED).permit(Event.FLUSH, State.FLUSHED)
                .permitReentry(Event.WRITE).permit(Event.CLOSE, State.CLOSED);
        this.config.configure(State.INITIAL).permit(Event.OUTPUTSTREAMINIT, State.WRITING_OUTPUTSTREAMCREATED);
        this.config.configure(State.CLOSED).permitReentry(Event.TOBYTEARRAY).permitReentry(Event.TOSTRING);
        this.config.configure(State.FLUSHED).permitReentry(Event.FLUSH).permitReentry(Event.TOBYTEARRAY)
                .permitReentry(Event.TOSTRING).permit(Event.WRITE, State.WRITING_OUTPUTSTREAMCREATED)
                .permit(Event.CLOSE, State.CLOSED);
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
        // CHECKSTYLE:OFF
        System.out.println("Trace: " + trace + " violated specification " + specName + " on transition "
                + transition.getSource() + " - " + transition.getTrigger() + " -> " + transition.getDestination());
        // CHECKSTYLE:ON
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
        this.verdict = VerdictCategory.DONTKNOW;
        StateMachine<State, Event> stateEventStateMachine = new StateMachine<>(State.INITIAL, this.config);
        stateEventStateMachine.onUnhandledTrigger(new Action2<State, Event>() {

            @Override()
            public void doIt(State state, Event event) {
                handler(new Transition<>(state, State.FAIL, event));
            }
        });
        this.automaton = stateEventStateMachine;
    }

    public void toDot() throws IOException {
        FileOutputStream fos = new FileOutputStream("/tmp/monitor.dot");
        this.config.generateDotFileInto(fos, true);
    }
}
