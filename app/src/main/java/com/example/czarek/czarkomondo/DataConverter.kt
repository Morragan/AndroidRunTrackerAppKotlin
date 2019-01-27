package com.example.czarek.czarkomondo

import android.arch.persistence.room.TypeConverter
import com.example.czarek.czarkomondo.models.dto.RouteSection
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*


class DataConverter {
    private val gson: Gson = Gson()

    @TypeConverter
    fun fromRouteSectionDTOList(routeSections: List<RouteSection>): String {
        val type = object : TypeToken<List<RouteSection>>() {}.type
        return gson.toJson(routeSections, type)
    }

    @TypeConverter
    fun toRouteSectionDTOList(routeSectionsString: String): List<RouteSection> {
        val type = object : TypeToken<List<RouteSection>>() {}.type
        return gson.fromJson<List<RouteSection>>(routeSectionsString, type)
    }

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return if (value == null) null else Date(value)
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}