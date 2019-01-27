package com.example.czarek.czarkomondo

import android.arch.lifecycle.LiveData
import android.support.annotation.WorkerThread
import com.example.czarek.czarkomondo.models.dto.Training

class TrainingRepository(private val trainingDao: TrainingDao) {
    var allTrainings: LiveData<List<Training>> = trainingDao.getAllTrainings()

    @WorkerThread
    suspend fun insert(training: Training) {
        trainingDao.insert(training)
    }

    @WorkerThread
    suspend fun delete(training: Training){
        trainingDao.deleteTraining(training)
    }

    suspend fun getLastTrainings(startDate: Long): List<Training> {
        return trainingDao.getLastTrainings(startDate)
    }
}