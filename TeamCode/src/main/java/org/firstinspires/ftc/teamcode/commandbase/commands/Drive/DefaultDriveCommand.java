package org.firstinspires.ftc.teamcode.commandbase.commands.Drive;

import com.seattlesolvers.solverslib.command.CommandBase;

import org.firstinspires.ftc.teamcode.commandbase.subsystems.DriveSubsystem;

import java.util.function.DoubleSupplier;

public class DefaultDriveCommand extends CommandBase {

    private final DriveSubsystem drive;
    private final DoubleSupplier strafe;
    private final DoubleSupplier forward;
    private final DoubleSupplier turn;
    public DefaultDriveCommand(DriveSubsystem drive, DoubleSupplier strafe, DoubleSupplier forward, DoubleSupplier turn) {

        this.drive = drive;
        this.strafe = strafe;
        this.forward = forward;
        this.turn = turn;

        addRequirements(drive);

    }

    @Override
    public void execute() {
        drive.driveRobotCentric(strafe.getAsDouble(), forward.getAsDouble(), turn.getAsDouble());
    }
}
