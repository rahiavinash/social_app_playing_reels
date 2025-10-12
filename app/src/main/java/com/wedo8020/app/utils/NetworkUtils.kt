package com.wedo8020.app.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

object NetworkUtils {
    private val _isInternetAvailable = MutableLiveData<Boolean>()
    val isInternetAvailable: LiveData<Boolean>
        get() = _isInternetAvailable

    fun registerNetworkCallback(context: Context) {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val networkRequest = NetworkRequest.Builder().build()

        connectivityManager.registerNetworkCallback(networkRequest, object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                _isInternetAvailable.postValue(true)
            }

            override fun onLost(network: Network) {
                _isInternetAvailable.postValue(false)
            }
        })
    }

    fun unregisterNetworkCallback(context: Context) {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.unregisterNetworkCallback(object : ConnectivityManager.NetworkCallback() {})
    }

    fun initNetworkAvailabilityCheck(context: Context) {
        registerNetworkCallback(context)
    }
}