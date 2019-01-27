package com.example.czarek.czarkomondo

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationCompat
import com.example.czarek.czarkomondo.App.Companion.CHANNEL_ID
import com.example.czarek.czarkomondo.activities.ResolutionSummaryActivity

class ResolutionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {

        val notification = NotificationCompat.Builder(context!!, CHANNEL_ID)
            .setContentTitle(context.getString(R.string.summary))
            .setContentText(context.getString(R.string.resolution_notification_text))
            .setSmallIcon(R.drawable.ic_play_arrow)
            .setContentIntent(
                PendingIntent.getActivity(
                    context,
                    0,
                    Intent(context, ResolutionSummaryActivity::class.java),
                    0
                )
            )
            .build()
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val NOTIFICATION_ID = 2
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}