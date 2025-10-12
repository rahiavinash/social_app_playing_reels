package com.wedo8020.app.roomdb

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object StringListConverter {
    @TypeConverter
    fun fromStringList(stringList: List<String>?): String? {
        if (stringList.isNullOrEmpty()) {
            return null
        }
        val gson = Gson()
        return gson.toJson(stringList)
    }

    @TypeConverter
    fun toStringList(string: String?): List<String>? {
        if (string.isNullOrEmpty()) {
            return null
        }
        val gson = Gson()
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(string, listType)
    }
}