package com.example.czarek.czarkomondo.services

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.location.Location
import android.os.*
import android.support.v4.app.NotificationCompat
import android.widget.Toast
import com.example.czarek.czarkomondo.App.Companion.CHANNEL_ID
import com.example.czarek.czarkomondo.MainActivity
import com.example.czarek.czarkomondo.R
import com.example.czarek.czarkomondo.fragments.TrackTrainingFragment
import com.example.czarek.czarkomondo.models.dto.RouteSection
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import java.util.*
import java.util.concurrent.TimeUnit

class TrackTrainingService : Service() {

    private var vibrationInterval = 1000
    private var vibrationsSinceStart = 0
    private lateinit var vibrator: Vibrator
    private var sharedPref: SharedPreferences? = null
    private lateinit var context: Context

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locations: MutableList<RouteSection> = ArrayList()
    private var lastLocation: Location? = null

    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest

    private var locationUpdateState = false
    private var currentTimeInMillis = 0L

    companion object {
        private var timer = Timer()
        var paused = false
        const val NOTIFICATION_ID = 1
        const val MILLIS_DATA_KEY = "com.example.czarek.czarkomondo.trackactivityservice.millisdatakey"
        const val ROUTE_SECTIONS_DATA_KEY = "com.example.czarek.czarkomondo.trackactivityservice.routesectionsdatakey"
        const val RESOLUTION_DATA_KEY = "com.example.czarek.czarkomondo.trackactivityservice.resolutiondatakey"
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        context = this
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        vibrationInterval = sharedPref?.getInt(getString(R.string.preference_vibe_interval), 1000)!!

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)

                if (locationResult == null) return
                if (paused) {
                    lastLocation = null
                    return
                }

                // unless it is the first location after start or resume, save section to list
                if (lastLocation != null) {
                    locations.add(
                        RouteSection(
                            com.example.czarek.czarkomondo.models.dto.Location(
                                lastLocation!!.latitude,
                                lastLocation!!.longitude
                            ),
                            com.example.czarek.czarkomondo.models.dto.Location(
                                locationResult.lastLocation.latitude,
                                locationResult.lastLocation.longitude
                            )
                        )
                    )
                }
                lastLocation = locationResult.lastLocation

                val intent = Intent()
                intent.action = TrackTrainingFragment.BROADCAST_ACTION_LOCATION
                intent.putParcelableArrayListExtra(ROUTE_SECTIONS_DATA_KEY, locations as ArrayList<out Parcelable>)
                sendBroadcast(intent)

                val distance = locations.sumByDouble { section -> section.distance.toDouble() }
                if (distance > (vibrationsSinceStart + 1) * vibrationInterval) {
                    vibrationsSinceStart++
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 100, 75, 100, 200, 100, 150, 100, 200, 100, 75, 100, 200, 100, 150, 1000), -1))
                    }
                    else{
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(longArrayOf(0, 100, 75, 100, 200, 100, 150, 100, 200, 100, 75, 100, 200, 100, 150, 1000), -1)
                    }
                }
            }
        }
        startService()
    }

    override fun onDestroy() {
        super.onDestroy()

        paused = false
        currentTimeInMillis = 0
        timer.cancel()
        timer = Timer()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        Toast.makeText(this, "Timer service stopped. Locations: ".plus(locations.size), Toast.LENGTH_SHORT).show()
    }

    private fun startService() {
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        timer.scheduleAtFixedRate(UpdateTimeTask(), 10, 10)
        timer.scheduleAtFixedRate(UpdateNotificationTask(), 1000, 1000)
        timer.scheduleAtFixedRate(NotifyTimer(), 1000, 500)

        createLocationRequest()

        Toast.makeText(this, "Timer service started", Toast.LENGTH_SHORT).show()

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(currentTimeInMillis.toString())
            .setSmallIcon(R.drawable.ic_play_arrow)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    private fun createLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

        val client = LocationServices.getSettingsClient(context)
        val task = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            locationUpdateState = true
            startUpdatingLocation()
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                val pendingIntent: PendingIntent = exception.resolution
                client.applicationContext.startActivity(
                    Intent(
                        client.applicationContext,
                        MainActivity::class.java
                    ).putExtra(RESOLUTION_DATA_KEY, pendingIntent).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
                val intent = Intent()
                intent.action = TrackTrainingFragment.BROADCAST_ACTION_STOP_TIMER
                sendBroadcast(intent)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startUpdatingLocation() {
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    private inner class UpdateTimeTask : TimerTask() {
        override fun run() {
            if (!paused) currentTimeInMillis += 10
        }
    }

    private inner class NotifyTimer : TimerTask() {
        override fun run() {
            val intent = Intent()
            intent.action = TrackTrainingFragment.BROADCAST_ACTION_TIME
            intent.putExtra(MILLIS_DATA_KEY, currentTimeInMillis)
            sendBroadcast(intent)
        }
    }

    private inner class UpdateNotificationTask : TimerTask() {
        override fun run() {
            if (!paused) {
                val notificationIntent = Intent(context, MainActivity::class.java)
                val pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0)

                val timeFormatted =
                    if (currentTimeInMillis >= 3600000)
                        String.format(
                            "%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(currentTimeInMillis),
                            TimeUnit.MILLISECONDS.toMinutes(currentTimeInMillis) % TimeUnit.HOURS.toMinutes(1),
                            TimeUnit.MILLISECONDS.toSeconds(currentTimeInMillis) % TimeUnit.MINUTES.toSeconds(1)
                        )
                    else
                        String.format(
                            "%02d:%02d",
                            TimeUnit.MILLISECONDS.toMinutes(currentTimeInMillis) % TimeUnit.HOURS.toMinutes(1),
                            TimeUnit.MILLISECONDS.toSeconds(currentTimeInMillis) % TimeUnit.MINUTES.toSeconds(1)
                        )

                val distance = Math.round(locations.sumByDouble { section -> section.distance.toDouble() } * 100.0) / 100.0

                val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setContentTitle(getString(R.string.notification_title))
                    .setContentText(timeFormatted.plus(" ").plus(distance.toString()))
                    .setSmallIcon(R.drawable.ic_play_arrow)
                    .setContentIntent(pendingIntent)
                    .build()

                val mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                mNotificationManager.notify(NOTIFICATION_ID, notification)
            }
        }
    }
}
