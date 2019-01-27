package com.example.czarek.czarkomondo

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import android.content.Context
import com.example.czarek.czarkomondo.models.dto.Training


@Database(entities = [Training::class], version = 1, exportSchema = false)
@TypeConverters(DataConverter::class)
abstract class TrainingRoomDatabase : RoomDatabase() {

    abstract fun trainingDataDao(): TrainingDao

    companion object {
        @Volatile
        private var INSTANCE: TrainingRoomDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = INSTANCE ?: synchronized(LOCK) {
            INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
        }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(context.applicationContext, TrainingRoomDatabase::class.java, "training.db")
                .build()

    }
}