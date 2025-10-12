package com.wedo8020.app.mainui.home.adapter

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.RecyclerView
import com.wedo8020.app.MainApplication
import com.wedo8020.app.base.PlayerViewAdapter
import com.wedo8020.app.databinding.SoundRecycleviewBinding
import com.wedo8020.app.response.Data
import com.wedo8020.app.utils.Utils.disableMultiTap
import com.wedo8020.app.utils.Utils.loadImageAsync
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SoundAdapter(var list: ArrayList<Data>, onClickListener: OnClickListeners) :
    RecyclerView.Adapter<SoundAdapter.ViewHolder>() {
    private val onClickListeners: OnClickListeners? = onClickListener
    private var selectedPosition = -1
    private var selectedItemPosition = -1
    var longPressTypes = ""

    companion object {
        var player: ExoPlayer? = null
        var listener: Player.Listener? = null
        val handlerSeekBar = Handler(Looper.myLooper()!!)
        var onPause = false

        @SuppressLint("UnsafeOptInUsageError")
        fun setMediaSourceFromFrag(
            url: String,
            playerView: PlayerView,
        ) {

            if (this@Companion.player != null)
                this@Companion.player!!.release()

            this@Companion.player = ExoPlayer.Builder(MainApplication.instance.applicationContext)
                .setLoadControl(PlayerViewAdapter.getLoadControl())
                .setTrackSelector(PlayerViewAdapter.getTrackSelector(MainApplication.instance.applicationContext))
                .build()
            this@Companion.player!!.playWhenReady = false
            this@Companion.player!!.repeatMode = Player.REPEAT_MODE_OFF

            val mediaItem: MediaItem = MediaItem.fromUri(url)
            // Creating media source and adding video to cache
            val mediaSource =
                ProgressiveMediaSource.Factory(PlayerViewAdapter.cacheDataSourceFactory!!)
                    .createMediaSource(mediaItem)

            this@Companion.player!!.setMediaSource(mediaSource)
            this@Companion.player!!.prepare()
            this@Companion.player!!.setVideoSurfaceView(null)
            playerView.useController = false
            playerView.hideController()
            playerView.player = this@Companion.player
        }


    }

    class ViewHolder(val view: SoundRecycleviewBinding) : RecyclerView.ViewHolder(view.root)


    @SuppressLint("SuspiciousIndentation")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = SoundRecycleviewBinding.inflate(LayoutInflater.from(parent.context))
        return ViewHolder(binding)


    }


    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        /** set the video Cover */
        CoroutineScope(Dispatchers.Main).launch {
            holder.view.ivPost.loadImageAsync(list[position].cover)
        }

        /** Handle LongPress Feature for Every Post Type*/
        if (longPressTypes == "save") {
            holder.view.ivRoundEdit.visibility = View.GONE
            holder.view.ivRoundArchive.visibility = View.GONE
            holder.view.ivImageIcon.visibility = View.GONE
            holder.view.ivPlay.visibility = View.INVISIBLE
            holder.view.ivPlayButton.visibility = View.VISIBLE
        }


        holder.view.ivPlayButton.setOnClickListener {
            holder.view.ivPlayButton.disableMultiTap()
            setMediaSourceFromFrag(list[position].url?:"", holder.view.playerView)
            handlerSeekBar.removeCallbacksAndMessages(null)
            holder.view.apply {
                playerView.player!!.addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        if (isPlaying) {
                            /**Handling the progress bar with sound */
                            handlerSeekBar.postDelayed(object : Runnable {
                                override fun run() {
                                    holder.view.progressBar.max =
                                        holder.view.playerView.player!!.duration.toInt()
                                    holder.view.progressBar.progress =
                                        holder.view.playerView.player!!.currentPosition.toInt()

                                    /**Timer (00:00:00) set for sound player on click of post*/
                                    val totalMilliseconds =
                                        holder.view.playerView.player!!.currentPosition.toInt()
                                    val seconds = (totalMilliseconds / 1000)
                                    val hours = seconds / 3600
                                    val minutes = (seconds % 3600) / 60
                                    val remainingSeconds = seconds % 60
                                    val timeString = String.format(
                                        "%02d:%02d:%02d",
                                        hours,
                                        minutes,
                                        remainingSeconds
                                    )
                                    holder.view.tvTimer.text = timeString

                                    handlerSeekBar.postDelayed(this, 10)
                                }
                            }, 10)


                            /**Timer (00:00:00) set for sound player on click of post*/
                            /*  val totalMilliseconds = holder.view.playerView.player!!.duration.toInt()
                              object : CountDownTimer(totalMilliseconds.toLong(), 1000) {
                                  override fun onTick(millisUntilFinished: Long) {
                                      // Convert remaining milliseconds to HH:MM:SS format
                                      val seconds = (millisUntilFinished / 1000).toInt()
                                      val hours = seconds / 3600
                                      val minutes = (seconds % 3600) / 60
                                      val remainingSeconds = seconds % 60
                                      val timeString = String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds)
                                      holder.view.tvTimer.text = timeString
                                  }
                                  @SuppressLint("SetTextI18n")
                                  override fun onFinish() {
                                        //  holder.view.tvTimer.text = "00:00:00" // Set timer to 00:00:00 when finished
                                  }
                              }.start()*/
                        }
                    }

                    override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                        super.onPlayWhenReadyChanged(playWhenReady, reason)
                        if (playWhenReady) {
                            holder.view.ivPlayButton.visibility = View.GONE
                            holder.view.clPauseButton.visibility = View.VISIBLE
                            holder.view.tvTimer.visibility = View.VISIBLE
                        }
                    }

                    override fun onPlaybackStateChanged(state: Int) {
                        if (state == Player.STATE_ENDED) {
                            /** Remove the player to prevent automatic replay*/
                            player?.playWhenReady = false
                            holder.view.ivPlayButton.visibility = View.VISIBLE
                            holder.view.clPauseButton.visibility = View.GONE
                            holder.view.tvTimer.visibility = View.GONE
                        }
                    }
                })
            }
            player!!.playWhenReady = true
            if (selectedItemPosition != position) {
                /** Deselect the previously selected item (if any)*/
                if (selectedItemPosition != -1) {
                    list[selectedItemPosition].isPlaySelect = false
                    notifyItemChanged(selectedItemPosition)
                }
                /** Select the clicked item*/
                selectedItemPosition = position
                list[selectedItemPosition].isPlaySelect = true
                // notifyItemChanged(position)
            }


        }


        /** Play the Selected Item In RecycleView*/
        if (list[position].isPlaySelect) {
            holder.view.clPauseButton.visibility = View.VISIBLE
            holder.view.ivPlayButton.visibility = View.GONE
            holder.view.tvTimer.visibility = View.VISIBLE
            player!!.play()

        } else {
            holder.view.ivPlayButton.visibility = View.VISIBLE
            holder.view.clPauseButton.visibility = View.GONE
            holder.view.tvTimer.visibility = View.GONE
        }
        holder.view.clPauseButton.setOnClickListener {
            player!!.stop()
            player?.playWhenReady = false
            list[selectedItemPosition].isPlaySelect = false
            //  selectedItemPosition = -1
            holder.view.tvTimer.visibility = View.GONE
            holder.view.ivPlayButton.visibility = View.VISIBLE
            holder.view.clPauseButton.visibility = View.GONE
            handlerSeekBar.removeCallbacksAndMessages(null)
        }

        if (!list[position].pinTime.isNullOrEmpty()) {
            holder.view.ivPPin.visibility = View.VISIBLE
        } else {
            holder.view.ivPPin.visibility = View.GONE
        }

        /** Handle the LongPress*/

        holder.view.clProfileCardView.setOnLongClickListener {
            if (selectedPosition != -1) {
                list[selectedPosition].isSelect = false
                notifyItemChanged(selectedPosition)
                selectedPosition = position
                list[selectedPosition].isSelect = true
                notifyItemChanged(selectedPosition)
            } else {
                selectedPosition = position
                list[selectedPosition].isSelect = true
                notifyItemChanged(position)
            }
            player?.playWhenReady = false

            /* holder.view.let {it->
                 */
            /** Handle The UnPin Functionality Feature for Every Post Type*//*
                if (it.ivPPin.visibility == View.VISIBLE) {
                    it.ivRoundUnPin.visibility = View.VISIBLE
                } else {
                    it.ivRoundUnPin.visibility = View.GONE
                }

                if (it.ivRoundPin.visibility == View.VISIBLE) {
                    it.clLongClick.visibility = View.GONE

                    */
            /** Handle LongPress Feature for Every Post Type*//*
                    if (LongPressTypes == "save") {
                        holder.view.ivRoundEdit.visibility = View.GONE
                        holder.view.ivRoundArchive.visibility = View.GONE
                    }
                    it.playCount.visibility = View.VISIBLE

                    it.ivPlay.visibility = View.INVISIBLE

                }


            }*/


            onClickListeners!!.onLongPressClicks(it, position, list[position])
            true
        }


        /** Handle isSelect for handle the longPress Functionality */
        if (list[position].isSelect) {
            selectedPosition = position
            holder.view.let {
                it.clLongClick.visibility = View.VISIBLE
                if (it.ivPPin.visibility == View.VISIBLE) {
                    it.ivRoundUnPin.visibility = View.VISIBLE
                } else {
                    it.ivRoundUnPin.visibility = View.GONE
                }

            }
            holder.view.ivPlay.visibility = View.INVISIBLE
            holder.view.ivPPin.visibility = View.GONE
            holder.view.ivPlayButton.visibility = View.GONE
            holder.view.clPauseButton.visibility = View.GONE
        } else {
            holder.view.clLongClick.visibility = View.GONE
            holder.view.ivPlay.visibility = View.INVISIBLE
            holder.view.ivPlayButton.visibility = View.VISIBLE
            if (list[position].pinTime != null) {
                holder.view.ivPPin.visibility = View.VISIBLE
            } else {
                holder.view.ivPPin.visibility = View.GONE
            }
        }

        /** Handle the SinglePress Functionality*/
        holder.view.clProfileCardView.setOnClickListener {
            holder.view.clLongClick.visibility = View.GONE
            list[position].isSelect = false
            holder.view.playCount.visibility = View.GONE
            holder.view.ivImageIcon.visibility = View.GONE
            holder.view.ivPlay.visibility = View.INVISIBLE
            holder.view.ivPlayButton.visibility = View.VISIBLE
            /** Handle Pin post for the single and double tap on post*/
            if (list[position].pinTime != null) {
                holder.view.ivPPin.visibility = View.VISIBLE
            } else {
                holder.view.ivPPin.visibility = View.GONE
            }

        }



        holder.view.ivRoundPin.setOnClickListener {
            holder.view.ivRoundPin.disableMultiTap()
            holder.view.clLongClick.visibility = View.GONE
            onClickListeners!!.onClickPins(it, position, list[position], true)

        }
        holder.view.ivRoundDelete.setOnClickListener {
            holder.view.ivRoundPin.disableMultiTap()
            onClickListeners!!.onDeletePosts(it, position, list[position])
            holder.view.clLongClick.visibility = View.GONE
        }
        if (onPause) {
            holder.view.ivPlayButton.visibility = View.VISIBLE
            holder.view.clPauseButton.visibility = View.GONE
        }



    }

    interface OnClickListeners {
        fun onLongPressClicks(view: View, position: Int, data: Data)
        fun onDeletePosts(view: View, position: Int, data: Data)
        fun onClickPins(view: View, position: Int, data: Data, click: Boolean)
    }
}