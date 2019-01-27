package com.example.czarek.czarkomondo

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.support.annotation.RequiresApi

class App : Application() {
    companion object {
        const val CHANNEL_ID = "timerServiceChannel"
    }

    override fun onCreate() {
        super.onCreate()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }
    }

    @RequiresApi(26)
    private fun createNotificationChannel() {
        val notificationChannel =
            NotificationChannel(CHANNEL_ID, "Timer Service Channel", NotificationManager.IMPORTANCE_DEFAULT)

        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(notificationChannel)
    }
}