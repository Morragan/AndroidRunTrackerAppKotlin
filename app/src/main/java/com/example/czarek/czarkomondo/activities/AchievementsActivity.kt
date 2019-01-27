package com.example.czarek.czarkomondo.activities

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.example.czarek.czarkomondo.R
import com.example.czarek.czarkomondo.TrainingRepository
import com.example.czarek.czarkomondo.TrainingViewModel
import com.example.czarek.czarkomondo.TrainingViewModelFactory
import kotlinx.android.synthetic.main.activity_achievements.*
import java.util.concurrent.TimeUnit

class AchievementsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_achievements)

        val viewModel = TrainingViewModelFactory(application).create(TrainingViewModel::class.java)
        viewModel.trainingsList.observe(this, Observer { trainings ->
            trainings?.let {
                achievements_trainings_count.text = trainings.size.toString()
                achievements_distance_sum.text =
                    Math.round((trainings.sumByDouble { training -> training.routeSections.sumByDouble { section -> section.distance.toDouble() } } / 10.0) / 100.0)
                        .toString()
                achievements_time_sum.text =
                    (trainings.sumBy { training -> training.timeInMillis.toInt() } / 3_600_000).toString()
                achievements_calories_sum.text = trainings.sumBy { training -> training.caloriesBurnt }.toString()
                achievements_best_distance.text = (Math.round(
                    trainings.maxBy { training -> training.routeSections.sumByDouble { section -> section.distance.toDouble() } }!!
                        .routeSections.sumByDouble { section -> section.distance.toDouble() } * 100.0
                ) / 100.0).toString().plus(" m")
                achievements_best_calories.text =
                    trainings.maxBy { training -> training.caloriesBurnt }!!.caloriesBurnt.toString().plus(" kcal")
                val longestTime = trainings.maxBy { training -> training.timeInMillis }!!.timeInMillis
                achievements_longest_time.text = if (longestTime >= 3600000)
                    String.format(
                        "%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(longestTime),
                        TimeUnit.MILLISECONDS.toMinutes(longestTime) % TimeUnit.HOURS.toMinutes(1),
                        TimeUnit.MILLISECONDS.toSeconds(longestTime) % TimeUnit.MINUTES.toSeconds(1)
                    )
                else
                    String.format(
                        "%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(longestTime) % TimeUnit.HOURS.toMinutes(1),
                        TimeUnit.MILLISECONDS.toSeconds(longestTime) % TimeUnit.MINUTES.toSeconds(1)
                    )
            }
        })
    }
}
