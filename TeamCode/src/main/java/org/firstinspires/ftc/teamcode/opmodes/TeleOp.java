package org.firstinspires.ftc.teamcode.opmodes;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.seattlesolvers.solverslib.command.Command;
import com.seattlesolvers.solverslib.command.CommandOpMode;
import com.seattlesolvers.solverslib.command.CommandScheduler;
import com.seattlesolvers.solverslib.command.InstantCommand;
import com.seattlesolvers.solverslib.gamepad.GamepadEx;
import com.seattlesolvers.solverslib.gamepad.GamepadKeys;

import org.firstinspires.ftc.teamcode.StateMachine;
import org.firstinspires.ftc.teamcode.commandbase.commands.Drive.DefaultDriveCommand;
import org.firstinspires.ftc.teamcode.commandbase.commands.Intake.IntakeCommand;
import org.firstinspires.ftc.teamcode.commandbase.commands.Intake.OuttakeCommand;
import org.firstinspires.ftc.teamcode.commandbase.subsystems.DriveSubsystem;
import org.firstinspires.ftc.teamcode.commandbase.subsystems.IntakeSubsystem;
import org.firstinspires.ftc.teamcode.globals.RobotHardware;

public class TeleOp extends OpMode {

    CommandScheduler commandScheduler = CommandScheduler.getInstance();

    private RobotHardware robot;
    private DriveSubsystem driveSubsystem;
    private IntakeSubsystem intakeSubsystem;
    private GamepadEx driverGamepad;

    enum State {
        INTAKE, OUTTAKE, IDLE
    }

    @Override
    public void init() {
        commandScheduler.reset();

        StateMachine.resetInstance();

        robot = RobotHardware.getInstance(this);

        driveSubsystem = new DriveSubsystem(robot.frontLeft, robot.frontRight, robot.backLeft, robot.backRight);

        intakeSubsystem = new IntakeSubsystem(robot.intakeMotor);

        driverGamepad = new GamepadEx(gamepad1);

        StateMachine sm = StateMachine.builder()
                .state(State.INTAKE, new IntakeCommand(intakeSubsystem))
                .state(State.OUTTAKE, new OuttakeCommand(intakeSubsystem))
                .state(State.IDLE, new InstantCommand())
                .transition(State.INTAKE, State.OUTTAKE, () -> driverGamepad.getButton(GamepadKeys.Button.LEFT_BUMPER))
                .transition(State.OUTTAKE, State.INTAKE, () -> driverGamepad.getButton(GamepadKeys.Button.RIGHT_BUMPER))
                .anyTransition(State.IDLE, () -> driverGamepad.getButton(GamepadKeys.Button.A))
                .build();

        commandScheduler.schedule(sm);
    }

    @Override
    public void loop() {

        commandScheduler.run();

        driveSubsystem.driveRobotCentric(driverGamepad.getLeftX(), driverGamepad.getLeftY(), driverGamepad.getRightX());

    }
}
