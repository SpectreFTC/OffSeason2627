package org.firstinspires.ftc.teamcode.lib.fsm.transition;

import org.firstinspires.ftc.teamcode.lib.fsm.State;

import java.util.function.BooleanSupplier;

public class TransitionBase implements Transition {
    private final State from;
    private final State to;
    private final BooleanSupplier condition;
    private final Runnable action;

    public TransitionBase(State from, State to, BooleanSupplier condition, Runnable action) {
        this.from = from;
        this.to = to;
        this.condition = condition;
        this.action = action;
    }

    public TransitionBase(State from, State to, BooleanSupplier condition) {
        this(from, to, condition, null);
    }

    /** from = null means this fires from any state — see Transition.isGlobal(). */
    public static TransitionBase anyState(State to, BooleanSupplier condition, Runnable action) {
        return new TransitionBase(null, to, condition, action);
    }

    public static TransitionBase anyState(State to, BooleanSupplier condition) {
        return anyState(to, condition, null);
    }

    @Override public State from() { return from; }
    @Override public State to() { return to; }
    @Override public boolean condition() { return condition.getAsBoolean(); }
    @Override public Runnable action() { return action; }
}
