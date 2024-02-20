package com.ket.brutalarm

import android.annotation.SuppressLint // the Pressed was underline because i could only be access withing the library so this fixes it.
import android.app.TimePickerDialog
import android.icu.util.Calendar
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.format.DateFormat
import androidx.appcompat.app.AppCompatDelegate
import soup.neumorphism.NeumorphButton
import soup.neumorphism.NeumorphImageButton
import soup.neumorphism.ShapeType.Companion.FLAT
import soup.neumorphism.ShapeType.Companion.PRESSED

class MainActivity : AppCompatActivity() {

     private lateinit var buttonDisplayTime1 : NeumorphButton
     private lateinit var buttonRing1 : NeumorphImageButton
     var alarmOn = false


    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        // the text on the button diplays the time the user picked
        buttonDisplayTime1 = findViewById(R.id.Button1)
        buttonRing1 = findViewById(R.id.buttonRing1)

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
        } else {
            buttonRing1.setShapeType(FLAT)
            buttonRing1.setImageResource(R.drawable.baseline_circle_notifications_24)
            buttonDisplayTime1.setShapeType(FLAT)
        }

        buttonDisplayTime1.setOnClickListener {
            showTimePickerDialog()
        }

        buttonRing1.setOnClickListener{

            //sending the button to the switchfunction that will then also modify the display
            switchAlarmOnOff (buttonRing1, buttonDisplayTime1)
        }
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
            editor.putBoolean("ALARM_STATE", true)
        } else {
            buttonRing.setShapeType(FLAT)
            buttonRing.setImageResource(R.drawable.baseline_circle_notifications_24)
            buttonDisplayTime.setShapeType(FLAT)
            editor.putBoolean("ALARM_STATE", false)
        }

        editor.apply() // Save the changes
    }


    private fun showTimePickerDialog() {
        // Use the current time as the default values for the picker
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        // Create and show a TimePickerDialog
        val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
            val time = String.format("%02d:%02d", hourOfDay, minute)
            buttonDisplayTime1.text = time // Update the TextView with the selected time

            // Save the selected time to Shared Preferences
            val sharedPref = getSharedPreferences("com.ket.brutalarm.PREFERENCE_FILE_KEY", MODE_PRIVATE)
            with(sharedPref.edit()) {
                putString("SELECTED_TIME", time)
                apply()
            }
        }

        // Create a new instance of TimePickerDialog and show it
        TimePickerDialog(this, timeSetListener, hour, minute, DateFormat.is24HourFormat(this)).show()
    }


    }

