package com.wedo8020.app.utils


import android.content.Context
import android.net.Uri
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class VideoCacheManager(private val context: Context) {
    private val backgroundExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    fun preloadVideos(urls: List<String?>) {
      /*  backgroundExecutor.execute {
            urls.forEach { url ->
                preloadVideo(url)
            }
        }*/
    }
    private fun preloadVideo(videoUrl: String) {
        try {
            val url = URL(videoUrl)
            val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
            connection.connect()

            // Get the file name from the URL
            val fileName = videoUrl.substring(videoUrl.lastIndexOf('/') + 1)

            // Save the video file to the cache directory
            val outputFile = File(context.cacheDir, fileName)
            val outputStream = FileOutputStream(outputFile)
            val inputStream = BufferedInputStream(connection.inputStream)

            val buffer = ByteArray(1024)
            var bytesRead: Int
            while (inputStream.read(buffer, 0, buffer.size).also { bytesRead = it } >= 0) {
                outputStream.write(buffer, 0, bytesRead)
            }

            outputStream.close()
            inputStream.close()

            // Notify on the main thread or perform UI-related operations
            // For example, update UI to indicate video is preloaded

            // Later, you can use ExoPlayer to play the video using the URI
            val videoUri = Uri.fromFile(outputFile)
            // Now, use this videoUri with ExoPlayer to play the video

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    fun release() {
        backgroundExecutor.shutdown()
    }
}
/*class VideoPreloadUtil {

    @SuppressLint("UnsafeOptInUsageError")
    suspend fun preloadVideos(videoUrls: List<String>): List<MediaItem> {
        return withContext(Dispatchers.IO) {
            val mediaItems = mutableListOf<MediaItem>()

            for (videoUrl in videoUrls) {
                val cacheKey = CacheUtil.generateKey(Uri.parse(videoUrl))
                val httpDataSourceFactory: HttpDataSource.Factory = DefaultHttpDataSource.Factory()
                    .setUserAgent(Util.getUserAgent(MainApplication.instance.applicationContext,MainApplication.instance.applicationContext.getString(
                        R.string.app_name) ))
                val upstreamFactory: DataSource.Factory = DefaultDataSource.Factory(MainApplication.instance.applicationContext, httpDataSourceFactory)
                val dataSource = CacheDataSource(MainApplication.simpleCache!!, upstreamFactory.createDataSource(), CacheDataSource.FLAG_BLOCK_ON_CACHE or CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
                dataSource.open(DataSpec(Uri.parse(videoUrl)))

                // Here you can add the video to cache
                // For example, you can add the video bytes to cache with dataSource.read() method

                val mediaItem = MediaItem.Builder()
                    .setUri(videoUrl)
                    .setMediaId(cacheKey)
                    .build()
                mediaItems.add(mediaItem)
            }

            mediaItems
        }
    }
}*/

/*import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.util.Util
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.DefaultDataSourceFactory
import androidx.media3.datasource.cache.CacheDataSource
import com.app.wedo8020.MainApplication
import com.app.wedo8020.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class VideoPreloadUtil {
    @SuppressLint("UnsafeOptInUsageError")
    suspend fun preLoadVideo(videoUrls: ArrayList<String>,cachedDataSourceFactory: CachedDataSourceFactory){
        withContext(Dispatchers.IO){
            videoUrls.forEach {
                MediaItem.fromUri(it)
                val cacheDataSource = cachedDataSourceFactory.createDataSource()
                val dataSpec = DataSpec(Uri.parse(it))
                try {
                    // Open the cache data source and load the video into the cache
                    val inputStream = cacheDataSource.open(dataSpec)
                    // Consume the inputStream if needed
                } catch (e: Exception) {
                    // Handle exceptions, if any
                }
            }
        }
    }
}*/
//Todo video preload in cache
