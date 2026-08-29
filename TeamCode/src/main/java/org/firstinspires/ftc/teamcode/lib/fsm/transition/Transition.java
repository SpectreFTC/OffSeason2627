package org.firstinspires.ftc.teamcode.lib.fsm.transition;

import org.firstinspires.ftc.teamcode.lib.fsm.State;

public interface Transition {
    State from();
    State to();
    boolean condition();
    Runnable action();

    default boolean isGlobal() {
        return from() == null;
    }
}
