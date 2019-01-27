package com.example.czarek.czarkomondo.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Parcelable
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.czarek.czarkomondo.R
import com.example.czarek.czarkomondo.activities.TrainingDetailsActivity
import com.example.czarek.czarkomondo.fragments.TrackTrainingFragment
import com.example.czarek.czarkomondo.models.dto.Training
import kotlinx.android.synthetic.main.training_history_row.view.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class TrainingHistoryAdapter(private val context: Context) :
    RecyclerView.Adapter<TrainingHistoryAdapter.TrainingHistoryViewHolder>() {
    private var trainings: List<Training> = Collections.emptyList()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrainingHistoryViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.training_history_row, parent, false)
        return TrainingHistoryViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return trainings.size
    }

    override fun onBindViewHolder(holder: TrainingHistoryViewHolder, position: Int) {
        holder.setData(trainings[position], position)
    }

    fun setData(trainings: List<Training>) {
        this.trainings = trainings
        notifyDataSetChanged()
    }

    inner class TrainingHistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        @SuppressLint("SetTextI18n")
        fun setData(training: Training, position: Int) {
            itemView.history_training_date.text =
                    SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(training.date)
            itemView.history_distance.text =
                    "%.2f KM".format(training.routeSections.sumByDouble { it.distance.toDouble() }/1000)
            val formattedTime = String.format(
                "%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(training.timeInMillis),
                TimeUnit.MILLISECONDS.toMinutes(training.timeInMillis) % TimeUnit.HOURS.toMinutes(1)
            )
            itemView.history_time.text = formattedTime
            itemView.history_kcal.text = training.caloriesBurnt.toString().plus(" KCAL")

            itemView.setOnClickListener {
                val intent = Intent(context, TrainingDetailsActivity::class.java)
                    .putExtra(TrackTrainingFragment.TIME_DATA_KEY, training.timeInMillis)
                    .putParcelableArrayListExtra(TrackTrainingFragment.ROUTE_SECTIONS_DATA_KEY, training.routeSections as ArrayList<Parcelable>)
                    .putExtra(TrackTrainingFragment.CALORIES_DATA_KEY, training.caloriesBurnt)
                    .putExtra(TrackTrainingFragment.STATUS_DATA_KEY, training.status)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
            }
        }
    }
}