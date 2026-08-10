package org.firstinspires.ftc.teamcode.opmodes;

import com.seattlesolvers.solverslib.command.CommandOpMode;
import com.seattlesolvers.solverslib.gamepad.GamepadEx;
import com.seattlesolvers.solverslib.gamepad.GamepadKeys;

import org.firstinspires.ftc.teamcode.commandbase.commands.Drive.DefaultDriveCommand;
import org.firstinspires.ftc.teamcode.commandbase.commands.Intake.IntakeCommand;
import org.firstinspires.ftc.teamcode.commandbase.commands.Intake.OuttakeCommand;
import org.firstinspires.ftc.teamcode.commandbase.subsystems.DriveSubsystem;
import org.firstinspires.ftc.teamcode.commandbase.subsystems.IntakeSubsystem;
import org.firstinspires.ftc.teamcode.globals.RobotHardware;

public class TeleOp extends CommandOpMode {

    private RobotHardware robot;
    private DriveSubsystem driveSubsystem;
    private IntakeSubsystem intakeSubsystem;
    private GamepadEx driverGamepad;

    @Override
    public void initialize() {
        robot = RobotHardware.getInstance(this);

        driveSubsystem = new DriveSubsystem(robot.frontLeft, robot.frontRight, robot.backLeft, robot.backRight);

        intakeSubsystem = new IntakeSubsystem(robot.intakeMotor);

        driverGamepad = new GamepadEx(gamepad1);

        driveSubsystem.setDefaultCommand(new DefaultDriveCommand(driveSubsystem, () -> driverGamepad.getLeftX(), () -> driverGamepad.getLeftY(), () -> driverGamepad.getRightX()));

        driverGamepad.getGamepadButton(GamepadKeys.Button.RIGHT_BUMPER).whileHeld(new IntakeCommand(intakeSubsystem));

        driverGamepad.getGamepadButton(GamepadKeys.Button.LEFT_BUMPER).whileHeld(new OuttakeCommand(intakeSubsystem));

    }
}
