/**
 * A BroadcastReceiver that responds to broadcast intents by playing a specific audio file.
 *
 * This receiver is designed to be triggered by alarm events or other conditions that broadcast intents
 * within the application or from the system. Upon receiving an intent, it initializes a MediaPlayer
 * instance to play an audio file from the application's raw resources directory.
 *
 * The audio playback is configured to loop continuously and play at a high volume level to ensure
 * it captures the user's attention, making it suitable for alarm or notification purposes.
 *
 * Usage:
 * - The AlarmReceiver must be registered in the AndroidManifest.xml file to respond to specific intents.
 * - Ensure the audio file `R.raw.brutalshortsound1` exists in the `res/raw` directory.
 *
 * Example AndroidManifest.xml declaration:
 * `<receiver android:name=".AlarmReceiver"/>`
 *
 * Note:
 * - Context provided to `onReceive` may be used to safely access system services and application-specific resources.
 * - The intent parameter can be used to pass additional data to this receiver when broadcasting the intent.
 */


package com.ket.brutalarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity




class AlarmReceiver : BroadcastReceiver()   {
    override fun onReceive(context: Context?, intent: Intent?) {


            val sharedPrefAlarmState = context?.getSharedPreferences("com.ket.brutalarm.PREFERENCE_FILE_KEY",
                AppCompatActivity.MODE_PRIVATE
            )
            val isAlarmEnabled = sharedPrefAlarmState?.getBoolean("ALARM_STATE", false) ?: false

        if (isAlarmEnabled) {
            val intent = Intent(context, MainActivity::class.java).apply {
                // Flag to indicate that this intent should start the isRingingTime method
                putExtra("ACTION", "RING_ALARM")
                // Add FLAG_ACTIVITY_SINGLE_TOP to ensure the MainActivity is not recreated if it's already running
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            context?.startActivity(intent)
        }

    }
}
