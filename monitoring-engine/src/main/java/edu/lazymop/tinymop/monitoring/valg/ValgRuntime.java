package edu.lazymop.tinymop.monitoring.valg;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

/** Owns the per-specification, per-thread, per-location Valg agents used by generated monitors. */
public final class ValgRuntime {
    private static final ConcurrentMap<String, SpecState> SPEC_STATES = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, ConcurrentMap<Integer, String>> LOCATIONS =
            new ConcurrentHashMap<>();
    private static final AtomicBoolean SHUTDOWN_HOOK_REGISTERED = new AtomicBoolean(false);

    private ValgRuntime() {}

    private static final class SpecState {
        private final Set<Object> uniqueTraces = Collections.newSetFromMap(new IdentityHashMap<>());
        private final Set<Integer> suppressedBindings = ConcurrentHashMap.newKeySet();
        private final ConcurrentMap<AgentKey, ValgAgent> agents = new ConcurrentHashMap<>();
    }

    private static final class AgentKey {
        private final long threadId;
        private final int locationId;

        private AgentKey(long threadId, int locationId) {
            this.threadId = threadId;
            this.locationId = locationId;
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof AgentKey)) {
                return false;
            }
            AgentKey key = (AgentKey) other;
            return threadId == key.threadId && locationId == key.locationId;
        }

        @Override
        public int hashCode() {
            int result = Long.hashCode(threadId);
            return 31 * result + locationId;
        }
    }

    private static final class TrajectoryEntry {
        private final String specName;
        private final AgentKey key;
        private final ValgAgent agent;

        private TrajectoryEntry(String specName, AgentKey key, ValgAgent agent) {
            this.specName = specName;
            this.key = key;
            this.agent = agent;
        }

        private String location() {
            Map<Integer, String> specLocations = LOCATIONS.get(specName);
            if (specLocations == null) {
                return "UnknownLocation:" + key.locationId;
            }
            return specLocations.getOrDefault(key.locationId, "UnknownLocation:" + key.locationId);
        }
    }

    public static boolean shouldCreate(String specName, int event, double alpha, double epsilon,
                                       double threshold, double initialCreateValue,
                                       double initialNoCreateValue, boolean saveTrajectory,
                                       Object... bindings) {
        int locationId = event >> 4;
        AgentKey key = new AgentKey(Thread.currentThread().getId(), locationId);
        SpecState state = SPEC_STATES.computeIfAbsent(specName, ignored -> new SpecState());

        int bindingId = 0;
        for (Object binding : bindings) {
            bindingId += System.identityHashCode(binding);
        }
        if (state.suppressedBindings.contains(bindingId)) {
            return false;
        }

        ValgAgent agent = state.agents.computeIfAbsent(key, ignored -> {
            if (saveTrajectory) {
                registerShutdownHook();
            }
            return new ValgAgent(state.uniqueTraces, alpha, epsilon, threshold,
                    initialCreateValue, initialNoCreateValue, saveTrajectory);
        });

        boolean create = agent.decideAction();
        if (!create) {
            agent.clearMonitor();
            // Make sure that the same binding is not considered for future checks
            // otherwise we will have a bunch of extra unique traces for the same binding
            state.suppressedBindings.add(bindingId);
        }
        return create;
    }

    public static void monitorCreated(String specName, int event, ValgTrace monitor) {
        SpecState state = SPEC_STATES.get(specName);
        if (state == null) {
            return;
        }
        AgentKey key = new AgentKey(Thread.currentThread().getId(), event >> 4);
        ValgAgent agent = state.agents.get(key);
        if (agent != null) {
            agent.setMonitor(monitor);
        }
    }

    public static void registerLocation(String specName, int locationId, String location) {
        LOCATIONS.computeIfAbsent(specName, ignored -> new ConcurrentHashMap<>())
                .put(locationId, location);
    }

    private static void registerShutdownHook() {
        if (SHUTDOWN_HOOK_REGISTERED.compareAndSet(false, true)) {
            Runtime.getRuntime().addShutdownHook(new Thread(ValgRuntime::writeTrajectories));
        }
    }

    static void writeTrajectories() {
        List<TrajectoryEntry> entries = new ArrayList<>();
        for (Map.Entry<String, SpecState> specEntry : SPEC_STATES.entrySet()) {
            for (Map.Entry<AgentKey, ValgAgent> agentEntry : specEntry.getValue().agents.entrySet()) {
                entries.add(new TrajectoryEntry(specEntry.getKey(), agentEntry.getKey(), agentEntry.getValue()));
            }
        }

        entries.sort(Comparator.comparing((TrajectoryEntry entry) -> entry.specName)
                .thenComparing(TrajectoryEntry::location)
                .thenComparingLong(entry -> entry.key.threadId));

        File output = new File(System.getProperty("user.dir"), "trajectories");
        try (FileWriter writer = new FileWriter(output, true)) {
            for (TrajectoryEntry entry : entries) {
                String trajectory = entry.agent.getTrajectoryString();
                if (!trajectory.isEmpty()) {
                    writer.write(entry.specName + " @ " + entry.location() + System.lineSeparator());
                    writer.write(" => " + trajectory + System.lineSeparator() + System.lineSeparator());
                }
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}
