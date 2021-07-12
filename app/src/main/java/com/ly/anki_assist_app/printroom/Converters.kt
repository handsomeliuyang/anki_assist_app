package com.ly.anki_assist_app.printroom

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

class Converters {

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time?.toLong()
    }

    @TypeConverter
    fun stringToList(string: String): List<CardIdAndState> {
        val itemType = object : TypeToken<List<CardIdAndState>>() {}.type
        return Gson().fromJson<List<CardIdAndState>>(string, itemType)
    }

    @TypeConverter
    fun listToString(list: List<CardIdAndState>):String? {
        return Gson().toJson(list)
    }

}