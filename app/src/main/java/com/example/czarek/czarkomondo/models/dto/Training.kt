package com.example.czarek.czarkomondo.models.dto

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import java.util.*

@Entity(tableName = "training_data")
data class Training(
    @PrimaryKey val date: Date,
    val timeInMillis: Long,
    val routeSections: List<RouteSection>,
    var status: String,
    val caloriesBurnt: Int,
    val recommendedWaterIntake: Int
)