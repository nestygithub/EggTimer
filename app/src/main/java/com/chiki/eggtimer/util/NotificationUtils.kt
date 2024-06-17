package com.chiki.eggtimer.util

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.core.app.NotificationCompat
import com.chiki.eggtimer.MainActivity
import com.chiki.eggtimer.R
import com.chiki.eggtimer.receiver.SnoozeReceiver

private const val NOTIFICATION_ID = 0
private const val REQUEST_CODE = 0

fun NotificationManager.sendNotification(messageBody:String, applicationContext: Context){

    val contentIntent = Intent(applicationContext,MainActivity::class.java)
    val contentPendingIntent = PendingIntent.getActivity(applicationContext, NOTIFICATION_ID,contentIntent,PendingIntent.FLAG_IMMUTABLE)

    val eggImage = BitmapFactory.decodeResource(applicationContext.resources,R.drawable.cooked_egg)
    val bigPictureStyle = NotificationCompat.BigPictureStyle()
        .bigPicture(eggImage)
        .bigLargeIcon(null)

    val snoozeIntent = Intent(applicationContext,SnoozeReceiver::class.java)
    val snoozePendingIntent = PendingIntent.getBroadcast(applicationContext, REQUEST_CODE,snoozeIntent, PendingIntent.FLAG_IMMUTABLE)

    val builder = NotificationCompat.Builder(applicationContext,applicationContext.getString(R.string.egg_notification_channel_id))
        .setStyle(bigPictureStyle)
        .setLargeIcon(eggImage)
        .setSmallIcon(R.drawable.cooked_egg)
        .setContentTitle(applicationContext.getString(R.string.notification_title))
        .setContentText(messageBody)
        .setContentIntent(contentPendingIntent)
        .setAutoCancel(true)
        .addAction(R.drawable.egg_icon,applicationContext.getString(R.string.snooze),snoozePendingIntent)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
    notify(NOTIFICATION_ID,builder.build())
}
fun NotificationManager.cancelNotifications(){
    cancelAll()
}