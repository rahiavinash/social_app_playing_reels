package com.wedo8020.app.utils

//import android.content.Context
//import com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS
//import com.arthenica.mobileffmpeg.FFmpeg
//import java.io.File
//
//
//interface FFmpegExecuteCallback {
//    fun onSuccess(imagePath: String?)
//    fun onFailure()
//}
//
//fun extractFrameFromUrl(videoUrl: String, positionMs: Int, callback: FFmpegExecuteCallback,context:Context) {
//    val outputPath: String = context.filesDir.absolutePath + File.separator + "extracted_frame.jpg"
//    val cmd =
//        "-i $videoUrl" + " -an -vf \"scale=320:-1\" -ss " + positionMs / 1000 + " -vframes 1 -q:v 5 " + outputPath
//    val outputFile = File(outputPath)
//    if (outputFile.exists()) {
//        outputFile.delete()
//    }
//    FFmpeg.executeAsync(cmd) { _, returnCode ->
//        if (returnCode == RETURN_CODE_SUCCESS) {
//            callback.onSuccess(outputPath)
//        } else {
//            callback.onFailure()
//        }
//    }
//}