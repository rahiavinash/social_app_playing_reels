package com.wedo8020.app.dataSources

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
import com.wedo8020.app.response.FriendListResponse
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

interface RemoteDataSource {
    suspend fun loginResponse(payload: LoginRequest): BaseResponse
    suspend fun postResponse(payload: PostRequest): PostBaseResponse
    suspend fun postRepostResponse(payload: PostRequest): PostBaseResponse
    suspend fun addCommentResponse(payload: CommentRequest): PostBaseResponse
    suspend fun searchListResponse(payload: SearchRequest): SearchBaseResponse
    suspend fun postShareUserResponse(payload: PostRequest): PostBaseResponse
    suspend fun notInterestedResponse(payload: PostRequest): PostBaseResponse
    suspend fun userRestrictResponse(payload: UserRestrict): PostBaseResponse

    suspend fun postRestrictResponse(postRestrict: PostRestrict): PostBaseResponse
    suspend fun postReportResponse(payload: PostRequest): PostBaseResponse
    suspend fun commentReportResponse(payload: PostRequest): PostBaseResponse
    suspend fun userReportResponse(payload: PostRequest): PostBaseResponse
    suspend fun profilePosts(data: Map<String, String>): ProfilePostResponse
    suspend fun savedPosts(profilePostRequest: ProfilePostRequest): SavedProfilePost
    suspend fun linkedAccount(payload:LinkedAccountRequest): LinkedAccountResponse
    suspend fun userDetails(data: Map<String, String>): UserDetailsResponse
    suspend fun userLogout(): BaseResponse
    suspend fun userDeleteAccount(): BaseResponse
    suspend fun postLikeResponse(payload: PostRequest): BaseResponse
    suspend fun userDecryptFile(payload: UserDecryptFile): BaseResponse
    suspend fun postSaveResponse(payload: PostRequest): BaseResponse
    suspend fun postSaveSoundResponse(payload: PostRequest): BaseResponse
    suspend fun postViewedResponse(payload: PostRequest): BaseResponse
    suspend fun followUnfollowUserResponse(payload: UserRequest): BaseResponse
    suspend fun postFollowRequestStatusResponse(payload: UserRequest): BaseResponse
    suspend fun switchNotificationResponse(payload: SwitchNotificationRequest): BaseResponse
    suspend fun commentListResponse(payload: PostRequest): CommentBaseResponse
    suspend fun commentLikeResponse(payload: PostRequest): CommentBaseResponse
    suspend fun PostPin(payload: PostPinRequest): BaseResponse
    suspend fun userList(payload: UserListRequest): UserListResponse
    suspend fun DeletePost(payload: DeletePost): BaseResponse

    suspend fun deleteComment(payload : DeleteCommentRequest) : BaseResponse

    suspend fun pinComment(payload: PinCommentRequest) : BaseResponse
    suspend fun postSoundPin(payload: SoundPinRequest): BaseResponse
    suspend fun EditPost(payload: EditPost): BaseResponse
    suspend fun postFcmToken(payload: FcmRequest): BaseResponse
    suspend fun ArchivePost(payload: DeletePost): BaseResponse
    suspend fun UnArchivePost(payload: DeletePost): BaseResponse
    suspend fun PostDetail(payload: DeletePost): PostDetailsResponse
    suspend fun mentionUser(payload: mentionUserRequest): MentionUserResponse

    suspend fun userProfilePrivacy(): ProfilePrivacyResponse

    suspend fun userProfileAccount(): ProfileAccountResponse
    suspend fun userProfileNotification(): ProfileNotificationResponse
    suspend fun editPrivacy(editPrivacy: EditPrivacy): BaseResponse
    suspend fun finalEditPrivacy(editPrivacy: EditProfilePrivacy): BaseResponse
    suspend fun editUserAccount(data :Map<String, String>): BaseResponse
    suspend fun getUserExperience(): UserExperienceResponse
    suspend fun getInterest(interestRequest: InterestRequest): InterestResponse
    suspend fun updateExperience(interestRequest: ExperienceRequest): BaseResponse
    suspend fun editUserNotification(userNotificationRequest: UserNotificationRequest): BaseResponse
    suspend fun restrictReportUser(restrictReportRequest: RestrictReportRequest): RestrictResponse
    suspend fun userBlockedList(blockedRequest: BlockedRequest): BlockedResponse

    suspend fun updateInterest(updateInterestRequest: UpdateInterestRequest): BaseResponse
    suspend fun createPost(payload: CreatePost) : BaseResponse
    suspend fun createPostWithMedia(payload: CreatePostWithMedia) : BaseResponse

    suspend fun hashTagList(payload: HashtagPayload) : HashTagListResponse
    suspend fun userFeedBack(feedBackRequest: FeedBackRequest) : BaseResponse
    suspend fun userLikeList(likeListRequest: LikeListRequest) : LikeListResponse

    suspend fun userBlocked(userBlockedRequest: UserBlockedRequest) : BaseResponse
    suspend fun friendList(friendListRequest: FriendListRequest) : FriendListResponse
    suspend fun followingList(followingListRequest: FollowingListRequest) : FriendListResponse
    suspend fun followerList(followerListRequest: FollowerListRequest) : FriendListResponse
    suspend fun notificationList(notificationRequest: NotificationRequest) : NotificationListResponse
    suspend fun downloadMyData() : DownloadMyDataResponse

    suspend fun premiumPurchasesApi(payload : PremiumPurchasePayLoad) : BaseResponse
    suspend fun purchaseTreeApi(payload : PurchaseTreesPayload) : BaseResponse

    suspend fun purchaseSrcApi(payload : PurchaseSrcPayload) : BaseResponse
    suspend fun removeFollower(payload : UserRequest) : BaseResponse

    suspend fun getPurchaseDetails() : PurchaseDetails

    suspend fun getCountryList() : CountryListImpactScreen

    suspend fun getImpactDetails() : ImpactDetail
    suspend fun getPrizeDetails() : PrizeDetailsResponse

    suspend fun getUserDetails(payload : UserListRequest) : UserDataList

    suspend fun getPurchaseHistory(payload : UserListRequest) : PurchaseHistoryList

    suspend fun sendSrc(payload : TransferSrcDetails) : BaseResponse

    suspend fun getLeaderBoardDetails(payload : LeaderboardRequest) : LeaderboardResponse
    suspend fun getHashtagPost(payload : HashtagPayload) : PostBaseResponse
    suspend fun deleteNotification(notification: DeleteNotification) : BaseResponse

    suspend fun getMostPlantedTrees(payload : MostTreesPlantedRequest) : MostTreesPlantedResponse

    suspend fun getMostPlantedTreesByRegion(payload : RegionRequest) : MostTreesPlantedByRegionResponse

    suspend fun registerUser(payload : RegisterUserRequest) : RegisterUserResponse

    suspend fun getDropsDetails(payload : DropsDataRequest) : DropsDataResponse
    suspend fun chatImageUpload(payload : ChatImageUpload) : BaseResponse

    suspend fun recoveryCode(payload : RecoveryCodeRequest) :RecoveryCodeResponse

    suspend fun sentMessageRequest(payload: MessageRequest) : MessageResponse

    suspend fun receivedMessageRequest(payload: MessageRequest) : MessageResponse

    suspend fun  acceptRejectRequest(payload: AcceptRejectRequest) : AcceptRejectResponse

    suspend fun accountAvailability() : AccountAvailabilityResponse
    suspend fun removeLikes(removeLikes: RemoveLikes) : BaseResponse
    suspend fun chatList(chatList: ChatList) : MessageListResponse
    suspend fun chatHistory(chatHistoryRequest: ChatHistoryRequest) : ShowChatResponse

}