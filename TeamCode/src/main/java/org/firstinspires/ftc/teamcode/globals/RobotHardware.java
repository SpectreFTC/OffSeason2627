package org.firstinspires.ftc.teamcode.globals;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.seattlesolvers.solverslib.hardware.motors.Motor;
import com.seattlesolvers.solverslib.hardware.motors.MotorEx;

public final class RobotHardware {
    private static RobotHardware instance;
    private final OpMode opMode;

    public MotorEx intakeMotor;

    public MotorEx backLeft;
    public MotorEx frontLeft;
    public MotorEx backRight;
    public MotorEx frontRight;


    public RobotHardware(OpMode opMode) {


        this.opMode = opMode;
        HardwareMap hardwareMap = opMode.hardwareMap;

        intakeMotor = new MotorEx(hardwareMap, "intakeMotor");

        backLeft = new MotorEx(hardwareMap, "backLeft");
        frontLeft = new MotorEx(hardwareMap, "frontLeft");
        backRight = new MotorEx(hardwareMap, "backRight");
        frontRight = new MotorEx(hardwareMap, "frontRight");

        frontLeft.setInverted(false);
        backLeft.setInverted(false);
        frontRight.setInverted(true);
        backRight.setInverted(true);
    }

    public static RobotHardware getInstance(OpMode opMode) {
        if (opMode == null) {
            return null;
        }

        if (instance == null || instance.opMode != opMode) {
            instance = new RobotHardware(opMode);
        }

        return instance;
    }
}
