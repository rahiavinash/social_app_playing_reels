package com.wedo8020.app.repo

import android.util.Log
import com.wedo8020.app.dataSources.RemoteDataSourceImp
import com.wedo8020.app.request.BlockedRequest
import com.wedo8020.app.request.ChatHistoryRequest
import com.wedo8020.app.request.ChatList
import com.wedo8020.app.request.CommentRequest
import com.wedo8020.app.request.CreatePost
import com.wedo8020.app.request.CreatePostWithMedia
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
import com.wedo8020.app.request.LikeListRequest
import com.wedo8020.app.request.HashTagListResponse
import com.wedo8020.app.request.HashtagPayload
import com.wedo8020.app.request.InterestRequest
import com.wedo8020.app.request.LinkedAccountRequest
import com.wedo8020.app.request.LoginRequest
import com.wedo8020.app.request.NotificationRequest
import com.wedo8020.app.request.PinCommentRequest
import com.wedo8020.app.request.PostPinRequest
import com.wedo8020.app.request.PostRequest
import com.wedo8020.app.request.PostRestrict
import com.wedo8020.app.request.PremiumPurchasePayLoad
import com.wedo8020.app.request.ProfilePostRequest
import com.wedo8020.app.request.PurchaseDetails
import com.wedo8020.app.request.PurchaseSrcPayload
import com.wedo8020.app.request.PurchaseTreesPayload
import com.wedo8020.app.request.RegionRequest
import com.wedo8020.app.request.RemoveLikes
import com.wedo8020.app.request.RestrictReportRequest
import com.wedo8020.app.request.SearchRequest
import com.wedo8020.app.request.SoundPinRequest
import com.wedo8020.app.request.SwitchNotificationRequest
import com.wedo8020.app.request.UpdateInterestRequest
import com.wedo8020.app.request.UserBlockedRequest
import com.wedo8020.app.request.UserDecryptFile
import com.wedo8020.app.request.UserListRequest
import com.wedo8020.app.request.UserNotificationRequest
import com.wedo8020.app.request.UserRequest
import com.wedo8020.app.request.UserRestrict
import com.wedo8020.app.request.mentionUserRequest
import com.wedo8020.app.response.AcceptRejectRequest
import com.wedo8020.app.response.AcceptRejectResponse
import com.wedo8020.app.response.AccountAvailabilityResponse
import com.wedo8020.app.response.BaseResponse
import com.wedo8020.app.response.BlockedResponse
import com.wedo8020.app.response.ChatImageUpload
import com.wedo8020.app.response.CommentBaseResponse
import com.wedo8020.app.response.CountryListImpactScreen
import com.wedo8020.app.response.DownloadMyDataResponse
import com.wedo8020.app.response.DropsDataRequest
import com.wedo8020.app.response.DropsDataResponse
import com.wedo8020.app.response.ImpactDetail
import com.wedo8020.app.response.FriendListResponse
import com.wedo8020.app.response.InterestResponse
import com.wedo8020.app.response.LeaderboardRequest
import com.wedo8020.app.response.LeaderboardResponse
import com.wedo8020.app.response.LikeListResponse
import com.wedo8020.app.response.LinkedAccountResponse
import com.wedo8020.app.response.MentionUserResponse
import com.wedo8020.app.response.MessageListResponse
import com.wedo8020.app.response.MessageRequest
import com.wedo8020.app.response.MessageResponse
import com.wedo8020.app.response.MostTreesPlantedByRegionResponse
import com.wedo8020.app.response.MostTreesPlantedRequest
import com.wedo8020.app.response.MostTreesPlantedResponse
import com.wedo8020.app.response.NotificationListResponse
import com.wedo8020.app.response.PostBaseResponse
import com.wedo8020.app.response.PostDetailsResponse
import com.wedo8020.app.response.PrizeDetailsResponse
import com.wedo8020.app.response.ProfileAccountResponse
import com.wedo8020.app.response.ProfileNotificationResponse
import com.wedo8020.app.response.ProfilePostResponse
import com.wedo8020.app.response.ProfilePrivacyResponse
import com.wedo8020.app.response.PurchaseHistoryList
import com.wedo8020.app.response.RecoveryCodeRequest
import com.wedo8020.app.response.RecoveryCodeResponse
import com.wedo8020.app.response.RegisterUserRequest
import com.wedo8020.app.response.RegisterUserResponse
import com.wedo8020.app.response.RestrictResponse
import com.wedo8020.app.response.SavedProfilePost
import com.wedo8020.app.response.SearchBaseResponse
import com.wedo8020.app.response.ShowChatResponse
import com.wedo8020.app.response.TransferSrcDetails
import com.wedo8020.app.response.UserDataList
import com.wedo8020.app.response.UserDetailsResponse
import com.wedo8020.app.response.UserExperienceResponse
import com.wedo8020.app.response.UserListResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class RemoteRepositaryImp(

) : RemoteRepositary {

    private val authenticationDataSource by lazy {
        RemoteDataSourceImp()
    }
    private val myCoroutineScope = CoroutineScope(Dispatchers.IO)

    @OptIn(DelicateCoroutinesApi::class)
    override fun login(
        loginRequest: LoginRequest,
        onResult: (isSuccess: Boolean, baseResponse: BaseResponse) -> Unit
    ) {
        myCoroutineScope.launch {
            val response = authenticationDataSource.loginResponse(loginRequest)

            Log.e("response login",response.toString())
            if (!response.success) {
                Log.e("response login","not success")
                onResult(false, response)
            } else {
                Log.e("response login","success")
                onResult(true, response)
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun post(
        postRequest: PostRequest,
        onResult: (isSuccess: Boolean, baseResponse: PostBaseResponse) -> Unit
    ) {
        myCoroutineScope.launch {
            val response = authenticationDataSource.postResponse(postRequest)
            Log.e("response login",response.toString())
            if (!response.success) {

                onResult(false, response)
            } else {


                onResult(true, response)
            }
        }
    }
    @OptIn(DelicateCoroutinesApi::class)
    override fun postShareUser(
        postRequest: PostRequest,
        onResult: (isSuccess: Boolean, baseResponse: PostBaseResponse) -> Unit
    ) {
        myCoroutineScope.launch {
            val response = authenticationDataSource.postShareUserResponse(postRequest)

            Log.e("response login",response.toString())
            if (!response.success) {
                onResult(false, response)
            } else {
                onResult(true, response)
            }
        }
    }
    @OptIn(DelicateCoroutinesApi::class)
    override fun notInterested(
        postRequest: PostRequest,
        onResult: (isSuccess: Boolean, baseResponse: PostBaseResponse) -> Unit
    ) {
        myCoroutineScope.launch{
            val response = authenticationDataSource.notInterestedResponse(postRequest)

            Log.e("response login",response.toString())
            if (!response.success) {
                onResult(false, response)
            } else {
                onResult(true, response)
            }
        }
    }
    @OptIn(DelicateCoroutinesApi::class)
    override fun userRestrict(
        userRestrict: UserRestrict,
        onResult: (isSuccess: Boolean, baseResponse: PostBaseResponse) -> Unit
    ) {
        myCoroutineScope.launch {
            val response = authenticationDataSource.userRestrictResponse(userRestrict)

            Log.e("response login",response.toString())
            if (!response.success) {
                onResult(false, response)
            } else {
                onResult(true, response)
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun postRestrict(
        postRestrict: PostRestrict,
        onResult: (isSuccess: Boolean, baseResponse: PostBaseResponse) -> Unit
    ) {
        myCoroutineScope.launch {
            val response = authenticationDataSource.postRestrictResponse(postRestrict)
            if (!response.success) {
                onResult(false, response)
            } else {
                onResult(true, response)
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun postReport(
        postRequest: PostRequest,
        onResult: (isSuccess: Boolean, baseResponse: PostBaseResponse) -> Unit
    ) {
        myCoroutineScope.launch {
            val response = authenticationDataSource.postReportResponse(postRequest)
            if (!response.success) {
                onResult(false, response)
            } else {
                onResult(true, response)
            }
        }
    }
    @OptIn(DelicateCoroutinesApi::class)
    override fun commentReport(
        postRequest: PostRequest,
        onResult: (isSuccess: Boolean, baseResponse: PostBaseResponse) -> Unit
    ) {
        myCoroutineScope.launch {
            val response = authenticationDataSource.commentReportResponse(postRequest)
            if (!response.success) {
                onResult(false, response)
            } else {
                onResult(true, response)
            }
        }
    }

    override fun userReport(
        postRequest: PostRequest,
        onResult: (isSuccess: Boolean, baseResponse: PostBaseResponse) -> Unit
    ) {
        myCoroutineScope.launch {
            val response = authenticationDataSource.userReportResponse(postRequest)

            Log.e("response login",response.toString())
            if (!response.success) {
                onResult(false, response)
            } else {
                onResult(true, response)
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun postRepost(
        postRequest: PostRequest,
        onResult: (isSuccess: Boolean, baseResponse: PostBaseResponse) -> Unit
    ) {
        myCoroutineScope.launch {
            val response = authenticationDataSource.postRepostResponse(postRequest)

            Log.e("response login",response.toString())
            if (!response.success) {
                onResult(false, response)
            } else {
                onResult(true, response)
            }
        }
    }
    @OptIn(DelicateCoroutinesApi::class)
    override fun addComment(
        postRequest: CommentRequest,
        onResult: (isSuccess: Boolean, baseResponse: PostBaseResponse) -> Unit
    ) {
        myCoroutineScope.launch {
            Log.e("postShareUser",postRequest.toString())

            val response = authenticationDataSource.addCommentResponse(postRequest)

            Log.e("response login",response.toString())
            if (!response.success) {
                onResult(false, response)
            } else {
                onResult(true, response)
            }
        }
    }
    @OptIn(DelicateCoroutinesApi::class)
    override fun searchList(
        postRequest: SearchRequest,
        onResult: (isSuccess: Boolean, baseResponse: SearchBaseResponse) -> Unit
    ) {
        myCoroutineScope.launch {
            val response = authenticationDataSource.searchListResponse(postRequest)

            Log.e("response login",response.toString())
            if (!response.success) {
                onResult(false, response)
            } else {
                onResult(true, response)
            }
        }
    }

    override fun profilePost(
        data: Map<String, String>,
        onResult: (isSuccess: Boolean, baseResponse: ProfilePostResponse) -> Unit
    ) {
        myCoroutineScope.launch {
            val response = authenticationDataSource.profilePosts(data)
            Log.e("response login",response.toString())
            if (!response.success) {
                onResult(false, response)
            } else {
                onResult(true, response)
            }
        }
    }

    override fun savedPost(
        profilePostRequest: ProfilePostRequest,
        onResult: (isSuccess: Boolean, baseResponse: SavedProfilePost) -> Unit
    ) {
        myCoroutineScope.launch {
            val response = authenticationDataSource.savedPosts(profilePostRequest)
            Log.e("response login",response.toString())
            if (!response.success) {
                onResult(false, response)
            } else {
                onResult(true, response)
            }
        }
    }

    override fun userDeatils(data: Map<String, String>,
        onResult: (isSuccess: Boolean, baseResponse: UserDetailsResponse) -> Unit) {
        myCoroutineScope.launch{
            val response = authenticationDataSource.userDetails(data)

            Log.e("responseLogin",response.toString())
            if (!response.success) {
                onResult(false, response)
            } else {
                onResult(true, response)
            }
        }
    }

    override fun userLogout(onResult: (isSuccess: Boolean, baseResponse: BaseResponse) -> Unit) {
        myCoroutineScope.launch {
            val response = authenticationDataSource.userLogout()
            if (!response.success) {
                onResult(false, response)
            } else {
                onResult(true, response)
            }
        }
    }

    override fun userDeleteAccount(onResult: (isSuccess: Boolean, baseResponse: BaseResponse) -> Unit) {
        myCoroutineScope.launch {
            val response = authenticationDataSource.userDeleteAccount()
            if (!response.success) {
                onResult(false, response)
            } else {
                onResult(true, response)
            }
        }
    }

    override fun postLike(
        postRequest: PostRequest,
        onResult: (isSuccess: Boolean, baseResponse: BaseResponse) -> Unit
    ) {
        myCoroutineScope.launch {
            val response = authenticationDataSource.postLikeResponse(postRequest)

            Log.e("response login",response.toString())
            if (!response.success) {
                onResult(false, response)
            } else {
                onResult(true, response)
            }
        }
    }
    override fun postSave(
        postRequest: PostRequest,
        onResult: (isSuccess: Boolean, baseResponse: BaseResponse) -> Unit
    ) {
        myCoroutineScope.launch {
            val response = authenticationDataSource.postSaveResponse(postRequest)

            Log.e("response login",response.toString())
            if (!response.success) {
                onResult(false, response)
            } else {
                onResult(true, response)
            }
        }
    }

    override fun postPin(
        postPinRequest: PostPinRequest,
        onResult: (isSuccess: Boolean, baseResponse: BaseResponse) -> Unit
    ) {
        myCoroutineScope.launch {
            val response = authenticationDataSource.PostPin(postPinRequest)

            Log.e("response login",response.toString())
            if (!response.success) {
                onResult(false, response)
            } else {
                onResult(true, response)
            }
        }
    }

    override fun DeletePost(
        deletePost: DeletePost,
        onResult: (isSuccess: Boolean, baseResponse: BaseResponse) -> Unit
    ) {
        myCoroutineScope.launch {
            val response = authenticationDataSource.DeletePost(deletePost)

            Log.e("response login",response.toString())
            if (!response.success) {
                onResult(false, response)
            } else {
                onResult(true, response)
            }
        }
    }



    override fun postSoundPin(
       soundPinRequest: SoundPinRequest,
        onResult: (isSuccess: Boolean, baseResponse: BaseResponse) -> Unit
    ) {
        myCoroutineScope.launch {
            val response = authenticationDataSource.postSoundPin(soundPinRequest)

            Log.e("response login",response.toString())
            if (!response.success) {
                onResult(false, response)
            } else {
                onResult(true, response)
            }
        }
    }

    override fun ArchivePost(
        deletePost: DeletePost,
        onResult: (isSuccess: Boolean, baseResponse: BaseResponse) -> Unit
    ) {
        myCoroutineScope.launch {
            val response = authenticationDataSource.ArchivePost(deletePost)

            Log.e("response login",response.toString())
            if (!response.success) {
                onResult(false, response)
            } else {
                onResult(true, response)
            }
        }
    }

    override fun unArchivePost(
        deletePost: DeletePost,
        onResult: (isSuccess: Boolean, baseResponse: BaseResponse) -> Unit
    ) {
        myCoroutineScope.launch {
            val response = authenticationDataSource.UnArchivePost(deletePost)

            Log.e("response login",response.toString())
            if (!response.success) {
                onResult(false, response)
            } else {
                onResult(true, response)
            }
        }
    }

    override fun userList(
        userListRequest: UserListRequest,
        onResult: (isSuccess: Boolean, baseResponse: UserListResponse) -> Unit
    ) {
        myCoroutineScope.launch {
            val response = authenticationDataSource.userList(userListRequest)

            Log.e("response login",response.toString())
            if (!response.success) {
                onResult(false, response)
            } else {
                onResult(true, response)
            }
        }
    }

    override fun postDetails(
        deletePost: DeletePost,
        onResult: (isSuccess: Boolean, baseResponse: PostDetailsResponse) -> Unit
    ) {
        myCoroutineScope.launch {
            val response = authenticationDataSource.PostDetail(deletePost)

            Log.e("response login",response.toString())
            if (!response.success) {
                onResult(false, response)
            } else {
                onResult(true, response)
            }
        }
    }

    override fun EditPost(
        editPost: EditPost,
        onResult: (isSuccess: Boolean, baseResponse: BaseResponse) -> Unit
    ) {
        myCoroutineScope.launch {
            val response = authenticationDataSource.EditPost(editPost)
            if (!response.success) {
                onResult(false, response)
            } else {
                onResult(true, response)
            }
        }
    }
    override fun postFcmToken(
        payload: FcmRequest,
        onResult: (isSuccess: Boolean, baseResponse: BaseResponse) -> Unit
    ) {
        myCoroutineScope.launch {
            val response = authenticationDataSource.postFcmToken(payload)
            if (!response.success) {
                onResult(false, response)
            } else {
                onResult(true, response)
            }
        }
    }

    override fun MentionUser(
        mentionUserRequest: mentionUserRequest,
        onResult: (isSuccess: Boolean, baseResponse: MentionUserResponse) -> Unit
    ) {
        myCoroutineScope.launch(Dispatchers.IO) {
            val response = authenticationDataSource.mentionUser(mentionUserRequest)
            if (!response.success) {
                onResult(false, response)
            } else {
                onResult(true, response)
            }
        }
    }

    override fun userProfilePrivacy(onResult: (isSuccess: Boolean, baseResponse: ProfilePrivacyResponse) -> Unit) {
        myCoroutineScope.launch(Dispatchers.Main) {
            val response = authenticationDataSource.userProfilePrivacy()
            if (!response.success) {
                onResult(false, response)
            } else {
                onResult(true, response)
            }
        }
    }

    override fun accountAvailability(onResult: (isSuccess: Boolean, response: AccountAvailabilityResponse) -> Unit) {
        myCoroutineScope.launch {
            val response = authenticationDataSource.accountAvailability()
            if (response.success == false) {
                onResult(false, response)
            } else {
                onResult(true, response)
            }
        }
    }

    override fun userProfileAccount(onResult: (isSuccess: Boolean, baseResponse: ProfileAccountResponse) -> Unit) {
        myCoroutineScope.launch(Dispatchers.Main) {
            val response = authenticationDataSource.userProfileAccount()
            if (!response.success) {
                onResult(false, response)
            } else {
                onResult(true, response)
            }
        }
    }

    override fun userProfileNotification(onResult: (isSuccess: Boolean, baseResponse: ProfileNotificationResponse) -> Unit) {
            myCoroutineScope.launch(Dispatchers.IO) {
                val response = authenticationDataSource.userProfileNotification()
                if (!response.success) {
                    onResult(false, response)
                } else {
                    onResult(true, response)
                }
            }

    }

    override fun getUserExperience(onResult: (isSuccess: Boolean, baseResponse: UserExperienceResponse) -> Unit) {
        myCoroutineScope.launch {
            val response = authenticationDataSource.getUserExperience()
            if (!response.success!!) {
                onResult(false, response)
            } else {
                onResult(true, response)
            }
        }
    }

    override fun getUserInterest(interestRequest: InterestRequest, onResult: (isSuccess: Boolean, baseResponse: InterestResponse) -> Unit) {
        myCoroutineScope.launch {
            val response = authenticationDataSource.getInterest(interestRequest)
            if (!response.success) {
                onResult(false, response)
            } else {
                onResult(true, response)
            }
        }
    }

    override fun linkedAccount(
        linkedAccountRequest: LinkedAccountRequest,
        onResult: (isSuccess: Boolean, baseResponse: LinkedAccountResponse) -> Unit
    ) {
        myCoroutineScope.launch {
            val response = authenticationDataSource.linkedAccount(linkedAccountRequest)
            if (!response.success) {
                onResult(false, response)
            } else {
                onResult(true, response)
            }
        }
    }

    override fun saveUserInterest(
        interestRequest: ExperienceRequest,
        onResult: (isSuccess: Boolean, baseResponse: BaseResponse) -> Unit
    ) {
        myCoroutineScope.launch {
            val response = authenticationDataSource.updateExperience(interestRequest)
            if (!response.success) {
                onResult(false, response)
            } else {
                onResult(true, response)
            }
        }
    }

    override fun userEditProfilePrivacy(
        editPrivacy: EditPrivacy,
        onResult: (isSuccess: Boolean, baseResponse: BaseResponse) -> Unit
    ) {
        myCoroutineScope.launch {
            val response = authenticationDataSource.editPrivacy(editPrivacy)
            if (!response.success) {
                onResult(false, response)
            } else {
                onResult(true, response)
            }
        }
    }

    override fun editPrivacyFromProfile(
        editPrivacy: EditProfilePrivacy,
        onResult: (isSuccess: Boolean, baseResponse: BaseResponse) -> Unit
    ) {
        myCoroutineScope.launch {
            val response = authenticationDataSource.finalEditPrivacy(editPrivacy)
            if (!response.success) {
                onResult(false, response)
            } else {
                onResult(true, response)
            }
        }
    }

    override fun userEditProfileAccount(
        data :Map<String, String>,
        onResult: (isSuccess: Boolean, baseResponse: BaseResponse) -> Unit
    ) {
        myCoroutineScope.launch {
            val response = authenticationDataSource.editUserAccount(data)
            if (!response.success) {
                onResult(false, response)
            } else {
                onResult(true, response)
            }
        }
    }

    override fun userEditProfileNotification(
        userNotificationRequest: UserNotificationRequest,
        onResult: (isSuccess: Boolean, baseResponse: BaseResponse) -> Unit
    ) {
        myCoroutineScope.launch {
            val response = authenticationDataSource.editUserNotification(userNotificationRequest)
            if (!response.success) {
                onResult(false, response)
            } else {
                onResult(true, response)
            }
        }
    }

    override fun postSaveSound(
        postRequest: PostRequest,
        onResult: (isSuccess: Boolean, baseResponse: BaseResponse) -> Unit
    ) {
        myCoroutineScope.launch {
            val response = authenticationDataSource.postSaveSoundResponse(postRequest)
            if (!response.success) {
                onResult(false, response)
            } else {
                onResult(true, response)
            }
        }
    }

    override fun userDecrypt(
        userDecryptFile: UserDecryptFile,
        onResult: (isSuccess: Boolean, baseResponse: BaseResponse) -> Unit
    ) {
        myCoroutineScope.launch {
            val response = authenticationDataSource.userDecryptFile(userDecryptFile)
            if (!response.success) {
                onResult(false, response)
            } else {
                onResult(true, response)
            }
        }
    }

    override fun postViewed(
        postRequest: PostRequest,
        onResult: (isSuccess: Boolean, baseResponse: BaseResponse) -> Unit
    ) {
        myCoroutineScope.launch {
            val response = authenticationDataSource.postViewedResponse(postRequest)

            Log.e("response login",response.toString())
            if (!response.success) {
                onResult(false, response)
            } else {
                onResult(true, response)
            }
        }
    }

    override fun followUnfollowUser(
        postRequest: UserRequest,
        onResult: (isSuccess: Boolean, baseResponse: BaseResponse) -> Unit
    ) {
        myCoroutineScope.launch {
            val response = authenticationDataSource.followUnfollowUserResponse(postRequest)
            if (!response.success) {
                onResult(false, response)
            } else {
                onResult(true, response)
            }
        }
    }
    override fun postFollowRequestStatus(
        postRequest: UserRequest,
        onResult: (isSuccess: Boolean, baseResponse: BaseResponse) -> Unit
    ) {
        myCoroutineScope.launch {
            val response = authenticationDataSource.postFollowRequestStatusResponse(postRequest)
            if (!response.success) {
                onResult(false, response)
            } else {
                onResult(true, response)
            }
        }
    }
    override fun switchNotification(
        postRequest: SwitchNotificationRequest,
        onResult: (isSuccess: Boolean, baseResponse: BaseResponse) -> Unit
    ) {
        myCoroutineScope.launch {
            val response = authenticationDataSource.switchNotificationResponse(postRequest)
            if (!response.success) {
                onResult(false, response)
            } else {
                onResult(true, response)
            }
        }
    }


    override fun commentList(
        postRequest: PostRequest,
        onResult: (isSuccess: Boolean, baseResponse: CommentBaseResponse) -> Unit
    ) {
        myCoroutineScope.launch {
            val response = authenticationDataSource.commentListResponse(postRequest)
            if (!response.success) {
                onResult(false, response)
            } else {
                onResult(true, response)
            }
        }
    }
    override fun commentLike(
        postRequest: PostRequest,
        onResult: (isSuccess: Boolean, baseResponse: CommentBaseResponse) -> Unit
    ) {
        myCoroutineScope.launch {
            val response = authenticationDataSource.commentLikeResponse(postRequest)
            if (!response.success) {
                onResult(false, response)
            } else {
                onResult(true, response)
            }
        }
    }
    override fun createPost(
        createPost: CreatePost,
        onResult: (isSuccess: Boolean, baseResponse: BaseResponse) -> Unit
    ) {
        myCoroutineScope.launch {
            val response = authenticationDataSource.createPost(createPost)
            if (!response.success) {
                onResult(false, response)
            } else {
                Log.d("successApiRepo", " repo response without media is success")
                onResult(true, response)
            }
        }
    }

    override fun createPostWithMedia(
        payload: CreatePostWithMedia,
        onResult: (isSuccess: Boolean, baseResponse: BaseResponse) -> Unit
    ) {
        myCoroutineScope.launch {
            val response = authenticationDataSource.createPostWithMedia(payload)
            if (!response.success) {
                onResult(false, response)
            } else {
                Log.d("successApiRepo", " repo response without media is success")
                onResult(true, response)
            }
        }
    }

    override fun userBlockedLists(
        blockedRequest: BlockedRequest,
        onResult: (isSuccess: Boolean, baseResponse: BlockedResponse) -> Unit
    ) {
        myCoroutineScope.launch {
            val response = authenticationDataSource.userBlockedList(blockedRequest)
            if (!response.success) {
                onResult(false, response)
            } else {
                Log.d("successApiRepo", " repo response without media is success")
                onResult(true, response)
            }
        }
    }


    override fun hashTagList(
        payload: HashtagPayload,
        onResult: (isSuccess: Boolean, baseResponse: HashTagListResponse) -> Unit
    ) {
        myCoroutineScope.launch {
            val response = authenticationDataSource.hashTagList(payload)
            if (!response.success!!) {
                onResult(false, response)
            } else {
                Log.d("HashTagResponse", "HashTag List response is  success")
                onResult(true, response)
            }
        }
    }



    override fun userRestrictReportList(
        restrictReportRequest: RestrictReportRequest,
        onResult: (isSuccess: Boolean, baseResponse: RestrictResponse) -> Unit
    ) {
        myCoroutineScope.launch{
            val response = authenticationDataSource.restrictReportUser(restrictReportRequest)
            if (!response.success) {
                onResult(false, response)
            } else {
                onResult(true, response)
            }
        }
    }

    override fun updateInterest(
        updateInterestRequest: UpdateInterestRequest,
        onResult: (isSuccess: Boolean, baseResponse: BaseResponse) -> Unit
    ) {
        myCoroutineScope.launch {
            val response = authenticationDataSource.updateInterest(updateInterestRequest)
            if (!response.success) {
                onResult(false, response)
            } else {
                onResult(true, response)
            }
        }
    }

    override fun userFeedBack(
        feedBackRequest: FeedBackRequest,
        onResult: (isSuccess: Boolean, baseResponse: BaseResponse) -> Unit
    ) {
        myCoroutineScope.launch {
            val response = authenticationDataSource.userFeedBack(feedBackRequest)
            if (!response.success) {
                onResult(false, response)
            } else {
                onResult(true, response)
            }
        }
    }

    override fun downloadMyData(onResult: (isSuccess: Boolean, baseResponse: DownloadMyDataResponse) -> Unit) {
        myCoroutineScope.launch {
            val response = authenticationDataSource.downloadMyData()
            if (!response.success) {
                onResult(false, response)
            } else {
                onResult(true, response)
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun userBlock(
        userBlockedRequest: UserBlockedRequest,
        onResult: (isSuccess: Boolean, baseResponse: BaseResponse) -> Unit
    ) {
        myCoroutineScope.launch {
            val response = authenticationDataSource.userBlocked(userBlockedRequest)
            if (!response.success) {
                onResult(false, response)
            } else {
                onResult(true, response)
            }
        }
    }

    override fun userLikeList(
        likeListRequest: LikeListRequest,
        onResult: (isSuccess: Boolean, baseResponse: LikeListResponse) -> Unit
    ) {
        myCoroutineScope.launch {
            val response = authenticationDataSource.userLikeList(likeListRequest)
            if (!response.success) {
                onResult(false, response)
            } else {
                onResult(true, response)
            }
        }
    }

    override fun friendList(
        friendListRequest: FriendListRequest,
        onResult: (isSuccess: Boolean, baseResponse: FriendListResponse) -> Unit
    ) {
        myCoroutineScope.launch {
            val response = authenticationDataSource.friendList(friendListRequest)
            if (!response.success) {
                onResult(false, response)
            } else {
                onResult(true, response)
            }
        }
    }

    override fun followerList(
        followerListRequest: FollowerListRequest,
        onResult: (isSuccess: Boolean, baseResponse: FriendListResponse) -> Unit
    ) {
        myCoroutineScope.launch {
            val response = authenticationDataSource.followerList(followerListRequest)
            if (!response.success) {
                onResult(false, response)
            } else {
                onResult(true, response)
            }
        }
    }

    override fun followingList(
        followingListRequest: FollowingListRequest,
        onResult: (isSuccess: Boolean, baseResponse: FriendListResponse) -> Unit
    ) {
        myCoroutineScope.launch {
            val response = authenticationDataSource.followingList(followingListRequest)
            if (!response.success) {
                onResult(false, response)
            } else {
                onResult(true, response)
            }
        }
    }

    override fun premiumPurchasesApi(
        premiumPurchase: PremiumPurchasePayLoad,
        onResult: (isSuccess: Boolean, baseResponse: BaseResponse) -> Unit
    ) {
        myCoroutineScope.launch {
            val response = authenticationDataSource.premiumPurchasesApi(premiumPurchase)
            if (!response.success) {
                onResult(false, response)
            } else {
                Log.d("premiumPurchase", "premium purchase api is hit")
                onResult(true, response)
            }
        }
    }

    override fun purchaseTreeApi(
        purchaseTrees: PurchaseTreesPayload,
        onResult: (isSuccess: Boolean, baseResponse: BaseResponse) -> Unit
    ) {
        myCoroutineScope.launch {
            val response = authenticationDataSource.purchaseTreeApi(purchaseTrees)
            if (!response.success) {
                onResult(false, response)
            } else {
                Log.d("premiumPurchase", "purchase trees api is hit")
                onResult(true, response)
            }
        }
    }

    override fun purchaseSrcApi(
        purchaseSrc: PurchaseSrcPayload,
        onResult: (isSuccess: Boolean, baseResponse: BaseResponse) -> Unit
    ) {
        myCoroutineScope.launch {
            val response = authenticationDataSource.purchaseSrcApi(purchaseSrc)
            if (!response.success) {
                onResult(false, response)
            } else {
                Log.d("premiumPurchase", "purchase trees api is hit")
                onResult(true, response)
            }
        }
    }

    override fun getUserDetails(
        userList: UserListRequest,
        onResult: (isSuccess: Boolean, baseResponse: UserDataList) -> Unit
    ) {
        myCoroutineScope.launch {
            val response = authenticationDataSource.getUserDetails(userList)
            if (response.success == false) {
                onResult(false, response)
            } else {
                onResult(true, response)
            }
        }
    }

    override fun getPurchaseHistory(
        userList: UserListRequest,
        onResult: (isSuccess: Boolean, baseResponse: PurchaseHistoryList) -> Unit
    ) {
        myCoroutineScope.launch {
            val response = authenticationDataSource.getPurchaseHistory(userList)
            if (response.success == false) {
                Log.d("purchaseHistory", "Purchase history response is not success ${response}")
                onResult(false, response)
            } else {
                Log.d("purchaseHistory", "Purchase history response is success ${response}")
                onResult(true, response)
            }
        }
    }

    override fun removeFollower(
        purchaseSrc: UserRequest,
        onResult: (isSuccess: Boolean, baseResponse: BaseResponse) -> Unit
    ) {
        myCoroutineScope.launch {
            val response = authenticationDataSource.removeFollower(purchaseSrc)
            if (!response.success) {
                onResult(false, response)
            } else {
                Log.d("premiumPurchase", "purchase trees api is hit")
                onResult(true, response)
            }
        }
    }

    override fun getPurchaseDetails(onResult: (isSuccess: Boolean, response: PurchaseDetails) -> Unit) {
        myCoroutineScope.launch{
            val response = authenticationDataSource.getPurchaseDetails()
            if (response.success == false) {
                onResult(false, response)
                Log.d("premiumPurchase", "get details is  not hit")
            } else {
                onResult(true, response)
                Log.d("premiumPurchase", "get details is hit")
            }
        }
    }

    override fun getLeaderBoardDetails(
        leaderBoardList: LeaderboardRequest,
        onResult: (isSuccess: Boolean, leaderBoardResponse: LeaderboardResponse) -> Unit
    ) {
        myCoroutineScope.launch{
            val response = authenticationDataSource.getLeaderBoardDetails(leaderBoardList)
            if (response.success == false) {
                Log.d("leaderBoardSuccess", "no success")
                onResult(false, response)

            } else {
                Log.d("leaderBoardSuccess", "success")
                onResult(true, response)

            }
        }
    }
    override fun getHashtagPost(
        payload: HashtagPayload,
        onResult: (isSuccess: Boolean, postBaseResponse: PostBaseResponse) -> Unit
    ) {
        myCoroutineScope.launch{
            val response = authenticationDataSource.getHashtagPost(payload)
            if (response.success == false) {
                Log.d("leaderBoardSuccess", "no success")
                onResult(false, response)

            } else {
                Log.d("leaderBoardSuccess", "success")
                onResult(true, response)

            }
        }
    }

    override fun notificationList(
        notificationRequest: NotificationRequest,
        onResult: (isSuccess: Boolean, baseResponse: NotificationListResponse) -> Unit
    ) {
        myCoroutineScope.launch{
            val response = authenticationDataSource.notificationList(notificationRequest)
            if (!response.success) {
                Log.d("leaderBoardSuccess", "no success")
                onResult(false, response)

            } else {
                Log.d("leaderBoardSuccess", "success")
                onResult(true, response)

            }
        }
    }

    override fun getMostPlantedTrees(
        payload: MostTreesPlantedRequest,
        onResult: (isSuccess: Boolean, response: MostTreesPlantedResponse) -> Unit
    ) {
        myCoroutineScope.launch{
            val response = authenticationDataSource.getMostPlantedTrees(payload)
            if (response.success == false) {
                Log.d("mostPlantedTreesSuccess", "no success")
                onResult(false, response)

            } else {
                Log.d("mostPlantedTreesSuccess", "success")
                onResult(true, response)

            }
        }
    }

    override fun getMostPlantedTreesByRegion(
        payload: RegionRequest,
        onResult: (isSuccess: Boolean, response: MostTreesPlantedByRegionResponse) -> Unit
    ) {
        myCoroutineScope.launch{
            val response = authenticationDataSource.getMostPlantedTreesByRegion(payload)
            if (response.success == false) {
                Log.d("mostPlantedTreesSuccess", "no success")
                onResult(false, response)

            } else {
                Log.d("mostPlantedTreesSuccessForRegion", "success : ${response}")
                onResult(true, response)

            }
        }
    }

    override fun getDropsDetails(
        dropsList: DropsDataRequest,
        onResult: (isSuccess: Boolean, dropsResponse: DropsDataResponse) -> Unit
    ) {
        myCoroutineScope.launch{
            val response = authenticationDataSource.getDropsDetails(dropsList)
            if (response.success == false) {
                Log.d("leaderBoardSuccess", "no success")
                onResult(false, response)

            } else {
                Log.d("leaderBoardSuccess", "success")
                onResult(true, response)

            }
        }
    }

    override fun sendSrc(
        details: TransferSrcDetails,
        onResult: (isSuccess: Boolean, baseResponse: BaseResponse) -> Unit
    ) {
        myCoroutineScope.launch{
            val response = authenticationDataSource.sendSrc(details)
            if (!response.success) {
                onResult(false, response)

            } else {
                onResult(true, response)

            }
        }
    }

    override fun registerUser(
        details: RegisterUserRequest,
        onResult: (isSuccess: Boolean, response: RegisterUserResponse) -> Unit
    ) {
        myCoroutineScope.launch{
            val response = authenticationDataSource.registerUser(details)
            if (response.success == true) {
                Log.d("successUserRegister", "Response is ${response.success}")
                onResult(true, response)

            } else {
                Log.d("successUserRegister", "Response is ${response.success}")
                onResult(false, response)


            }
        }
    }

    override fun getCountryDetails(onResult: (isSuccess: Boolean, response: CountryListImpactScreen) -> Unit) {
        myCoroutineScope.launch{
            val response = authenticationDataSource.getCountryList()
            if (response.success==false) {
                onResult(false, response)
                Log.d("countryList", " country get details is not hit")
            } else {
                onResult(true, response)
                Log.d("countryList", "country get details is hit")
            }
        }
    }

    override fun getImpactDetails(onResult: (isSuccess: Boolean, response: ImpactDetail) -> Unit) {
        myCoroutineScope.launch{
            val response = authenticationDataSource.getImpactDetails()
            if (response.success==false) {
                onResult(false, response)
                Log.d("impactDetailList", " impactDetailList details is not hit")
            } else {
                onResult(true, response)
                Log.d("impactDetailList", "impactDetailList details is hit")
            }
        }
    }

    override fun getPrizeDetails(onResult: (isSuccess: Boolean, response: PrizeDetailsResponse) -> Unit) {
        myCoroutineScope.launch{
            val response = authenticationDataSource.getPrizeDetails()
            if (!response.success) {
                onResult(false, response)
                Log.d("prizeDetails", " Prize Details details is not hit")
            } else {
                onResult(true, response)
                Log.d("impactDetailList", "Prize Details details is hit")
            }
        }
    }

    override fun deleteNotification(
        notification: DeleteNotification,
        onResult: (isSuccess: Boolean, baseResponse: BaseResponse) -> Unit
    ) {
        myCoroutineScope.launch{
            val response = authenticationDataSource.deleteNotification(notification)
            if (!response.success) {
                onResult(false, response)
                Log.d("DeleteNotification", " DeleteNotification details is not hit")
            } else {
                onResult(true, response)
                Log.d("DeleteNotification", "DeleteNotification details is hit")
            }
        }
    }

    override fun deleteComment(
        comment: DeleteCommentRequest,
        onResult: (isSuccess: Boolean, baseResponse: BaseResponse) -> Unit
    ) {
        myCoroutineScope.launch{
            val response = authenticationDataSource.deleteComment(comment)
            if (!response.success) {
                onResult(false, response)
                Log.d("DeleteComment", " DeleteComment details is not hit $response")
            } else {
                onResult(true, response)
                Log.d("DeleteComment", "Delete comment details is hit")
            }
        }
    }

    override fun pinComment(
        pinComment: PinCommentRequest,
        onResult: (isSuccess: Boolean, baseResponse: BaseResponse) -> Unit
    ) {
        myCoroutineScope.launch{
            val response = authenticationDataSource.pinComment(pinComment)
            if (!response.success) {
                onResult(false, response)
                Log.d("PinComment", " Pin Comment is not hit $response")
            } else {
                onResult(true, response)
                Log.d("PinComment", "Pin comment details is hit $response")
            }
        }
    }

    override fun recoveryCode(
        request: RecoveryCodeRequest,
        onResult: (isSuccess: Boolean, response: RecoveryCodeResponse) -> Unit
    ) {
        myCoroutineScope.launch{
            val response = authenticationDataSource.recoveryCode(request)
            if (response.success == false) {
                onResult(false, response)
                Log.d("DeleteNotification", " Recovery Code  details is not hit")
            } else {
                onResult(true, response)
                Log.d("DeleteNotification", "Recovery Code details is hit")
            }
        }
    }

    override fun sentMessagesRequest(
        request: MessageRequest,
        onResult: (isSuccess: Boolean, baseResponse: MessageResponse) -> Unit
    ) {
        myCoroutineScope.launch{
            val response = authenticationDataSource.sentMessageRequest(request)
            if (response.success == false) {
                onResult(false, response)
                Log.d("MessagesRequest", "sentMessagesRequest is not hit")
            } else {
                onResult(true, response)
                Log.d("MessagesRequest", "sentMessagesRequest details is hit")
            }
        }
    }

    override fun receivedMessagesRequest(
        request: MessageRequest,
        onResult: (isSuccess: Boolean, baseResponse: MessageResponse) -> Unit
    ) {
        myCoroutineScope.launch{
            val response = authenticationDataSource.receivedMessageRequest(request)
            if (response.success == false) {
                onResult(false, response)
                Log.d("MessagesRequest", " receivedMessagesRequest  details is not hit")
            } else {
                onResult(true, response)
                Log.d("MessagesRequest", "receivedMessagesRequest is hit")
            }
        }
    }

    override fun removeLikes(
        removeLikes: RemoveLikes,
        onResult: (isSuccess: Boolean, baseResponse: BaseResponse) -> Unit
    ) {
        myCoroutineScope.launch{
            val response = authenticationDataSource.removeLikes(removeLikes)
            if (!response.success) {
                onResult(false, response)
            } else {
                onResult(true, response)
            }
        }
    }

    override fun acceptRejectRequests(
        request: AcceptRejectRequest,
        onResult: (isSuccess: Boolean, baseResponse: AcceptRejectResponse) -> Unit
    ) {
        myCoroutineScope.launch{
            val response = authenticationDataSource.acceptRejectRequest(request)
            if (response.success == false) {
                onResult(false, response)
                Log.d("MessagesRequestAcceptReject", "  accept receivedMessagesRequest  details is not hit")
            } else {
                onResult(true, response)
                Log.d("MessagesRequestAcceptReject", " accept receivedMessagesRequest is hit")
            }
        }
    }

    override fun chatList(
        chatList: ChatList,
        onResult: (isSuccess: Boolean, baseResponse: MessageListResponse) -> Unit
    ) {
        myCoroutineScope.launch{
            val response = authenticationDataSource.chatList(chatList)
            if (!response.success) {
                onResult(false, response)

            } else {
                onResult(true, response)

            }
        }
    }

    override fun chatHistory(
        chatHistoryRequest: ChatHistoryRequest,
        onResult: (isSuccess: Boolean, baseResponse: ShowChatResponse) -> Unit
    ) {
        myCoroutineScope.launch{
            val response = authenticationDataSource.chatHistory(chatHistoryRequest)
            if (!response.success) {
                onResult(false, response)

            } else {
                onResult(true, response)

            }
        }
    }

    override fun chatImageUpload(
        chatImageUpload: ChatImageUpload,
        onResult: (isSuccess: Boolean, baseResponse: BaseResponse) -> Unit
    ) {
        myCoroutineScope.launch{
            val response = authenticationDataSource.chatImageUpload(chatImageUpload)
            if (!response.success) {
                onResult(false, response)

            } else {
                onResult(true, response)

            }
        }
    }

    fun cancelCoroutine(){
        myCoroutineScope.cancel()
    }

}




