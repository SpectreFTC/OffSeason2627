package org.firstinspires.ftc.teamcode.commandbase.commands.Intake;

import com.seattlesolvers.solverslib.command.CommandBase;

import org.firstinspires.ftc.teamcode.commandbase.subsystems.IntakeSubsystem;

public class OuttakeCommand extends CommandBase {

    private final IntakeSubsystem intake;

    public OuttakeCommand(IntakeSubsystem intake) {
        this.intake = intake;

        addRequirements(intake);
    }

    @Override
    public void initialize() {
        intake.outtake();
    }

    @Override
    public void end(boolean interrupted) {
        intake.stop();
    }

}
