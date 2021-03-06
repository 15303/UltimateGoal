package org.firstinspires.ftc.teamcode.wrappers

import com.qualcomm.hardware.bosch.BNO055IMU
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.hardware.*
import com.qualcomm.robotcore.util.ElapsedTime
import org.firstinspires.ftc.teamcode.wrappers.Motors.*
import kotlin.math.abs


open class Robot(_env: LinearOpMode) {

    private val runtime = ElapsedTime()
    protected val env = _env

    protected var driver: Array<DcMotor>

    val launcher: DcMotor
    private val intake: DcMotor
    private val conveyor: DcMotor
    val arm: DcMotor

    private val grabber: CRServo

    val webcam: TfodWrapper
    val imu: BNO055IMU


    private var ring: Ring

    init {
        // Initialize the hardware variables. Note that the strings used here as parameters
        // to 'get' must correspond to the names assigned during the robot configuration
        // step (using the FTC Robot Controller app on the phone).

        /*
        * WARNING:
        *
        * The enum Motors contains the index of each motor.
        * After changing this, fix Motor's .i attribute for
        * each Motor.
        *
        * */
        driver = arrayOf(
                getMotor(LF.s),
                getMotor(RF.s),
                getMotor(LB.s),
                getMotor(RB.s)
        )

        launcher = getMotor(R_LAUNCH.s)
        intake = getMotor("intake")
        conveyor = getMotor("conveyor")
        arm = getMotor("arm")

        grabber = getServo("grabber")

        webcam = TfodWrapper(env)
        val parameters = BNO055IMU.Parameters()
        parameters.mode = BNO055IMU.SensorMode.IMU
        parameters.angleUnit = BNO055IMU.AngleUnit.DEGREES
        parameters.accelUnit = BNO055IMU.AccelUnit.METERS_PERSEC_PERSEC
        parameters.loggingEnabled = false

        imu = env.hardwareMap.get(BNO055IMU::class.java, "imu")
        imu.initialize(parameters)

        ring = Ring(getColorSensor("sensor"), getDistanceSensor("sensor"))

        //set runmodes
        //encode(*launcher)
        reverse(
                driver[RF.i],
                driver[RB.i],
                launcher,
                intake,
                arm
        )

    }

    private fun getMotor(name: String): DcMotor {
        return env.hardwareMap.get(DcMotor::class.java, name)
    }

    private fun getColorSensor(name: String): ColorSensor {
        return env.hardwareMap.get(ColorSensor::class.java, name)
    }

    private fun getDistanceSensor(name: String): DistanceSensor {
        return env.hardwareMap.get(DistanceSensor::class.java, name)
    }

    private fun getServo(name: String): CRServo {
        return env.hardwareMap.get(CRServo::class.java, name)
    }

    private fun reverse(vararg motors: DcMotor) {
        for (motor in motors) {
            motor.direction = DcMotorSimple.Direction.REVERSE
        }
    }

    fun encode(vararg motors: DcMotor) {
        for (motor in motors) {
            motor.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
            motor.targetPosition = 0
            motor.mode = DcMotor.RunMode.RUN_TO_POSITION
            motor.power = 0.0
        }
    }

    fun encodeDrive() {
        encode(*driver)
    }


    fun travel(power: Double = 1.0,
               ms: Long,
               atime: Long = Math.min(1000,ms),
               useIMU: Boolean = true,
               targetAngle: Float = imu.angularOrientation.firstAngle) {
        accelerate(power, ms, atime, useIMU, targetAngle.toFloat())
        env.sleep(ms)
        drive(0.0)
    }

    private fun accelerate(power: Double,
                           ms: Long,
                           atime: Long = Math.min(1000, ms),
                           useIMU: Boolean = true,
                           targetAngle: Float = imu.angularOrientation.firstAngle) {
        val start: Double = env.runtime
        while (env.opModeIsActive() && (env.runtime - start) * 1000 <= ms) {
            val apower: Double = Math.min((env.runtime - start) / (atime / 1000.0), power)
            if (useIMU) {
                imudrive(apower, angle = targetAngle)
            } else {
                drive(apower)
            }
        }
        off()
    }

    open fun turnTo(ms: Int, targetAngle: Float = -45.0f){
        travel(0.0, 750, targetAngle = targetAngle)
    }

    open fun goTo(power: Double,
             position: Int,
             targetAngle:Float = imu.angularOrientation.firstAngle) {

        accelerateTo(power, position, targetAngle)
        //TODO: add a+bx mintime thing so that it doesn't run for too long
    }

    private fun driverTargetAvg(): Double {
        return (driver[0].targetPosition +
                driver[1].targetPosition +
                driver[2].targetPosition +
                driver[3].targetPosition)/4.0
    }

    private fun driverCurrAvg(): Double {
        return (driver[0].currentPosition +
                driver[1].currentPosition +
                driver[2].currentPosition +
                driver[3].currentPosition)/4.0
    }

    private fun accelerateTo(power:Double,
                             position:Int,
                             targetAngle:Float = imu.angularOrientation.firstAngle) {
        //there must be a better way of doing this
        for(M in driver){
            M.targetPosition = M.currentPosition + position
        }
        val start = env.runtime
        var stage = 0
        var adjustment = 0.0
        while ((stage == 0 || env.runtime - 0.2 < adjustment) && env.opModeIsActive()) {
            if(!env.opModeIsActive()) return
            if (stage == 0 && abs(driverCurrAvg()-driverTargetAvg()) <= 15) {
                stage = 1
                adjustment = env.runtime
            }
            val calculatedPower:
                    Double = Utils.varMin(abs(power),
                (abs(driverCurrAvg() - driverTargetAvg()) / 1500.0 - 0.2), //deceleration
                (env.runtime - start),
                (if(stage==0) 0.2 else 0.0))
            if (driverTargetAvg() - driverCurrAvg() > 0) {
                imudrive(calculatedPower, angle = targetAngle)
            } else {
                imudrive(-calculatedPower, angle = targetAngle)
            }
            env.telemetry.addData("cpower", calculatedPower)
                    .addData("stage, diff", "$stage, ${driverCurrAvg()-driverTargetAvg()}")
            env.telemetry.update()
        }

        drive(0.0)
    }

    fun setLaunchPower(power: Double = 0.0) {
        launcher.power = power

    }

    fun conveyor(p: Double) {
        conveyor.power = p
    }

    fun lift(p: Double) {
        arm.power = p
    }

    fun liftPosition(p: Int) {
        arm.targetPosition = p
    }

    fun getArmMovement(): Double {
        var result = 0.0
//        result = abs(halfSecondAgo - arm.currentPosition)
        result *= arm.power
        return abs(result)
    }

    fun grab(p: Double) {
        grabber.power = p
    }

    private fun imudrive(power: Double, icorrection: Double = 40.0, angle: Float = 0.0f) {
        val correction: Double = (imu.angularOrientation.firstAngle.toDouble()- angle)/ icorrection
        val l = power - correction
        val r = power + correction
        drive(l, r, l, r)
    }

    fun drive(power: Array<Double>) {
        drive(power[0], power[1], power[2], power[3])
    }

    fun drive(lf: Double, rf: Double, lb: Double, rb: Double) {
        driver[LF.i].power = lf * 0.9
        driver[RF.i].power = rf
        driver[LB.i].power = lb * 0.9
        driver[RB.i].power = rb
    }

    fun intake(p: Double) {
        intake.power = p
    }

    fun drive(p: Double) {
        drive(p, p, p, p)
    }

    fun off() {
        drive(0.0)
        intake(0.0)
    }

}
