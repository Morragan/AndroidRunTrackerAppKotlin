package com.example.czarek.czarkomondo

import android.arch.lifecycle.LiveData
import android.support.annotation.WorkerThread
import com.example.czarek.czarkomondo.models.dto.Training

class TrainingRepository(private val trainingDao: TrainingDao) {
    var allTrainings: LiveData<List<Training>> = trainingDao.getAllTrainings()
    var queryResult: List<Training> = emptyList()

    @WorkerThread
    suspend fun insert(training: Training) {
        trainingDao.insert(training)
    }

    suspend fun getLastTrainings(startDate: Long): List<Training> {
        return trainingDao.getLastTrainings(startDate)
    }

//    private fun queryFinished(result: List<Training>){
//        queryResult = result
//    }
//
//    private class queryAsyncTask internal constructor(private val mAsyncTaskDao: TrainingDao): AsyncTask<Long, Void, List<Training>>(){
//        private val delegate: TrainingRepository? = null
//
//        override fun doInBackground(vararg params: Long?): List<Training> {
//            return mAsyncTaskDao.getLastTrainings(params[0]!!)
//        }
//
//        override fun onPostExecute(result: List<Training>) {
//            delegate!!.queryFinished(result)
//        }
//    }

}