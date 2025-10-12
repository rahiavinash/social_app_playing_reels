package com.wedo8020.app.mainui.home.createpost

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import android.widget.SeekBar
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.wedo8020.app.R
import com.wedo8020.app.databinding.FragmentThumbNailBinding
import com.wedo8020.app.mainui.base.BaseFragment

class ThumbNailFragment : BaseFragment() {
    private var _binding: FragmentThumbNailBinding? = null
    private val binding get() =
        _binding ?: error("Binding should not be accessed before onCreateView or after onDestroyView")
    private var videoUri: Uri? = Uri.EMPTY
    private var selectedSeekBarPosition: Int = 0
    private var seektime : String ="1.12111111"
    private var savedSeekPosition: Int = 0
    private var updateSeekBar = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentThumbNailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)




        arguments?.let {
            val retrieveVideoUri: Uri? = it.getParcelable("video")
            videoUri = retrieveVideoUri
//            Log.d("videoUri", videoUri.toString())
            //videoUri?.let { it1 -> setVideoThumbnail(it1.toUri()) }
        }

        Log.e("seekBarPosition", "OnViewCreated is called")

            val map = videoUri?.let { createVideoThumb(requireContext(),it) }
            binding.backgroundImageView.setImageBitmap(map)
            binding.video.setVideoURI(videoUri)

            val mediaController = MediaController(requireContext())
            mediaController.setAnchorView(binding.video)
            binding.video.setMediaController(null)



        binding.video.setOnPreparedListener { mediaPlayer ->
            val videoDuration = mediaPlayer.duration
            binding.seekBar.max = videoDuration
            //binding.video.start()
            binding.video.seekTo(videoDuration)

        }



        binding.video.setOnCompletionListener {
            binding.seekBar.progress = 0

        }
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.selectThumbnailButton.setOnClickListener {
            bundle.putParcelable("video",videoUri)
            bundle.putString("mediaType", "video")
            bundle.putString("seekTime", seektime)
            findNavController().navigate(R.id.createPostFragment, bundle)
        }

        binding.video.setOnInfoListener { _, what, _ ->
            when (what) {
                MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START -> {
                    //binding.videoThumbnailImageView.visibility = View.GONE
                }
                MediaPlayer.MEDIA_INFO_BUFFERING_START -> {
                    //binding.videoThumbnailImageView.visibility = View.VISIBLE
                }
            }
            true
        }
        val media = MediaMetadataRetriever()
        val videoWidth = media.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toInt() ?: 0
        val videoHeight = media.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toInt() ?: 0

        val aspectRatio = videoWidth.toFloat() / videoHeight.toFloat()


        val screenWidth = resources.displayMetrics.widthPixels
        val videoViewHeight = (screenWidth / aspectRatio).toInt()


        binding.video.layoutParams.height = videoViewHeight
        binding.video.layoutParams.width = screenWidth







        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {

                    val timeInSeconds = progress / 1000.0 // Convert milliseconds to seconds
                    val formattedTime = String.format("%.6f", timeInSeconds) // Show up to 6 decimal places
                    Log.d("SeekBarprogress", "Current Time: $formattedTime seconds")
                    seektime = formattedTime
                    Log.d("SeekBarprogress", "Current Time: $seektime $formattedTime")
                    selectedSeekBarPosition = progress
                    binding.video.seekTo(progress)
                    Log.d("progress", selectedSeekBarPosition.toString())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                updateSeekBar = false
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                updateSeekBar = true
                updateSeekBarProgressAutomatically()
            }
        })

        Log.d("progressAfter", selectedSeekBarPosition.toString())
    }

    private fun createVideoThumb(context: Context, uri: Uri): Bitmap? {
        try {
            val mediaMetadataRetriever = MediaMetadataRetriever()
            mediaMetadataRetriever.setDataSource(context, uri)
            return mediaMetadataRetriever.frameAtTime
        } catch (ex: Exception) {
            Toast
                .makeText(context, "Error retrieving bitmap", Toast.LENGTH_SHORT)
                .show()
        }
        return null

    }
    private fun updateSeekBarProgressAutomatically() {
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (updateSeekBar) {
                    binding.seekBar.progress = binding.video.currentPosition
                    handler.postDelayed(this, 100) // Update every second for smoother performance
                }
            }
        }, 100)
    }

    override fun onResume() {
        super.onResume()

        Log.e("seekBarPosition", "OnResume is called  $selectedSeekBarPosition")
        binding.video.seekTo(selectedSeekBarPosition)
    }

    override fun onPause() {
        super.onPause()
        Log.e("seekBarPosition", "OnPause is called")
    }

    override fun onStop() {
        super.onStop()
        Log.e("seekBarPosition", "OnStop is called")
    }




    companion object {
        val bundle = Bundle()
    }
}


