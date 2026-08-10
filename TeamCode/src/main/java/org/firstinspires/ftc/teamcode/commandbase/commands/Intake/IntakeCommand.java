package org.firstinspires.ftc.teamcode.commandbase.commands.Intake;

import com.seattlesolvers.solverslib.command.CommandBase;

import org.firstinspires.ftc.teamcode.commandbase.subsystems.IntakeSubsystem;

public class IntakeCommand extends CommandBase {

    private final IntakeSubsystem intake;

    public IntakeCommand(IntakeSubsystem intake) {
        this.intake = intake;

        addRequirements(intake);
    }

    @Override
    public void initialize() {
        intake.intake();
    }

    @Override
    public void end(boolean interrupted) {
        intake.stop();
    }

}
