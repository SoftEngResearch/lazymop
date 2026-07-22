package edu.lazymop.tinymop.specparser.valg;

/** Hyper parameters used by a Valg agent for one specification. */
public final class ValgConfig {
    /** The default Valg configuration. Used when per-spec hyper parameters are not used. */
    public static final ValgConfig DEFAULT = new ValgConfig(false, 0.9, 0.1, 0.0001, 5.0, 0.0);

    private final boolean disabled;
    private final double alpha;
    private final double epsilon;
    private final double threshold;
    private final double initialCreateValue;
    private final double initialNoCreateValue;

    public ValgConfig(boolean disabled, double alpha, double epsilon, double threshold, double initialCreateValue,
            double initialNoCreateValue) {
        this.disabled = disabled;
        this.alpha = alpha;
        this.epsilon = epsilon;
        this.threshold = threshold;
        this.initialCreateValue = initialCreateValue;
        this.initialNoCreateValue = initialNoCreateValue;
    }

    public static ValgConfig disabled() {
        // Different from DEFAULT in that disabled is set
        return new ValgConfig(true, DEFAULT.alpha, DEFAULT.epsilon, DEFAULT.threshold,
                DEFAULT.initialCreateValue, DEFAULT.initialNoCreateValue);
    }

    // Getters for various components of hyper parameters
    public boolean isDisabled() { return disabled; }
    public double getAlpha() { return alpha; }
    public double getEpsilon() { return epsilon; }
    public double getThreshold() { return threshold; }
    public double getInitialCreateValue() { return initialCreateValue; }
    public double getInitialNoCreateValue() { return initialNoCreateValue; }
}
