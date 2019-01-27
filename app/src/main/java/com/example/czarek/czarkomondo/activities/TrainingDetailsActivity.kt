package com.example.czarek.czarkomondo.activities

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.example.czarek.czarkomondo.R
import com.example.czarek.czarkomondo.TrainingRepository
import com.example.czarek.czarkomondo.TrainingViewModel
import com.example.czarek.czarkomondo.TrainingViewModelFactory
import com.example.czarek.czarkomondo.fragments.TrackTrainingFragment
import com.example.czarek.czarkomondo.models.dto.RouteSection
import com.example.czarek.czarkomondo.models.dto.Training
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.PolylineOptions
import kotlinx.android.synthetic.main.activity_training_details.*
import java.util.*
import java.util.concurrent.TimeUnit

class TrainingDetailsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private var sharedPref: SharedPreferences? = null

    private lateinit var currentDate: Date
    private var measuredTimeInMillis = 0L
    private lateinit var routeSections: List<RouteSection>
    private var calories = 0
    private var recommendedWaterIntake = 0
    private var launchedFromHistory = true
    private var statusText = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_training_details)

        currentDate = Calendar.getInstance().time
        measuredTimeInMillis = intent.getLongExtra(TrackTrainingFragment.TIME_DATA_KEY, 0L)
        routeSections =
            intent.getParcelableArrayListExtra<RouteSection>(TrackTrainingFragment.ROUTE_SECTIONS_DATA_KEY) as List<RouteSection>
        calories = intent.getIntExtra(TrackTrainingFragment.CALORIES_DATA_KEY, 0)
        launchedFromHistory = intent.getBooleanExtra(TrackTrainingFragment.MODE_DATA_KEY, true)

        if (launchedFromHistory) {
            statusText = intent.getStringExtra(TrackTrainingFragment.STATUS_DATA_KEY)
            status.setText(statusText)
            status.isFocusable = false
        }
        sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        val weight = sharedPref!!.getInt(getString(R.string.preference_weight), 62)
        recommendedWaterIntake = Math.round(12F / 3_720_000F * weight * measuredTimeInMillis)

        summary_measured_time.text =
            if (measuredTimeInMillis >= 3600000)
                String.format(
                    "%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(measuredTimeInMillis),
                    TimeUnit.MILLISECONDS.toMinutes(measuredTimeInMillis) % TimeUnit.HOURS.toMinutes(1),
                    TimeUnit.MILLISECONDS.toSeconds(measuredTimeInMillis) % TimeUnit.MINUTES.toSeconds(1)
                )
            else
                String.format(
                    "%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(measuredTimeInMillis) % TimeUnit.HOURS.toMinutes(1),
                    TimeUnit.MILLISECONDS.toSeconds(measuredTimeInMillis) % TimeUnit.MINUTES.toSeconds(1)
                )

        val distance = routeSections.sumByDouble { section -> section.distance.toDouble() }
        val distanceInKm = Math.round(distance / 10).toDouble() / 100
        val pace = measuredTimeInMillis / distance / 60
        summary_measured_distance.text = distanceInKm.toString().plus(" km")
        summary_measured_speed.text =
            (Math.round((distance * 360000.0) / measuredTimeInMillis.toDouble()) / 100.0).toString().plus(" km/h")
        summary_measured_pace.text = String.format(
            "%01d:%02d",
            Math.floor(pace).toInt(),
            Math.floor((pace - Math.floor(pace)) * 60).toInt()
        )
        summary_measured_water.text = recommendedWaterIntake.toString().plus(" ml")
        summary_measured_calories.text = calories.toString().plus(" kcal")

        summary_map.onCreate(savedInstanceState)
        summary_map.onResume()
        summary_map.getMapAsync(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (launchedFromHistory) return
        val viewModel = TrainingViewModelFactory(application).create(TrainingViewModel::class.java)
        val training =
            Training(
                currentDate,
                measuredTimeInMillis,
                routeSections,
                status.text.toString(),
                calories,
                recommendedWaterIntake
            )
        viewModel.insert(training)
    }

    override fun onMapReady(_googleMap: GoogleMap) {
        googleMap = _googleMap
        fillMap()
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
                ).color(ContextCompat.getColor(baseContext!!, R.color.azure)).width(5f)
            )
        }
        if (routeSections.isNotEmpty()) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(routeSections.last().end.toLatLng(), 14f))
        }
    }
}
