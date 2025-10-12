package com.wedo8020.app.dataSources

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.lang.Math.min


object ImageCompressionUtil {

    private const val MAX_BITMAP_DIMENSION = 1920 // Define maximum supported dimension

    suspend fun compressImages(
        context: Context,
        originalImageUris: ArrayList<Uri>?
    ): ArrayList<Uri> = withContext(Dispatchers.IO) {
        val compressedImageUris = ArrayList<Uri>()
        val reqWidth = 100 // specify your desired width
        val reqHeight = 100

        originalImageUris?.forEach { originalImageUri ->
            val compressedImageUri = compressImage(context, originalImageUri, reqWidth, reqHeight)
            compressedImageUri?.let {
                compressedImageUris.add(compressedImageUri)
            }
        }

        compressedImageUris
    }
    fun uriToCompressedBitmap(context: Context, uri: Uri): ByteArray {
        val pfd = context.contentResolver.openFileDescriptor(uri, "r")
        val bitmap =
            BitmapFactory.decodeFileDescriptor(pfd?.fileDescriptor, null, null)
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 75, baos)
        return baos.toByteArray()
    }

    fun compressImage(context: Context, originalImageUri: Uri, reqWidth: Int, reqHeight: Int): Uri? {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }

        // Open an input stream from the Uri
        val inputStream = context.contentResolver.openInputStream(originalImageUri)
        BitmapFactory.decodeStream(inputStream, null, options)
        inputStream?.close()

        val imageHeight: Int = options.outHeight
        val imageWidth: Int = options.outWidth
        val imageType: String = options.outMimeType

        // Decode the sampled bitmap from the input stream
        val resizedBitmap = decodeSampledBitmapFromStream(context.contentResolver, originalImageUri, reqWidth, reqHeight)

        if (resizedBitmap == null) {
            Log.e("Compression", "Failed to decode bitmap")
            return null
        }

        val compressedImageUri = createCompressedImageFile(context).toUri()
        val compressedOutputStream = context.contentResolver.openOutputStream(compressedImageUri)

        try {
            if (compressedOutputStream != null) {
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 40, compressedOutputStream)
                Log.d("Compression", "Image compressed successfully")
            } else {
                Log.e("Compression", "Failed to compress image")
            }
        } catch (e: IOException) {
            Log.e("Compression", "Error compressing image: ${e.message}")
            return null
        } finally {
            compressedOutputStream?.close()
        }

        return compressedImageUri
    }

    fun decodeSampledBitmapFromStream(
        resolver: ContentResolver,
        uri: Uri,
        reqWidth: Int,
        reqHeight: Int
    ): Bitmap? {
        // Open an input stream from the Uri
        val inputStream = resolver.openInputStream(uri) ?: return null

        // Decode the bitmap with the requested width and height
        return BitmapFactory.Options().run {
            inJustDecodeBounds = false
            inSampleSize = calculateInSampleSize(this, reqWidth, reqHeight)
            BitmapFactory.decodeStream(inputStream, null, this)
        }
    }



    fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }



    private fun calculateScale(width: Int, height: Int): Int {
        return min(MAX_BITMAP_DIMENSION / width, MAX_BITMAP_DIMENSION / height)
    }

    private fun createCompressedImageFile(context: Context): File {
        val storageDir = context.cacheDir
        return File.createTempFile("compressed", ".jpg", storageDir)
    }
}



















