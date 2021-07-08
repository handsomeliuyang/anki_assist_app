package com.ly.anki_assist_app.printroom

import androidx.room.TypeConverter
import com.google.gson.Gson
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
        return Gson().fromJson<List<CardIdAndState>>(string, CardIdAndState::class.java)
    }

    @TypeConverter
    fun listToString(list: List<CardIdAndState>):String? {
        return Gson().toJson(list)
    }

}