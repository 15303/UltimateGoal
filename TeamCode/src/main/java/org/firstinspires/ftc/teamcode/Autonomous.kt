package org.firstinspires.ftc.teamcode

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.util.ElapsedTime

@Autonomous(name = "Autonomous", group = "Auto") //@Disabled
class Autonomous : LinearOpMode() {
    // Declare OpMode members.
    private val runtime = ElapsedTime()
    private lateinit var robot : Robot

    override fun runOpMode() {

        robot = Robot(this)

        telemetry.addData("Status:", "Initialized")
        telemetry.update()

        // Wait for the game to start (driver presses PLAY)
        waitForStart()
        runtime.reset()

        // run until the end of the match (driver presses STOP)
        sleep(100)
        robot.travel(0.8, 2000)

	var count:Int = robot.ring.count()
	telemetry.addData("rings",count) 

	when(count){
		0 -> //stuff
		1 -> //stuff
		3 -> //stuff
	}


    }

}
