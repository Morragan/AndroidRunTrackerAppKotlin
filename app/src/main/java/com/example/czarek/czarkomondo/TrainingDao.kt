package com.example.czarek.czarkomondo

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import com.example.czarek.czarkomondo.models.dto.Training

@Dao
interface TrainingDao {
    @Insert
    fun insert(trainingData: Training)

    @Query("SELECT * FROM training_data ORDER BY date DESC")
    fun getAllTrainings(): LiveData<List<Training>>

    @Query("SELECT *FROM training_data WHERE date > :startDate")
    fun getLastTrainings(startDate: Long): List<Training>

    @Delete
    fun deleteTraining(training: Training)
}