package com.wedo8020.app.roomdb

import android.net.Uri
import androidx.room.TypeConverter


    object UriConverter {
        @JvmStatic
        @TypeConverter
        fun fromUri(uri: Uri?): String? {
            return uri?.toString()
        }

        @JvmStatic
        @TypeConverter
        fun toUri(uriString: String?): Uri? {
            return uriString?.let { Uri.parse(it) }
        }
    }

