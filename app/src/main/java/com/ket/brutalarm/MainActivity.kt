package com.ket.brutalarm

import android.annotation.SuppressLint // the Pressed was underline because i could only be access withing the library so this fixes it.
import android.app.TimePickerDialog
import android.content.BroadcastReceiver
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
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build




class MainActivity : AppCompatActivity(), SensorEventListener {

     private lateinit var buttonDisplayTime1 : NeumorphButton
     private lateinit var buttonRing1 : NeumorphImageButton
     var alarmOn = false
    lateinit private var mediaPlayer: MediaPlayer

    private lateinit var sensorManager: SensorManager
    private lateinit var buttonDisplayTime2 : NeumorphButton

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)// phone remain in light mode


        initiliazeMediaplayer ()
        // the text on the button diplays the time the user picked
        buttonDisplayTime1 = findViewById(R.id.Button1)
        buttonRing1 = findViewById(R.id.buttonRing1)

        buttonDisplayTime2 = findViewById(R.id.Button2)// show the sensor data
        setUpSensor()

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
            initiliazeMediaplayer ()
            //mediaPlayer.start()
        } else {
            buttonRing1.setShapeType(FLAT)
            buttonRing1.setImageResource(R.drawable.baseline_circle_notifications_24)
            buttonDisplayTime1.setShapeType(FLAT)
            initiliazeMediaplayer ()
            //mediaPlayer.pause()
        }

        buttonDisplayTime1.setOnClickListener {
            showTimePickerDialog()
        }

        buttonRing1.setOnClickListener{

            //sending the button to the switchfunction that will then also modify the display
            switchAlarmOnOff (buttonRing1, buttonDisplayTime1)
        }


        isRingingTime()
    }


   fun setUpSensor(){


    }

    fun initiliazeMediaplayer (){

        mediaPlayer = MediaPlayer.create(this, R.raw.brutalshortsound1)
        mediaPlayer.setLooping(true) // Set looping for continuous playback
        mediaPlayer.setVolume(0.5f, 0.5f) // reducing the volume to eliminate distortion/peaking

    }

    @SuppressLint("RestrictedApi")
    private fun switchAlarmOnOff(buttonRing: NeumorphImageButton, buttonDisplayTime: NeumorphButton){
        val sharedPref = getSharedPreferences("com.ket.brutalarm.PREFERENCE_FILE_KEY", MODE_PRIVATE)
        val editor = sharedPref.edit()

        // Change the neumorph_shapeType and Change the image source
        if (buttonRing.getShapeType() == FLAT){
            buttonRing.setShapeType(PRESSED)
            buttonRing.setImageResource(R.drawable.baseline_block_24)
            buttonDisplayTime.setShapeType(PRESSED)
            initiliazeMediaplayer ()
            //mediaPlayer.start()
            editor.putBoolean("ALARM_STATE", true)
        } else {
            buttonRing.setShapeType(FLAT)
            buttonRing.setImageResource(R.drawable.baseline_circle_notifications_24)
            buttonDisplayTime.setShapeType(FLAT)
            mediaPlayer.pause() // stops sound when pressed
            editor.putBoolean("ALARM_STATE", false)
        }

        editor.apply() // Save the changes
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
    }


    fun isRingingTime(){
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val currentTime = String.format("%02d:%02d", hour, minute)
        val userTime = buttonDisplayTime1.text.toString()

        // start playing sound
        if (currentTime == userTime ){
            initiliazeMediaplayer()
            mediaPlayer.start()

        }
    }

    override fun onStop() {
        super.onStop() // has to be recalled
        mediaPlayer.pause()
    }

    override fun onResume() {
        super.onResume() // has to be recalled
        //mediaPlayer.start()

    }

    override fun onSensorChanged(event: SensorEvent?) {
        TODO("Not yet implemented")
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        TODO("Not yet implemented")
    }


}

