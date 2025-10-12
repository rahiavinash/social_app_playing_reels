package com.wedo8020.app.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.MediaStore
import android.telephony.TelephonyManager
import android.text.InputFilter
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.auth0.android.jwt.JWT
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.wedo8020.app.MainApplication
import com.wedo8020.app.R
import com.wedo8020.app.response.BioMentionUser
import com.wedo8020.app.response.MentionUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLConnection
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty


object Utils {
    val TAG = Utils::class.java.name
    var dialog: Dialog? = null
    var daysDiff: Long = 0L
    var monthsDiff: Long = 0L
    var seconds: Long = 0L
    var minutes: Long = 0L
    var hours: Long = 0L
    var amPm: String = ""
    var finalDateFormats: String = ""
    var time: String = ""
    var timeProperFormat: String = ""
    var date: String = ""
    var yearMonthDate: String = ""
    var finalDateFormat: String = ""
    var messageDateMonth: String = ""


    fun isTokenExpired(): Boolean {
        val token: String = Prefs.init().accessToken
        return run {
            val jwt = JWT(token)
            Log.e(
                TAG,
                "isTokenExpired: " + System.currentTimeMillis() + "-" + jwt.expiresAt!!
                    .time + "=" + (System.currentTimeMillis() - jwt.expiresAt!!.time)
            )
            if (System.currentTimeMillis() > jwt.expiresAt!!.time && System.currentTimeMillis() - jwt.expiresAt!!
                    .time > 0
            ) {
                Log.e(TAG, "isTokenExpired: true")
                true
            } else {
                Log.e(TAG, "isTokenExpired: false")
                false
            }
        }
    }

    inline fun runWithDelay(delay: Long, crossinline run: () -> Unit) {
        Handler(Looper.getMainLooper()).postDelayed({
            run()
        }, delay)
    }

    fun View.showView() {
        visibility = View.VISIBLE
        animate()
            .alpha(1.0f)
            .setDuration(500)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)

                }
            })
    }

    //    fun convertDateAndTime(dateTimeString: String) {
//        try {
//            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH)
//            val outputFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss a", Locale.ENGLISH)
//
//            // Parse the input timestamp to Date object
//            val data = inputFormat.parse(dateTimeString)
//
//            // Format the date to the desired output format
//            val formattedDate = data?.let { outputFormat.format(it) }
//
//            // Convert UTC formatted date to local time
//            val df = SimpleDateFormat("yyyy-MM-dd hh:mm:ss a", Locale.ENGLISH)
//            df.timeZone = TimeZone.getTimeZone("UTC")
//            val utcDate = df.parse(formattedDate)
//            df.timeZone = TimeZone.getDefault()
//            val localFormattedDate = utcDate?.let { df.format(it) }
//
//            // Get hours/minutes information
//            val timeFormat = SimpleDateFormat("hh:mm a", Locale.ENGLISH)
//            time = localFormattedDate?.let { timeFormat.format(df.parse(it)) }!!
//            // Get year information
//            val yearMonth = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
//            yearMonthDate = localFormattedDate?.let { yearMonth.format(df.parse(it)) }!!
//
//            // Get AM/PM information
//            val amPmFormat = SimpleDateFormat("a", Locale.ENGLISH)
//            amPm = localFormattedDate?.let { amPmFormat.format(df.parse(it)) }!!
//
//            // Calculate time differences
//            val cal1: Calendar = Calendar.getInstance()
//            val cal2: Calendar = Calendar.getInstance()
//            cal2.time = df.parse(localFormattedDate)
//
//            val  msDiff: Long = cal1.timeInMillis - cal2.timeInMillis
//            daysDiff = TimeUnit.MILLISECONDS.toDays(msDiff)
//            monthsDiff = (daysDiff / 30.44).toLong()
//            seconds = TimeUnit.MILLISECONDS.toSeconds(msDiff)
//            minutes = TimeUnit.MILLISECONDS.toMinutes(msDiff)
//            hours = TimeUnit.MILLISECONDS.toHours(msDiff)
//
//            // Now, you have daysDiff, monthsDiff, seconds, minutes, hours, and amPm available
//            println("AM/PM: $amPm")
//        } catch (e: Exception) {
//            e.printStackTrace()
//            // Handle the parsing exception
//        }
//    }
    fun ignoreFirstWhiteSpace(et: EditText, limit: Int): Array<InputFilter> {
        val filter: InputFilter = object : InputFilter {
            var canEnterSpace = false
            override fun filter(
                source: CharSequence, start: Int, end: Int,
                dest: Spanned, dstart: Int, dend: Int,
            ): CharSequence {
                if (et.text.toString() == "") {
                    canEnterSpace = false
                }
                val builder = StringBuilder()
                for (i in start until end) {
                    val currentChar = source[i]
                    if (Character.isLetterOrDigit(currentChar) || currentChar == '_') {
                        builder.append(currentChar)
                        canEnterSpace = true
                    }
                    if (Character.isWhitespace(currentChar) && canEnterSpace) {
                        builder.append(currentChar)
                    }
                }
                return builder.toString()
            }
        }
        return arrayOf(filter, InputFilter.LengthFilter(limit), ignoreSpecialCharacters())
    }

    fun ignoreSpecialCharacters(): InputFilter {
        val filter =
            InputFilter { source, start, end, dest, dstart, dend ->
                var filtered = ""
                for (i in start until end) {
                    val character = source[i]
                    if (Character.isWhitespace(character) || Character.isLetterOrDigit(character)) {
                        filtered += character
                    }
                }
                filtered
            }
        return filter
    }

    fun View.disableMultiTap(duration: Long = 1000) {
        try {
            isEnabled = false
            android.os.Handler(Looper.getMainLooper()).postDelayed({
                isEnabled = true
            }, duration)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun View.messgaegDisableMultiTap() {
        try {
            isEnabled = false
            android.os.Handler(Looper.getMainLooper()).postDelayed({
                isEnabled = true
            }, 2000)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun String.toEncrypt(): String {
        return CryptoManager.encrypt(this)
    }

    fun Any.toJsonString(): String {
        val gson = Gson()
        return gson.toJson(this)
    }

    inline fun <reified T> String.fromJsonToGson(): T {
        return Gson().fromJson(this, T::class.java)
    }

    inline fun <reified T> String?.fromJsonToList(): List<T>? {
        if (this.isNullOrEmpty()) return null

        val type = object : TypeToken<List<T>>() {}.type
        return Gson().fromJson<List<T>>(this, type)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun String.toDecrypt(): String {
        return CryptoManager.decrypt(this)
    }


    /** Save Image In Device after crop the image */
    fun Context.saveImage(bitmap: Bitmap): Uri? {
        var uri: Uri? = null
        try {
            val fileName = System.nanoTime().toString() + ".png"
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                } else {
                    val directory =
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                    val file = File(directory, fileName)
                    put(MediaStore.MediaColumns.DATA, file.absolutePath)
                }
            }

            uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            uri?.let {
                contentResolver.openOutputStream(it).use { output ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, output!!)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    values.apply {
                        clear()
                        put(MediaStore.Audio.Media.IS_PENDING, 0)
                    }
                    contentResolver.update(uri, values, null, null)
                }
            }
            return uri
        } catch (e: java.lang.Exception) {
            if (uri != null) {
                // Don't leave an orphan entry in the MediaStore
                contentResolver.delete(uri, null, null)
            }
            throw e
        }
    }

    /**  Load BitMap From View */
    fun loadBitmapFromView(v: View): Bitmap {
        val b = Bitmap.createBitmap(v.width, v.height, Bitmap.Config.ARGB_8888)
        val c = Canvas(b)
        v.draw(c)
        return b
    }

    fun isInternetAvailables(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val networkCapabilities = connectivityManager.activeNetwork ?: return false
            val actNw =
                connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false

            return when {
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            // For devices below Android M
            val networkInfo = connectivityManager.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnected
        }
    }

    fun <T> toJsonObject(vararg pairs: Pair<String, T>): JSONObject {
        val parentObject = JSONObject()
        pairs.forEach { pair ->
            parentObject.put(pair.first, pair.second)
        }
        return parentObject
    }

    fun vibrateOnce(context: Context, amplitude: Int, millis: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            val vib = vibratorManager.defaultVibrator

            vib.vibrate(VibrationEffect.createOneShot(millis, amplitude))
        } else {
            val vib = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vib.vibrate(millis)
        }
    }

    /**To load image on background before setting to image view*/
    @SuppressLint("UseCompatLoadingForDrawables")
    suspend fun ImageView.loadImageAsync(url: String, drawable: Int = R.drawable.ic_greyuser) {
        val bitmap: Bitmap? = withContext(Dispatchers.IO) {
            try {
                // Perform the image loading operation here (e.g., using Glide, Picasso, or any other image loading library)
                // For example, using Glide:
                Glide.with(this@loadImageAsync)
                    .asBitmap()
                    //      .apply(RequestOptions().override(10, 10))
                    .load(url)
                    .error(drawable)
                    .submit()
                    .get()
            } catch (e: Exception) {
                // Handle exceptions if any
                drawableToBitmap(MainApplication.get().applicationContext.getDrawable(drawable)!!)
            }
        }

        // Set the loaded image to ImageView on the main thread
        bitmap?.let {
            withContext(Dispatchers.Main) {
                setImageBitmap(it)
            }
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    suspend fun ImageView.loadImageAsyncMessage(url: String) {
        val bitmap: Bitmap? = withContext(Dispatchers.IO) {
            try {
                // Perform the image loading operation here (e.g., using Glide, Picasso, or any other image loading library)
                // For example, using Glide:
                Glide.with(this@loadImageAsyncMessage)
                    .asBitmap()
                    //      .apply(RequestOptions().override(10, 10))
                    .encodeQuality(10)
                    .load(url)
                    .error(R.drawable.ic_greyuser)
                    .submit()
                    .get()
            } catch (e: Exception) {
                // Handle exceptions if any
                Log.e("failedToLoadImage", "Failed Load Image ")
                drawableToBitmap(MainApplication.get().applicationContext.getDrawable(R.drawable.ic_greyuser)!!)

            }
        }

        // Set the loaded image to ImageView on the main thread
        bitmap?.let {
            withContext(Dispatchers.Main) {
                setImageBitmap(it)
            }
        }
    }


    @SuppressLint("UseCompatLoadingForDrawables")
    suspend fun ImageView.loadImageMessage(url: String) {
        val bitmap: Bitmap? = withContext(Dispatchers.IO) {
            try {
                // Perform the image loading operation here (e.g., using Glide, Picasso, or any other image loading library)
                // For example, using Glide:
                Glide.with(this@loadImageMessage)
                    .asBitmap()
                    //      .apply(RequestOptions().override(10, 10))
                    .encodeQuality(10)
                    .load(url)
                    .error(R.drawable.post_not_found)
                    .submit()
                    .get()
            } catch (e: Exception) {
                // Handle exceptions if any
                Log.e("failedToLoadImage", "Failed Load Image ")
                drawableToBitmap(MainApplication.get().applicationContext.getDrawable(R.drawable.ic_greyuser)!!)

            }
        }

        // Set the loaded image to ImageView on the main thread
        bitmap?.let {
            withContext(Dispatchers.Main) {
                setImageBitmap(it)
            }
        }
    }

    /**convert drawable resource to bitmap*/
    private fun drawableToBitmap(drawable: Drawable): Bitmap? {
        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }
        val width = drawable.intrinsicWidth
        val height = drawable.intrinsicHeight
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }


    /** For description conversion*/
    fun convertDesc(desc: String): String {
        if (desc.isNullOrEmpty()) {
            return ""
        }

        val regex = Regex("@\\[([^\\]]+)]\\(([^)]+)\\)")
//        val regex = Regex("@\\w+")
        val outputText = regex.replace(desc) { result ->
            "@${result.groupValues[1]}"
        }

        return outputText
    }


    fun getUsernames(description: String): ArrayList<String> {
        val mentions = ArrayList<String>()
        val regex = Regex("@\\[([^]]+)]\\(([^)]+)\\)")

        val matches = regex.findAll(description)
        matches.forEach { mention ->
            val mentionText = mention.groupValues[0]
            val mentionName = mentionText.substringAfter("[").substringBefore("]")
                .substringAfter("@")
            mentions.add(mentionName)
        }
        return mentions
    }

    fun getSpans(body: String?, prefix: Char): ArrayList<IntArray> {
        val spans = ArrayList<IntArray>()
        val pattern: Pattern = Pattern.compile("$prefix\\w+")
        val matcher: Matcher = pattern.matcher(body.toString())

        // Check all occurrences
        while (matcher.find()) {
            val currentSpan = IntArray(2)
            currentSpan[0] = matcher.start()
            currentSpan[1] = matcher.end()
            spans.add(currentSpan)
        }
        return spans


    }

    fun setSpanComment(
        commentsContent: SpannableString,
        hashtagSpans: ArrayList<IntArray>,
        click: Hashtag.onHashtagClick
    ) {
        for (i in hashtagSpans.indices) {
            val span = hashtagSpans[i]
            val hashTagStart = span[0]
            val hashTagEnd = span[1]
            commentsContent.setSpan(
                Hashtag(click, ""),
                hashTagStart,
                hashTagEnd, 0
            )
        }
    }

    fun setSpanComment(
        commentsContent: SpannableString,
        hashtagSpans: ArrayList<IntArray>,
        click: Hashtag.onHashtagClick,
        hashtagList: ArrayList<com.wedo8020.app.response.Hashtag>
    ) {
        Log.e("hashtag id", commentsContent.toString())
        for (i in hashtagSpans.indices) {
            val span = hashtagSpans[i]
            val hashTagStart = span[0]
            val hashTagEnd = span[1]

            val hashtag = hashtagList?.filter {

                "#${it.tag}" == commentsContent.substring(
                    hashTagStart,
                    hashTagEnd
                )
            }
            commentsContent.setSpan(
                Hashtag(
                    click, if (!hashtag.isNullOrEmpty()) {
                        hashtag?.get(0)?._id ?: ""
                    } else {
                        ""
                    }
                ),
                hashTagStart,
                hashTagEnd, 0
            )
        }
    }

    fun highLightInBlue(mentionUserWithText: String): SpannableStringBuilder {
        var spannableString = convertDesc(mentionUserWithText)
        val spannableStringBuilder = SpannableStringBuilder(spannableString)

// Find and highlight mentions
        val pattern = Regex("@[a-zA-Z0-9_]+") // Regular expression pattern to find mentions
        val matches = pattern.findAll(spannableString)

        for (match in matches) {
            val start = match.range.first
            val end = match.range.last

            // Apply color to mentions
            spannableStringBuilder.setSpan(
                ForegroundColorSpan(Color.BLUE),
                start, end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        return spannableStringBuilder
    }

    fun setSpanUname(
        commentsUname: SpannableString,
        callOutSpans: ArrayList<IntArray>,
        click: Hashtag.onHashtagClick,
        mentionUser: List<MentionUser>?
    ) {
        for (i in callOutSpans.indices) {
            val span = callOutSpans[i]
            val callOutStart = span[0]
            val callOutEnd = span[1]
            val user = mentionUser?.filter {
                "@${it.username}" == commentsUname.substring(
                    callOutStart,
                    callOutEnd
                )
            }
            Log.e("mentionClickcomment", user.toString() + " " + mentionUser.toString())
            commentsUname.setSpan(
                CalloutLink(
                    click, if (!user.isNullOrEmpty()) {
                        user?.get(0)?._id ?: ""
                    } else {
                        ""
                    }
                ),
                callOutStart,
                callOutEnd, 0
            )

        }
    }

    //download post
    /*    fun downloadFile(context: Context, url: String, fileName: String) {

            Log.e("downloadFile","dfh")
            val request = DownloadManager.Request(Uri.parse(url))
                .setTitle("Downloading File")
                .setDescription("Downloading $fileName")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, fileName)

            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.enqueue(request)
        }*/


    suspend fun downloadFileFromUrl(
        context: Context,
        fileUrl: String,
        fileName: String,
        type: String
    ): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL(fileUrl)
                val connection: URLConnection = url.openConnection()
                connection.connect()

                if (connection.getInputStream() != null) {
                    val contentResolver = context.contentResolver

                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                        // Adjust MIME type as per your file type
                        if (type == "video")
                            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4") // For videos
                        else
                            put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                        // put(MediaStore.MediaColumns.MIME_TYPE, "image/png") // For images
                    }

                    val collection =
                        MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                    val itemUri = contentResolver.insert(collection, contentValues)

                    itemUri?.let { newUri ->
                        contentResolver.openOutputStream(newUri)?.use { outputStream ->
                            connection.getInputStream().use { inputStream ->
                                inputStream.copyTo(outputStream)
                            }
                        }
                        return@withContext true
                    } ?: run {
                        return@withContext false
                    }
                } else {
                    // Handle unsuccessful connection
                    return@withContext false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Handle exceptions
                return@withContext false
            }
        }
    }

    /* fun setSpanUname(
         commentsUname: SpannableString,
         callOutSpans: ArrayList<IntArray>,
         click: Hashtag.onHashtagClick,
         mentionUser: List<MentionUser>
     ) {
         for (i in callOutSpans.indices) {
             val span = callOutSpans[i]
             val callOutStart = span[0]
             val callOutEnd = span[1]
             val user = mentionUser.filter {
                 "@${it.username}" == commentsUname.substring(
                     callOutStart,
                     callOutEnd
                 )
             }
             commentsUname.setSpan(
                 CalloutLink(
                     click, if (user.isNotEmpty()) {
                         user[0]._id
                     } else {
                         ""
                     }
                 ),
                 callOutStart,
                 callOutEnd, 0
             )

         }
     }*/
    fun setSpanHashtagName(
        commentsContent: SpannableString,
        hashtagSpans: ArrayList<IntArray>,
        context: Context,
        click: Hashtag.onHashtagClick

    ) {
        for (i in hashtagSpans.indices) {
            val span = hashtagSpans[i]
            val hashTagStart = span[0]
            val hashTagEnd = span[1]
            commentsContent.setSpan(
                Hashtag(click, ""),
                hashTagStart,
                hashTagEnd, 0
            )
        }
    }

    fun setSpanUnameForBio(
        commentsUname: SpannableString,
        callOutSpans: ArrayList<IntArray>,
        click: Hashtag.onHashtagClick,
        mentionUser: List<BioMentionUser>
    ) {
        for (i in callOutSpans.indices) {
            val span = callOutSpans[i]
            val callOutStart = span[0]
            val callOutEnd = span[1]
            val user = mentionUser.filter {
                "@${it.username}" == commentsUname.substring(
                    callOutStart,
                    callOutEnd
                )
            }
            commentsUname.setSpan(
                CalloutLink(
                    click, if (user.isNotEmpty()) {
                        user[0]._id
                    } else {
                        ""
                    }
                ),
                callOutStart,
                callOutEnd, 0
            )

        }
    }

    fun downloadFile(fileUrl: String, fileName: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val storageDirectory =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val outputFile = File(storageDirectory, fileName)


                val url = URL(fileUrl)
                withContext(Dispatchers.IO) {
                    val connection = url.openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"
                    connection.connect()

                    val inputStream = BufferedInputStream(url.openStream(), 8192)
                    val outputStream = FileOutputStream(outputFile)
                    val data = ByteArray(1024)
                    var total: Long = 0
                    var count: Int

                    while (inputStream.read(data).also { count = it } != -1) {
                        total += count.toLong()
                        outputStream.write(data, 0, count)
                    }

                    outputStream.flush()
                    outputStream.close()
                    inputStream.close()

                    Handler(Looper.getMainLooper()).post {
                        showToast("File downloaded successfully")
                    }
                }
            } catch (e: Exception) {
                Handler(Looper.getMainLooper()).post {
                    Log.e("downloadFile", e.toString())
                    showToast("Download failed")
                }
            }
        }

    }

    fun downloadFile(context: Context, fileUrl: String, fileName: String) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val storageDirectory =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val outputFile = File(storageDirectory, fileName)


                val url = URL(fileUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connect()

                val inputStream = BufferedInputStream(url.openStream(), 8192)
                val outputStream = FileOutputStream(outputFile)
                val data = ByteArray(1024)
                var total: Long = 0
                var count: Int

                while (inputStream.read(data).also { count = it } != -1) {
                    total += count.toLong()
                    outputStream.write(data, 0, count)
                }

                outputStream.flush()
                outputStream.close()
                inputStream.close()

                Handler(Looper.getMainLooper()).post {
                    showToast("File downloaded successfully")
                }
            } catch (e: Exception) {
                Handler(Looper.getMainLooper()).post {
                    Log.e("downloadFile", e.toString())
                    showToast("Download failed")
                }
            }
        }

    }

    fun showToast(
        msg: String,
        heading: String = "",
        imageUrl: String = "",
        color: Int = R.color.card_notification_color, run: (() -> Unit)? = null
    ) {
        if (run == null) {
            ShowCustomMessage.addCustomLayout(msg, heading, imageUrl, color)
        } else {
            ShowCustomMessage.addCustomLayout(msg, heading, imageUrl, color) {
                run()
            }
        }

//        Toast.makeText(MainApplication.get().applicationContext, msg, Toast.LENGTH_SHORT).show()
    }

    fun printLog(logType: LogTypeEnum = LogTypeEnum.ERROR, tag: String, msg: String) {
        when (logType) {
            LogTypeEnum.ERROR -> Log.e(tag, msg)
            LogTypeEnum.DEBUG -> Log.d(tag, msg)
            LogTypeEnum.INFO -> Log.i(tag, msg)
            LogTypeEnum.WARNING -> Log.w(tag, msg)
            LogTypeEnum.VERBOSE -> Log.v(tag, msg)
            LogTypeEnum.WTF -> Log.wtf(tag, msg)
        }
    }

    fun View.hideWithDelay() {
        this.visibility = View.VISIBLE
        runWithDelay(1500) {
            this.visibility = View.GONE
        }
    }

    fun View.visibleWithDelay(delay: Long = 1500) {
        this.visibility = View.INVISIBLE
        runWithDelay(delay) {
            this.visibility = View.VISIBLE
        }
    }

    fun <T> LiveData<T>.observeOnce(observer: Observer<T>) {
        observeForever(object : Observer<T> {
            override fun onChanged(t: T) {
                observer.onChanged(t)
                removeObserver(this)
            }
        })
    }


    private fun calculateScale(width: Int, height: Int): Int {
        val targetWidth = 1080
        val targetHeight = 1920
        var scale = 1

        while (width / (2 * scale) > targetWidth && height / (2 * scale) > targetHeight) {
            scale *= 2
        }

        return scale
    }

    fun compressImage(context: Context, originalImageUri: Uri): Uri {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }

        val inputStream = context.contentResolver.openInputStream(originalImageUri)
        BitmapFactory.decodeStream(inputStream, null, options)
        inputStream?.close()

        val scale = calculateScale(options.outWidth, options.outHeight)
        options.inSampleSize = scale
        options.inJustDecodeBounds = false

        val originalBitmap = context.contentResolver.openInputStream(originalImageUri)?.use {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                BitmapFactory.decodeStream(it, null, options)
            } else {
                // Use BitmapFactory to decode the stream for SDK versions less than 28
                BitmapFactory.decodeStream(it)
            }
        }

        val compressedImageUri = createCompressedImageFile(context).toUri()
        val compressedOutputStream = context.contentResolver.openOutputStream(compressedImageUri)

        try {
            if (compressedOutputStream != null && originalBitmap != null) {
                originalBitmap.compress(Bitmap.CompressFormat.JPEG, 80, compressedOutputStream)
                Log.d("Compression", "Image compressed successfully")
            } else {
                Log.e("Compression", "Failed to compress image")
            }
        } catch (e: IOException) {
            Log.e("Compression", "Error compressing image: ${e.message}")
        } finally {
            compressedOutputStream?.close()
        }

        return compressedImageUri
    }

    /*    private fun calculateScale(width: Int, height: Int): Int {
            val targetWidth = 1080
            val targetHeight = 1920
            var scale = 1

            while (width / (2 * scale) > targetWidth && height / (2 * scale) > targetHeight) {
                scale *= 2
            }

            return scale
        }*/

    private fun createCompressedImageFile(context: Context): File {
        val storageDir = context.cacheDir
        return File.createTempFile("compressed", ".jpg", storageDir)
    }

    fun findLastWord(text: String): String {
        val textTrim = text.trim()
        val spaceIndex = textTrim.lastIndexOf(' ')
        return if (spaceIndex != -1) {
            textTrim.substring(spaceIndex + 1)
        } else {
            textTrim
        }
    }

    fun buildLink(customScheme: String): String? {
        return try {
            val baseUrl = "https://wedo80.page.link"
            val link = "https://wedo80.com"

            val dynamicLink = Uri.parse(baseUrl).buildUpon()
                .appendQueryParameter("link", link)
                .appendQueryParameter("ibi", "com.wedo8020.app") // iOS Bundle ID
                .appendQueryParameter("ifl", customScheme) // iOS custom scheme
                .appendQueryParameter("utm_campaign", customScheme) // Analytics parameter
                .build()

            dynamicLink.toString()
        } catch (error: Exception) {
            null
        }
    }

    fun <T : Any> autoCleared() = AutoClearedValue<T>()

    class AutoClearedValue<T : Any> : ReadWriteProperty<Fragment, T?> {

        private var _value: T? = null

        override fun getValue(thisRef: Fragment, property: KProperty<*>): T? {
            return _value
        }

        override fun setValue(thisRef: Fragment, property: KProperty<*>, value: T?) {
            thisRef.viewLifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
                override fun onDestroy(owner: LifecycleOwner) {
                    _value = null
                }
            })
            _value = value
        }
    }

    fun formatDate(inputDate: String): String {
        if (inputDate == "") {
            return ""
        }
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMM yyyy \nhh:mm:ss a", Locale.getDefault())

        var formattedDate = ""
        try {
            val date = inputFormat.parse(inputDate)
            formattedDate = outputFormat.format(date ?: Date())
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return formattedDate
    }

    fun setDisappearInText(disappearIn: String): String {
        return when (disappearIn) {
            AppConstant.H24 -> "24 hours"
            AppConstant.H48 -> "48 hours"
            AppConstant.W1 -> "1 week"
            AppConstant.M1 -> "1 month"
            AppConstant.M3 -> "3 months"
            AppConstant.INFINITY -> "Never"
            else -> {
                "Never"
            }
        }
    }

    // Capitalize first letter of a string
    fun String.capitalizeFirstChar(): String {
        return replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
        }
    }

    /** Convert the date in day month years second or minutes */
    /*   fun convertDateAndTime(dateTimeString: String) {
           val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH)
           val outputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)

               */
    /** Parse the input timestamp to Date object*//*
            val data = inputFormat.parse(dateTimeString)

            */
    /** Format the date to the desired output format*//*
            val dates = data?.let { outputFormat.format(it) }
            val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
            df.timeZone = TimeZone.getTimeZone("UTC")
            val date = df.parse(dates)
            df.timeZone = TimeZone.getDefault()
            val formattedDate = date?.let { df.format(it) }
            val cal: Calendar = Calendar.getInstance()
            val sdf = SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss",
                Locale.ENGLISH
            )
            cal.time = sdf.parse(formattedDate) // all done
            val cal1: Calendar = Calendar.getInstance()
            val formatted: String = sdf.format(cal1.time) //formatted date as i want
            cal1.time = sdf.parse(formatted) // all done
            val msDiff: Long = cal1.timeInMillis - cal.timeInMillis

            daysDiff = TimeUnit.MILLISECONDS.toDays(msDiff)
            monthsDiff = (daysDiff / 30.44).toLong()
            seconds = TimeUnit.MILLISECONDS.toSeconds(msDiff)
            minutes = TimeUnit.MILLISECONDS.toMinutes(msDiff)
            hours = TimeUnit.MILLISECONDS.toHours(msDiff)
        }*/



    fun convertDateAndTime(dateTimeString: String) {
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH)
            val outputFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss a", Locale.ENGLISH)

            /** Parse the input timestamp to Date object*/
            val data = inputFormat.parse(dateTimeString)

            // Format the date to the desired output format
            val formattedDate = data?.let { outputFormat.format(it) }

            /** Convert UTC formatted date to local time*/
            val df = SimpleDateFormat("yyyy-MM-dd hh:mm:ss a", Locale.ENGLISH)
            df.timeZone = TimeZone.getTimeZone("UTC")
            val utcDate = formattedDate?.let { df.parse(it) }
            df.timeZone = TimeZone.getDefault()
            val localFormattedDate = utcDate?.let { df.format(it) }

            /** Get hours/time information here*/
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.ENGLISH)
            time = localFormattedDate?.let { df.parse(it)?.let { it1 -> timeFormat.format(it1) } }!!

            val timeFormatOne = SimpleDateFormat("hh:mm:ss a", Locale.ENGLISH)
            timeProperFormat = localFormattedDate.let {
                df.parse(it)
                    ?.let { it1 -> timeFormatOne.format(it1) }
            }!!

            /** Get year-month-days(2001-01-01) information here*/
            val yearMonth = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
            yearMonthDate = localFormattedDate.let {
                df.parse(it)
                    ?.let { it1 -> yearMonth.format(it1) }
            }!!

            /** Get year-month-days (01Jan2001) information here*/
            val yearMonths = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
            finalDateFormat = localFormattedDate.let {
                df.parse(it)
                    ?.let { it1 -> yearMonths.format(it1) }
            }!!

            /** Get year-month-days (01Jan2001) information here*/
            val yearsMonths = SimpleDateFormat("MMM dd , yyyy", Locale.ENGLISH)
            messageDateMonth = localFormattedDate.let {
                df.parse(it)
                    ?.let { it1 -> yearsMonths.format(it1) }
            }!!
            /** Get AM/PM information here*/
            val amPmFormat = SimpleDateFormat("a", Locale.ENGLISH)
            amPm = localFormattedDate.let { df.parse(it)?.let { it1 -> amPmFormat.format(it1) } }!!


            /** Get year-month-days (01Jan2001) information here*/
            val yearsMonth = SimpleDateFormat("EEEE, dd MMM yyyy", Locale.ENGLISH)
            finalDateFormats = localFormattedDate.let {
                df.parse(it)
                    ?.let { it1 -> yearsMonth.format(it1) }
            }!!
            /** Calculate time differences*/
            val cal1: Calendar = Calendar.getInstance()
            val cal2: Calendar = Calendar.getInstance()
            cal2.time = df.parse(localFormattedDate)!!

            /** Now, you have daysDiff, monthsDiff, seconds, minutes, hours, and amPm available*/
            val msDiff: Long = cal1.timeInMillis - cal2.timeInMillis
            daysDiff = TimeUnit.MILLISECONDS.toDays(msDiff)
            monthsDiff = (daysDiff / 30.44).toLong()
            seconds = TimeUnit.MILLISECONDS.toSeconds(msDiff)
            minutes = TimeUnit.MILLISECONDS.toMinutes(msDiff)
            hours = TimeUnit.MILLISECONDS.toHours(msDiff)

        } catch (e: Exception) {
            e.printStackTrace()
            // Handle the parsing exception
        }
    }


    /*  fun isInternetAvailables(context: Context): Boolean {
          val connectivityManager =
              context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
              val networkCapabilities = connectivityManager.activeNetwork ?: return false
              val actNw =
                  connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false

              return when {
                  actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                  actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                  actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                  else -> false
              }
          } else {
              // For devices below Android M
              val networkInfo = connectivityManager.activeNetworkInfo
              return networkInfo != null && networkInfo.isConnected
          }
      }*/
    fun getCountryCode(context: Context): String {
        val telephonyManager =
            context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val countryCode = telephonyManager.networkCountryIso
        val locale = context.resources.configuration.locale.country



        return if (countryCode.isNotEmpty()) {
            countryCode
        } else if (locale.isNotEmpty()) {
            // Fallback to default locale if network country ISO is empty
            locale
        } else {
            "in"
        }
    }

    fun getCountryNameFromCode(): String {
        val countryCode = getCountryCode(MainApplication.get().applicationContext)
        val locale = Locale("", countryCode)
        val country = locale.displayCountry
        return if (country.trim().isEmpty()) {
            "India"
        } else {
            country
        }
    }


    /** Here we get the image size */
    fun getRealPathFromUri(context: Context, uri: Uri?): String? {
        var filePath: String? = null
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        context.contentResolver.query(uri!!, projection, null, null, null).use { cursor ->
            if (cursor != null && cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                filePath = cursor.getString(columnIndex)
            }
        }
        return filePath
    }
}