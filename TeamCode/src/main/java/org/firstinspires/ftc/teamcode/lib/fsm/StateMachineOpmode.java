package org.firstinspires.ftc.teamcode.lib.fsm;

import com.seattlesolvers.solverslib.command.Command;
import com.seattlesolvers.solverslib.command.CommandBase;
import com.seattlesolvers.solverslib.command.CommandScheduler;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.lib.fsm.transition.TransitionBase;

import java.util.function.Supplier;

/**
 * Example wiring {@link StateMachine#builder()}'s {@code whileIn} to FTCLib
 * {@code Command}s, via {@link CommandLifecycle} — a small helper that forces a fresh
 * {@code Command} instance to be created every time a state is entered, and cancels
 * that exact instance (never a stale or already-finished reference) when the state is
 * left. Reusing one shared {@code Command} object across multiple schedule/cancel
 * cycles is the usual cause of scheduler bookkeeping errors — this pattern makes that
 * mistake structurally hard to make.
 */
@TeleOp(name = "StateMachine Opmode (safe command lifecycle)")
public class StateMachineOpmode extends OpMode {

    private enum State implements org.firstinspires.ftc.teamcode.lib.fsm.State {
        IDLE, AIMED
    }

    private final CommandScheduler scheduler = CommandScheduler.getInstance();
    private StateMachine sm;
    private ShooterSubsystem shooter;

    @Override
    public void init() {
        shooter = new ShooterSubsystem(hardwareMap);

        StateMachine.resetInstance();

        // A fresh HoldFlywheelCommand is created every time AIMED is entered, and that
        // exact instance is canceled every time AIMED is left — never a shared, reused
        // command object.
        CommandLifecycle holdFlywheel = CommandLifecycle.of(scheduler, () -> new HoldFlywheelCommand(shooter));

        sm = StateMachine.builder()
                .initial(State.IDLE)
                .whileIn(State.AIMED, holdFlywheel.start(), holdFlywheel.stop())
                .transition(new TransitionBase(
                        State.IDLE, State.AIMED,
                        () -> gamepad1.a,
                        () -> { /* one-shot transition action, if you need one, goes here */ }))
                .transition(TransitionBase.anyState(State.IDLE, () -> gamepad1.back))
                .build();

        sm.initialize();
    }

    @Override
    public void loop() {
        sm.execute();
        scheduler.run();

        telemetry.addData("state", sm.getCurrentState());
        telemetry.update();
    }

    /**
     * Wraps a {@link Supplier} of fresh {@link Command}s into a start/stop pair safe to
     * hand to {@link StateMachine.Builder#whileIn}. {@link #start()} always schedules a
     * brand-new command instance; {@link #stop()} cancels exactly that instance and
     * clears the reference, so a stale or already-finished command is never
     * re-scheduled or re-canceled.
     */
    private static final class  CommandLifecycle {
        private final CommandScheduler scheduler;
        private final Supplier<Command> factory;
        private Command active;

        private CommandLifecycle(CommandScheduler scheduler, Supplier<Command> factory) {
            this.scheduler = scheduler;
            this.factory = factory;
        }

        static CommandLifecycle of(CommandScheduler scheduler, Supplier<Command> factory) {
            return new CommandLifecycle(scheduler, factory);
        }

        Runnable start() {
            return () -> {
                active = factory.get();
                scheduler.schedule(active);
            };
        }

        Runnable stop() {
            return () -> {
                if (active != null) {
                    scheduler.cancel(active);
                    active = null;
                }
            };
        }
    }

    // ==================== Illustrative subsystem + command ====================

    private static class ShooterSubsystem {
        ShooterSubsystem(com.qualcomm.robotcore.hardware.HardwareMap hardwareMap) {
            // hardware setup omitted for brevity
        }

        void setFlywheelPower(double power) {}
        void stopFlywheel() {}
    }

    /** Loops on its own via the scheduler — StateMachine only starts and stops it once each. */
    private static class HoldFlywheelCommand extends CommandBase {
        private final ShooterSubsystem shooter;

        HoldFlywheelCommand(ShooterSubsystem shooter) {
            this.shooter = shooter;
        }

        @Override public void initialize() { shooter.setFlywheelPower(1.0); }
        @Override public void execute() { /* re-checks/holds RPM each loop, illustrative */ }
        @Override public boolean isFinished() { return false; } // runs until canceled
        @Override public void end(boolean interrupted) { shooter.stopFlywheel(); }
    }
}