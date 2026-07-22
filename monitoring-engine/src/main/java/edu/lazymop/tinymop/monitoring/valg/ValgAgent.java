package edu.lazymop.tinymop.monitoring.valg;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

/** A Valg agent. Mirrors TraceMOP's RLAgent. */
public final class ValgAgent {
    private double noCreateValue; // Qn
    private double createValue; // Qc
    private double reward;

    private int totalTraces;
    private int duplicateTraces;

    private final double epsilon;
    private final double alpha;

    private ValgTrace monitor;
    private Set<Object> uniqueTraces;

    private int timeStep;

    private final double threshold;
    private boolean converged;
    private boolean convStatus;

    private final boolean saveTrajectory;
    private final List<Step> trajectory;

    private boolean lastAction = true;
    private int lastTimeStep = -1;
    private double lastCreateValue;
    private double lastNoCreateValue;

    private final Random random = new Random();

    private static final class Step {
        private final boolean action;
        private final float reward;
        private final int timeStep;
        private final double createValue;
        private final double noCreateValue;

        private Step(boolean action, double reward, int timeStep, double createValue, double noCreateValue) {
            this.action = action;
            this.reward = (float) reward;
            this.timeStep = timeStep;
            this.createValue = createValue;
            this.noCreateValue = noCreateValue;
        }
    }

    public ValgAgent(Set<Object> uniqueTraces, double alpha, double epsilon, double threshold,
            double initialCreateValue, double initialNoCreateValue, boolean saveTrajectory) {
        this.uniqueTraces = uniqueTraces;
        this.alpha = alpha;
        this.epsilon = epsilon;
        this.threshold = threshold;
        this.createValue = initialCreateValue;
        this.noCreateValue = initialNoCreateValue;
        this.saveTrajectory = saveTrajectory;
        this.trajectory = saveTrajectory ? new ArrayList<>() : null;
    }

    private void checkConverged() {
        if (!converged && Math.abs(1.0 - Math.abs(createValue - noCreateValue)) < threshold) {
            converged = true;
            convStatus = noCreateValue < createValue;
        }
    }

    public boolean decideAction() {
        if (timeStep++ == 0) {
            boolean initialAction = noCreateValue <= createValue;
            lastAction = initialAction;
            lastTimeStep = 0;
            return initialAction;
        }

        if (converged) {
            return convStatus;
        }

        int currentTimeStep = timeStep - 1;
        lastCreateValue = createValue;
        lastNoCreateValue = noCreateValue;
        updateReward();

        boolean action;
        if (random.nextDouble() < epsilon) {
            action = random.nextBoolean();
        } else {
            action = noCreateValue <= createValue;
        }

        if (saveTrajectory && lastTimeStep >= 0) {
            trajectory.add(new Step(lastAction, reward, lastTimeStep,
                    lastCreateValue, lastNoCreateValue));
        }

        checkConverged();
        lastAction = action;
        lastTimeStep = currentTimeStep;
        return action;
    }

    private void updateReward() {
        if (monitor != null) {
            totalTraces += 1;
            Object traceIdentity = monitor.getValgTraceIdentity();
            if (uniqueTraces.add(traceIdentity)) {
                reward = 1.0;
            } else {
                duplicateTraces += 1;
                reward = 0.0;
            }
            createValue = createValue + alpha * (reward - createValue);
        } else {
            reward = totalTraces == 0 ? 0.0 : (double) duplicateTraces / totalTraces;
            noCreateValue = noCreateValue + alpha * (reward - noCreateValue);
        }
    }

    public void setMonitor(ValgTrace monitor) {
        this.monitor = monitor;
    }

    public void clearMonitor() {
        monitor = null;
    }

    public String getTrajectoryString() {
        if (!saveTrajectory) {
            return "";
        }

        if (!converged && lastTimeStep >= 0) {
            double finalReward;
            if (monitor != null) {
                finalReward = uniqueTraces.contains(monitor.getValgTraceIdentity()) ? 0.0 : 1.0;
            } else {
                finalReward = totalTraces == 0 ? 0.0 : (double) duplicateTraces / totalTraces;
            }
            trajectory.add(new Step(lastAction, finalReward, lastTimeStep,
                    createValue, noCreateValue));
        }

        StringBuilder result = new StringBuilder();
        for (Step step : trajectory) {
            if (result.length() > 0) {
                result.append(' ');
            }
            result.append('<')
                    .append(step.timeStep)
                    .append(": A=")
                    .append(step.action ? "create" : "ncreate")
                    .append(", R=")
                    .append(String.format("%.2f", step.reward))
                    .append(", Qc=")
                    .append(String.format("%.2f", step.createValue))
                    .append(", Qn=")
                    .append(String.format("%.2f", step.noCreateValue))
                    .append('>');
        }
        if (converged) {
            if (result.length() > 0) {
                result.append(' ');
            }
            result.append("[converged]");
        }
        return result.toString();
    }
}
