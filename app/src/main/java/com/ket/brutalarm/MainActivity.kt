package com.ket.brutalarm

import android.app.Dialog
import android.app.TimePickerDialog
import android.icu.util.Calendar
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.format.DateFormat
import android.widget.Button
import android.widget.TextView
import android.widget.TimePicker
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.DialogFragment

class MainActivity : AppCompatActivity() {

     lateinit var text : TextView
     lateinit var button : Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        text = findViewById(R.id.showtext)
        button = findViewById(R.id.Button1)

        button.setOnClickListener {
            showTimePickerDialog()
        }
    }



    private fun showTimePickerDialog() {
        // Use the current time as the default values for the picker
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        // Create and show a TimePickerDialog
        val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
            // Format the time in a 24-hour format or as you prefer
            val time = String.format("%02d:%02d", hourOfDay, minute)
            button.text = time // Update the TextView with the selected time
        }

        // Create a new instance of TimePickerDialog and show it
        TimePickerDialog(this, timeSetListener, hour, minute, DateFormat.is24HourFormat(this)).show()
    }


    }

