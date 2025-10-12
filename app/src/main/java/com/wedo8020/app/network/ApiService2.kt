package com.wedo8020.app.network

import android.content.Intent
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.widget.Toast
import com.wedo8020.app.MainApplication
import com.wedo8020.app.R
import com.wedo8020.app.mainui.activities.LoginActivity
import com.wedo8020.app.request.LoginRequest
import com.wedo8020.app.response.BaseResponse
import com.wedo8020.app.utils.Prefs
import com.google.gson.GsonBuilder
import com.wedo8020.app.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Streaming
import retrofit2.http.Url
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

interface ApiService2 {
    @POST("user/refresh")
    open fun refreshToken(@Body body: LoginRequest): Call<BaseResponse?>?
    @GET
    @Streaming
    suspend fun downloadFile(@Url fileUrl: String):  retrofit2.Response<ResponseBody>
    companion object {
        private fun getAPIService(): ApiService2 {
            val trustAllCerts = arrayOf<TrustManager>(
                object : X509TrustManager {
                    override fun checkClientTrusted(
                        chain: Array<X509Certificate?>?,
                        authType: String?
                    ) {
                    }

                    override fun checkServerTrusted(
                        chain: Array<X509Certificate?>?,
                        authType: String?
                    ) {
                    }

                    override fun getAcceptedIssuers(): Array<X509Certificate?>? {
                        return arrayOf()
                    }
                }
            )
            val client =
                OkHttpClient.Builder()
                    .addInterceptor(ApiService.Companion.ForbiddenInterceptor())
                    .connectTimeout(3000, TimeUnit.SECONDS)
                    .readTimeout(3000, TimeUnit.SECONDS)
                    .writeTimeout(3000, TimeUnit.SECONDS)
                    .sslSocketFactory(getSslSocketFactory()!!, trustAllCerts[0] as X509TrustManager)
                    .addInterceptor(provideHttpLoggingInterceptor()!!)
                    .addInterceptor(ExceptionInterceptor())
                    .build()
            val gson = GsonBuilder()
                .setLenient()
                .create()
            return Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
//                .addCallAdapterFactory(CoroutineCallAdapterFactory())
                .build().create(ApiService2::class.java)
        }

        private fun getSslSocketFactory(): SSLSocketFactory? {
            try {
                val trustAllCerts = arrayOf<TrustManager>(
                    object : X509TrustManager {
                        override fun checkClientTrusted(
                            chain: Array<X509Certificate?>?,
                            authType: String?
                        ) {
                        }

                        override fun checkServerTrusted(
                            chain: Array<X509Certificate?>?,
                            authType: String?
                        ) {
                        }

                        override fun getAcceptedIssuers(): Array<X509Certificate?>? {
                            return arrayOf()
                        }
                    }
                )
                val sslContext = SSLContext.getInstance("SSL")
                sslContext.init(null, trustAllCerts, SecureRandom())
                return sslContext.socketFactory


            } catch (e: Exception) {

                return null
            }
        }
        class ExceptionInterceptor : Interceptor {
            override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
                val request = chain.request()
                val response = chain.proceed(request)
                when (response.code) {
                    500 -> {
                        return response
                    }
                    402 -> {
                        return response
                    }
                    401 -> {

                        Prefs.init().isLogIn = false
                        Prefs.init().clear()

                        android.os.Handler(Looper.getMainLooper()).post {
                            val toast = Toast.makeText(
                                MainApplication.get().applicationContext,
                                MainApplication.get().applicationContext.getString(R.string.auth_error),
                                Toast.LENGTH_SHORT
                            )
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show()
                        }
                        MainApplication.get().applicationContext.startActivity(
                            Intent(
                                MainApplication.get().applicationContext,
                                LoginActivity::class.java
                            ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        )
                        return response
                    }
                    else -> return response
                }
            }
        }
        private fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor? {
            val httpLoggingInterceptor =
                HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
                    override fun log(message: String) {
                        Log.d("API Logging", "response => $message")
                    }
                })
//            httpLoggingInterceptor.setLevel(if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE)
            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
            return httpLoggingInterceptor
        }

        operator fun invoke() = getAPIService()

    }

}