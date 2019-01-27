package com.example.czarek.czarkomondo.activities

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.example.czarek.czarkomondo.R
import com.example.czarek.czarkomondo.ResolutionReceiver
import kotlinx.android.synthetic.main.activity_resolution.*
import java.util.*

class ResolutionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resolution)

        resolution_save_button.setOnClickListener {
            val distance = resolution_distance.text.toString().toFloat()
            val distanceUnit = resolution_distance_unit.selectedItemId
            val timeUnit = resolution_time_unit.selectedItemId

            val sharedPrefs = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
            with(sharedPrefs.edit()) {
                putFloat(getString(R.string.preference_distance), if (distanceUnit == 0L) distance * 1000 else distance)
                putLong(getString(R.string.preference_time_unit), timeUnit)
                apply()
            }

            val calendar = Calendar.getInstance()
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
            calendar.set(Calendar.HOUR_OF_DAY, 10)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            while (calendar.before(Calendar.getInstance()))
                calendar.add(Calendar.DATE, 1)
            val interval: Long
            interval = if (timeUnit == 0L)
                604_800_000L
            else
                2_628_000_000L
            startAlarm(calendar, interval)
            finish()
        }
        resolution_cancel_button.setOnClickListener {
            cancelAlarm()
            finish()
        }
    }

    private fun startAlarm(calendar: Calendar, interval: Long) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, ResolutionReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, 3, intent, 0)
        alarmManager.cancel(pendingIntent)
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, interval, pendingIntent)
    }

    private fun cancelAlarm() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, ResolutionReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, 3, intent, 0)
        alarmManager.cancel(pendingIntent)
    }
}
