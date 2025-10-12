package com.wedo8020.app.repo

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

interface RemoteRepositary {
    fun login(loginRequest: LoginRequest,
    onResult:(isSuccess: Boolean, baseResponse: BaseResponse) -> Unit
              )
    fun post(postRequest: PostRequest,
    onResult:(isSuccess: Boolean, baseResponse: PostBaseResponse) -> Unit
              )
    fun postShareUser(postRequest: PostRequest,
    onResult:(isSuccess: Boolean, baseResponse: PostBaseResponse) -> Unit
              )
    fun notInterested(postRequest: PostRequest,
    onResult:(isSuccess: Boolean, baseResponse: PostBaseResponse) -> Unit
              )
    fun userRestrict(userRestrict: UserRestrict,
                     onResult:(isSuccess: Boolean, baseResponse: PostBaseResponse) -> Unit
              )

    fun postRestrict(postRestrict: PostRestrict,
                     onResult:(isSuccess: Boolean, baseResponse: PostBaseResponse) -> Unit
    )
    fun postReport(postRequest: PostRequest,
    onResult:(isSuccess: Boolean, baseResponse: PostBaseResponse) -> Unit
              )
    fun commentReport(postRequest: PostRequest,
    onResult:(isSuccess: Boolean, baseResponse: PostBaseResponse) -> Unit
              )

    fun userReport(postRequest: PostRequest,
                   onResult:(isSuccess: Boolean, baseResponse: PostBaseResponse) -> Unit
    )
    fun postRepost(postRequest: PostRequest,
    onResult:(isSuccess: Boolean, baseResponse: PostBaseResponse) -> Unit
              )
    fun addComment(postRequest: CommentRequest,
    onResult:(isSuccess: Boolean, baseResponse: PostBaseResponse) -> Unit
              )
    fun searchList(postRequest: SearchRequest,
    onResult:(isSuccess: Boolean, baseResponse: SearchBaseResponse) -> Unit
              )

    fun profilePost(data: Map<String, String>,
             onResult:(isSuccess: Boolean, baseResponse: ProfilePostResponse) -> Unit
    )
    fun savedPost(profilePostRequest: ProfilePostRequest,
                    onResult:(isSuccess: Boolean, baseResponse: SavedProfilePost) -> Unit
    )
    fun userDeatils(data: Map<String, String>, onResult:(isSuccess: Boolean, baseResponse: UserDetailsResponse) -> Unit
    )

    fun userLogout(onResult:(isSuccess: Boolean, baseResponse: BaseResponse) -> Unit
    )
    fun userDeleteAccount(onResult:(isSuccess: Boolean, baseResponse: BaseResponse) -> Unit
    )
    fun postLike(postRequest: PostRequest,
                 onResult:(isSuccess: Boolean, baseResponse: BaseResponse) -> Unit
    )
    fun postSave(postRequest: PostRequest,
                 onResult:(isSuccess: Boolean, baseResponse: BaseResponse) -> Unit
    )
    fun postSaveSound(postRequest: PostRequest,
                 onResult:(isSuccess: Boolean, baseResponse: BaseResponse) -> Unit
    )

    fun userDecrypt(userDecryptFile: UserDecryptFile,
                      onResult:(isSuccess: Boolean, baseResponse: BaseResponse) -> Unit
    )
    fun postViewed(postRequest: PostRequest,
                 onResult:(isSuccess: Boolean, baseResponse: BaseResponse) -> Unit
    )
    fun followUnfollowUser(postRequest: UserRequest,
                 onResult:(isSuccess: Boolean, baseResponse: BaseResponse) -> Unit
    )
    fun postFollowRequestStatus(postRequest: UserRequest,
                 onResult:(isSuccess: Boolean, baseResponse: BaseResponse) -> Unit
    )
    fun switchNotification(postRequest: SwitchNotificationRequest,
                 onResult:(isSuccess: Boolean, baseResponse: BaseResponse) -> Unit
    )
    fun commentList(postRequest: PostRequest,
                 onResult:(isSuccess: Boolean, baseResponse: CommentBaseResponse) -> Unit
    )
    fun commentLike(postRequest: PostRequest,
                 onResult:(isSuccess: Boolean, baseResponse: CommentBaseResponse) -> Unit
    )

    fun postPin(postPinRequest: PostPinRequest,
                onResult:(isSuccess: Boolean, baseResponse: BaseResponse) -> Unit
    )
    fun DeletePost(deletePost: DeletePost,
                onResult:(isSuccess: Boolean, baseResponse: BaseResponse) -> Unit
    )
    fun postSoundPin(soundPinRequest: SoundPinRequest,
                   onResult:(isSuccess: Boolean, baseResponse: BaseResponse) -> Unit
    )
    fun ArchivePost(deletePost: DeletePost,
                   onResult:(isSuccess: Boolean, baseResponse: BaseResponse) -> Unit
    )

    fun unArchivePost(deletePost: DeletePost,
                    onResult:(isSuccess: Boolean, baseResponse: BaseResponse) -> Unit
    )

    fun userList(userListRequest: UserListRequest,
                    onResult:(isSuccess: Boolean, baseResponse: UserListResponse) -> Unit
    )

    fun postDetails(deletePost: DeletePost ,
                 onResult:(isSuccess: Boolean, baseResponse: PostDetailsResponse) -> Unit
    )

    fun EditPost(editPost: EditPost ,
                    onResult:(isSuccess: Boolean, baseResponse: BaseResponse) -> Unit
    )
    fun postFcmToken(payload: FcmRequest ,
                    onResult:(isSuccess: Boolean, baseResponse: BaseResponse) -> Unit
    )

    fun MentionUser(mentionUserRequest: mentionUserRequest ,
                 onResult:(isSuccess: Boolean, baseResponse: MentionUserResponse) -> Unit
    )

    fun userProfilePrivacy(onResult:(isSuccess: Boolean, baseResponse: ProfilePrivacyResponse) -> Unit
    )

    fun accountAvailability(onResult:(isSuccess: Boolean, response: AccountAvailabilityResponse) -> Unit
    )

    fun userProfileAccount(onResult:(isSuccess: Boolean, baseResponse: ProfileAccountResponse) -> Unit
    )

    fun userProfileNotification(onResult:(isSuccess: Boolean, baseResponse: ProfileNotificationResponse) -> Unit
    )

    fun getUserExperience(onResult:(isSuccess: Boolean, baseResponse: UserExperienceResponse) -> Unit
    )

    fun getUserInterest(interestRequest: InterestRequest, onResult:(isSuccess: Boolean, baseResponse: InterestResponse) -> Unit
    )
    fun linkedAccount(linkedAccountRequest: LinkedAccountRequest, onResult:(isSuccess: Boolean, baseResponse: LinkedAccountResponse) -> Unit
    )
    fun saveUserInterest(interestRequest: ExperienceRequest, onResult:(isSuccess: Boolean, baseResponse: BaseResponse) -> Unit
    )
    fun userEditProfilePrivacy( editPrivacy: EditPrivacy,onResult:(isSuccess: Boolean, baseResponse: BaseResponse) -> Unit)
    fun editPrivacyFromProfile(editPrivacy: EditProfilePrivacy, onResult:(isSuccess: Boolean, baseResponse: BaseResponse) -> Unit)

    fun userEditProfileAccount(data :Map<String, String>,onResult:(isSuccess: Boolean, baseResponse: BaseResponse) -> Unit
    )fun userEditProfileNotification( userNotificationRequest: UserNotificationRequest,onResult:(isSuccess: Boolean, baseResponse: BaseResponse) -> Unit
    )
    fun createPost(createPost : CreatePost, onResult: (isSuccess: Boolean, baseResponse: BaseResponse) -> Unit)
    fun createPostWithMedia(payload: CreatePostWithMedia, onResult:
        (isSuccess: Boolean, baseResponse: BaseResponse)-> Unit)

    fun hashTagList(payload: HashtagPayload, onResult:
        (isSuccess: Boolean, baseResponse: HashTagListResponse)-> Unit)

    fun userBlockedLists( blockedRequest: BlockedRequest,onResult:(isSuccess: Boolean, baseResponse: BlockedResponse) -> Unit
    )
    fun userRestrictReportList(  restrictReportRequest: RestrictReportRequest,onResult:(isSuccess: Boolean, baseResponse: RestrictResponse) -> Unit
    )

    fun updateInterest(  updateInterestRequest: UpdateInterestRequest,onResult:(isSuccess: Boolean, baseResponse: BaseResponse) -> Unit
    )
    fun userFeedBack(  feedBackRequest: FeedBackRequest,onResult:(isSuccess: Boolean, baseResponse: BaseResponse) -> Unit
    )
    fun downloadMyData(  onResult:(isSuccess: Boolean, baseResponse: DownloadMyDataResponse) -> Unit
    )
    fun userBlock( userBlockedRequest: UserBlockedRequest, onResult:(isSuccess: Boolean, baseResponse: BaseResponse) -> Unit
    )
    fun userLikeList( likeListRequest: LikeListRequest, onResult:(isSuccess: Boolean, baseResponse: LikeListResponse) -> Unit
    )

    fun friendList( friendListRequest: FriendListRequest, onResult:(isSuccess: Boolean, baseResponse: FriendListResponse) -> Unit
    )

    fun followerList( followerListRequest: FollowerListRequest, onResult:(isSuccess: Boolean, baseResponse: FriendListResponse) -> Unit
    )
    fun followingList( followingListRequest: FollowingListRequest, onResult:(isSuccess: Boolean, baseResponse: FriendListResponse) -> Unit
    )
    fun premiumPurchasesApi(premiumPurchase: PremiumPurchasePayLoad,
                            onResult:(isSuccess: Boolean, baseResponse: BaseResponse) ->Unit)
    fun purchaseTreeApi(purchaseTrees: PurchaseTreesPayload,
                        onResult:(isSuccess: Boolean, baseResponse: BaseResponse) ->Unit)
    fun purchaseSrcApi(purchaseSrc: PurchaseSrcPayload,
                       onResult:(isSuccess: Boolean, baseResponse: BaseResponse) ->Unit)

    fun getUserDetails(userList: UserListRequest,
                       onResult:(isSuccess: Boolean, baseResponse: UserDataList) ->Unit)

    fun getPurchaseHistory(userList: UserListRequest,
                       onResult:(isSuccess: Boolean, baseResponse: PurchaseHistoryList) ->Unit)

    fun getLeaderBoardDetails(leaderBoardList: LeaderboardRequest,
                           onResult:(isSuccess: Boolean, leaderBoardResponse: LeaderboardResponse) ->Unit)


    fun getMostPlantedTrees(payload: MostTreesPlantedRequest,
                              onResult:(isSuccess: Boolean, response: MostTreesPlantedResponse) ->Unit)
    fun getMostPlantedTreesByRegion(payload: RegionRequest,
                            onResult:(isSuccess: Boolean, response: MostTreesPlantedByRegionResponse) ->Unit)
    fun getDropsDetails(dropsList: DropsDataRequest,
                              onResult:(isSuccess: Boolean, dropsResponse: DropsDataResponse) ->Unit)

    fun notificationList(notificationRequest: NotificationRequest,
                              onResult:(isSuccess: Boolean, baseResponse:NotificationListResponse) ->Unit)
    fun getHashtagPost(payload: HashtagPayload,
                           onResult:(isSuccess: Boolean, postBaseResponse: PostBaseResponse) ->Unit)
    fun sendSrc(details: TransferSrcDetails,
                           onResult:(isSuccess: Boolean, baseResponse: BaseResponse) ->Unit)


    fun registerUser(details: RegisterUserRequest,
                onResult:(isSuccess: Boolean, response: RegisterUserResponse) ->Unit)
    fun removeFollower(purchaseSrc: UserRequest,
                       onResult:(isSuccess: Boolean, baseResponse: BaseResponse) ->Unit)
    fun getPurchaseDetails(onResult:(isSuccess: Boolean, response: PurchaseDetails) -> Unit
    )

    fun getCountryDetails(onResult:(isSuccess: Boolean, response: CountryListImpactScreen) -> Unit)

    fun getImpactDetails(onResult:(isSuccess: Boolean, response: ImpactDetail) -> Unit)

    fun getPrizeDetails(onResult:(isSuccess: Boolean, response: PrizeDetailsResponse) -> Unit)

    fun deleteNotification(notification: DeleteNotification,
                    onResult:(isSuccess: Boolean, baseResponse: BaseResponse) -> Unit
    )
    fun deleteComment(comment: DeleteCommentRequest,
                           onResult:(isSuccess: Boolean, baseResponse: BaseResponse) -> Unit
    )

    fun pinComment(pinComment: PinCommentRequest,
                           onResult:(isSuccess: Boolean, baseResponse: BaseResponse) -> Unit
    )
    fun chatImageUpload(chatImageUpload: ChatImageUpload ,
                           onResult:(isSuccess: Boolean, baseResponse: BaseResponse) -> Unit
    )

    fun recoveryCode(request:RecoveryCodeRequest,
                           onResult:(isSuccess: Boolean, response: RecoveryCodeResponse) -> Unit
    )

    fun sentMessagesRequest(request: MessageRequest, onResult: (isSuccess: Boolean, baseResponse: MessageResponse) -> Unit)

    fun receivedMessagesRequest(request: MessageRequest, onResult: (isSuccess: Boolean, baseResponse: MessageResponse) -> Unit)

    fun removeLikes(removeLikes: RemoveLikes, onResult: (isSuccess: Boolean, baseResponse: BaseResponse) -> Unit)

    fun acceptRejectRequests(request: AcceptRejectRequest, onResult: (isSuccess: Boolean, baseResponse: AcceptRejectResponse) -> Unit)
    fun chatList(chatList: ChatList, onResult: (isSuccess: Boolean, baseResponse: MessageListResponse) -> Unit)

    fun chatHistory(chatHistoryRequest: ChatHistoryRequest, onResult: (isSuccess: Boolean, baseResponse: ShowChatResponse) -> Unit)
}