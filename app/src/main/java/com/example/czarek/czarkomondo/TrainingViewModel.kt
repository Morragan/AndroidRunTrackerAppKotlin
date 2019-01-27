package com.example.czarek.czarkomondo

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import com.example.czarek.czarkomondo.models.dto.Training
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class TrainingViewModel(application: Application) : AndroidViewModel(application) {
    private var parentJob = Job()
    private val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.Main
    private val scope = CoroutineScope(coroutineContext)

    private val repository: TrainingRepository
    internal val trainingsList: LiveData<List<Training>>

    init {
        val trainingDao = TrainingRoomDatabase.invoke(application).trainingDataDao()
        repository = TrainingRepository(trainingDao)
        trainingsList = repository.allTrainings
    }

    fun insert(training: Training) = scope.launch(Dispatchers.IO){
        repository.insert(training)
    }

    suspend fun getLastTrainings(startDate: Long): List<Training>{
        return repository.getLastTrainings(startDate)
    }

    override fun onCleared() {
        super.onCleared()
        parentJob.cancel()
    }
}