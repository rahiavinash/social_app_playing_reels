package com.wedo8020.app.base

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.ImageView
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.Util
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.LoadControl
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.exoplayer.trackselection.TrackSelector
import androidx.media3.exoplayer.upstream.DefaultAllocator
import androidx.media3.ui.PlayerView
import com.wedo8020.app.MainApplication
import com.wedo8020.app.R
import com.wedo8020.app.utils.CachedDataSourceFactory
import com.wedo8020.app.utils.LogTypeEnum
import com.wedo8020.app.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference


class PlayerViewAdapter {

    companion object {
        var player: ExoPlayer? = null
        var retriever: MediaMetadataRetriever? = null
        var cacheDataSourceFactory: CachedDataSourceFactory? = null

        var playerCurrentTime = 0L
        private var lastIndex = 0

        private var nextMediaSource: ProgressiveMediaSource? = null

        private var prevPost = ""

        private var scrollItemPlayingTime: MutableMap<String, Long>? = mutableMapOf()


        fun clearScrollItemPlayingTimeMap() {
            scrollItemPlayingTime?.clear()
        }

        fun getScrollItemPlayingTime(id: String): Long {
            return scrollItemPlayingTime?.get(id) ?: 0
        }

        fun setPlayerCurrentTime(){
            val playerCurrentTime = this@Companion.player?.currentPosition ?: 0L
            scrollItemPlayingTime?.set(prevPost, playerCurrentTime)
        }
        fun releaseAllPlayers(str: String = "postother") {
            Utils.printLog(tag = "onItemIsFirstVisibleItem", msg = str)

            if (player != null) {
                player?.release()
                player = null
            }
        }


        // call when scroll to pause any playing player
        fun pauseCurrentPlayingVideo() {
            if (player != null) {
                player?.playWhenReady = false
            }
        }


        // call when scroll to pause any playing player
        fun playCurrentPlayingVideo() {
            if (player != null) {
                player?.playWhenReady = true
            }

        }

        fun playIndexThenPausePreviousPlayer(index: Int) {
            if (player != null && !player!!.playWhenReady) {
//                pauseCurrentPlayingVideo()
                if (lastIndex != index) {
                    player?.seekTo(0)
                }
//                player?.playWhenReady = true
            }
        }

        // video optimisation according Wifi and device compatibility
        private fun getDeviceMaxSupportedBitrate(context: Context): Int {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
            val networkCapabilities = connectivityManager!!.getNetworkCapabilities(
                connectivityManager.activeNetwork
            )

            // Check if the device is on a fast network (e.g., Wi-Fi)
            return if (networkCapabilities != null && networkCapabilities.hasTransport(
                    NetworkCapabilities.TRANSPORT_WIFI
                )
            ) {
                400 // Set maximum bitrate for Wi-Fi
            } else {
                800 // Set maximum bitrate for mobile networks
            }
        }

        @SuppressLint("UnsafeOptInUsageError")
        fun addCaching(context: Context): CachedDataSourceFactory {
            cacheDataSourceFactory = CachedDataSourceFactory(
                WeakReference(context),
                Util.getUserAgent(context, context.getString(R.string.app_name)),
                MainApplication.simpleCache!!
            )
            return cacheDataSourceFactory!!
        }

        @SuppressLint("UnsafeOptInUsageError")
        fun getLoadControl(): LoadControl {
            return DefaultLoadControl.Builder()
                .setAllocator(DefaultAllocator(true, C.DEFAULT_BUFFER_SEGMENT_SIZE))
                .setBufferDurationsMs(8 * 1024, 64 * 1024, 1024, 1024)
                .setTargetBufferBytes(-1)
                .setPrioritizeTimeOverSizeThresholds(true)
                .build()
        }

        @SuppressLint("UnsafeOptInUsageError")
        fun getTrackSelector(context: Context): TrackSelector {
            val trackSelector = DefaultTrackSelector(context)

            trackSelector.parameters = trackSelector.buildUponParameters()
//                .setMaxVideoSizeSd() // Set maximum video size for SD quality
                .setMaxVideoBitrate(getDeviceMaxSupportedBitrate(context)) // Set maximum video bitrate based on device capabilities
                .build()
            return trackSelector
        }

        private suspend fun loadVideo(videoUri: String): MediaItem {
            // Simulate loading video asynchronously
            return withContext(Dispatchers.IO) {
                // Use ExoPlayer's MediaItem.fromUri to create a MediaItem from the videoUri
                MediaItem.fromUri(videoUri)
            }
        }

        @SuppressLint("UnsafeOptInUsageError")
        suspend fun setNextMediaSource(url: String) {
            withContext(Dispatchers.IO){
                val mediaItem = loadVideo(url)
                ProgressiveMediaSource.Factory(cacheDataSourceFactory!!)
                    .createMediaSource(mediaItem)
            }
        }

        @SuppressLint("UnsafeOptInUsageError")
        fun setMediaSourceFromFrag(
            context: Context,
            url: String?,
            id: String,
            playerView: PlayerView,
            run: () -> Unit
        ) {

            if (this@Companion.player != null) {

                playerCurrentTime = this@Companion.player?.currentPosition ?: 0L
                scrollItemPlayingTime?.set(prevPost, playerCurrentTime)

                this@Companion.player?.release()
            }

            CoroutineScope(Dispatchers.IO).launch {
                prevPost = id

                this@Companion.player = ExoPlayer.Builder(context)
                    .setLoadControl(getLoadControl())
                    .setTrackSelector(getTrackSelector(context))
                    .build()
                val mediaItem: MediaItem = loadVideo(url ?: "")
                val mediaSource = ProgressiveMediaSource.Factory(cacheDataSourceFactory!!)
                    .createMediaSource(mediaItem)
                withContext(Dispatchers.Main) {
                    this@Companion.player?.playWhenReady = false
                    this@Companion.player?.repeatMode = Player.REPEAT_MODE_ALL
                    Utils.printLog(logType = LogTypeEnum.DEBUG, tag = "post_url", msg = url ?: "")
                    this@Companion.player?.setMediaSource(mediaSource)
                    this@Companion.player?.prepare()
                    playerView.player = this@Companion.player
                    run()
//                    setNextMediaSource(secondUrl)
                }
            }


        }

        suspend fun mediaMetadataRetriever(url: String) {
            withContext(Dispatchers.IO) {
                retriever = MediaMetadataRetriever()
                retriever?.setDataSource(url)
            }
        }

        suspend fun getBitmap(duration: Double, imageView: ImageView, url: String) {
            val bitmap = withContext(Dispatchers.IO) {
                try {
                    val retriever = MediaMetadataRetriever()
                    retriever.setDataSource(url, HashMap<String, String>())
                    retriever.getFrameAtTime(
                        (duration * 1000).toLong(),
                        MediaMetadataRetriever.OPTION_CLOSEST_SYNC
                    )
                } catch (e: Exception) {
                    null
                }
            }
            bitmap?.let {
                withContext(Dispatchers.Main) {
                    imageView.setImageBitmap(it)
                }
            }
        }


        /*
        *  url is a url of stream video
        *  progressbar for show when start buffering stream
        * thumbnail for show before video start
        * */
        /*@SuppressLint("UnsafeOptInUsageError", "SuspiciousIndentation")
        @JvmStatic
        @BindingAdapter(
            value = ["video_url", "progressbar", "thumbnail"],
            requireAll = false
        )
        fun PlayerView.loadVideo(
            url: String,
            progressbar: ProgressBar,
            thumbnail: ImageView
        ) {



            Log.e("exoplayer", "Set new item")

            setKeepContentOnPlayerReset(true)
            // We'll show the controller, change to true if want controllers as pause and start
            this.useController = false
            this.hideController()
            this.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
        }
    }*/

    }
}