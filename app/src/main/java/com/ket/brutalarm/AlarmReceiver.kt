package com.ket.brutalarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val mediaPlayer: MediaPlayer? = MediaPlayer.create(context, R.raw.brutalshortsound1)
        mediaPlayer?.apply {
            isLooping = true
            setVolume(0.9f, 0.9f)
            start()
        }
    }
}
