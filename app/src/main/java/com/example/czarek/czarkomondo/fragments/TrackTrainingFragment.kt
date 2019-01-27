package com.example.czarek.czarkomondo.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.content.*
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Parcelable
import android.os.SystemClock
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.czarek.czarkomondo.*
import com.example.czarek.czarkomondo.activities.TrainingDetailsActivity
import com.example.czarek.czarkomondo.models.dto.RouteSection
import com.example.czarek.czarkomondo.services.TrackTrainingService
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.fragment_track_training.*
import java.util.*

class TrackTrainingFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var googleMap: GoogleMap
    private var sharedPref: SharedPreferences? = null

    private val trackTrainingBroadcastReceiver = object : BroadcastReceiver() {
        @SuppressLint("SetTextI18n")
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent!!.action
            when (action) {
                BROADCAST_ACTION_TIME -> {
                    measuredTimeInMillis = intent.getLongExtra(TrackTrainingService.MILLIS_DATA_KEY, 0L)
                    if (measuredTimeInMillis == 0L) Log.w("broadcastreceiver", "service returned time 0")
                    time.base = SystemClock.elapsedRealtime() - measuredTimeInMillis
                }
                BROADCAST_ACTION_LOCATION -> {
                    if (activity == null) return
                    routeSections =
                            intent.getParcelableArrayListExtra(TrackTrainingService.ROUTE_SECTIONS_DATA_KEY)
                    context?.let {
                        fillMap()
                        distanceInMeters = routeSections.sumByDouble { section -> section.distance.toDouble() }
                        metrics_distance.text = "%.2f".format(distanceInMeters / 1000)
                        val pace = measuredTimeInMillis / distanceInMeters / 60
                        if (pace < 1000) metrics_rate.text = String.format(
                            "%01d:%02d",
                            Math.floor(pace).toInt(),
                            Math.floor((pace - Math.floor(pace)) * 60).toInt()
                        )
                        if (sharedPref == null) return
                        val weight = sharedPref!!.getInt(getString(R.string.preference_weight), 62)
                        measuredCalories = (0.001033416853125 * weight.toDouble() * distanceInMeters).toInt()
                        metrics_calories.text = measuredCalories.toString()
                    }
                }
                BROADCAST_ACTION_STOP_TIMER -> {
                    stopTimer()
                }
            }
        }
    }

    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 1
        const val REQUEST_CHECK_SETTINGS = 2
        const val BROADCAST_ACTION_TIME =
            "com.example.czarek.czarkomondo.tracktrainingfragment.broadcastreceivertime"
        const val BROADCAST_ACTION_LOCATION =
            "com.example.czarek.czarkomondo.tracktrainingfragment.broadcastreceiverlocation"
        const val BROADCAST_ACTION_STOP_TIMER = "com.example.czarek.czarkomondo.tracktrainingfragment.broadcastreceiverstoptimer"
        const val TIME_DATA_KEY = "com.example.czarek.czarkomondo.tracktrainingfragment.timedatakey"
        const val ROUTE_SECTIONS_DATA_KEY = "com.example.czarek.czarkomondo.tracktrainingfragment.routesectionsdatakey"
        const val CALORIES_DATA_KEY = "com.example.czarek.czarkomondo.tracktrainingfragment.caloriesdatakey"
        const val MODE_DATA_KEY = "com.example.czarek.czarkomondo.tracktrainingfragment.modedatakey"
        private var timerState = TimerState.Idle
        private var measuredTimeInMillis = 0L
        private var distanceInMeters = 0.0
        private var measuredCalories = 0
        private var routeSections: List<RouteSection> = ArrayList()
        const val STATUS_DATA_KEY = "com.example.czarek.czarkomondo.tracktrainingfragment.status"
    }

    override fun onMarkerClick(p0: Marker?) = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_track_training, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPref = activity?.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)

        //region restore timer state
        timerState = TimerState.values()[sharedPref!!.getInt(getString(R.string.timer_state), 0)]
        when (timerState) {
            TimerState.Idle -> {
                time.base = SystemClock.elapsedRealtime()
            }
            TimerState.Running -> {
                time.base = SystemClock.elapsedRealtime() - measuredTimeInMillis
                metrics_distance.text = "%.2f".format(distanceInMeters / 1000)
                val pace = measuredTimeInMillis / distanceInMeters / 60
                if (pace < 1000) metrics_rate.text = String.format(
                    "%01d:%02d",
                    Math.floor(pace).toInt(),
                    Math.floor((pace - Math.floor(pace)) * 60).toInt()
                )
                metrics_calories.text = measuredCalories.toString()
                fillMap()

                go_fab_idle.hide()
                pause_fab_running.show()
            }
            TimerState.Paused -> {
                time.base = SystemClock.elapsedRealtime() - measuredTimeInMillis
                metrics_distance.text = "%.2f".format(distanceInMeters / 1000)
                val pace = measuredTimeInMillis / distanceInMeters / 60
                if (pace < 1000) metrics_rate.text = String.format(
                    "%01d:%02d",
                    Math.floor(pace).toInt(),
                    Math.floor((pace - Math.floor(pace)) * 60).toInt()
                )
                fillMap()

                go_fab_idle.hide()
                fabs_paused.visibility = View.VISIBLE
            }
        }
        //endregion

        //region fabs onclicklisteners
        go_fab_idle.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(
                    context!!,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    activity!!,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE
                )
                return@setOnClickListener
            }

            go_fab_idle.hide()
            pause_fab_running.show()
            timerState = TimerState.Running

            val intentFilter = IntentFilter()
            intentFilter.addAction(BROADCAST_ACTION_LOCATION)
            intentFilter.addAction(BROADCAST_ACTION_TIME)
            intentFilter.addAction(BROADCAST_ACTION_STOP_TIMER)
            activity!!.registerReceiver(trackTrainingBroadcastReceiver, intentFilter)
            ContextCompat.startForegroundService(context!!, Intent(context, TrackTrainingService::class.java))
        }

        pause_fab_running.setOnClickListener {
            TrackTrainingService.paused = true

            pause_fab_running.hide()
            fabs_paused.visibility = View.VISIBLE
            timerState = TimerState.Paused
        }

        go_fab_paused.setOnClickListener {
            TrackTrainingService.paused = false

            fabs_paused.visibility = View.GONE

            pause_fab_running.show()
            timerState = TimerState.Running
        }

        stop_fab_paused.setOnLongClickListener {
            stopTimer()
            return@setOnLongClickListener true
        }
        //endregion

        val intentFilter = IntentFilter()
        intentFilter.addAction(BROADCAST_ACTION_LOCATION)
        intentFilter.addAction(BROADCAST_ACTION_TIME)
        activity!!.registerReceiver(trackTrainingBroadcastReceiver, intentFilter)

        try {
            map.onCreate(savedInstanceState)
            map.onResume()
            map.getMapAsync(this)
        } catch (ex: Resources.NotFoundException) {
            Log.e("Google Map", "Resources\$NotFoundException")
        }
    }

    @SuppressLint("ApplySharedPref")
    override fun onDestroyView() {
        super.onDestroyView()
        try {
            activity!!.unregisterReceiver(trackTrainingBroadcastReceiver)
        } catch (e: IllegalArgumentException) {
        }
        with(sharedPref!!.edit()) {
            putInt(getString(R.string.timer_state), timerState.ordinal)
            commit()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                go_fab_idle.hide()
                pause_fab_running.show()
                timerState = TimerState.Running
                ContextCompat.startForegroundService(context!!, Intent(context, TrackTrainingService::class.java))
            }
        }
    }

    private fun placeMarkerOnMap(location: LatLng) {
        val markerOptions = MarkerOptions().position(location)
        markerOptions.icon(
            BitmapDescriptorFactory.fromBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.ic_user_location)
            )
        )
        googleMap.addMarker(markerOptions)
    }

    private fun setUpMap() {
        if (context == null) return
        if (ActivityCompat.checkSelfPermission(
                context!!,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity!!,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }
        googleMap.isMyLocationEnabled = true
    }

    private fun fillMap() {
        try {
            googleMap.clear()
        } catch (ex: UninitializedPropertyAccessException) {
            Log.w("Google map", "fillMap() invoked with uninitialized googleMap")
            return
        }
        for (i in 0 until routeSections.size) {
            googleMap.addPolyline(
                PolylineOptions().add(
                    routeSections[i].beginning.toLatLng(),
                    routeSections[i].end.toLatLng()
                ).color(ContextCompat.getColor(context!!, R.color.azure)).width(5f)
            )
        }
        if (routeSections.isNotEmpty()) {
            placeMarkerOnMap(routeSections.last().end.toLatLng())
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(routeSections.last().end.toLatLng(), 18f))
        }
    }

    override fun onMapReady(_googleMap: GoogleMap) {
        googleMap = _googleMap

        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.setOnMarkerClickListener(this)

        setUpMap()
    }

    private fun stopTimer(){
        timerState = TimerState.Idle

        activity?.stopService(Intent(context, TrackTrainingService::class.java))
        activity!!.unregisterReceiver(trackTrainingBroadcastReceiver)
        @Suppress("UNCHECKED_CAST") val localRouteSections = routeSections as ArrayList<Parcelable>
        routeSections = ArrayList()
        googleMap.clear()

        time.base = SystemClock.elapsedRealtime()
        metrics_rate.text = getString(R.string.zero_colon)
        metrics_distance.text = getString(R.string.zero_dot)
        metrics_calories.text = getString(R.string.zero)

        fabs_paused.visibility = View.GONE
        pause_fab_running.hide()
        go_fab_idle.show()

        if (localRouteSections.size < 1) return
        val intent = Intent(activity!!, TrainingDetailsActivity::class.java)
            .putExtra(TIME_DATA_KEY, measuredTimeInMillis)
            .putParcelableArrayListExtra(ROUTE_SECTIONS_DATA_KEY, localRouteSections)
            .putExtra(CALORIES_DATA_KEY, measuredCalories)
            .putExtra(MODE_DATA_KEY, false)
        startActivity(intent)
    }

    enum class TimerState { Idle, Running, Paused }
}
