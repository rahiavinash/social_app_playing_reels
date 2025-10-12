package com.wedo8020.app.dataSources

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream

object ImagesVideoConverter {
     fun uriToBase64(context: Context, uri: Uri): String? {
        try {
            // Load the image from URI
            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)

            // Convert Bitmap to Base64
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()
            return Base64.encodeToString(byteArray, Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun urisToBase64List(context: Context, uris: ArrayList<Uri>?): ArrayList<String> {
        val base64List = ArrayList<String>()

        if (uris != null) {
            for (uri in uris) {
                val base64 = uri.let { uriToBase64(context, it) }
                if (base64 != null) {
                    base64List.add(base64)
                }
            }
        }

        return base64List
    }


    fun convertVideoToBase64(videoBytes: ByteArray): String? {
        return Base64.encodeToString(videoBytes, Base64.DEFAULT)
    }
}

//    private fun videoUriToBase64(context: Context, videoUri: Uri): String? {
//        try {
//            val inputStream: InputStream? = context.contentResolver.openInputStream(videoUri)
//            if (inputStream != null) {
//                val bytes = inputStream.readBytes()
//                inputStream.close()
//
//                // Convert the byte array to base64
//                return Base64.encodeToString(bytes, Base64.DEFAULT)
//            }
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }
//        return null
//    }
