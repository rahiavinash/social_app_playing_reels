package com.wedo8020.app.utils

import android.content.Context
import android.net.ConnectivityManager
import android.os.Handler


class InternetChecker(private val context: Context) {

    private var handler: Handler = Handler()
    private var isCheckingInternet = false

    fun startInternetCheck(intervalMillis: Long = 5000, callback: (Boolean) -> Unit) {
        if (!isCheckingInternet) {
            isCheckingInternet = true
            checkInternetConnection(callback)
            handler.postDelayed(object : Runnable {
                override fun run() {
                    checkInternetConnection(callback)
                    handler.postDelayed(this, intervalMillis)
                }
            }, intervalMillis)
        }
    }

    fun stopInternetCheck() {
        isCheckingInternet = false
        handler.removeCallbacksAndMessages(null)
    }

    private fun checkInternetConnection(callback: (Boolean) -> Unit) {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        val isConnected = networkInfo != null && networkInfo.isConnected
        callback(isConnected)
    }
}
