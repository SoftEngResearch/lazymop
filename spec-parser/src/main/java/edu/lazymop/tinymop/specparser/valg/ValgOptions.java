package edu.lazymop.tinymop.specparser.valg;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Build-time Valg options shared by the LazyMOP code generators. */
public final class ValgOptions {
    // Migrated from TraceMOP's EventMethodBody
    private static final Set<String> DIRECT_CREATION_EXCLUSIONS = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList(
                    "Collections_SynchronizedCollection",
                    "Collections_SynchronizedMap",
                    "Console_FillZeroPassword",
                    "Map_UnsafeIterator",
                    "NavigableMap_Modification",
                    "NavigableMap_UnsafeIterator",
                    "NavigableSet_Modification",
                    "ObjectStreamClass_Initialize",
                    "PasswordAuthentication_FillZeroPassword",
                    "PipedStream_SingleThread",
                    "Closeable_MultipleClose"
            )));

    private static final Set<String> CLONE_EVENT_EXCLUSIONS = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList("createSet", "getset1", "getset2")));

    private final boolean enabled;
    private final boolean trajectoryEnabled;
    private final ValgConfig defaultConfig;
    private final Map<String, ValgConfig> specConfigs;

    private ValgOptions(boolean enabled, boolean trajectoryEnabled, ValgConfig defaultConfig,
                        Map<String, ValgConfig> specConfigs) {
        this.enabled = enabled;
        this.trajectoryEnabled = trajectoryEnabled;
        this.defaultConfig = defaultConfig;
        this.specConfigs = Collections.unmodifiableMap(new HashMap<>(specConfigs));
    }

    public static ValgOptions disabled() {
        return new ValgOptions(false, false, ValgConfig.DEFAULT, Collections.emptyMap());
    }

    public static ValgOptions parse(String[] args) {
        boolean enabled = false;
        boolean trajectoryEnabled = false;
        ValgConfig defaultConfig = ValgConfig.DEFAULT;
        Map<String, ValgConfig> specConfigs = new HashMap<>();

        int index = 0;
        while (index < args.length) {
            String option = args[index];
            if ("-valg".equals(option)) {
                enabled = true;
                index += 1;
                if (index < args.length && !args[index].startsWith("-")) {
                    defaultConfig = parseConfig(args[index], false);
                    index += 1;
                }
            } else if ("-traj".equals(option)) {
                trajectoryEnabled = true;
                index += 1;
            } else if ("-spec".equals(option)) {
                if (index + 2 >= args.length) {
                    throw new IllegalArgumentException("Missing arguments for -spec");
                }
                String specName = args[index + 1];
                if (specName.startsWith("-") || specName.trim().isEmpty()) {
                    throw new IllegalArgumentException("Invalid specification name for -spec: " + specName);
                }
                specConfigs.put(specName, parseConfig(args[index + 2], true));
                index += 3;
            } else {
                throw new IllegalArgumentException("Unknown option: " + option);
            }
        }

        if (!enabled && !specConfigs.isEmpty()) {
            throw new IllegalArgumentException("-spec can only be used when -valg is enabled");
        }
        if (!enabled && trajectoryEnabled) {
            throw new IllegalArgumentException("-traj can only be used when -valg is enabled");
        }

        return new ValgOptions(enabled, trajectoryEnabled, defaultConfig, specConfigs);
    }

    private static ValgConfig parseConfig(String value, boolean allowOff) {
        String trimmed = value.trim();
        if (allowOff && "off".equalsIgnoreCase(trimmed)) {
            return ValgConfig.disabled();
        }
        if (!trimmed.startsWith("{") || !trimmed.endsWith("}")) {
            throw new IllegalArgumentException(
                    "Expected Valg configuration {alpha,epsilon,threshold,initc,initn}: " + value);
        }

        String[] values = trimmed.substring(1, trimmed.length() - 1).split(",", -1);
        if (values.length != 5) {
            throw new IllegalArgumentException("Expected five Valg hyperparameter values: " + value);
        }

        try {
            return new ValgConfig(false,
                    Double.parseDouble(values[0].trim()),
                    Double.parseDouble(values[1].trim()),
                    Double.parseDouble(values[2].trim()),
                    Double.parseDouble(values[3].trim()),
                    Double.parseDouble(values[4].trim()));
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Unable to parse Valg hyperparameters: " + value, exception);
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isTrajectoryEnabled() {
        return trajectoryEnabled;
    }

    public ValgConfig getConfig(String specName) {
        ValgConfig config = specConfigs.get(specName);
        return config == null ? defaultConfig : config;
    }

    public boolean isEnabledFor(String specName, int parameterCount) {
        return enabled && parameterCount > 0 && !getConfig(specName).isDisabled();
    }

    /**
     * Mirrors TraceMOP's creation-site exclusions. Some specifications create their useful
     * monitors through cloning, while three multi-parameter creation events must remain intact.
     */
    public boolean shouldInstrument(String specName, String eventUniqueId,
                                    int parameterCount, boolean cloneCreation) {
        if (!isEnabledFor(specName, parameterCount)) {
            return false;
        }
        if (cloneCreation) {
            return !CLONE_EVENT_EXCLUSIONS.contains(eventUniqueId);
        }
        return !DIRECT_CREATION_EXCLUSIONS.contains(specName);
    }
}
