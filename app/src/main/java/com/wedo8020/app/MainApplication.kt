package com.wedo8020.app

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.net.Uri
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.documentfile.provider.DocumentFile
import androidx.media3.database.DatabaseProvider
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import com.arthenica.ffmpegkit.FFmpegKitConfig
import com.google.firebase.FirebaseApp
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.google.GoogleEmojiProvider
import com.vincent.videocompressor.VideoCompress
import com.wedo8020.app.mainui.home.createpost.CreatePostViewModel
import com.wedo8020.app.mainui.home.createpost.CreatePostViewModelFactory
import com.wedo8020.app.request.MediaData
import com.wedo8020.app.roomdb.CreatePostDbCreation
import com.wedo8020.app.roomdb.CreatePostDbRepo
import com.wedo8020.app.utils.Utils
import com.wedo8020.zelpyy.SocketProvider.SocketIOImpl
import io.branch.referral.Branch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale

class MainApplication : Application() {
    private val exoPlayerCacheSize = 100*1024*1024
    private lateinit var viewModel : CreatePostViewModel

    private lateinit var repository : CreatePostDbRepo
    private lateinit var factory : CreatePostViewModelFactory
    private var postDb : CreatePostDbCreation? = null
    private lateinit var outputDirectory: File
    private val fileName = "yyyy-MM-dd-HH-mm-ss-SSS"
    var file1: File?= null
    val data = ArrayList<MediaData>()
    var appLifecycleObserver = AppLifecycleObserver()
    companion object{
        var simpleCache: SimpleCache? = null
        lateinit var instance: MainApplication

        fun get(): MainApplication = instance
    }
    val socket = SocketIOImpl()
    @SuppressLint("UnsafeOptInUsageError")
    override fun onCreate() {
        super.onCreate()
        instance = this
        Log.d("mainApplication", "Main application is getting triggered")
        registerActivityLifecycleCallbacks(appLifecycleObserver)
        FirebaseApp.initializeApp(this)
        Branch.enableLogging()
        Branch.getAutoInstance(this.applicationContext)
        val cacheDir = File(cacheDir, "media_cache")
        val evictor = LeastRecentlyUsedCacheEvictor(exoPlayerCacheSize.toLong())
        val databaseProvider: DatabaseProvider = StandaloneDatabaseProvider(applicationContext)
        simpleCache = SimpleCache(cacheDir, evictor, databaseProvider)
        postDb = CreatePostDbCreation(applicationContext)
        repository = CreatePostDbRepo(postDb!!)
        factory = CreatePostViewModelFactory(repository)

        EmojiManager.install(GoogleEmojiProvider())
        FFmpegKitConfig.enableLogCallback(null)
        FFmpegKitConfig.enableStatisticsCallback(null)
        applyCustomConfiguration(baseContext!!)
        /*  val config = BundledEmojiCompatConfig(this)
          EmojiCompat.init(config)*/


    /*    workManager = WorkManager.getInstance(applicationContext)
        CoroutineScope(Dispatchers.IO).launch{
            val localPostList = postDb?.getCreatePostData()?.fetchAllPost()
            Utils.printLog(tag = "localPostList", msg = localPostList.toString())
            if (!localPostList.isNullOrEmpty()) {
                localPostList.forEach{listData ->
                    val data = Data.Builder()
                        .putString("videoUri", listData.video)
                        .putStringArray(
                            "images",
                            listData.images?.toTypedArray() ?: ArrayList<String>().toTypedArray()
                        )
                        .putString("postId", listData.id)
                        .build()
                    val compressionUploadWorkRequest =
                        OneTimeWorkRequest.Builder(CompressionUploadWorker::class.java)
                            .setInputData(data)
                            .build()

                    workManager.enqueue(compressionUploadWorkRequest)
                    // Observe the work status and post events accordingly
                    CoroutineScope(Dispatchers.Main).launch {
                        workManager.getWorkInfoByIdLiveData(compressionUploadWorkRequest.id)
                            .observeForever { workInfo ->
                                when (workInfo.state) {
                                    WorkInfo.State.SUCCEEDED -> {
                                        EventBus.getDefault().post(UploadStatus(true))
                                    }

                                    WorkInfo.State.FAILED -> {
                                        val error = workInfo.outputData.getString("error")
                                        EventBus.getDefault().post(UploadStatus(false))
                                    }
                                    // Handle other states if necessary
                                    else -> {}
                                }
                            }
                    }

                }
            }
        }*/



    }
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        applyCustomConfiguration(base!!)
    }
    private fun applyCustomConfiguration(base: Context) {
        // Create a custom configuration with a fixed display size
        val configuration = Configuration()
        configuration.densityDpi = 240 // Set your desired density DPI

        // Apply the custom configuration
        val customContext = base.createConfigurationContext(configuration)
        resources.updateConfiguration(configuration, resources.displayMetrics)
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
    private fun apiCallWithVideoMedia(filePath : String) {


        Log.d("mediaSuccess", "U are selecting video $filePath")

        val base64String = videoToBase64(filePath)
        Log.d("hello", "video base 64 $base64String")
        data.add(MediaData("mp4", base64String))


        createPostWithMedia()
        /*viewModel.mediaResponse.observeForever() {

                responseWithMedia ->
//            dialogs?.dismiss()
            if (responseWithMedia.success) {
                Log.d("mediaSuccess1111", "Post is created with video media")
                CoroutineScope(Dispatchers.IO).launch {
                    viewModel.deleteAll()
                }
                file1?.delete()

            } else {
//                dialogs?.dismiss()
                responseWithMedia.error
                Log.d("postCreated", "i am here ${responseWithMedia.error}")
            }

        }*/
    }
    private fun createPostWithMedia(){
        if(Utils.isInternetAvailables(applicationContext)){

//            viewModel.createPostWithMedia(CreatePostWithMedia(post,seekTime,data))
        }
        else {
            Toast.makeText(applicationContext, "Please check your internet connection", Toast.LENGTH_SHORT).show()
        }
    }
    private fun compressVideoFromGallery(videoUri : Uri){

        val fileForVideoCompress = getVideoFileFromUri(applicationContext,videoUri)
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

                    //dialogOne!!.dismiss()
                    //Log.d("FileSize", "File size before compressing, ${getFileSizeInMB(File(videoString))}")
                    //compressedVideoFile = File(compressedVideoFile.path)
                    file1= File(compressedVideoFile.path)
                    //compressedVideoPath = compressedVideoFile.path
                    Log.d("FileSize", "File size after compressing, ${getFileSizeInMB(file1!!.absolutePath)
                    }")
                    apiCallWithVideoMedia(file1!!.absolutePath)

                }

                override fun onFail() {
                    Log.e("compressor", "fail")
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
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
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
}