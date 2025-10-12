package com.wedo8020.app.network

import com.wedo8020.app.BuildConfig
import com.wedo8020.app.request.LoginRequest
import com.wedo8020.app.response.BaseResponse
import com.wedo8020.app.utils.Prefs
import retrofit2.Call
import retrofit2.Response
import java.io.IOException


class TokenManager(private val apiService: ApiService2) {
    var accessToken: String = ""

    @Throws(IOException::class)
    fun refreshAccessToken(): String? {

            // Make a request to refresh the access token using the refresh token
            val call: Call<BaseResponse?>? = apiService.refreshToken(LoginRequest(refreshToken = Prefs.init().refreshToken,
                appVersion = BuildConfig.VERSION_NAME))
            val response: Response<BaseResponse?>? = call!!.execute()
            if (response!!.isSuccessful) {
                val tokenResponse: BaseResponse? = response!!.body()
                if (tokenResponse != null) {
                    accessToken = tokenResponse.data!!.accessToken
                    Prefs.init().accessToken = accessToken
                    // Update the new access token in your API requests
                    // Optionally, you can also update the refresh token if it changes
                    return accessToken
                }
            } else {
                // Handle error response (e.g., invalid refresh token)
                // You might want to throw an exception or log the error
            }

        return accessToken
    }
}
