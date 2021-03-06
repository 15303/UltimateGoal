package org.firstinspires.ftc.teamcode.driverControlled

import com.qualcomm.robotcore.eventloop.opmode.Disabled
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorSimple
import com.qualcomm.robotcore.util.ElapsedTime
import com.qualcomm.robotcore.util.Range

@TeleOp(name = "basicDrive", group = "Tele")
@Disabled
class Test : LinearOpMode() {
    // Declare OpMode members.
    private val runtime = ElapsedTime()

    override fun runOpMode() {
        telemetry.addData("Status", "Initialized")
        telemetry.update()

        // Initialize the hardware variables. Note that the strings used here as parameters
        // to 'get' must correspond to the names assigned during the robot configuration
        // step (using the FTC Robot Controller app on the phone).

        val leftFront = hardwareMap.get(DcMotor::class.java, "left_front")
        val rightFront = hardwareMap.get(DcMotor::class.java, "right_front")
        val leftBack = hardwareMap.get(DcMotor::class.java, "left_back")
        val rightBack = hardwareMap.get(DcMotor::class.java, "right_back")

        val launch1 = hardwareMap.get(DcMotor::class.java, "launch1")
        val launch2 = hardwareMap.get(DcMotor::class.java, "launch2")

        // Most robots need the motor on one side to be reversed to drive forward
        // Reverse the motor that runs backwards when connected directly to the battery
        //leftFront.direction = DcMotorSimple.Direction.FORWARD
        //leftBack.direction = DcMotorSimple.Direction.FORWARD //Commented out for redundency
        rightFront.direction = DcMotorSimple.Direction.REVERSE
        rightBack.direction = DcMotorSimple.Direction.REVERSE
        //launch1.direction = DcMotorSimple.Direction.FORWARD
        launch2.direction = DcMotorSimple.Direction.REVERSE

        // Wait for the game to start (driver presses PLAY)
        waitForStart()
        runtime.reset()

        // run until the end of the match (driver presses STOP)
        while (opModeIsActive()) {

            // Setup a variable for each drive wheel to save power level for telemetry
            var leftPower: Double
            var rightPower: Double

            // Choose to drive using either Tank Mode, or POV Mode
            // Comment out the method that's not used.  The default below is POV.

            // POV Mode uses left stick to go forward, and right stick to turn.
            // - This uses basic math to combine motions and is easier to drive straight.
            val drive = -gamepad1.left_stick_y.toDouble()
            val turn = gamepad1.right_stick_x.toDouble()


            leftPower = Range.clip(drive + turn, -1.0, 1.0)
            rightPower = Range.clip(drive - turn, -1.0, 1.0)

            // Tank Mode uses one stick to control each wheel.
            // - This requires no math, but it is hard to drive forward slowly and keep straight.
            // leftPower  = -gamepad1.left_stick_y ;
            // rightPower = -gamepad1.right_stick_y ;

            // Send calculated power to wheels
            rightFront.power = rightPower
            rightBack.power = rightPower
            leftBack.power = leftPower
            leftFront.power = leftPower

            //launching input based on up & down on dpad
            when {
                gamepad1.dpad_down -> {
                    launch1.power = 1.0
                    launch2.power = 2.0
                }
                gamepad1.dpad_up -> {
                    launch1.power = -1.0
                    launch2.power = -1.0
                }
                else -> {
                    launch1.power = 0.0
                    launch2.power = 0.0
                }
            }

            // Show the elapsed game time and wheel power.
            telemetry.addData("Status", "Run Time: $runtime")
            telemetry.addData("Motors", "left (%.2f), right (%.2f)", leftPower, rightPower)
            telemetry.update()
        }
    }
}
