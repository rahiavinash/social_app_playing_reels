        package com.wedo8020.app

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.OpenableColumns
import android.util.Base64
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.documentfile.provider.DocumentFile
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import com.wedo8020.app.dataSources.ImagesVideoConverter
import com.wedo8020.app.network.ApiService
import com.wedo8020.app.request.CreatePostWithMedia
import com.wedo8020.app.request.MediaData
import com.wedo8020.app.roomdb.CreatePostDbCreation
import com.wedo8020.app.utils.Utils
import com.vincent.videocompressor.VideoCompress
import com.wedo8020.app.mainui.home.createpost.UploadFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.CompletableFuture

class CompressionUploadWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {
    private val apiService = ApiService()
    private var postDb: CreatePostDbCreation? = null
    private lateinit var outputDirectory: File
    private val fileName = "yyyy-MM-dd-HH-mm-ss-SSS"
    var file1: File? = null
    private var seekTime = ""
    private var postID: String? = ""
    val data = ArrayList<MediaData>()


    override fun doWork(): Result {
        val videoUriString = inputData.getString("videoUri")
        val imageUriArray = inputData.getStringArray("images")
        seekTime = inputData.getString("seekTime").toString()
        postID = inputData.getString("postId")
        outputDirectory = getOutputDirectory()

        try {
            val future = CompletableFuture<Result>()

            CoroutineScope(Dispatchers.IO).launch {
                if (!videoUriString.isNullOrEmpty() && videoUriString != "null") {
                    Utils.printLog(tag = "imagesPermission", msg = "video uri is not empty")
                    val videoUri = Uri.parse(videoUriString) // Convert the string to a Uri object

                    Log.d(
                        "storagePermission",
                        "U have storage permissions for android"
                    )

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

                        val hasPermission =
                            applicationContext.contentResolver.persistedUriPermissions.any {
                                it.uri == videoUri && (it.isReadPermission)
                            }
                        if (hasPermission) {
                            applicationContext.contentResolver.takePersistableUriPermission(
                                videoUri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                            )
                            Utils.printLog(
                                tag = "storagePermission",
                                msg = "videoUriString ==== permission granted for persisting uri granted"
                            )
                            if (hasStoragePermissionsForAndroid13()) {
                                Log.d(
                                    "storagePermission",
                                    "U have storage permissions for android 13 but not persist permission $hasPermission"
                                )
                                val outputDir = File(
                                    applicationContext.getExternalFilesDir(Environment.DIRECTORY_MOVIES),
                                    "CompressedVideos"
                                )
                                outputDir.mkdirs()
                                val outputFilePath =
                                    File(outputDir, "compressed_video.mp4").absolutePath

                                compressVideo(videoUri, outputFilePath, future)
                            }



                        }
                        else {
                            if (hasStoragePermissionsForAndroid13()) {
                                Log.d(
                                    "storagePermission",
                                    "U have storage permissions for android 13 but not persist permission $hasPermission"
                                )
                                val outputDir = File(
                                    applicationContext.getExternalFilesDir(Environment.DIRECTORY_MOVIES),
                                    "CompressedVideos"
                                )
                                outputDir.mkdirs()
                                val outputFilePath =
                                    File(outputDir, "compressed_video.mp4").absolutePath

                                compressVideo(videoUri, outputFilePath, future)
                            }
                        }






                        // Request permissions for the URI

                        Utils.printLog(tag = "storagePermission", msg = "videoUriString")


                    }
                    else {
                        Log.d(
                            "storagePermission",
                            "U do not need permission for android 11 and android 12"
                        )
                        if(hasStoragePermissions()) {

                            Log.d(
                                "storagePermission",
                                "U have storage permissions for android 12 and below"
                            )
                            val outputDir = File(
                                applicationContext.getExternalFilesDir(Environment.DIRECTORY_MOVIES),
                                "CompressedVideos"
                            )
                            outputDir.mkdirs()
                            val outputFilePath =
                                File(outputDir, "compressed_video.mp4").absolutePath

                            compressVideo(videoUri, outputFilePath, future)
                        }
                    }




                } else if (!imageUriArray.isNullOrEmpty()) {

                    Utils.printLog(tag = "imgesPermission", msg = "videoUriString   $imageUriArray")
                    val imagesUri = ArrayList<Uri>()
                    imageUriArray.forEach {
                        Utils.printLog(tag = "CompressionUploadWorker", msg = it)
                        val uri = Uri.parse(it)
                        imagesUri.add(uri)
                    }

                    apiCallWithImageMedia(imagesUri) { isSuccess ->
                        Utils.printLog(tag = "CompressionUploadWorker", msg = "imageUriArray === success")
                        handleUploadResult(isSuccess, future)
                    }
                } else {
                    // No valid input, consider it a failure
                    future.complete(Result.failure())
                }
            }


            // Wait for the future to complete and return the Result
            return future.get()
        } catch (e: Exception) {
            Utils.printLog(tag = "CompressionUploadWorker", msg = e.toString())

            return Result.failure()
        }
    }

    private fun hasStoragePermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED ||
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    ContextCompat.checkSelfPermission(
                        applicationContext,
                        Manifest.permission.MANAGE_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
                } else {
                    true
                }
    }
    private fun hasStoragePermissionsForAndroid13(): Boolean {
        return ContextCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.READ_MEDIA_IMAGES
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.READ_MEDIA_VIDEO
                ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.READ_MEDIA_AUDIO
                ) == PackageManager.PERMISSION_GRANTED

    }

    private fun requestStoragePermissions() {
        var permissions = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            permissions += Manifest.permission.MANAGE_EXTERNAL_STORAGE
        }

        if (MainApplication.get().appLifecycleObserver.activity != null){
            ActivityCompat.requestPermissions(
                MainApplication.get().appLifecycleObserver.activity as Activity,
                permissions,
                UploadFragment.REQUEST_STORAGE_PERMISSIONS
            )
        }

    }

    private fun createPostWithMedia(callback: (Boolean) -> Unit) {
        if (Utils.isInternetAvailables(MainApplication.get().applicationContext)) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    Log.e("hgjgh",seekTime)

                    val response = if (seekTime != "null"){
                        apiService.createPostWithMedia(CreatePostWithMedia(postID, seekTime.toDouble(), data))
                    } else {
                        apiService.createPostWithMedia(CreatePostWithMedia(postID, 0.0, data))

                    }
                    if (response.success) {
                        Log.e("afjhksfda","lsjdahfsagfd")
                        postDb = CreatePostDbCreation(applicationContext)
                        postDb?.getCreatePostData()?.deleteItem(postID?:"")
                        callback(true)
                    } else {
                        Log.e("afjhksfda","else lsjdahfsagfd")
                        callback(false)
                    }
                } catch (e: Exception) {
                    Log.e("afjhksfda",e.toString())
                    // Handle exceptions here if needed
                    callback(false)
                }
            }
        } else {
//            Utils.showToast("Please check your internet connection")
            callback(false)
        }
    }

    /*private suspend fun createPostWithMedia(callback: (Boolean) -> Unit) {
        if (Utils.isInternetAvailable(MainApplication.get().applicationContext)) {
            try {
                val response = withContext(Dispatchers.IO) {
                    apiService.createPostWithMedia(CreatePostWithMedia(postID, 0, data))
                }
                if (response.success) {
                    withContext(Dispatchers.Main) {
                        postDb?.getCreatePostData()?.deleteAll()
                        callback(true)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        callback(false)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    callback(false)
                }
            }
        } else {
            withContext(Dispatchers.Main) {
                Utils.showToast("Please check your internet connection")
                callback(false)
            }
        }
    }*/

    private fun apiCallWithImageMedia(imagesUri: ArrayList<Uri>, callback: (Boolean) -> Unit) {
        val base64Images =
            ImagesVideoConverter.urisToBase64List(
                MainApplication.get().applicationContext,
                imagesUri
            )
        for (x in base64Images) {

            data.add(MediaData("jpg", x))
        }

        var success: Boolean

        createPostWithMedia {
            success = it
            callback(success)
        }


    }

    private fun compressVideo(inputFilePath: Uri, outputFilePath: String, future : CompletableFuture<Result>) {

        val inputFile  = getRealPathFromURI(inputFilePath, applicationContext)




        //-y-r 25 -b:v 1000k -b:a 48000 -ac 2 -ar 22050
        val complexCommand =
    " -y -i $inputFile -vcodec mpeg4 -b:v 0 -crf 20 -qscale 10 -b:a 192k -ac 2 -ar 44100 $outputFilePath"




//        val complexCommand =
//            "-y -i $inputFile -r 25 -vcodec mpeg4 -crf 28 $outputFilePath"
//



//        val complexCommand = "-y -i $inputFile -vf scale=1280:-1 -vcodec mpeg4 -preset superfast -crf 22  -c:a aac -b:a 128k $outputFilePath"

        val command = try {
            FFmpegKit.executeAsync(complexCommand) { session ->
                val returnCode = session.returnCode
                if (ReturnCode.isSuccess(returnCode)) {
                    val isSuccess = true
                    Log.d("CompressVideoFile", "Video compression successful ${session.output}")
                    file1 = File(outputFilePath)
                    //compressedVideoPath = compressedVideoFile.path
                    val outputVideoPath = file1?.absolutePath?.let { getFileSizeInMB(it) }
                    Log.d(
                        "CompressVideoFile", "File size after compressing, $outputVideoPath")


                    if (outputVideoPath != null) {
                        if (outputVideoPath < 16.00 && outputVideoPath > 5.00) {
                            file1?.absolutePath?.let { s ->
                                apiCallWithVideoMedia(s) {
                                    handleUploadResult(it, future)
                                }
                            }
                        }
                        else if(outputVideoPath < 5.00){
                            Log.e("compressorVideoUpload", "Video size is less than 5 mb so we do not need to compress it")
                            file1?.absolutePath?.let { s ->
                                apiCallWithVideoMedia(s) {
                                    handleUploadResult(it, future)
                                }
                            }
                        }
                        else {
                            Log.e("compressorVideoUpload", "Will try with compressor 2 as video size is large")
                            compressVideoFromGallery(inputFilePath) { isSuccess ->
                                Utils.printLog(tag = "CompressionUploadWorker", msg = "videoUriString === success")

                                handleUploadResult(isSuccess, future)
                            }
                        }
                    }
//                    handleUploadResult(isSuccess, future)
                } else {
                    Log.e("CompressVideoFile", "Video compression failed $inputFile ${session.output}")
                    compressVideoFromGallery(inputFilePath) { isSuccess ->
                        Utils.printLog(tag = "CompressionUploadWorker", msg = "videoUriString === success")

                        handleUploadResult(isSuccess, future)
                    }

                }
            }
        } catch (e: Exception) {

            Log.e("CompressVideoFile", "Exception while executing FFmpeg command", e)


        }





    }

    private fun getRealPathFromURI(uri: Uri, context: Context): String? {
        val returnCursor = context.contentResolver.query(uri, null, null, null, null)
        val nameIndex =  returnCursor!!.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        val sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE)
        returnCursor.moveToFirst()
        val name = returnCursor.getString(nameIndex)
        val size = returnCursor.getLong(sizeIndex).toString()
        val file = File(context.filesDir, name)
        try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(file)
            var read = 0
            val maxBufferSize = 1 * 1024 * 1024
            val bytesAvailable: Int = inputStream?.available() ?: 0
            //int bufferSize = 1024;
            val bufferSize = Math.min(bytesAvailable, maxBufferSize)
            val buffers = ByteArray(bufferSize)
            while (inputStream?.read(buffers).also {
                    if (it != null) {
                        read = it
                    }
                } != -1) {
                outputStream.write(buffers, 0, read)
            }
            Log.e("File Size", "Size " + file.length())
            inputStream?.close()
            outputStream.close()
            Log.e("File Path", "Path " + file.path)

        } catch (e: java.lang.Exception) {
            Log.e("Exception", e.message!!)
        }
        return file.path
    }

    private fun videoToBase64(filePath: String): String {
        val file = File(filePath)
        if (!file.exists()) {
            return "" // Handle file not found scenario
        }

        try {
            val inputStream = FileInputStream(file)
            val bytes = ByteArray(file.length().toInt())

            inputStream.read(bytes)
            inputStream.close()

            return Base64.encodeToString(bytes, Base64.DEFAULT)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return "" // Return empty string in case of exception
    }

    private fun apiCallWithVideoMedia(filePath: String, callback: (Boolean) -> Unit) {
        Log.d("mediaSuccess", "U are selecting video $filePath")

        val base64String = videoToBase64(filePath)
       // Log.d("hello", "video base 64 $base64String")
        data.add(MediaData("mp4", base64String))

//        CoroutineScope(Dispatchers.IO).launch {
        createPostWithMedia {
            callback(it)
//            }
        }

//        return future.get()
    }

    private fun compressVideoFromGallery(videoUri: Uri, callback: (Boolean) -> Unit) {
        val fileForVideoCompress = getVideoFileFromUri(applicationContext, videoUri)
        val compressedVideoFile = createFile(
            outputDirectory,
            fileName,
            "_compressed.mp4"
        )

        @Suppress("INACCESSIBLE_TYPE")
        (VideoCompress.compressVideoLow(
            fileForVideoCompress.absolutePath, compressedVideoFile.path,
            object : VideoCompress.CompressListener {
                override fun onStart() {
                    Log.e("compressor", "start")
                    //Start Compress
                    //dialogOne = ProgressDialog.show(requireActivity(), "", "Compressing...")

                    //dialogOne!!.show()
                }

                override fun onSuccess() {
                    Log.e("compressor", "success")
                    Utils.printLog(tag = "CompressionUploadWorker", msg = "onSuccess")

                    //dialogOne!!.dismiss()
                    //Log.d("FileSize", "File size before compressing, ${getFileSizeInMB(File(videoString))}")
                    //compressedVideoFile = File(compressedVideoFile.path)
                    file1 = File(compressedVideoFile.path)
                    //compressedVideoPath = compressedVideoFile.path
                    Log.d(
                        "CompressVideoFile", "File size after compressing with compressor 2, ${
                            file1?.absolutePath?.let { getFileSizeInMB(it) }
                        }"
                    )
                    val outputFilePath = getFileSizeInMB(file1?.absolutePath?:"")

                    if(outputFilePath < 18.00){
                        file1?.absolutePath?.let { s ->
                            apiCallWithVideoMedia(s) {
                                callback(it)
                            }
                        }
                    }
                    else {

                        Log.e("compressorVideoUpload", "Video upload failed")
                        CoroutineScope(Dispatchers.IO).launch {
                            postDb = CreatePostDbCreation(applicationContext)
                            postDb?.getCreatePostData()?.deleteItem(postID?:"")
                        }


                        Utils.showToast("Video Upload Failed. Please try again..")
                        Result.failure()
                    }


//                    future.complete(isSuccess)
                    Utils.printLog(tag = "compressor", msg = "isSuccess = true")
//                    callback(future.get())
                }

                override fun onFail() {
                    Log.e("compressor", "fail")
                    callback(false)
                    //Failed
                }

                override fun onProgress(percent: Float) {


                    Log.e("compressor", percent.toString())
                    Log.e("compressor", "progress")
                    //Progress
                }
            }))
    }

    private fun createFile(baseFolder: File, format: String, extension: String) =
        File(
            baseFolder, SimpleDateFormat(format, Locale.US)
                .format(System.currentTimeMillis()) + extension
        )


    private fun getOutputDirectory(): File {
        val context = MainApplication.get().applicationContext
        val mediaDir = context.externalMediaDirs.firstOrNull()?.let {
            File(it, context.resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else context.filesDir
    }

    fun getFileSizeInMB(filePath: String): Double {
        val file = File(filePath)
        if (file.exists()) {
            val fileSizeInBytes = file.length()
            val fileSizeInKB = fileSizeInBytes.toDouble() / 1024 // Convert bytes to kilobytes
            return fileSizeInKB / 1024 // Convert kilobytes to megabytes
        }
        return -1.0 // Return -1 if file doesn't exist
    }


    private fun getVideoFileFromUri(context: Context, uri: Uri): File {
        val contentResolver = context.contentResolver
        /*context.contentResolver.takePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )*/
        val documentFile = DocumentFile.fromSingleUri(context, uri)
        val displayName = documentFile?.name ?: ("file_" + System.currentTimeMillis())
        val outputDir = context.cacheDir // You can change this to another directory if needed
        val outputFile = File(outputDir, displayName)

        val inputStream = contentResolver.openInputStream(uri)
        val outputStream = FileOutputStream(outputFile)

        inputStream?.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }

        return outputFile
    }

    private fun handleUploadResult(isUploadSuccess: Boolean, future: CompletableFuture<Result>) {
        val result = if (isUploadSuccess) {
            Utils.printLog(tag = "CompressionUploadWorker", msg = "Upload success")
            Result.success()
        } else {
            Utils.printLog(tag = "CompressionUploadWorker", msg = "Upload failure")
            Result.retry()
        }

        // Complete the future with the determined result
        future.complete(result)
    }
}