package de.bmoth.modelchecker;

import java.util.Collections;
import java.util.Set;

public class ModelCheckingResult {

    private final int steps;
    private final State lastState;
    private final Type type;
    private final String reason;
    private final Set<StateSpaceNode> stateSpaceRoot;

    public enum Type {
        COUNTER_EXAMPLE_FOUND,
        EXCEEDED_MAX_STEPS,
        VERIFIED,
        ABORTED,
        UNKNOWN,
        STATE_SPACE_COMPLETED
    }

    private ModelCheckingResult(State lastState, int steps, Type type, String reason, Set<StateSpaceNode> stateSpaceRoot) {
        this.lastState = lastState;
        this.steps = steps;
        this.type = type;
        this.reason = reason;
        this.stateSpaceRoot = stateSpaceRoot;
    }

    public static ModelCheckingResult createVerified(int steps) {
        return new ModelCheckingResult(null, steps, Type.VERIFIED, null, null);
    }

    public static ModelCheckingResult createAborted(int steps) {
        return new ModelCheckingResult(null, steps, Type.ABORTED, null, null);
    }

    public static ModelCheckingResult createUnknown(int steps, String reason) {
        return new ModelCheckingResult(null, steps, Type.UNKNOWN, reason, null);
    }

    public static ModelCheckingResult createCounterExampleFound(int steps, State lastState) {
        return new ModelCheckingResult(lastState, steps, Type.COUNTER_EXAMPLE_FOUND, null, null);
    }

    public static ModelCheckingResult createExceededMaxSteps(int maxSteps) {
        return new ModelCheckingResult(null, maxSteps, Type.EXCEEDED_MAX_STEPS, null, null);
    }

    public static ModelCheckingResult createStateSpaceCompleted(int steps, Set<StateSpaceNode> stateSpaceRoot) {
        return new ModelCheckingResult(null, steps, Type.STATE_SPACE_COMPLETED, null, stateSpaceRoot);
    }

    public State getLastState() {
        return lastState;
    }

    public Type getType() {
        return type;
    }

    public boolean isCorrect() {
        return type == Type.VERIFIED || type == Type.STATE_SPACE_COMPLETED;
    }

    public Set<StateSpaceNode> getStateSpaceRoot() {
        return stateSpaceRoot != null ? stateSpaceRoot : Collections.emptySet();
    }

    public int getSteps() {
        return steps;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(type.name()).append(' ');

        switch (type) {
            case COUNTER_EXAMPLE_FOUND:
                sb.append(lastState.toString()).append(' ');
                break;
            case UNKNOWN:
                sb.append(reason).append(' ');
                break;
            case EXCEEDED_MAX_STEPS:
            case VERIFIED:
            case ABORTED:
            case STATE_SPACE_COMPLETED:
        }

        return sb.append("after ").append(steps).append(" steps").toString();
    }
}
