package com.wedo8020.app.network

//import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import android.content.Intent
import android.os.Build
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import androidx.annotation.RequiresApi
import com.wedo8020.app.MainApplication
import com.wedo8020.app.R
import com.wedo8020.app.mainui.activities.LoginActivity
import com.wedo8020.app.request.BlockedRequest
import com.wedo8020.app.request.CommentRequest
import com.wedo8020.app.request.CreatePost
import com.wedo8020.app.request.CreatePostWithMedia
import com.wedo8020.app.request.DataPayload
import com.wedo8020.app.request.DeletePost
import com.wedo8020.app.request.EditPost
import com.wedo8020.app.request.EditPrivacy
import com.wedo8020.app.request.EditProfilePrivacy
import com.wedo8020.app.request.ExperienceRequest
import com.wedo8020.app.request.FeedBackRequest
import com.wedo8020.app.request.LikeListRequest
import com.wedo8020.app.request.HashTagListResponse
import com.wedo8020.app.request.HashtagPayload
import com.wedo8020.app.request.LoginRequest
import com.wedo8020.app.request.PostPinRequest
import com.wedo8020.app.request.PostRequest
import com.wedo8020.app.request.PostRestrict
import com.wedo8020.app.request.RestrictReportRequest
import com.wedo8020.app.request.SearchRequest
import com.wedo8020.app.request.SoundPinRequest
import com.wedo8020.app.request.UpdateInterestRequest
import com.wedo8020.app.request.UserBlockedRequest
import com.wedo8020.app.request.UserListRequest
import com.wedo8020.app.request.UserNotificationRequest
import com.wedo8020.app.request.UserRequest
import com.wedo8020.app.request.UserRestrict
import com.wedo8020.app.request.mentionUserRequest
import com.wedo8020.app.response.BaseResponse
import com.wedo8020.app.response.CommentBaseResponse
import com.wedo8020.app.response.InterestResponse
import com.wedo8020.app.response.LikeListResponse
import com.wedo8020.app.response.MentionUserResponse
import com.wedo8020.app.response.PostBaseResponse
import com.wedo8020.app.response.PostDetailsResponse
import com.wedo8020.app.response.ProfileAccountResponse
import com.wedo8020.app.response.ProfileNotificationResponse
import com.wedo8020.app.response.ProfilePostResponse
import com.wedo8020.app.response.ProfilePrivacyResponse
import com.wedo8020.app.response.RestrictResponse
import com.wedo8020.app.response.UserDetailsResponse
import com.wedo8020.app.response.UserExperienceResponse
import com.wedo8020.app.response.UserListResponse
import com.wedo8020.app.utils.LogTypeEnum
import com.wedo8020.app.utils.Prefs
import com.wedo8020.app.utils.Utils
import com.wedo8020.app.utils.Utils.toDecrypt
import com.wedo8020.app.utils.Utils.toEncrypt
import com.wedo8020.app.utils.Utils.toJsonString
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.wedo8020.app.BuildConfig
import com.wedo8020.app.request.ChatHistoryRequest
import com.wedo8020.app.request.ChatList
import com.wedo8020.app.request.DeleteCommentRequest
import com.wedo8020.app.request.DeleteNotification
import com.wedo8020.app.request.FcmRequest
import com.wedo8020.app.request.FollowerListRequest
import com.wedo8020.app.request.FollowingListRequest
import com.wedo8020.app.request.FriendListRequest
import com.wedo8020.app.request.InterestRequest
import com.wedo8020.app.request.LinkedAccountRequest
import com.wedo8020.app.request.NotificationRequest
import com.wedo8020.app.request.PinCommentRequest
import com.wedo8020.app.request.PremiumPurchasePayLoad
import com.wedo8020.app.request.ProfilePostRequest
import com.wedo8020.app.request.PurchaseDetails
import com.wedo8020.app.request.PurchaseSrcPayload
import com.wedo8020.app.request.PurchaseTreesPayload
import com.wedo8020.app.request.RegionRequest
import com.wedo8020.app.request.RemoveLikes
import com.wedo8020.app.request.SwitchNotificationRequest
import com.wedo8020.app.request.UserDecryptFile
import com.wedo8020.app.response.AcceptRejectRequest
import com.wedo8020.app.response.AcceptRejectResponse
import com.wedo8020.app.response.AccountAvailabilityResponse
import com.wedo8020.app.response.CountryListImpactScreen
import com.wedo8020.app.response.ImpactDetail
import com.wedo8020.app.response.BlockedResponse
import com.wedo8020.app.response.ChatImageUpload
import com.wedo8020.app.response.DownloadMyDataResponse
import com.wedo8020.app.response.DropsDataRequest
import com.wedo8020.app.response.DropsDataResponse
import com.wedo8020.app.response.FriendListResponse
import com.wedo8020.app.response.LeaderboardRequest
import com.wedo8020.app.response.LeaderboardResponse
import com.wedo8020.app.response.LinkedAccountResponse
import com.wedo8020.app.response.MessageListResponse
import com.wedo8020.app.response.MessageRequest
import com.wedo8020.app.response.MessageResponse
import com.wedo8020.app.response.MostTreesPlantedByRegionResponse
import com.wedo8020.app.response.MostTreesPlantedRequest
import com.wedo8020.app.response.MostTreesPlantedResponse
import com.wedo8020.app.response.PrizeDetailsResponse
import com.wedo8020.app.response.NotificationListResponse
import com.wedo8020.app.response.PurchaseHistoryList
import com.wedo8020.app.response.RecoveryCodeRequest
import com.wedo8020.app.response.RecoveryCodeResponse
import com.wedo8020.app.response.RegisterUserRequest
import com.wedo8020.app.response.RegisterUserResponse
import com.wedo8020.app.response.SavedProfilePost
import com.wedo8020.app.response.SearchBaseResponse
import com.wedo8020.app.response.ShowChatResponse
import com.wedo8020.app.response.ShowChatResponseData
import com.wedo8020.app.response.TransferSrcDetails
import com.wedo8020.app.response.UserDataList
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import okio.Buffer
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

interface ApiService {
    @POST("user/login")
    suspend fun login(
        @Body body: LoginRequest
    ): BaseResponse
    @POST("post/remove/likes")
    suspend fun removeLikes(
        @Body body: RemoveLikes
    ): BaseResponse
    @POST("post/list")
    suspend fun postList(
        @Body body: PostRequest
    ): PostBaseResponse

    @POST("chat/history")
    suspend fun chatHistory(
        @Body body: ChatHistoryRequest
    ): ShowChatResponse

    @POST("user/details")
    suspend fun userDetails(
        @Body data: Map<String, String>
    ): UserDetailsResponse

    @POST("user/logout")
    suspend fun userLogout(
    ): BaseResponse

    @HTTP(method = "DELETE", path = "user/remove", hasBody = false)
    suspend fun userDeleteAccount(
    ): BaseResponse
    @POST("user/posts")
    suspend fun profilePosts(
        @Body data: Map<String, String>
    ): ProfilePostResponse
    @POST("user/posts")
    suspend fun savedPosts(
        @Body profilePostRequest: ProfilePostRequest
    ): SavedProfilePost
    @POST("post/like")
    suspend fun postLike(
        @Body body: PostRequest
    ): BaseResponse

    @POST("notification/list")
    suspend fun notificationList(
        @Body body: NotificationRequest
    ): NotificationListResponse

    @POST("chat/list")
    suspend fun chatList(
        @Body body: ChatList
    ): MessageListResponse
 /*   @DELETE("notification/delete")
    suspend fun notificationDelete(
        @Query("notification") notification: String
    ): BaseResponse*/


    @HTTP(method = "DELETE", path = "notification/delete", hasBody = true)
    suspend fun notificationDelete(@Body deleteNotification: DeleteNotification): BaseResponse


    @HTTP(method = "DELETE", path = "comment/delete", hasBody = true)
    suspend fun deleteComment(@Body deleteComment: DeleteCommentRequest): BaseResponse


    @POST("comment/pin")
    suspend fun pinComment(@Body pinComment : PinCommentRequest): BaseResponse


    @POST("user/list")
    suspend fun userList(
        @Body body: UserListRequest
    ): UserListResponse

    @POST("post/save")
    suspend fun postSave(
        @Body body: PostRequest
    ): BaseResponse

    @POST("post/sound")
    suspend fun postSound(
        @Body body: PostRequest
    ): BaseResponse

    @POST("sound/pin")
    suspend fun postSoundPin(
        @Body body: SoundPinRequest
    ): BaseResponse

    @POST("post/delete")
    suspend fun postDelete(
        @Body body: DeletePost
    ): BaseResponse

    @POST("post/detail")
    suspend fun postDetail(
        @Body body: DeletePost
    ): PostDetailsResponse

    @PUT("user/fcm")
    suspend fun postFcmToken(
        @Body body: FcmRequest
    ):BaseResponse
    @PUT("post/edit")
    suspend fun editPost(
        @Body body: EditPost
    ): BaseResponse


    @PUT("user/profile")
    suspend fun editProfilePrivacy(
        @Body body: EditPrivacy
    ): BaseResponse

    @PUT("user/profile")
    suspend fun finalEditProfilePrivacy(
        @Body body: EditProfilePrivacy
    ): BaseResponse

    @PUT("user/account")
    suspend fun editUserAccount(
        //  @Body body: EditAccountRequest
        @Body data: Map<String, String>
    ): BaseResponse


    @PUT("user/notification")
    suspend fun editUserNotification(
        @Body body: UserNotificationRequest
    ): BaseResponse

    @POST("post/archive")
    suspend fun postArchive(
        @Body body: DeletePost
    ): BaseResponse

    @POST("post/unarchive")
    suspend fun postUnArchive(
        @Body body: DeletePost
    ): BaseResponse

    @POST("search/mention")
    suspend fun mentionUser(
        @Body body: mentionUserRequest
    ): MentionUserResponse

    @POST("post/pin")
    suspend fun PostPin(
        @Body body: PostPinRequest
    ): BaseResponse

    @POST("user/categories")
    suspend fun updateUserInterest(
        @Body updateInterestRequest: UpdateInterestRequest
    ): BaseResponse

    @POST("info/follow")
    suspend fun followUnfollowUser(
        @Body body: UserRequest
    ): BaseResponse
    @POST("user/switchNotification")
    suspend fun switchNotification(
        @Body body: SwitchNotificationRequest
    ): BaseResponse

    @POST("info/request/status")
    suspend fun postFollowRequestStatus(
        @Body body: UserRequest
    ): BaseResponse
    @POST("chat/postShare")
    suspend fun postShareUser(
        @Body body: PostRequest
    ): PostBaseResponse

    @POST("post/notInterested")
    suspend fun notInterested(
        @Body body: PostRequest
    ): PostBaseResponse

    @POST("user/restrict")
    suspend fun userRestrict(
        @Body body: UserRestrict
    ): PostBaseResponse

    @POST("post/notInterested")
    suspend fun postRestrict(
        @Body postRestrict: PostRestrict
    ): PostBaseResponse

    @POST("post/report")
    suspend fun postReport(
        @Body body: PostRequest
    ): PostBaseResponse
    @POST("comment/report")
    suspend fun commentReport(
        @Body body: PostRequest
    ): PostBaseResponse

    @POST("user/report")
    suspend fun userReport(
        @Body body: PostRequest
    ): PostBaseResponse
    @POST("user/block")
    suspend fun userBlocked(
        @Body body: UserBlockedRequest
    ): BaseResponse
    @POST("post/repost")
    suspend fun postRepost(
        @Body body: PostRequest
    ): PostBaseResponse

    @POST("info/friends")
    suspend fun friendList(
        @Body body: FriendListRequest
    ): FriendListResponse


    @POST("info/followings/list")
    suspend fun followingList(
        @Body body: FollowingListRequest
    ): FriendListResponse



    @POST("info/followers/list")
    suspend fun followerList(
        @Body body: FollowerListRequest
    ): FriendListResponse



    @POST("info/removeFollower")
    suspend fun removeFollower(
        @Body body: UserRequest
    ): BaseResponse
    @POST("user/linked-accounts")
    suspend fun linkedAccounts(
        @Body body: LinkedAccountRequest
    ): LinkedAccountResponse


    @POST("comment/add")
    suspend fun addComment(
        @Body body: CommentRequest
    ): PostBaseResponse

    @POST("post/tempPost")
    suspend fun createPost(@Body body: CreatePost): BaseResponse

    @POST("post/media")
    suspend fun createPostWithMedia(@Body body: CreatePostWithMedia): BaseResponse

    @POST("comment/list")
    suspend fun commentList(
        @Body body: PostRequest
    ): CommentBaseResponse

    @POST("comment/like")
    suspend fun commentLike(
        @Body body: PostRequest
    ): CommentBaseResponse

    @POST("search/list")
    suspend fun searchList(
        @Body body: SearchRequest
    ): SearchBaseResponse

    @POST("like/list")
    suspend fun userLikeList(
        @Body likeListRequest: LikeListRequest
    ): LikeListResponse

    @PUT("user/experience")
    suspend fun updateUserExperience(@Body experienceRequest: ExperienceRequest): BaseResponse

    @POST("user/restrict-report-list")
    suspend fun restrictReportUser(@Body restrictReportRequest: RestrictReportRequest): RestrictResponse

    @POST("user/feedback")
    suspend fun userFeedBack(@Body feedBackRequest: FeedBackRequest): BaseResponse
    @POST("user/decrypt-file")
    suspend fun userDecryptFile(@Body userDecryptFile: UserDecryptFile): BaseResponse
    @POST("category/list")
    suspend fun getInterest(@Body interestRequest: InterestRequest): InterestResponse
    @POST("info/blockUsers")
    suspend fun userBlockList(@Body blockedRequest: BlockedRequest): BlockedResponse

    /** Get Method*/
    @GET("user/experience")
    suspend fun getUserExperience(): UserExperienceResponse
    @GET("user/my-data")
    suspend fun downloadMyData(): DownloadMyDataResponse


    @GET("user/profile")
    suspend fun userProfile(): ProfilePrivacyResponse

    @GET("user/account")
    suspend fun userProfileAccount(): ProfileAccountResponse

    @GET("user/notification")
    suspend fun userProfileNotification(): ProfileNotificationResponse


    @POST("hashtag/search")
    suspend fun hashtagList(@Body body : HashtagPayload) : HashTagListResponse


    @POST("purchase/premium")
    suspend fun premiumPurchasesApi(@Body body : PremiumPurchasePayLoad) : BaseResponse


    @POST("purchase/tree")
    suspend fun purchaseTreeApi(@Body body : PurchaseTreesPayload) : BaseResponse


    @POST("purchase/wd")
    suspend fun purchaseSrcApi(@Body body : PurchaseSrcPayload) : BaseResponse

    @GET("purchase/check-subscription")
    suspend fun getPurchaseDetails(): PurchaseDetails

    @GET("user/countries")
    suspend fun getCountryDetails(): CountryListImpactScreen


    @GET("user/impact")
    suspend fun getImpactDetails(): ImpactDetail

    @GET("leaderboard/win-prize")
    suspend fun getPrizeDetails(): PrizeDetailsResponse

/*    @POST("post/detail")
    suspend fun getPostDetails():PostDetailsResponse*/

    @POST("user/list")
    suspend fun getUserList(@Body body : UserListRequest) : UserDataList

    @POST("purchase/transaction-history")
    suspend fun getPurchaseHistory(@Body body : UserListRequest) : PurchaseHistoryList



    @POST("chat/upload")
    suspend fun chatImageUpload(@Body body : ChatImageUpload) : BaseResponse

    @POST("user/wd-transfer")
    suspend fun sendSrc(@Body body : TransferSrcDetails) : BaseResponse
    @POST("leaderboard/list")
    suspend fun getLeaderBoardDetails(@Body body : LeaderboardRequest) : LeaderboardResponse

    @POST("leaderboard/drops")
    suspend fun getDropsDetails(@Body body : DropsDataRequest) : DropsDataResponse

    @POST("leaderboard/tree")
    suspend fun getMostPlantedTrees(@Body body : MostTreesPlantedRequest) : MostTreesPlantedResponse

    @POST("leaderboard/trees-planted-countries")
    suspend fun getTreesPlantedByRegion(@Body body : RegionRequest) : MostTreesPlantedByRegionResponse
    @POST("user/profile")
    suspend fun  registerUser(@Body body : RegisterUserRequest) : RegisterUserResponse
    @POST("hashtag/posts")
    suspend fun getHashtagPost(@Body body: HashtagPayload): PostBaseResponse
    @POST("post/view")
    suspend fun postViewed(@Body body: PostRequest):BaseResponse


    @POST("user/recover-account")
    suspend fun recoveryCode(@Body body: RecoveryCodeRequest): RecoveryCodeResponse

    @POST("info/receivedRequests/list")
    suspend fun receivedMessageRequests(@Body body : MessageRequest): MessageResponse


    @POST("info/sentRequests/list")
    suspend fun sentMessageRequests(@Body body : MessageRequest): MessageResponse


    @POST("info/request/status")
    suspend fun acceptRejectRequests(@Body body : AcceptRejectRequest): AcceptRejectResponse
    @GET("user/registration-enable-status")
    suspend fun accountAvailability(): AccountAvailabilityResponse

    @GET("user/impact")
    suspend fun getImpactDetail(): BaseResponse





    companion object {
        val TAG = ApiService::class.java.name

        private fun getAPIService(): ApiService {
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
                OkHttpClient.Builder().addInterceptor(provideHeaderInterceptor()!!)
                    .addInterceptor(ForbiddenInterceptor())
                    .connectTimeout(3000, TimeUnit.SECONDS)
                    .readTimeout(3000, TimeUnit.SECONDS)
                    .writeTimeout(3000, TimeUnit.SECONDS)
                    .sslSocketFactory(getSslSocketFactory()!!, trustAllCerts[0] as X509TrustManager)
                    /**  for Local Api to comment  this Interceptor (EncryptionInterceptor())*/
                   .addInterceptor(EncryptionInterceptor())
                   //.addInterceptor(EncryptionInterceptor())
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
                .build().create(ApiService::class.java)
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
                Log.e(TAG, e.toString())
                return null
            }
        }

        fun getErrorMessageFromGenericResponse(
            exception: Exception,
        ): String? {
            var error = ""
            try {
                when (exception) {
                    is HttpException -> {
                        val body = exception.response()?.errorBody()
                        val adapter = Gson().getAdapter(BaseResponse::class.java)
                        val errorParser = adapter.fromJson(body?.string())
                        val msg = errorParser.message ?: exception.message()
                        error = if (msg == "Unauthenticated.") {
                            MainApplication.instance.applicationContext.getString(R.string.auth_error)
                        } else {
                            msg
                        }
                    }

                    is ConnectException -> {
                        error =
                            MainApplication.instance.applicationContext.getString(R.string.no_internet_message)
                    }

                    is SocketTimeoutException -> {
                        error =
                            MainApplication.instance.applicationContext.getString(R.string.no_internet_message)
                    }

                }
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                return error
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

        private fun provideHeaderInterceptor(): Interceptor? {
            return Interceptor { chain -> //*Code for request with auth token
                val accessToken = if (Prefs.init().accessToken.isNullOrEmpty()) {
                    ""
                } else if (!Utils.isTokenExpired()) {
                    Prefs.init().accessToken
                } else {
                    val apiService = ApiService2()
                    TokenManager(apiService).refreshAccessToken()
                }

                //val accessToken: String =

                Log.e("accessToken: ", accessToken ?: "null")

                if (!TextUtils.isEmpty(accessToken)) {
                    val request: Request = chain.request().newBuilder()
                        .addHeader("Accept", "application/json")
                        .addHeader("Authorization", "Bearer $accessToken")
                        .addHeader("language", Locale.getDefault().language).build()
                    chain.proceed(request)

                } else {
                    val request: Request = chain.request().newBuilder()
                        .addHeader("Accept", "application/json").build()
                    chain.proceed(request)
                }
            }
        }


        class ForbiddenInterceptor : Interceptor {
            @Throws(IOException::class)
            override fun intercept(chain: Interceptor.Chain): Response {
                val request: Request = chain.request()
                return chain.proceed(request)
            }
        }

        class EncryptionInterceptor : Interceptor {

            @RequiresApi(Build.VERSION_CODES.O)
            override fun intercept(chain: Interceptor.Chain): Response {
                val originalRequest = chain.request()
                val requestBuilder = originalRequest.newBuilder()

                // Get the original request body
                val requestBody = originalRequest.body

                requestBody?.let {
                    val buffer = Buffer()
                    it.writeTo(buffer)
                    val originalRequestBody = buffer.readUtf8()

                    Utils.printLog(
                        LogTypeEnum.ERROR,
                        "originalRequestBody",
                        originalRequestBody.toString()
                    )
                    // Encrypt the request body here
                    val encryptedRequestBody =
                        DataPayload(originalRequestBody.toEncrypt()).toJsonString()

                    // Build a new request with encrypted data

                    val encryptedBody = RequestBody.create(it.contentType(), encryptedRequestBody)
                    requestBuilder.method(
                        originalRequest.method,
                        encryptedBody
                    )
                }

                // Proceed with the request
                return chain.proceed(requestBuilder.build())
            }
        }

        //for encryption
         class ExceptionInterceptor : Interceptor {
             @RequiresApi(Build.VERSION_CODES.O)
             override fun intercept(chain: Interceptor.Chain): Response {
                 val request = chain.request()
                 val response = chain.proceed(request)
                 val responseBody = response.body
                 val encryptedData = responseBody?.string()


        /**Decrypt the response body*/



                val decryptedData = encryptedData?.toDecrypt()
                val responseDecrypted = response.newBuilder()
                        .body(decryptedData!!.toResponseBody(response.body?.contentType()))
                        .build()

                when (responseDecrypted.code) {
                    500 -> {
                        return responseDecrypted
                    }

                    402 -> {
                        return responseDecrypted
                    }
                    401 -> {

                        Prefs.init().isLogIn = false
                        Prefs.init().clear()

                        android.os.Handler(Looper.getMainLooper()).post {
                            Utils.showToast(MainApplication.get().applicationContext.getString(R.string.auth_error))
//                           val toast = Toast.makeText(
//                                MainApplication.get().applicationContext,
//                                MainApplication.get().applicationContext.getString(R.string.auth_error),
//                                Toast.LENGTH_SHORT
//                            )
//                            toast.setGravity(Gravity.CENTER, 0, 0);
//                            toast.show()
                        }
                        MainApplication.get().applicationContext.startActivity(
                            Intent(
                                MainApplication.get().applicationContext,
                                LoginActivity::class.java
                            ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        )
                        return responseDecrypted
                    }
                    else -> return responseDecrypted
                }
            }
        }


//       class ExceptionInterceptor : Interceptor {
//            override fun intercept(chain: Interceptor.Chain): Response {
//                val request = chain.request()
//                val response = chain.proceed(request)
//                when (response.code) {
//                    500 -> {
//                        return response
//                    }
//
//                    402 -> {
//                        return response
//                    }
//
//                    401 -> {
//
//                        Prefs.init().isLogIn = false
//                        Prefs.init().clear()
//
//                        android.os.Handler(Looper.getMainLooper()).post {
////                            *//*        val toast = Toast.makeText(
////                                        MainApplication.get().applicationContext,
////                                        MainApplication.get().applicationContext.getString(R.string.auth_error),
////                                        Toast.LENGTH_SHORT
////                                    )
////                                    toast.setGravity(Gravity.CENTER, 0, 0);
////                                    toast.show()*//*
//                        }
//                        MainApplication.get().applicationContext.startActivity(
//                            Intent(
//                                MainApplication.get().applicationContext,
//                                LoginActivity::class.java
//                            ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
//                        )
//                        return response
//                    }
//
//                    else -> return response
//                }
//            }
//        }

        operator fun invoke() = getAPIService()
    }
}