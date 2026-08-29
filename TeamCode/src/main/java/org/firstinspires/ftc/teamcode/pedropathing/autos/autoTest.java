package org.firstinspires.ftc.teamcode.pedropathing.autos;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

// Update Pedro imports to use the SolversLib package:
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.Pose;
import com.pedropathing.geometry.BezierCurve;
import com.pedropathing.geometry.BezierLine;
import com.pedropathing.paths.PathChain;


import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.seattlesolvers.solverslib.command.CommandOpMode;
import com.seattlesolvers.solverslib.command.CommandScheduler;
import com.seattlesolvers.solverslib.command.InstantCommand;
import com.seattlesolvers.solverslib.command.SequentialCommandGroup;
import com.seattlesolvers.solverslib.command.WaitCommand;

// Standard Built-in SolversLib Pedro Command Import
import com.seattlesolvers.solverslib.pedroCommand.FollowPathCommand;

import org.firstinspires.ftc.teamcode.commandbase.commands.Intake.OuttakeCommand;
import org.firstinspires.ftc.teamcode.globals.RobotHardware;
import org.firstinspires.ftc.teamcode.commandbase.subsystems.IntakeSubsystem;
import org.firstinspires.ftc.teamcode.pedropathing.tuning.Constants;

@Autonomous(name = "autonTest", group = "tests")
public class autoTest extends CommandOpMode {
    private RobotHardware robot;
    private IntakeSubsystem intakeSubsystem;
    private Follower follower;

    private PathChain score1, retreat1;
    private final Pose startPose = new Pose(72, 72);
    private final Pose endPose = new Pose(72, 120);
    public void buildPaths() {
        score1 = follower.pathBuilder()
                .addPath(
                        new BezierLine(
                                startPose,
                                endPose
                        )
                )
                .setLinearHeadingInterpolation(Math.toRadians(90), Math.toRadians(270))
                .build();
        retreat1 = follower.pathBuilder()
                .addPath(
                        new BezierLine(
                                endPose,
                                startPose
                        )
                )
                .setLinearHeadingInterpolation(Math.toRadians(270), Math.toRadians(90))
                .build();
    }

    @Override
    public void initialize() {
        robot = RobotHardware.getInstance(this);
        intakeSubsystem = new IntakeSubsystem(robot.intakeMotor);

        follower = Constants.createFollower(hardwareMap);
        follower.setStartingPose(startPose);
        buildPaths();
        SequentialCommandGroup autoSequence = new SequentialCommandGroup(
                //go to score1 pose
                new FollowPathCommand(follower, score1),
                //wait 1 sec
                new WaitCommand(1000),

                new OuttakeCommand(intakeSubsystem).withTimeout(1000),

                //go to retreat pose
                new FollowPathCommand(follower, retreat1)
        );

        CommandScheduler.getInstance().schedule(autoSequence);
    }

    @Override
    public void run() {
        super.run();

        if (follower != null) {
            follower.update();
        }
    }
}