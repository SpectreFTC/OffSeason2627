package org.firstinspires.ftc.teamcode;

import com.seattlesolvers.solverslib.command.CommandScheduler;
import com.seattlesolvers.solverslib.command.InstantCommand;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.util.RobotLog;

public class StateMachineOpmode extends OpMode {
    enum State {
        A, B, C, D
    }
    /**
     * This method is only for an example, it should come from a subsystem
     */
    public void resetMotor(){

    }
    CommandScheduler commandScheduler = CommandScheduler.getInstance();
    @Override
    public void init() {

        StateMachine.resetInstance();

        StateMachine sm = StateMachine.builder()
                .state(State.A, new InstantCommand())
                .state(State.B, new InstantCommand())
                .state(State.C, new InstantCommand())
                .state(State.D, new InstantCommand())
                .initial(State.A)
                .transition(State.A, State.B, () -> gamepad1.a)
                .transition(State.B, State.C, () -> gamepad1.x)
                .transition(State.C, State.D, () -> gamepad1.b)
                .anyTransition(State.A, () -> gamepad1.back)
                .onEnter(State.B, this::resetMotor)
                .onExit(State.B, this::resetMotor)
                .telemetry(telemetry)
                .onStateChange((from, to) -> RobotLog.dd("SM", from + " -> " + to))
                .build();

        commandScheduler.schedule(sm);
    }

    @Override
    public void loop() {
        commandScheduler.run();
    }
}
