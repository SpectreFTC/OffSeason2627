package org.firstinspires.ftc.teamcode.commandbase.subsystems;

import com.seattlesolvers.solverslib.command.SubsystemBase;
import com.seattlesolvers.solverslib.hardware.motors.Motor;
import com.seattlesolvers.solverslib.hardware.motors.MotorEx;

import org.firstinspires.ftc.teamcode.globals.RobotConstants;

public class IntakeSubsystem extends SubsystemBase {

    private final Motor intakeMotor;

    public IntakeSubsystem(MotorEx intakeMotor) {
        this.intakeMotor = intakeMotor;
        intakeMotor.setInverted(false);
    }

    public void setPower(double power) {
        intakeMotor.set(power);
    }

    public void intake() {
        setPower(RobotConstants.intakeSpeed);
    }

    public void outtake() {
        setPower(RobotConstants.outtakeSpeed);
    }

    public void stop() {
        intakeMotor.stopMotor();
    }

}
