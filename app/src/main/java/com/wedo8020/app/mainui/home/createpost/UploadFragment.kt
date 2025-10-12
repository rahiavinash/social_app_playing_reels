package com.wedo8020.app.mainui.home.createpost


import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.wedo8020.app.R
import com.wedo8020.app.databinding.FragmentUploadBinding
import com.wedo8020.app.mainui.activities.MainActivity
import com.wedo8020.app.mainui.base.BaseFragment
import com.wedo8020.app.mainui.home.createpost.PermissionFragment.Companion.PERMISSION_REQUEST_CODE
import com.wedo8020.app.utils.Utils
import com.wedo8020.app.utils.Utils.disableMultiTap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Objects


@Suppress("DEPRECATION")
class UploadFragment : BaseFragment() {

    private val job = Job()
    var position = 0
    var dialog : Dialog?=null
    private var compressedImagesList : ArrayList<Uri>? = arrayListOf()
    private  var videoUri : Uri = Uri.EMPTY
    private var _binding: FragmentUploadBinding? = null
    private val binding get() = _binding!!
    private var mediaType = ""
    var screenNavigation : Boolean? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUploadBinding.inflate(inflater, container, false)
        showNavBottom()
        dialog = Dialog(requireActivity())
        dialog?.requestWindowFeature(Window.FEATURE_CONTENT_TRANSITIONS)
        return binding.root
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        var permis =  Intent.FLAG_GRANT_READ_URI_PERMISSION
        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Log.d("PermissionDenied", "You are allowed to have permissions for read and write uri ")
                    requireContext().contentResolver.takePersistableUriPermission(
                        videoUri,
                        permis
                    )

                } else {
                   Log.d("PermissionDenied", "You are not allowed to have permissions")
                }
            }
        }
    }






    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is granted
            Log.d("Permission", "READ_EXTERNAL_STORAGE permission is granted.")
        } else {
            // Permission is not granted
            Log.e("Permission", "READ_EXTERNAL_STORAGE permission is not granted.")
        }
        val callback = requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {

            val activity = activity
            activity?.let {
                if (activity is MainActivity){
                    activity.homeTabClick()
                }
            }
        }

        arguments.let {
            if(it?.getBoolean("navigation") != null){
                screenNavigation = it.getBoolean("navigating")

            }
            else {
                screenNavigation = false
            }

        }

        val pickVideoMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->

            if (uri != null) {
                mediaType = "video"

                lifecycleScope.launch(Dispatchers.IO) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

                    Log.d("PhotoPickerXXXXXXXX", "Selected URI: Need no permissions $uri")
                    val permissions = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    try {
                        requireContext().contentResolver.takePersistableUriPermission(
                            uri,
                            permissions
                        )
                    } catch (e: Exception) {
                        Log.d("PhotoPickerXXXXXXXX", "Exception is  $e")
                    }
                    videoUri = uri

                    val videoSizeInBytes = getFileSize(requireContext(), videoUri)

                    val duration = getVideoDuration(videoUri)
                    Log.d("PhotoPickerXXXXXXXX", "Video duration is $duration")
                    if (videoSizeInBytes > MAX_VIDEO_SIZE_BYTES) {
                        lifecycleScope.launch(Dispatchers.Main) {
                            Toast.makeText(
                                requireContext(),
                                "Max 100 mb video allowed",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                    } else if (duration > 180) {
                        lifecycleScope.launch(Dispatchers.Main) {
                            Toast.makeText(
                                requireContext(),
                                "Video duration exceeds 3 minutes",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        lifecycleScope.launch(Dispatchers.Main) {
                            bundle.putString("video", videoUri.toString())
                            bundle.putString("mediaType", mediaType)
                            findNavController().navigate(R.id.previewFragment, bundle)
                        }
                    }
                } else {
                    val videoSizeInBytes = getFileSize(requireContext(), uri)
                    val duration = getVideoDuration(uri)
                    if (videoSizeInBytes > MAX_VIDEO_SIZE_BYTES) {
                        lifecycleScope.launch(Dispatchers.Main) {
                            Toast.makeText(
                                requireContext(),
                                "Max 100 mb video allowed",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                    } else if (duration > 180) {
                        lifecycleScope.launch(Dispatchers.Main) {
                            Toast.makeText(
                                requireContext(),
                                "Video duration exceeds 3 minutes",
                                Toast.LENGTH_SHORT
                            ).show()
                        }


                    } else {
                        lifecycleScope.launch(Dispatchers.Main) {
                            bundle.putString("video", uri.toString())
                            bundle.putString("mediaType", mediaType)
                            findNavController().navigate(R.id.previewFragment, bundle)
                        }


                    }
                }

                Log.d(
                    "PhotoPickerXzzzzzzzzzz",
                    "Selected URI: ${requireContext().contentResolver.getType(uri)}"
                )


            }
            } else {
                Log.d("PhotoPicker", "No media selected")
            }
        }
        val pickMultipleImageMedia =
            registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(10)) { uris ->

                if (uris.isNotEmpty()) {
                    mediaType="image"
                    preparingPopUp()

                    Log.d("PhotoPickerdkhkdskdskd", "Number of items selected: ${compressedImagesList}")
                    lifecycleScope.launch(Dispatchers.IO) {
                        coroutineScope {
                            val data = async {
                                uris?.let {
                                    compressImagesWithGlide(
                                        requireContext(),
                                        it as ArrayList<Uri>
                                    )
                                }
                            }.await()

                            Log.d("hahahanothappening", "${data}")
                            lifecycleScope.launch(Dispatchers.Main) {
                                bundle.putParcelableArrayList("compressedImages", data)
                                bundle.putString("mediaType", mediaType)
                                dialog?.dismiss()
                                findNavController().navigate(R.id.previewFragment, bundle)
                            }
                        }




                    }
                } else {
                    dialog?.dismiss()
                    Log.d("PhotoPicker", "No media selected")
                }
            }




        binding.videoIcon.setOnClickListener {
            binding.imageIcon.disableMultiTap()
            binding.videoIcon.disableMultiTap()
            pickVideoMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
        }

        binding.imageIcon.setOnClickListener{
        binding.videoIcon.disableMultiTap()
        binding.imageIcon.disableMultiTap()
            if (!binding.imageIcon.isClickable) {
                return@setOnClickListener
            }
            binding.imageIcon.isClickable = false
            pickMultipleImageMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            CoroutineScope(Dispatchers.Main
            ).launch{
                delay(1000) // 1000 milliseconds = 1 second
                binding.imageIcon.isClickable = true
            }
        }


    }

    private fun getVideoDuration(uri: Uri): Int {
        val mediaMetadataRetriever = MediaMetadataRetriever()
        mediaMetadataRetriever.setDataSource(requireContext(), uri)
        val durationStr = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        return durationStr?.toLong()?.div(1000)?.toInt() ?: 0
    }

    fun compressImagesWithGlide(context: Context, originalImageUris: ArrayList<Uri>): ArrayList<Uri> {
        val compressedImageUris = ArrayList<Uri>()
        val displayMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)

        val screenWidth = displayMetrics.widthPixels


        originalImageUris.forEach { originalImageUri ->
            try {
                // Compress the image using Glide and obtain the compressed bitmap
                val compressedBitmap = Glide.with(context)
                    .asBitmap()
                    .load(originalImageUri)
                    .apply(RequestOptions()
                        .format(DecodeFormat.PREFER_RGB_565)
                        .override(screenWidth, 1024)
                        .fitCenter()
                        .diskCacheStrategy(DiskCacheStrategy.NONE) // Disable disk caching
                    )
                    .submit()
                    .get()


                val compressedImageFile = File.createTempFile("compressed_image", ".jpg", context.cacheDir)


                FileOutputStream(compressedImageFile).use { outputStream ->
                    compressedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
                }


                val compressedImageUri = Uri.fromFile(compressedImageFile)


                compressedImageUris.add(compressedImageUri)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return compressedImageUris
    }


    fun getImageSizeInMB(context: Context, uris: List<Uri>): List<Double> {
        val sizesInMB = mutableListOf<Double>()

        uris.forEach { uri ->
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    val fileSize = inputStream.available().toDouble()
                    inputStream.close()
                    val sizeInMB = fileSize / (1024 * 1024) // Convert to MB
                    sizesInMB.add(sizeInMB)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        return sizesInMB
    }












    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }



    private fun getFileSize(context: Context, uri: Uri): Long {
        val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)
        return cursor.use { cursor ->
            cursor?.let {
                val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
                it.moveToFirst()
                it.getLong(sizeIndex)
            } ?: 0
        }
    }
    private fun showSizeExceededDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Video Size Exceeded")
        builder.setMessage("Selected video size exceeds the maximum limit of 50 MB. Please choose a smaller video.")
        builder.setPositiveButton("OK") { _, _ ->
            // Handle the user's response or take appropriate action
        }
        val dialog = builder.create()
        dialog.show()
    }
    private fun preparingPopUp() {
        if (context!=null){
            if (!requireActivity().isFinishing) {
                dialog?.setCancelable(false)
                Objects.requireNonNull(dialog?.window)?.setBackgroundDrawable(
                    ColorDrawable(
                        Color.TRANSPARENT
                    )
                )
                dialog?.setContentView(R.layout.preparing_please_wait)
                dialog?.show()
            }
        }

    }


    companion object {

        const val REQUEST_STORAGE_PERMISSIONS = 100
        private const val MAX_VIDEO_SIZE_MB = 100
        private const val MAX_VIDEO_SIZE_BYTES = MAX_VIDEO_SIZE_MB * 1024 * 1024
        val bundle = Bundle()
    }

}
