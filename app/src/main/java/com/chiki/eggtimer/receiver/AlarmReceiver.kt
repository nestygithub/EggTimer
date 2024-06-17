package com.chiki.eggtimer.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.chiki.eggtimer.R
import com.chiki.eggtimer.util.sendNotification

class AlarmReceiver:BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager = ContextCompat.getSystemService(context,
            NotificationManager::class.java) as NotificationManager
        notificationManager.sendNotification(context.getString(R.string.timer_running),context)
    }
}