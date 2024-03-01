package com.ket.brutalarm

import android.annotation.SuppressLint // the Pressed was underline because i could only be access withing the library so this fixes it.
import android.app.TimePickerDialog
import android.icu.util.Calendar
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.format.DateFormat
import androidx.appcompat.app.AppCompatDelegate
import soup.neumorphism.NeumorphButton
import soup.neumorphism.NeumorphImageButton
import soup.neumorphism.ShapeType.Companion.FLAT
import soup.neumorphism.ShapeType.Companion.PRESSED
// added du to alarm manager and  classes
import android.content.Context
import android.content.Intent

import android.app.PendingIntent
import android.app.AlarmManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import kotlin.time.Duration.Companion.hours


class MainActivity : AppCompatActivity(), SensorEventListener {

     private lateinit var buttonDisplayTime1 : NeumorphButton
     private lateinit var buttonRing1 : NeumorphImageButton
     private var localAlarmState = false
    lateinit private var mediaPlayer: MediaPlayer

    private lateinit var sensorManagerAccelerometer: SensorManager
    private lateinit var buttonDisplayTime2 : NeumorphButton
    private lateinit var buttonDisplayTime3 : NeumorphButton


    // Variables to hold sensor data
    private var lastUpdate: Long = 0
    private var last_x: Float = 0.0f
    private var last_y: Float = 0.0f
    private var last_z: Float = 0.0f
    private val SHAKE_THRESHOLD = 4000 // Adjust this threshold based on your needs


    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)// phone remain in light mode

        setUpSensor()
        initiliazeMediaplayer ()
        // the text on the button diplays the time the user picked
        buttonDisplayTime1 = findViewById(R.id.Button1)
        buttonRing1 = findViewById(R.id.buttonRing1)

        buttonDisplayTime2 = findViewById(R.id.Button2)// show the sensor data
        buttonDisplayTime3 = findViewById(R.id.Button3)// show the Passed shaked data



        // Restore the saved time if it exists
        val sharedPrefDisplayTime = getSharedPreferences("com.ket.brutalarm.PREFERENCE_FILE_KEY", MODE_PRIVATE)
        buttonDisplayTime1.text = sharedPrefDisplayTime.getString("SELECTED_TIME", "00:00") // Use a default value if not found


        // Restore the saved time if it exists
        val sharedPrefAlarmState = getSharedPreferences("com.ket.brutalarm.PREFERENCE_FILE_KEY", MODE_PRIVATE)
        buttonDisplayTime1.text = sharedPrefAlarmState.getString("SELECTED_TIME", "00:00") // Use a default value if not found

        // Restore the alarm state
        val alarmState = sharedPrefAlarmState.getBoolean("ALARM_STATE", false)
        if(alarmState){
            buttonRing1.setShapeType(PRESSED)
            buttonRing1.setImageResource(R.drawable.baseline_block_24)
            buttonDisplayTime1.setShapeType(PRESSED)
            //initiliazeMediaplayer ()
            //mediaPlayer.start()
        } else {
            buttonRing1.setShapeType(FLAT)
            buttonRing1.setImageResource(R.drawable.baseline_circle_notifications_24)
            buttonDisplayTime1.setShapeType(FLAT)
            //initiliazeMediaplayer ()
            //mediaPlayer.pause()
        }

        buttonDisplayTime1.setOnClickListener {
            showTimePickerDialog()
        }

        buttonRing1.setOnClickListener{

            //sending the button to the switchfunction that will then also modify the display
            switchAlarmOnOff (buttonRing1, buttonDisplayTime1)
            //isRingingTime()
        }

        //override onNewIntent() if your activity might already be running when the intent is received.
        // when triggered by Alarmreceiver
        handleIntent(intent)
        isRingingTime() //not needed anymore, because the only place where it is started, it is from the AlarmReceiver
    }

// setting up the sensor, and working with the data on sensor change
   fun setUpSensor(){

       sensorManagerAccelerometer = getSystemService(SENSOR_SERVICE) as SensorManager
       sensorManagerAccelerometer.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also {
           sensorManagerAccelerometer.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_NORMAL)
       }

    }

    fun initiliazeMediaplayer (){

        mediaPlayer = MediaPlayer.create(this, R.raw.tiktok)
        mediaPlayer.setVolume(0.5f, 0.5f) // reducing the volume to eliminate distortion/peaking

    }

    @SuppressLint("RestrictedApi")
    private fun switchAlarmOnOff(buttonRing: NeumorphImageButton, buttonDisplayTime: NeumorphButton){
        val sharedPrefAlarmOnOff = getSharedPreferences("com.ket.brutalarm.PREFERENCE_FILE_KEY", MODE_PRIVATE)
        val editor = sharedPrefAlarmOnOff.edit()

        //variable to only desativate the alarm by simple click posible when it is not time to ring
        val (currentTime, userTime) = getCurrentAndUserTime()

        // Change the neumorph_shapeType and Change the image source
        if (buttonRing.getShapeType() == FLAT){
            buttonRing.setShapeType(PRESSED)
            buttonRing.setImageResource(R.drawable.baseline_block_24)
            buttonDisplayTime.setShapeType(PRESSED)
            //initiliazeMediaplayer ()
            //mediaPlayer.start()
            editor.putBoolean("ALARM_STATE", true)
            localAlarmState = true // tells localy if the alarm is on or off
        }
        // change icon for hime to know that he gotta shake the phone to stop it
        else if (currentTime == userTime){
            buttonRing.setShapeType(PRESSED)
            buttonRing.setImageResource(R.drawable.baseline_back_hand_24)
            buttonDisplayTime3.text= getString(R.string.ShakeTostopTheAlarm) +  "$userTime"
            buttonDisplayTime.setShapeType(FLAT)
            editor.putBoolean("ALARM_STATE", false)
            localAlarmState = false

        } else {
            // deactivate alarm easily with button only if it is not the ringing time
            if (currentTime != userTime) {
                buttonRing.setShapeType(FLAT)
                buttonRing.setImageResource(R.drawable.baseline_circle_notifications_24)
                buttonDisplayTime.setShapeType(FLAT)

                editor.putBoolean("ALARM_STATE", false)
                localAlarmState = false
            } // else only the shaking to stop will be able to make it flat


           // mediaPlayer.pause() // stops sound when pressed ( now only stopped in the shake to stp
           // editor.putBoolean("ALARM_STATE", false)
            //localAlarmState = false
            // alarmOn = false
        }

        editor.apply() // Save the changes
        //isRingingTime()
    }


    private fun showTimePickerDialog() {
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)

        val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
            val time = String.format("%02d:%02d", hourOfDay, minute)
            buttonDisplayTime1.text = time

            val sharedPref = getSharedPreferences("com.ket.brutalarm.PREFERENCE_FILE_KEY", MODE_PRIVATE)
            with(sharedPref.edit()) {
                putString("SELECTED_TIME", time)
                apply()
            }

            val alarmCalendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hourOfDay)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
            }

            val alarmIntent = Intent(this, AlarmReceiver::class.java)
            val pendingIntent: PendingIntent

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val alarmCanSchedule = getSystemService(AlarmManager::class.java).canScheduleExactAlarms()
                if (!alarmCanSchedule) {
                    // Redirect the user to the settings to get the right permissions
                    val intent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    startActivity(intent)
                    return@OnTimeSetListener
                }
            }

            pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.getBroadcast(this, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            } else {
                PendingIntent.getBroadcast(this, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            }

            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmCalendar.timeInMillis, pendingIntent)
        }

        TimePickerDialog(this, timeSetListener, currentHour, currentMinute, DateFormat.is24HourFormat(this)).show()

        isRingingTime() // check the state after the user does picks the time too
    }


    private fun isRingingTime() {
        //getting data from method for cleaner code
        val (currentTime, userTime) = getCurrentAndUserTime()

        val sharedPrefAlarmState = getSharedPreferences("com.ket.brutalarm.PREFERENCE_FILE_KEY", MODE_PRIVATE)
        val isAlarmEnabled = sharedPrefAlarmState?.getBoolean("ALARM_STATE", false) ?: false

        // start playing sound and check that the alarm was turned on
        if (currentTime == userTime && (localAlarmState || isAlarmEnabled )) {
            initiliazeMediaplayer()
            if (!mediaPlayer.isPlaying){
                mediaPlayer.start()
            }

        }
    }



    /**
     * Retrieves the current time and the user-set time.
     * @return A Pair containing the current time as the first element and the user-set time as the second element.
     */
    private fun getCurrentAndUserTime(): Pair<String, String> {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val currentTime = String.format("%02d:%02d", hour, minute)
        val userTime = buttonDisplayTime1.text.toString()

        return Pair(currentTime, userTime)
    }



    override fun onSensorChanged(event: SensorEvent?) {

        val curTime = System.currentTimeMillis()
        // Only allow one update every 100ms.
        if ((curTime - lastUpdate) > 100) {
            val diffTime = curTime - lastUpdate
            lastUpdate = curTime

            val x = event?.values?.get(0) ?: 0f
            val y = event?.values?.get(1) ?: 0f
            val z = event?.values?.get(2) ?: 0f

            val speed = Math.abs(x + y + z - last_x - last_y - last_z) / diffTime * 10000

            if (speed > SHAKE_THRESHOLD) {
                // Shake detected, stop the alarm here
                stopAlarmByshaking()
            }

            last_x = x
            last_y = y
            last_z = z
        }

        // Displaying feedback to user
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER){

            val xvalue = String.format("%.1f", event.values[0])
            buttonDisplayTime2.text= xvalue
        }
    }
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

        return
    }
    private fun stopAlarmByshaking() {
        mediaPlayer.stop()
       // val time = String.format("%02d:%02d", hourOfDay, minute)
        buttonDisplayTime3.text = getString(R.string.alarmstopped) // will say that sucessfully stopped in the default language
        buttonRing1.setShapeType(FLAT)
        buttonDisplayTime1.setShapeType(FLAT)
        buttonRing1.setImageResource(R.drawable.baseline_circle_notifications_24)

    }



    //new


    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Handle intent if activity is already running
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        // Check if the intent has the action to ring the alarm
        if ("RING_ALARM" == intent.getStringExtra("ACTION")) {
            isRingingTime()
        }
    }

    //new



    override fun onStop() {
        super.onStop() // has to be recalled
       //we dont want the clock to stop at that time mediaPlayer.pause()
    }

    override fun onResume() {
        super.onResume() // has to be recalled
        // hence no need to start after resume mediaPlayer.start()

    }
    override fun onDestroy() {
        super.onDestroy()
        sensorManagerAccelerometer.unregisterListener(this)
    }


}

