package com.wedo8020.app.dataSources

import android.util.Log
import com.wedo8020.app.network.ApiService
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
import com.wedo8020.app.response.FriendListResponse
import com.wedo8020.app.response.CountryListImpactScreen
import com.wedo8020.app.response.DownloadMyDataResponse
import com.wedo8020.app.response.DropsDataRequest
import com.wedo8020.app.response.DropsDataResponse
import com.wedo8020.app.response.ImpactDetail
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

class RemoteDataSourceImp() : RemoteDataSource {

    private val apiService by lazy {
        ApiService()
    }
    override suspend fun loginResponse(payload: LoginRequest): BaseResponse {
        var response = BaseResponse()
        try {
            response = apiService.login(payload)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()
            response.error = 1
        }
        return response
    }

    override suspend fun postResponse(payload: PostRequest): PostBaseResponse {
        var response = PostBaseResponse()
        try {
            response = apiService.postList(payload)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()
            response.error = 1
        }
        return response
    }
    override suspend fun postShareUserResponse(payload: PostRequest): PostBaseResponse {
        var response = PostBaseResponse()
        try {
            response = apiService.postShareUser(payload)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()
            response.error = 1
        }
        return response
    }
    override suspend fun notInterestedResponse(payload: PostRequest): PostBaseResponse {
        var response = PostBaseResponse()
        try {
            response = apiService.notInterested(payload)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()
            response.error = 1
        }
        return response
    }
    override suspend fun userRestrictResponse(payload: UserRestrict): PostBaseResponse {
        var response = PostBaseResponse()
        try {
            response = apiService.userRestrict(payload)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()
            response.error = 1
        }
        return response
    }

    override suspend fun postRestrictResponse(postRestrict: PostRestrict): PostBaseResponse {
        var response = PostBaseResponse()
        try {
            response = apiService.postRestrict(postRestrict)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()
            response.error = 1
        }
        return response
    }

    override suspend fun postReportResponse(payload: PostRequest): PostBaseResponse {
        var response = PostBaseResponse()
        try {
            response = apiService.postReport(payload)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()
            response.error = 1
        }
        return response
    }
    override suspend fun commentReportResponse(payload: PostRequest): PostBaseResponse {
        var response = PostBaseResponse()
        try {
            response = apiService.commentReport(payload)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()
            response.error = 1
        }
        return response
    }

    override suspend fun userReportResponse(payload: PostRequest): PostBaseResponse {
        var response = PostBaseResponse()
        try {
            response = apiService.userReport(payload)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()
            response.error = 1
        }
        return response
    }


    override suspend fun postRepostResponse(payload: PostRequest): PostBaseResponse {
        var response = PostBaseResponse()
        try {
            response = apiService.postRepost(payload)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()
            response.error = 1
        }
        return response
    }
    override suspend fun addCommentResponse(payload: CommentRequest): PostBaseResponse {
        var response = PostBaseResponse()
        try {
            Log.e("postShareUser",payload.toString())

            response = apiService.addComment(payload)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()
            response.error = 1
        }
        return response
    }

    override suspend fun profilePosts(data: Map<String, String>): ProfilePostResponse {
        var response = ProfilePostResponse()
        try {
            response = apiService.profilePosts(data)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()
            response.error = 1
        }
        return response
    }

    override suspend fun savedPosts(profilePostRequest: ProfilePostRequest): SavedProfilePost {
        var response = SavedProfilePost()
        try {
            response = apiService.savedPosts(profilePostRequest)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()
            response.error = 1
        }
        return response
    }

    override suspend fun linkedAccount(payload: LinkedAccountRequest): LinkedAccountResponse {
        var response = LinkedAccountResponse()
        try {
            response = apiService.linkedAccounts(payload)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()
            response.error = 1
        }
        return response
    }


    override suspend fun userDetails(data: Map<String, String>): UserDetailsResponse {
        var response = UserDetailsResponse()
        try {
            response = apiService.userDetails(data)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()
            response.error = 1
        }
        return response
    }

    override suspend fun userLogout(): BaseResponse {
        var response = BaseResponse()
        try {
            response = apiService.userLogout()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()
            response.error = 1
        }
        return response
    }

    override suspend fun userDeleteAccount(): BaseResponse {
        var response = BaseResponse()
        try {
            response = apiService.userDeleteAccount()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()
            response.error = 1
        }
        return response
    }

    override suspend fun postLikeResponse(payload: PostRequest): BaseResponse {
        var response = BaseResponse()
        try {
            response = apiService.postLike(payload)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()
            response.error = 1
        }
        return response
    }

    override suspend fun userDecryptFile(payload: UserDecryptFile): BaseResponse {
        var response = BaseResponse()
        try {
            response = apiService.userDecryptFile(payload)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()
            response.error = 1
        }
        return response
    }

    override suspend fun postSaveResponse(payload: PostRequest): BaseResponse {
        var response = BaseResponse()
        try {
            response = apiService.postSave(payload)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()
            response.error = 1
        }
        return response
    }

    override suspend fun PostPin(payload: PostPinRequest): BaseResponse {
        var response = BaseResponse()
        try {
            response = apiService.PostPin(payload)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()
            response.error = 1
        }
        return response
    }

    override suspend fun userList(payload: UserListRequest): UserListResponse {
        var response = UserListResponse()
        try {
            response = apiService.userList(payload)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()
            response.error = 1
        }
        return response
    }

    override suspend fun DeletePost(payload: DeletePost): BaseResponse {
        var response = BaseResponse()
        try {
            response = apiService.postDelete(payload)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()
            response.error = 1
        }
        return response
    }

    override suspend fun deleteComment(payload: DeleteCommentRequest): BaseResponse {
        var response = BaseResponse()
        try {
            response = apiService.deleteComment(payload)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()

        }
        return response
    }

    override suspend fun pinComment(payload: PinCommentRequest): BaseResponse {
        var response = BaseResponse()
        try {
            response = apiService.pinComment(payload)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()
            response.error = 1
        }
        return response
    }

    override suspend fun postSoundPin(payload: SoundPinRequest): BaseResponse {
        var response = BaseResponse()
        try {
            response = apiService.postSoundPin(payload)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()
            response.error = 1
        }
        return response
    }



    override suspend fun EditPost(payload: EditPost): BaseResponse {
        var response = BaseResponse()
        try {
            response = apiService.editPost(payload)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()
            response.error = 1
        }
        return response
    }
    override suspend fun postFcmToken(payload: FcmRequest): BaseResponse {
        var response = BaseResponse()
        try {
            response = apiService.postFcmToken(payload)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()
            response.error = 1
        }
        return response
    }

    override suspend fun ArchivePost(payload: DeletePost): BaseResponse {
        var response = BaseResponse()
        try {
            response = apiService.postArchive(payload)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()
            response.error = 1
        }
        return response
    }

    override suspend fun UnArchivePost(payload: DeletePost): BaseResponse {
        var response = BaseResponse()
        try {
            response = apiService.postUnArchive(payload)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()
            response.error = 1
        }
        return response
    }

    override suspend fun PostDetail(payload: DeletePost): PostDetailsResponse {
        var response = PostDetailsResponse()
        try {
            response = apiService.postDetail(payload)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()
            response.error = 1
        }
        return response
    }

    override suspend fun mentionUser(payload: mentionUserRequest): MentionUserResponse {
        var response = MentionUserResponse()
        try {
            response = apiService.mentionUser(payload)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()
            response.error = 1
        }
        return response
    }

    override suspend fun userProfilePrivacy(): ProfilePrivacyResponse {
        var response = ProfilePrivacyResponse()
        try {
            response = apiService.userProfile()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()
            response.error = 1
        }
        return response
    }

    override suspend fun userProfileAccount(): ProfileAccountResponse {
        var response = ProfileAccountResponse()
        try {
            response = apiService.userProfileAccount()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()
            response.error = 1
        }
        return response
    }

    override suspend fun userProfileNotification(): ProfileNotificationResponse {
        var response = ProfileNotificationResponse()
        try {
            response = apiService.userProfileNotification()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()
            response.error = 1
        }
        return response
    }

 /*   override suspend fun editPrivacy(editPrivacy: EditPrivacy): BaseResponse {
        var response = BaseResponse()
        try {
            response = apiService.editProfilePrivacy(editPrivacy)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()
            response.error = 1
        }
        return response
    }*/
 override suspend fun editPrivacy(editPrivacy: EditPrivacy): BaseResponse {
     var response = BaseResponse()
     try {
         response = apiService.editProfilePrivacy(editPrivacy)
     } catch (e: java.lang.Exception) {
         e.printStackTrace()
         response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()
         response.error = 1
     }
     return response
 }

    override suspend fun finalEditPrivacy(editPrivacy: EditProfilePrivacy): BaseResponse {
        var response = BaseResponse()
        try {
            response = apiService.finalEditProfilePrivacy(editPrivacy)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()
            response.error = 1
        }
        return response
    }

    override suspend fun editUserAccount(data :Map<String, String>): BaseResponse {
        var response = BaseResponse()
        try {
            response = apiService.editUserAccount(data)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()
            response.error = 1
        }
        return response
    }

    override suspend fun getUserExperience(): UserExperienceResponse {
        var response = UserExperienceResponse()
        try {
            response = apiService.getUserExperience()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()
            response.error = 1
        }
        return response
    }

    override suspend fun getInterest(interestRequest: InterestRequest): InterestResponse {
        var response = InterestResponse()
        try {
            response = apiService.getInterest(interestRequest)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()
            response.error = 1
        }
        return response
    }


    override suspend fun updateExperience(interestRequest: ExperienceRequest): BaseResponse {
        var response = BaseResponse()
        try {
            response = apiService.updateUserExperience(interestRequest)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()
            response.error = 1
        }
        return response
    }

    override suspend fun editUserNotification(userNotificationRequest: UserNotificationRequest): BaseResponse {
        var response = BaseResponse()
        try {
            response = apiService.editUserNotification(userNotificationRequest)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()
            response.error = 1
        }
        return response
    }

    override suspend fun restrictReportUser(restrictReportRequest: RestrictReportRequest): RestrictResponse {
        var response = RestrictResponse()
        try {
            response = apiService.restrictReportUser(restrictReportRequest)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()
            response.error = 1
        }
        return response
    }

    override suspend fun userBlockedList(blockedRequest: BlockedRequest): BlockedResponse {
        var response = BlockedResponse()
        try {
            response = apiService.userBlockList(blockedRequest)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()
            response.error = 1
        }
        return response
    }

    override suspend fun updateInterest(updateInterestRequest: UpdateInterestRequest): BaseResponse {
        var response = BaseResponse()
        try {
            response = apiService.updateUserInterest(updateInterestRequest)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()
            response.error = 1
        }
        return response
    }

    /*    override suspend fun mentionUser(payload: mentionUserRequest): MentionUserResponse {
            var response = MentionUserResponse()
            try {
                response = apiService.mentionUser(payload)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()
                response.error = 1
            }
            return response
        }*/

    override suspend fun postSaveSoundResponse(payload: PostRequest): BaseResponse {
        var response = BaseResponse()
        try {
            response = apiService.postSound(payload)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()
            response.error = 1
        }
        return response
    }
    override suspend fun postViewedResponse(payload: PostRequest): BaseResponse {
        var response = BaseResponse()
        try {
            response = apiService.postViewed(payload)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()
            response.error = 1
        }
        return response
    }


    override suspend fun followUnfollowUserResponse(payload: UserRequest): BaseResponse {
        var response = BaseResponse()
        try {
            response = apiService.followUnfollowUser(payload)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()
            response.error = 1
        }
        return response
    }
    override suspend fun switchNotificationResponse(payload: SwitchNotificationRequest): BaseResponse {
        var response = BaseResponse()
        try {
            response = apiService.switchNotification(payload)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()
            response.error = 1
        }
        return response
    }
    override suspend fun postFollowRequestStatusResponse(payload: UserRequest): BaseResponse {
        var response = BaseResponse()
        try {
            response = apiService.postFollowRequestStatus(payload)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()
            response.error = 1
        }
        return response
    }



    override suspend fun commentListResponse(payload: PostRequest): CommentBaseResponse {
        var response = CommentBaseResponse()
        try {
            response = apiService.commentList(payload)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()
            response.error = 1
        }
        return response
    }
    override suspend fun commentLikeResponse(payload: PostRequest): CommentBaseResponse {
        var response = CommentBaseResponse()
        try {
            response = apiService.commentLike(payload)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()
            response.error = 1
        }
        return response
    }
    override suspend fun searchListResponse(payload: SearchRequest): SearchBaseResponse {
        var response = SearchBaseResponse()
        try {
            response = apiService.searchList(payload)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()
            response.error = 1
        }
        return response
    }

    override suspend fun createPost(payload: CreatePost): BaseResponse {
        var response = BaseResponse()
        try {
            response = apiService.createPost(payload)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()
            response.error = 1
        }
        return response
    }

    override suspend fun createPostWithMedia(payload: CreatePostWithMedia): BaseResponse {
        var response = BaseResponse()
        try {
            response = apiService.createPostWithMedia(payload)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()
            response.error = 1
        }
        return response
    }

    override suspend fun hashTagList(payload: HashtagPayload): HashTagListResponse {
        var response = HashTagListResponse()
        try {
            response = apiService.hashtagList(payload)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()

        }
        return response
    }

    override suspend fun userFeedBack(feedBackRequest: FeedBackRequest): BaseResponse {
        var response = BaseResponse()
        try {
            response = apiService.userFeedBack(feedBackRequest)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()
            response.error = 1
        }
        return response
    }

    override suspend fun userLikeList(likeListRequest: LikeListRequest): LikeListResponse {
        var response = LikeListResponse()
        try {
            response = apiService.userLikeList(likeListRequest)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()
            response.error = 1
        }
        return response
    }

    override suspend fun userBlocked(userBlockedRequest: UserBlockedRequest): BaseResponse {
        var response = BaseResponse()
        try {
            response = apiService.userBlocked(userBlockedRequest)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()
            response.error = 1
        }
        return response
    }

    override suspend fun friendList(friendListRequest: FriendListRequest): FriendListResponse {
        var response = FriendListResponse()
        try {
            response = apiService.friendList(friendListRequest)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()
            response.error = 1
        }
        return response
    }

    override suspend fun followingList(followingListRequest: FollowingListRequest): FriendListResponse {
        var response = FriendListResponse()
        try {
            response = apiService.followingList(followingListRequest)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()
            response.error = 1
        }
        return response
    }

    override suspend fun followerList(followerListRequest: FollowerListRequest): FriendListResponse {
        var response = FriendListResponse()
        try {
            response = apiService.followerList(followerListRequest)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()
            response.error = 1
        }
        return response
    }

    override suspend fun notificationList(notificationRequest: NotificationRequest): NotificationListResponse {
        var response = NotificationListResponse()
        try {
            response = apiService.notificationList(notificationRequest)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()
            response.error = 1
        }
        return response
    }

    override suspend fun downloadMyData(): DownloadMyDataResponse {
        var response = DownloadMyDataResponse()
        try {
            response = apiService.downloadMyData()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()
            response.error = 1
        }
        return response
    }

    override suspend fun premiumPurchasesApi(payload: PremiumPurchasePayLoad): BaseResponse {
        var response = BaseResponse()
        try {
            response = apiService.premiumPurchasesApi(payload)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()
            response.error = 1
        }
        return response
    }

    override suspend fun purchaseTreeApi(payload: PurchaseTreesPayload): BaseResponse {
        var response = BaseResponse()
        try {
            response = apiService.purchaseTreeApi(payload)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()
            response.error = 1
        }
        return response
    }

    override suspend fun purchaseSrcApi(payload: PurchaseSrcPayload): BaseResponse {
        var response = BaseResponse()
        try {
            response = apiService.purchaseSrcApi(payload)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()
            response.error = 1
        }
        return response
    }

    override suspend fun removeFollower(payload: UserRequest): BaseResponse {
        var response = BaseResponse()
        try {
            response = apiService.removeFollower(payload)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()
            response.error = 1
        }
        return response
    }

    override suspend fun getPurchaseDetails(): PurchaseDetails {
        var response = PurchaseDetails()
        try {
            response = apiService.getPurchaseDetails()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()
        }
        return response
    }

    override suspend fun getPurchaseHistory(payload: UserListRequest): PurchaseHistoryList {
        var response = PurchaseHistoryList()
        try {
            response = apiService.getPurchaseHistory(payload)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()

        }
        return response
    }


    override suspend fun getCountryList(): CountryListImpactScreen {
        var response = CountryListImpactScreen()
        try {
            response = apiService.getCountryDetails()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()
        }
        return response
    }

    override suspend fun getImpactDetails(): ImpactDetail {
        var response = ImpactDetail()
        try {
            response = apiService.getImpactDetails()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()
        }
        return response
    }

    override suspend fun getPrizeDetails(): PrizeDetailsResponse {
        var response = PrizeDetailsResponse()
        try {
            response = apiService.getPrizeDetails()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()
        }
        return response
    }

    override suspend fun getUserDetails(payload: UserListRequest): UserDataList {
        var response = UserDataList()
        try {
            response = apiService.getUserList(payload)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()

        }
        return response
    }

    override suspend fun sendSrc(payload: TransferSrcDetails): BaseResponse {
        var response = BaseResponse()
        try {
            response = apiService.sendSrc(payload)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()

        }
        return response
    }

    override suspend fun getLeaderBoardDetails(payload: LeaderboardRequest): LeaderboardResponse {
        var response = LeaderboardResponse()
        try {
            response = apiService.getLeaderBoardDetails(payload)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()

        }
        return response
    }

    override suspend fun getMostPlantedTrees(payload: MostTreesPlantedRequest): MostTreesPlantedResponse {
        var response = MostTreesPlantedResponse()
        try {
            response = apiService.getMostPlantedTrees(payload)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()

        }
        return response
    }

    override suspend fun getMostPlantedTreesByRegion(payload: RegionRequest): MostTreesPlantedByRegionResponse {
        var response = MostTreesPlantedByRegionResponse()
        try {
            response = apiService.getTreesPlantedByRegion(payload)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()

        }
        return response
    }

    override suspend fun getDropsDetails(payload: DropsDataRequest): DropsDataResponse {
        var response = DropsDataResponse()
        try {
            response = apiService.getDropsDetails(payload)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()

        }
        return response
    }

    override suspend fun chatImageUpload(payload: ChatImageUpload): BaseResponse {
        var response = BaseResponse()
        try {
            response = apiService.chatImageUpload(payload)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()

        }
        return response
    }


    override suspend fun recoveryCode(payload: RecoveryCodeRequest): RecoveryCodeResponse {
        var response = RecoveryCodeResponse()
        try {
            response = apiService.recoveryCode(payload)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()

        }
        return response
    }

    override suspend fun sentMessageRequest(payload: MessageRequest): MessageResponse {
        var response = MessageResponse()
        try {
            response = apiService.sentMessageRequests(payload)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()

        }
        return response
    }

    override suspend fun receivedMessageRequest(payload: MessageRequest): MessageResponse {
        var response = MessageResponse()
        try {
            response = apiService.receivedMessageRequests(payload)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()

        }
        return response
    }

    override suspend fun acceptRejectRequest(payload: AcceptRejectRequest): AcceptRejectResponse {
        var response = AcceptRejectResponse()
        try {
            response = apiService.acceptRejectRequests(payload)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()

        }
        return response
    }

    override suspend fun accountAvailability(): AccountAvailabilityResponse {

        var response = AccountAvailabilityResponse()
        try {
            response = apiService.accountAvailability()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()
        }
        return response
    }

    override suspend fun removeLikes(removeLikes: RemoveLikes): BaseResponse {
        var response = BaseResponse()
        try {
            response = apiService.removeLikes(removeLikes)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()
        }
        return response
    }

    override suspend fun chatList(chatList: ChatList): MessageListResponse {
        var response = MessageListResponse()
        try {
            response = apiService.chatList(chatList)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()
        }
        return response
    }

    override suspend fun chatHistory(chatHistoryRequest: ChatHistoryRequest): ShowChatResponse {
        var response = ShowChatResponse()
        try {
            response = apiService.chatHistory(chatHistoryRequest)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()
        }
        return response
    }

    override suspend fun getHashtagPost(payload: HashtagPayload): PostBaseResponse {
        var response = PostBaseResponse()
        try {
            response = apiService.getHashtagPost(payload)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()

        }
        return response
    }

    override suspend fun deleteNotification(notification: DeleteNotification): BaseResponse {
        var response = BaseResponse()
        try {
            response = apiService.notificationDelete(notification)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()

        }
        return response
    }

    override suspend fun registerUser(payload: RegisterUserRequest): RegisterUserResponse {
        var response = RegisterUserResponse()
        try {
            response = apiService.registerUser(payload)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()

        }
        return response
    }


}



