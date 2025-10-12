package com.wedo8020.app.roomdb

import android.net.Uri
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object ImageListConverter {
    @TypeConverter
    fun fromUriList(uriList: List<Uri>?): String? {
        if (uriList.isNullOrEmpty()) {
            return null
        }
        val gson = Gson()
        return gson.toJson(uriList)
    }

    @TypeConverter
    fun toUriList(uriString: String?): List<Uri>? {
        if (uriString.isNullOrEmpty()) {
            return null
        }
        val gson = Gson()
        val listType = object : TypeToken<List<Uri>>() {}.type
        return gson.fromJson(uriString, listType)
    }
}