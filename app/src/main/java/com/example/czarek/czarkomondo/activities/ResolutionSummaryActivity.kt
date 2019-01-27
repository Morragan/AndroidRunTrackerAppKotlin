package com.example.czarek.czarkomondo.activities

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.example.czarek.czarkomondo.R
import com.example.czarek.czarkomondo.TrainingViewModel
import com.example.czarek.czarkomondo.TrainingViewModelFactory
import kotlinx.android.synthetic.main.activity_resolution_summary.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.*
import kotlin.coroutines.CoroutineContext

class ResolutionSummaryActivity : AppCompatActivity(), CoroutineScope {
    lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resolution_summary)
        job = Job()
        val viewModel = TrainingViewModelFactory(application).create(TrainingViewModel::class.java)

        launch {
            val sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
            val timeUnit = sharedPref.getLong(getString(R.string.preference_time_unit), 0L)
            val calendar = Calendar.getInstance()
            if (timeUnit == 0L) {
                calendar.add(Calendar.DAY_OF_YEAR, -7)
            } else {
                calendar.add(Calendar.MONTH, -1)
            }
            val trainings = viewModel.getLastTrainings(calendar.timeInMillis)
            launch(context = Dispatchers.Main) {
                val distanceRan =
                    trainings.sumByDouble { training -> training.routeSections.sumByDouble { section -> section.distance.toDouble() } }
                val distanceGoal = sharedPref.getFloat(getString(R.string.preference_distance), 0F)
                val percentage =
                    if (Math.round(distanceRan / distanceGoal * 100) > 100) 100 else Math.round(distanceRan / distanceGoal * 100)
                progress_bar.setProgressWithAnimation(percentage.toFloat(), 2000)
                percentage_text.text = percentage.toString().plus("%")
                if(percentage >= 100)
                    result_text.text = getString(R.string.resolution_completed)
                else
                    result_text.text = getString(R.string.resolution_not_completed)
                completion_text.text = Math.round(distanceRan).toString().plus("/").plus(distanceGoal).plus("m")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
