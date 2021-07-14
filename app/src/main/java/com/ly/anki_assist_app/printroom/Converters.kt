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
    fun stringToList(string: String): List<DeckEntity> {
        val itemType = object : TypeToken<List<DeckEntity>>() {}.type
        return Gson().fromJson<List<DeckEntity>>(string, itemType)
    }

    @TypeConverter
    fun listToString(list: List<DeckEntity>):String? {
        return Gson().toJson(list)
    }

}