package org.firstinspires.ftc.teamcode.lib.fsm.transition;

import org.firstinspires.ftc.teamcode.lib.fsm.State;

import java.util.function.BooleanSupplier;

public class DebouncedTransition extends TransitionBase {
    private final BooleanSupplier rawCondition;
    private int consecutiveTrue = 0;
    private final int requiredLoops;

    public DebouncedTransition(State from, State to, BooleanSupplier rawCondition, int requiredLoops, Runnable action) {
        super(from, to, null, action); // condition is overridden below, so this arg is unused
        this.rawCondition = rawCondition;
        this.requiredLoops = requiredLoops;
    }

    @Override
    public boolean condition() {
        consecutiveTrue = rawCondition.getAsBoolean() ? consecutiveTrue + 1 : 0;
        if (consecutiveTrue >= requiredLoops) {
            consecutiveTrue = 0;
            return true;
        }
        return false;
    }
}