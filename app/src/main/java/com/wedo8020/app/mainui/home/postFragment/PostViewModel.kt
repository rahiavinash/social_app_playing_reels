package com.wedo8020.app.mainui.home.postFragment

import android.content.Intent
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.wedo8020.app.MainApplication
import com.wedo8020.app.R
import com.wedo8020.app.base.BaseViewModel
import com.wedo8020.app.base.PlayerViewAdapter
import com.wedo8020.app.mainui.activities.LoginActivity
import com.wedo8020.app.repo.RemoteRepositaryImp
import com.wedo8020.app.request.BlockedRequest
import com.wedo8020.app.request.CommentRequest
import com.wedo8020.app.request.DeleteCommentRequest
import com.wedo8020.app.request.DeleteNotification
import com.wedo8020.app.request.DeletePost
import com.wedo8020.app.request.EditPost
import com.wedo8020.app.request.EditPrivacy
import com.wedo8020.app.request.EditProfilePrivacy
import com.wedo8020.app.request.ExperienceRequest
import com.wedo8020.app.request.FcmRequest
import com.wedo8020.app.request.FeedBackRequest
import com.wedo8020.app.request.FollowerListRequest
import com.wedo8020.app.request.FollowingListRequest
import com.wedo8020.app.request.FriendListRequest
import com.wedo8020.app.request.HashTagListResponse
import com.wedo8020.app.request.HashtagPayload
import com.wedo8020.app.request.InterestRequest
import com.wedo8020.app.request.LikeListRequest
import com.wedo8020.app.request.LinkedAccountRequest
import com.wedo8020.app.request.NotificationRequest
import com.wedo8020.app.request.PinCommentRequest
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
import com.wedo8020.app.request.ProfilePostRequest
import com.wedo8020.app.request.RemoveLikes
import com.wedo8020.app.request.SwitchNotificationRequest
import com.wedo8020.app.response.BaseResponse
import com.wedo8020.app.response.BlockedResponse
import com.wedo8020.app.response.CommentBaseResponse
import com.wedo8020.app.response.DownloadMyDataResponse
import com.wedo8020.app.response.FriendListResponse
import com.wedo8020.app.response.InterestResponse
import com.wedo8020.app.response.LikeListResponse
import com.wedo8020.app.response.LinkedAccountResponse
import com.wedo8020.app.response.MentionUserResponse
import com.wedo8020.app.response.NotificationListResponse
import com.wedo8020.app.response.PostBaseResponse
import com.wedo8020.app.response.PostDetailsResponse
import com.wedo8020.app.response.ProfileAccountResponse
import com.wedo8020.app.response.ProfileNotificationResponse
import com.wedo8020.app.response.ProfilePostResponse
import com.wedo8020.app.response.ProfilePrivacyResponse
import com.wedo8020.app.response.RestrictResponse
import com.wedo8020.app.response.SavedProfilePost
import com.wedo8020.app.response.SearchBaseResponse
import com.wedo8020.app.response.ShowChatResponse
import com.wedo8020.app.response.UserDetailsResponse
import com.wedo8020.app.response.UserExperienceResponse
import com.wedo8020.app.response.UserListResponse
import com.wedo8020.app.roomdb.CreatePostDbCreation
import com.wedo8020.app.utils.Prefs
import com.wedo8020.app.utils.Utils
//import com.app.wedo8020.utils.VideoPreloadUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PostViewModel : BaseViewModel() {
    val TAG = PostViewModel::class.java.name
    val authenticationRepository = RemoteRepositaryImp()

    //    val videoPreloadUtil = VideoPreloadUtil()
    var postRequest = PostRequest()
    var commentRequest = PostRequest()
    var commentLikeRequest = PostRequest()

    var profilePostRequest = ProfilePostRequest()
    var postData = MutableLiveData<PostBaseResponse>()
    var postShareUserRequest = PostRequest()
    var postShareUserData = MutableLiveData<PostBaseResponse>()
    var postShareUserLoading = MutableLiveData(false)
    var userDetails = MutableLiveData<UserDetailsResponse>()
    var userLogoutData = MutableLiveData<BaseResponse>()
    val userProfilePost: MutableLiveData<com.wedo8020.app.utils.Event<ProfilePostResponse>> by lazy{ MutableLiveData<com.wedo8020.app.utils.Event<ProfilePostResponse>>() }
    val userSavedPost: MutableLiveData<com.wedo8020.app.utils.Event<SavedProfilePost>> by lazy{ MutableLiveData<com.wedo8020.app.utils.Event<SavedProfilePost>>() }
    //var userSavedPost = MutableLiveData<SavedProfilePost>()
    var postLikeRequest = PostRequest()
    var notInterestedRequest = PostRequest()
    var postReportRequest = PostRequest()
    var postRepostRequest = PostRequest()
    var userRestrictRequest = PostRequest()
    var postLikeData = MutableLiveData<BaseResponse>()
    var notInterestedData = MutableLiveData<PostBaseResponse>()
    val userRestrictData: MutableLiveData<com.wedo8020.app.utils.Event<PostBaseResponse>> by lazy{ MutableLiveData<com.wedo8020.app.utils.Event<PostBaseResponse>>() }
    val restrictResponseData: MutableLiveData<com.wedo8020.app.utils.Event<RestrictResponse>> by lazy{ MutableLiveData<com.wedo8020.app.utils.Event<RestrictResponse>>() }

    var postReportData = MutableLiveData<PostBaseResponse>()
    var postRepostData = MutableLiveData<PostBaseResponse>()
    var addCommentData = MutableLiveData<PostBaseResponse>()
    var postSaveRequest = PostRequest()
    var postSaveData = MutableLiveData<BaseResponse>()
    var postSaveSoundRequest = PostRequest()
    var postSaveSoundData = MutableLiveData<BaseResponse>()
    var followUnfollowUserRequest = UserRequest()
    var followUnfollowUserData = MutableLiveData<BaseResponse>()
    var switchNotificationData = MutableLiveData<BaseResponse>()
    var commentListData = MutableLiveData<CommentBaseResponse>()
    var commentLikeData = MutableLiveData<CommentBaseResponse>()
    var postPinRequest = PostPinRequest()
    var postPinLikeData = MutableLiveData<BaseResponse>()
    var deletePostRequest = DeletePost()
    var soundPinRequest = SoundPinRequest()
    var editPosts = EditPost()
    var userList = UserListRequest()
    var userListResponseData = MutableLiveData<UserListResponse>()
    var postDetailData = MutableLiveData<PostDetailsResponse>()
    var baseResponse = MutableLiveData<BaseResponse>()
    var downloadMyData = MutableLiveData<DownloadMyDataResponse>()
    var baseResponseForNotification = MutableLiveData<BaseResponse>()
    var baseResponseForAccount = MutableLiveData<BaseResponse>()
    var mentionUserRequests = mentionUserRequest()
    var experienceResponseData = MutableLiveData<UserExperienceResponse>()
    var interestResponseData = MutableLiveData<InterestResponse>()

    var mentionUserData = MutableLiveData<MentionUserResponse>()
    var userProfilePrivacyData = MutableLiveData<ProfilePrivacyResponse>()
    var userProfileAccountData = MutableLiveData<ProfileAccountResponse>()
    var userProfileNotification = MutableLiveData<ProfileNotificationResponse>()
    var userBlockedListData = MutableLiveData<BlockedResponse>()

    var likeListListData = MutableLiveData<LikeListResponse>()
    var friendListData = MutableLiveData<FriendListResponse>()
    var hashTagListResponse = MutableLiveData<HashTagListResponse>()
    var notificationListResponse = MutableLiveData<NotificationListResponse>()
    var deleteNotificationResponse = MutableLiveData<BaseResponse>()
    var deleteCommentData = MutableLiveData<BaseResponse>()
    var pinCommentData = MutableLiveData<BaseResponse>()
   // val linkedAccountDetailsData: MutableLiveData<com.wedo8020.app.utils.Event<LinkedAccountResponse>> by lazy{ MutableLiveData<com.wedo8020.app.utils.Event<LinkedAccountResponse>>() }
    var linkedAccountDetailsData = MutableLiveData<LinkedAccountResponse>()
    fun post() {
        showLoading.postValue(true)
        viewModelScope.launch {
            Log.e(TAG, postRequest.toString())
            val type = postRequest.type
            authenticationRepository.post(postRequest)
            { isSuccess: Boolean, response: PostBaseResponse ->

                if (isSuccess) {
                    Log.e("postAPI response", response.toString())
                    if (type == "drop") {

                        response.postType = "drop"
                        postData.postValue(response)
                    } else {
                        response.postType = "following"
                        postData.postValue(response)
                    }
                    Log.e("postAPI response", response.data!!.size.toString())
                } else {

                    showMessage.postValue(response.message)
                    if (response.message == "Authorization token required.") {

                        Prefs.init().isLogIn = false
                        Prefs.init().clear()

                        android.os.Handler(Looper.getMainLooper()).post {
                            Utils.showToast(MainApplication.get().applicationContext.getString(R.string.auth_error))

                            MainApplication.get().applicationContext.startActivity(
                                Intent(
                                    MainApplication.get().applicationContext,
                                    LoginActivity::class.java
                                ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            )
                        }
                    }else {
                        showLoading.postValue(false)
                    }
                }
            }
        }
    }

    fun postShareUser() {
        postShareUserLoading.postValue(true)
        viewModelScope.launch {
            Log.e("postShareUser", postShareUserRequest.toString())
            authenticationRepository
                .postShareUser(postShareUserRequest) { isSuccess: Boolean, baseResponse: PostBaseResponse ->
                    postShareUserLoading.postValue(false)
                    if (isSuccess) {
                        postShareUserData.postValue(baseResponse)
                    } else {
                        showMessage.postValue(baseResponse.message)
                    }
                }
        }
    }

    fun notInterested() {
        viewModelScope.launch {
            Log.e("postShareUser", notInterestedRequest.toString())
            authenticationRepository
                .notInterested(notInterestedRequest) { isSuccess: Boolean, baseResponse: PostBaseResponse ->
                    if (isSuccess) {
                        notInterestedData.postValue(baseResponse)
                    } else {
                        showMessage.postValue(baseResponse.message)
                    }
                }
        }
    }

    fun userRestrict(userRestrict: UserRestrict) {
        viewModelScope.launch {
            authenticationRepository
                .userRestrict(userRestrict) { isSuccess: Boolean, baseResponse: PostBaseResponse ->
                    if (isSuccess) {
                        userRestrictData.postValue(com.wedo8020.app.utils.Event(baseResponse))
                    } else {
                        showMessage.postValue(baseResponse.message)
                    }
                }
        }
    }

    fun postRestrict(postRestrict: PostRestrict) {
        viewModelScope.launch {
            authenticationRepository
                .postRestrict(postRestrict) { isSuccess: Boolean, baseResponse: PostBaseResponse ->
                    if (isSuccess) {
                        userRestrictData.postValue(com.wedo8020.app.utils.Event(baseResponse))
                    } else {
                        showMessage.postValue(baseResponse.message)
                    }
                }
        }
    }

    fun postReport() {
        viewModelScope.launch {
            Log.e("postShareUser", postReportRequest.toString())
            authenticationRepository
                .postReport(postReportRequest) { isSuccess: Boolean, baseResponse: PostBaseResponse ->
                    if (isSuccess) {
                        postReportData.postValue(baseResponse)
                    } else {
                        showMessage.postValue(baseResponse.message)
                    }
                }
        }
    }

    fun commentReport(request: PostRequest) {
        viewModelScope.launch {
            Log.e("postShareUser", postReportRequest.toString())
            authenticationRepository
                .commentReport(request) { isSuccess: Boolean, baseResponse: PostBaseResponse ->
                    if (isSuccess) {
                        postReportData.postValue(baseResponse)
                    } else {
                        showMessage.postValue(baseResponse.message)
                    }
                }
        }
    }

    fun userReport() {
        viewModelScope.launch(Dispatchers.IO) {

            authenticationRepository
                .userReport(postReportRequest) { isSuccess: Boolean, baseResponse: PostBaseResponse ->
                    if (isSuccess) {
                        postReportData.postValue(baseResponse)
                    } else {
                        showMessage.postValue(baseResponse.message)
                    }
                }
        }
    }

    fun postRepost() {
        viewModelScope.launch {
            Log.e("postShareUser", postRepostRequest.toString())
            authenticationRepository
                .postRepost(postRepostRequest) { isSuccess: Boolean, baseResponse: PostBaseResponse ->
                    if (isSuccess) {
                        postRepostData.postValue(baseResponse)
                    } else {
                        showMessage.postValue(baseResponse.message)
                    }
                }
        }
    }

    fun addComment(commentRequest: CommentRequest) {
        showLoading.postValue(true)
        viewModelScope.launch {
            Log.e("postShareUser", commentRequest.toString())
            authenticationRepository
                .addComment(commentRequest) { isSuccess: Boolean, baseResponse: PostBaseResponse ->
                    if (isSuccess) {
                        addCommentData.postValue(baseResponse)
                    } else {
                        showMessage.postValue(baseResponse.message)
                    }
                }
        }
    }

    fun searchList(searchRequest: SearchRequest): LiveData<SearchBaseResponse> {
        val eventData = MutableLiveData<SearchBaseResponse>()
        showLoading.postValue(true)
        viewModelScope.launch {
            Log.e("postShareUser", commentRequest.toString())
            authenticationRepository
                .searchList(searchRequest) { isSuccess: Boolean, baseResponse: SearchBaseResponse ->
                    if (isSuccess) {
                        eventData.postValue(baseResponse)
                    } else {
                        showMessage.postValue(baseResponse.message)
                    }
                }
        }
        return eventData
    }

    fun likePost() {
        showLoading.postValue(true)
        viewModelScope.launch {
            authenticationRepository.postLike(postLikeRequest)
            { isSuccess: Boolean, response: BaseResponse ->
                showLoading.postValue(false)
                if (isSuccess) {
                    postLikeData.postValue(response)

                } else {
                    showMessage.postValue(response.message)
                }
            }
        }
    }

    fun postSave() {
        showLoading.postValue(true)
        viewModelScope.launch {
            authenticationRepository.postSave(postSaveRequest)
            { isSuccess: Boolean, response: BaseResponse ->
                showLoading.postValue(false)
                if (isSuccess) {
                    postSaveData.postValue(response)

                } else {
                    showMessage.postValue(response.message)
                }
            }
        }
    }

    fun userPostPin() {
        showLoading.postValue(true)
        viewModelScope.launch {
            authenticationRepository.postPin(postPinRequest)
            { isSuccess: Boolean, response: BaseResponse ->
                showLoading.postValue(false)
                if (isSuccess) {
                    postPinLikeData.postValue(response)

                } else {
                    showMessage.postValue(response.message)
                }
            }
        }
    }

    fun userDeletedPost() {
        showLoading.postValue(true)
        viewModelScope.launch {
            authenticationRepository.DeletePost(deletePostRequest)
            { isSuccess: Boolean, response: BaseResponse ->
                showLoading.postValue(false)
                if (isSuccess) {
                    postPinLikeData.postValue(response)

                } else {
                    showMessage.postValue(response.message)
                }
            }
        }
    }

    fun userPostSoundPin() {
        showLoading.postValue(true)
        viewModelScope.launch {
            authenticationRepository.postSoundPin(soundPinRequest)
            { isSuccess: Boolean, response: BaseResponse ->
                showLoading.postValue(false)
                if (isSuccess) {
                    postPinLikeData.postValue(response)

                } else {
                    showMessage.postValue(response.message)
                }
            }
        }
    }

    fun userArchivePost() {
        showLoading.postValue(true)
        viewModelScope.launch {
            Log.e("userUnArchivePost", deletePostRequest.toString())
            authenticationRepository.ArchivePost(deletePostRequest)
            { isSuccess: Boolean, response: BaseResponse ->
                showLoading.postValue(false)
                if (isSuccess) {
                    postPinLikeData.postValue(response)

                } else {
                    showMessage.postValue(response.message)
                }
            }
        }
    }

    fun userUnArchivePost() {
        showLoading.postValue(true)
        viewModelScope.launch {
            Log.e("userUnArchivePost7", deletePostRequest.toString())
            authenticationRepository.unArchivePost(deletePostRequest)
            { isSuccess: Boolean, response: BaseResponse ->
                showLoading.postValue(false)
                if (isSuccess) {
                    postPinLikeData.postValue(response)

                } else {
                    showMessage.postValue(response.message)
                }
            }
        }
    }

    fun userList() {
        showLoading.postValue(true)
        viewModelScope.launch {
            Log.e("postUserList", userList.toString())
            authenticationRepository.userList(userList)
            { isSuccess: Boolean, response: UserListResponse ->
                showLoading.postValue(false)
                if (isSuccess) {
                    userListResponseData.postValue(response)

                } else {
                    showMessage.postValue(response.message)
                }
            }
        }
    }

    fun postSaveSound() {
        showLoading.postValue(true)
        viewModelScope.launch {
            authenticationRepository.postSaveSound(postSaveSoundRequest)
            { isSuccess: Boolean, response: BaseResponse ->
                showLoading.postValue(false)
                if (isSuccess) {
                    postSaveSoundData.postValue(response)

                } else {
                    showMessage.postValue(response.message)
                }
            }
        }
    }


    fun postViewed() {
        showLoading.postValue(true)
        viewModelScope.launch {
            Log.e("postSaveSound", postSaveSoundRequest.post)
            authenticationRepository.postViewed(postSaveSoundRequest)
            { isSuccess: Boolean, response: BaseResponse ->
                showLoading.postValue(false)
                if (isSuccess) {
                    postSaveSoundData.postValue(response)

                } else {
                    showMessage.postValue(response.message)
                }
            }
        }
    }

    fun followUnfollowUser() {
        showLoading.postValue(true)
        viewModelScope.launch {
            Log.e("postSaveSound", postSaveSoundRequest.sound)
            authenticationRepository.followUnfollowUser(followUnfollowUserRequest)
            { isSuccess: Boolean, response: BaseResponse ->
                showLoading.postValue(false)
                if (isSuccess) {
                    followUnfollowUserData.postValue(response)

                } else {
                    showMessage.postValue(response.message)
                }
            }
        }
    }

    fun postFollowRequestStatus() {
        showLoading.postValue(true)
        viewModelScope.launch {
            Log.e("postSaveSound", postSaveSoundRequest.sound)
            authenticationRepository.postFollowRequestStatus(followUnfollowUserRequest)
            { isSuccess: Boolean, response: BaseResponse ->
                showLoading.postValue(false)
                if (isSuccess) {
                    followUnfollowUserData.postValue(response)

                } else {
                    showMessage.postValue(response.message)
                }
            }
        }
    }

    fun switchNotification(switchNotificationRequest: SwitchNotificationRequest) {
        showLoading.postValue(true)
        viewModelScope.launch {
            Log.e("postSaveSound", postSaveSoundRequest.sound)
            authenticationRepository.switchNotification(switchNotificationRequest)
            { isSuccess: Boolean, response: BaseResponse ->
                showLoading.postValue(false)
                if (isSuccess) {
                    switchNotificationData.postValue(response)

                } else {
                    showMessage.postValue(response.message)
                }
            }
        }
    }

    fun commentList() {
        showLoading.postValue(true)
        viewModelScope.launch {
            Log.e("postSaveSound", postRequest.sound.toString())
            authenticationRepository.commentList(commentRequest)
            { isSuccess: Boolean, response: CommentBaseResponse ->
                showLoading.postValue(false)
                if (isSuccess) {
                    commentListData.postValue(response)

                } else {
                    showMessage.postValue(response.message)
                }
            }
        }
    }

    fun commentLike() {
        showLoading.postValue(true)
        viewModelScope.launch {
            Log.e("postSaveSound", postRequest.sound.toString())
            authenticationRepository.commentLike(commentLikeRequest)
            { isSuccess: Boolean, response: CommentBaseResponse ->
                showLoading.postValue(false)
                if (isSuccess) {
                    commentLikeData.postValue(response)

                } else {
                    showMessage.postValue(response.message)
                }
            }
        }
    }


    fun removeLikesFromUser(removeLikes: RemoveLikes) {
        showLoading.postValue(true)
        viewModelScope.launch {
            authenticationRepository.removeLikes(removeLikes)
            { isSuccess: Boolean, response: BaseResponse ->
                showLoading.postValue(false)
                if (isSuccess) {
                    baseResponse.postValue(response)

                } else {
                    showMessage.postValue(response.message)
                }
            }
        }
    }

    fun profilePosts(data: Map<String, String>) {
        val createPostDb = CreatePostDbCreation(MainApplication.get().applicationContext)
        showLoading.postValue(true)
        viewModelScope.launch {
            Log.e(TAG, data.toString())
//            val type = profilePostRequest.type
            authenticationRepository.profilePost(data)
            { isSuccess: Boolean, response: ProfilePostResponse ->
                Log.e("postAPI response awe", response.toString())
                if (isSuccess) {

                    if (!data.containsKey("user")) {
                        val localPostList = createPostDb.getCreatePostData().fetchAllPost()
                        if (!localPostList.isNullOrEmpty() &&
                            !response.data.isNullOrEmpty()
                        ) {
                            localPostList.forEach { localData ->
                                if (response.data!!.any { it?._id == localData.id }) {
                                    response.data!!.first { it?._id == localData.id }?.seekTime =
                                        localData.seekTime.toString()
                                    response.data!!.first { it?._id == localData.id }?.video =
                                        localData.video ?: ""
                                    response.data!!.first { it?._id == localData.id }?.images =
                                        ArrayList(localData.images ?: ArrayList<String>())
                                }
                            }
                        }
                    }
                    userProfilePost.postValue(com.wedo8020.app.utils.Event(response))
                } else {
                    showMessage.postValue(response.message)
                }
            }
        }
    }

    fun savePost(profilePostRequest: ProfilePostRequest) {
        showLoading.postValue(true)
        viewModelScope.launch {
            authenticationRepository.savedPost(profilePostRequest)
            { isSuccess: Boolean, response: SavedProfilePost ->
                if (isSuccess) {
                    userSavedPost.postValue(com.wedo8020.app.utils.Event(response))
                } else {
                    showMessage.postValue(response.message)
                }
            }
        }
    }

    fun userDertails(data: Map<String, String> = mapOf("usser" to "")) {
        showLoading.postValue(true)
        viewModelScope.launch {
            authenticationRepository.userDeatils(data)
            { isSuccess: Boolean, response: UserDetailsResponse ->
                showLoading.postValue(false)
                if (isSuccess) {
                    userDetails.postValue(response)

                    if (response.success) {
                        if (response.data != null) {
                            if (!data.containsKey("user")) {
                                Prefs.init().apply {
                                    userId = response.data?._id ?: ""
                                    profileId = response.data?._id ?: ""
                                    userProfile = response.data?.profile ?: ""
                                    username = response.data?.username ?: ""
                                    name = response.data?.name ?: ""
                                }
                            }
                        }
                    }

                    /*if (response.success){
                        if (response.data != null){
                            Prefs.init().userId = response.data!!._id
                        }
                    }*/

                } else {
                    showMessage.postValue(response.message)
                }
            }
        }
    }

    fun userPostDetail() {
        showLoading.postValue(true)
        viewModelScope.launch {
            authenticationRepository.postDetails(deletePostRequest)
            { isSuccess: Boolean, response: PostDetailsResponse ->
                showLoading.postValue(false)
                if (isSuccess) {
                    postDetailData.postValue(response)

                } else {
                    showMessage.postValue(response.message)
                }
            }
        }
    }

    fun userEditPost() {
        showLoading.postValue(true)
        viewModelScope.launch {
            authenticationRepository.EditPost(editPosts)
            { isSuccess: Boolean, response: BaseResponse ->
                showLoading.postValue(false)
                if (isSuccess) {
                    baseResponse.postValue(response)

                } else {
                    showMessage.postValue(response.message)
                }
            }
        }
    }

    fun postFcmToken(payload: FcmRequest) {
        showLoading.postValue(true)
        viewModelScope.launch {
            authenticationRepository.postFcmToken(payload)
            { isSuccess: Boolean, response: BaseResponse ->
                showLoading.postValue(false)
                if (isSuccess) {
                    showMessage.postValue(response.message)

                } else {
                    showMessage.postValue(response.message)
                }
            }
        }
    }

    fun mentionUser() {
        showLoading.postValue(true)
        Log.e("MentionUser", mentionUserRequests.toString())
        viewModelScope.launch {
            authenticationRepository.MentionUser(mentionUserRequests)
            { isSuccess: Boolean, response: MentionUserResponse ->
                showLoading.postValue(false)
                if (isSuccess) {
                    mentionUserData.postValue(response)

                } else {
                    showMessage.postValue(response.message)
                }
            }
        }
    }


//    fun editProfilePrivacy(editPrivacy: EditPrivacy) {
//        showLoading.postValue(true)
//        viewModelScope.launch(Dispatchers.IO) {
//            authenticationRepository.userEditProfilePrivacy(editPrivacy)
//            { isSuccess: Boolean, response: BaseResponse ->
//                showLoading.postValue(false)
//                if (isSuccess) {
//                    baseResponse.postValue(response)
//                } else {
//                    showMessage.postValue(response.message)
//                }
//            }
//        }
//    }

    fun getUserExperience() {
        showLoading.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
            authenticationRepository.getUserExperience()
            { isSuccess: Boolean, response: UserExperienceResponse ->
                showLoading.postValue(false)
                if (isSuccess) {
                    experienceResponseData.postValue(response)
                } else {
                    showMessage.postValue(response.message)
                }
            }
        }
    }
    fun getUserInterest(interestRequest: InterestRequest) {
        showLoading.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
            authenticationRepository.getUserInterest(interestRequest)
            { isSuccess: Boolean, response: InterestResponse ->
                showLoading.postValue(false)
                if (isSuccess) {
                    interestResponseData.postValue(response)
                    Log.e("isSuccessFull" , response.toString())
                } else {
                    showMessage.postValue(response.message)
                }
            }
        }
    }
    fun getLinkedAccountDetails(linkedAccountRequest: LinkedAccountRequest) {
        showLoading.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
            authenticationRepository.linkedAccount(linkedAccountRequest)
            { isSuccess: Boolean, response: LinkedAccountResponse ->
                showLoading.postValue(false)
                if (isSuccess) {
                    linkedAccountDetailsData.postValue(response)
                    Log.e("isSuccessFull" , response.toString())
                } else {
                    showMessage.postValue(response.message)
                }
            }
        }
    }
    fun saveUserExperience(interestRequest: ExperienceRequest) {
        showLoading.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
            authenticationRepository.saveUserInterest(interestRequest)
            { isSuccess: Boolean, response: BaseResponse ->
                showLoading.postValue(false)
                if (isSuccess) {
                    baseResponse.postValue(response)
                } else {
                    showMessage.postValue(response.message)
                }
            }
        }
    }

    fun editProfilePrivacy(editPrivacy: EditPrivacy) {
        showLoading.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
            authenticationRepository.userEditProfilePrivacy(editPrivacy)
            { isSuccess: Boolean, response: BaseResponse ->
                showLoading.postValue(false)
                if (isSuccess) {
                    baseResponse.postValue(response)
                } else {
                    showMessage.postValue(response.message)
                }
            }
        }
    }

    fun finalEditProfilePrivacy(editPrivacy: EditProfilePrivacy) {
        showLoading.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
            authenticationRepository.editPrivacyFromProfile(editPrivacy)
            { isSuccess: Boolean, response: BaseResponse ->
                showLoading.postValue(false)
                if (isSuccess) {
                    baseResponse.postValue(response)
                } else {
                    showMessage.postValue(response.message)
                }
            }
        }
    }

    fun editProfileAccount(data: Map<String, String>) {
        showLoading.postValue(true)
        viewModelScope.launch {
            authenticationRepository.userEditProfileAccount(data)
            { isSuccess: Boolean, response: BaseResponse ->
                showLoading.postValue(false)
                if (isSuccess) {
                    baseResponseForAccount.postValue(response)

                } else {
                    showMessage.postValue(response.message)
                }
            }
        }
    }

    fun userBlockedList(blockedRequest: BlockedRequest) {
        showLoading.postValue(true)
        viewModelScope.launch {
            authenticationRepository.userBlockedLists(blockedRequest)
            { isSuccess: Boolean, response: BlockedResponse ->
                showLoading.postValue(false)
                if (isSuccess) {
                    userBlockedListData.postValue(response)

                } else {
                    showMessage.postValue(response.message)
                }
            }
        }
    }

    fun editProfileNotification(userNotificationRequest: UserNotificationRequest) {
        showLoading.postValue(true)
        viewModelScope.launch {
            authenticationRepository.userEditProfileNotification(userNotificationRequest)
            { isSuccess: Boolean, response: BaseResponse ->
                showLoading.postValue(false)
                if (isSuccess) {
                    baseResponseForNotification.postValue(response)

                } else {
                    showMessage.postValue(response.message)
                }
            }
        }
    }

    fun userProfilePrivacy() {
        showLoading.postValue(true)
        viewModelScope.launch {
            authenticationRepository.userProfilePrivacy()
            { isSuccess: Boolean, response: ProfilePrivacyResponse ->
                showLoading.postValue(false)
                if (isSuccess) {
                    userProfilePrivacyData.postValue(response)

                } else {
                    showMessage.postValue(response.message)
                }
            }
        }
    }

    fun userProfileAccount() {
        showLoading.postValue(true)
        viewModelScope.launch {
            authenticationRepository.userProfileAccount()
            { isSuccess: Boolean, response: ProfileAccountResponse ->
                showLoading.postValue(false)
                if (isSuccess) {
                    userProfileAccountData.postValue(response)

                } else {
                    showMessage.postValue(response.message)
                }
            }
        }
    }

    fun userProfileNotification() {
        showLoading.postValue(true)
        viewModelScope.launch {
            authenticationRepository.userProfileNotification()
            { isSuccess: Boolean, response: ProfileNotificationResponse ->
                showLoading.postValue(false)
                if (isSuccess) {
                    userProfileNotification.postValue(response)

                } else {
                    showMessage.postValue(response.message)
                }
            }
        }
    }


    fun userLogout() {
        showLoading.postValue(true)
        viewModelScope.launch {
            authenticationRepository.userLogout()
            { isSuccess: Boolean, response: BaseResponse ->
                showLoading.postValue(false)
                if (isSuccess) {
                    userLogoutData.postValue(response)

                } else {
                    showMessage.postValue(response.message)
                    if (response.message == "Authorization token required.") {

                        Prefs.init().isLogIn = false
                        Prefs.init().clear()

                        android.os.Handler(Looper.getMainLooper()).post {
                            Utils.showToast(MainApplication.get().applicationContext.getString(R.string.auth_error))

                            MainApplication.get().applicationContext.startActivity(
                                Intent(
                                    MainApplication.get().applicationContext,
                                    LoginActivity::class.java
                                ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            )
                        }
                    }else {
                        showLoading.postValue(false)
                    }
                }
            }
        }
    }

    fun userDeleteAccount() {
        showLoading.postValue(true)
        viewModelScope.launch {
            authenticationRepository.userDeleteAccount()
            { isSuccess: Boolean, response: BaseResponse ->
                showLoading.postValue(false)
                if (isSuccess) {
                    userLogoutData.postValue(response)

                } else {
                    showMessage.postValue(response.message)
                }
            }
        }
    }

    fun restrictedReportUser(restrictReportRequest: RestrictReportRequest) {
        showLoading.postValue(true)
        viewModelScope.launch {
            authenticationRepository.userRestrictReportList(restrictReportRequest)
            { isSuccess: Boolean, response: RestrictResponse ->
                showLoading.postValue(false)
                if (isSuccess) {
                    restrictResponseData.postValue(com.wedo8020.app.utils.Event(response))

                } else {
                    showMessage.postValue(response.message)
                }
            }
        }
    }

    fun updateInterest(updateInterestRequest: UpdateInterestRequest) {
        showLoading.postValue(true)
        viewModelScope.launch {
            authenticationRepository.updateInterest(updateInterestRequest)
            { isSuccess: Boolean, response: BaseResponse ->
                showLoading.postValue(false)
                if (isSuccess) {
                    baseResponse.postValue(response)
                } else {
                    showMessage.postValue(response.message)
                }
            }
        }
    }

    fun updateFeedback(feedBackRequest: FeedBackRequest) {
        showLoading.postValue(true)
        viewModelScope.launch {
            authenticationRepository.userFeedBack(feedBackRequest)
            { isSuccess: Boolean, response: BaseResponse ->
                showLoading.postValue(false)
                if (isSuccess) {
                    baseResponse.postValue(response)
                } else {
                    showMessage.postValue(response.message)
                }
            }
        }
    }

    fun downloadMyData() {
        showLoading.postValue(true)
        viewModelScope.launch {
            authenticationRepository.downloadMyData()
            { isSuccess: Boolean, response: DownloadMyDataResponse ->
                showLoading.postValue(false)
                if (isSuccess) {
                    downloadMyData.postValue(response)
                } else {
                    showMessage.postValue(response.message)
                }
            }
        }
    }

    fun userBlocked(userBlockedRequest: UserBlockedRequest) {
        showLoading.postValue(true)
        viewModelScope.launch {
            authenticationRepository.userBlock(userBlockedRequest)
            { isSuccess: Boolean, response: BaseResponse ->
                showLoading.postValue(false)
                if (isSuccess) {
                    baseResponse.postValue(response)
                } else {
                    showMessage.postValue(response.message)
                }
            }
        }
    }


    fun userLikeList(likeListRequest: LikeListRequest) {
        showLoading.postValue(true)
        viewModelScope.launch {
            authenticationRepository.userLikeList(likeListRequest)
            { isSuccess: Boolean, response: LikeListResponse ->
                showLoading.postValue(false)
                if (isSuccess) {
                    likeListListData.postValue(response)
                } else {
                    showMessage.postValue(response.message)
                }
            }
        }
    }

    fun friendList(friendListRequest: FriendListRequest) {
        showLoading.postValue(true)
        viewModelScope.launch {
            authenticationRepository.friendList(friendListRequest)
            { isSuccess: Boolean, response: FriendListResponse ->
                showLoading.postValue(false)
                if (isSuccess) {
                    friendListData.postValue(response)
                } else {
                    showMessage.postValue(response.message)
                }
            }
        }
    }

    fun followingList(followingListRequest: FollowingListRequest) {
        showLoading.postValue(true)
        viewModelScope.launch {
            authenticationRepository.followingList(followingListRequest)
            { isSuccess: Boolean, response: FriendListResponse ->
                showLoading.postValue(false)
                if (isSuccess) {
                    friendListData.postValue(response)
                } else {
                    showMessage.postValue(response.message)
                }
            }
        }
    }

    fun removeFollower(userRequest: UserRequest) {
        showLoading.postValue(true)
        viewModelScope.launch {
            authenticationRepository.removeFollower(userRequest)
            { isSuccess: Boolean, response: BaseResponse ->
                showLoading.postValue(false)
                if (isSuccess) {
                    baseResponse.postValue(response)
                } else {
                    showMessage.postValue(response.message)
                }
            }
        }
    }

    fun followerList(followerListRequest: FollowerListRequest) {
        showLoading.postValue(true)
        viewModelScope.launch {
            authenticationRepository.followerList(followerListRequest)
            { isSuccess: Boolean, response: FriendListResponse ->
                showLoading.postValue(false)
                if (isSuccess) {
                    friendListData.postValue(response)
                } else {
                    showMessage.postValue(response.message)
                }
            }
        }
    }

    fun hashTagList(payload: HashtagPayload) {
        viewModelScope.launch(Dispatchers.IO) {
            authenticationRepository.hashTagList(payload) { isSuccess: Boolean, response: HashTagListResponse ->
                if (isSuccess) {
                    Log.d("successApi", " hashtagListResponse : ${response.data?.size}")
                    hashTagListResponse.postValue(response)
                } else {
                    showMessage.postValue(response.message)

                }
            }
        }
    }

    fun notificationList(payload: NotificationRequest) {
        viewModelScope.launch(Dispatchers.IO) {
            authenticationRepository.notificationList(payload) { isSuccess: Boolean, response: NotificationListResponse ->
                if (isSuccess) {
                    notificationListResponse.postValue(response)

                } else {
                    showMessage.postValue(response.message)

                }
            }
        }
    }

    fun getHashtagPost(payload: HashtagPayload) {

        viewModelScope.launch(Dispatchers.IO) {
            authenticationRepository.getHashtagPost(payload) { isSuccess: Boolean, response: PostBaseResponse ->
                if (isSuccess) {
                    postData.postValue(response)

                } else {
                    showMessage.postValue(response.message)

                }
            }
        }
    }

    fun deleteNotification(payload: DeleteNotification) {
        viewModelScope.launch(Dispatchers.IO) {
            authenticationRepository.deleteNotification(payload) { isSuccess: Boolean, response: BaseResponse ->
                if (isSuccess) {
                    deleteNotificationResponse.postValue(response)

                } else {
                    showMessage.postValue(response.message)

                }
            }
        }
    }

    fun deleteComment(payload: DeleteCommentRequest) {
        viewModelScope.launch(Dispatchers.IO) {
            authenticationRepository.deleteComment(payload) { isSuccess: Boolean, response: BaseResponse ->
                if (isSuccess) {
                    deleteCommentData.postValue(response)

                } else {
                    showMessage.postValue(response.message)

                }
            }
        }
    }

    fun pinComment(payload: PinCommentRequest) {
        viewModelScope.launch(Dispatchers.IO) {
            authenticationRepository.pinComment(payload) { isSuccess: Boolean, response: BaseResponse ->
                if (isSuccess) {

                    pinCommentData.postValue(response)

                } else {
                    showMessage.postValue(response.message)

                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        authenticationRepository.cancelCoroutine()
    }


      fun preloadVideo(dataList: ArrayList<String?>){
          viewModelScope.launch(Dispatchers.IO){
              dataList.forEach {
                  it?.let {
                      PlayerViewAdapter.setNextMediaSource(it)
                  }
              }
          }
    }
}