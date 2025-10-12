package com.wedo8020.app.request

data class PostRequest(
    var offset: Int = 0,
    val limit: Int = 10,
    var search: String = "",
    val type: String = "",
    val post: String = "",
    val user: String = "",
    val sound: String = "",
    val disappearTime: String = "",
    val repostTo: String = "",
    val id: String? = null,
    val reason: String? = null,
    val comment: String? = null,
    val topComment: String? = null
)

data class SearchRequest(
    var offset: Int = 0,
    val limit: Int = 10,
    val videoOffset: Int = 10,
    val videoLimit: Int = 10,
    var search: String = "",
    val type: String = "post",
    val searchPro: Boolean = true,
)

data class RestrictReportRequest(
    var offset: Int = 0,
    val limit: Int = 100,
    var search: String = "",
    val type: String = "",

    )

data class ChatHistoryRequest(
    var offset: Int = 0,
    val limit: Int = 100,
    val user: String = "",
)

data class NotificationRequest(
    var offset: Int = 0,
    val limit: Int = 100,
)

data class ChatList(
    var offset: Int = 0,
    var search: String = "",
    val limit: Int = 100,
)

data class RemoveLikes(
    var user: String = "",
)

data class DeleteNotification(
    val notification: String? = null
)

data class DeleteCommentRequest(val comment: String? = null, val commentDetails: String? = null)

data class PinCommentRequest(val comment: String? = null)
data class FriendListRequest(
    var offset: Int = 0,
    val limit: Int = 100,
    val order: Int = 1,
    val search: String = "",
)

data class FollowingListRequest(
    var offset: Int = 0,
    val limit: Int = 100,
    val order: Int = 1,
    val search: String = "",
    val sort: String = "",
    val user: String? = null
)

data class FollowerListRequest(
    var offset: Int = 0,
    val limit: Int = 100,
    val order: Int = 1,
    val search: String = "",
    val sort: String = "",
    val user: String? = null
)

data class BlockedRequest(
    var offset: Int = 0,
    val limit: Int = 100,
    val order: Int = -1,
    val search: String = "",
)

data class FeedBackRequest(
    var title: String = "",
    val message: String = "",
)

data class UserDecryptFile(
    var url: String = "",
    val type: String = "",
)

data class InterestRequest(
    val country: String? = null,
)
data class CommentRequest(
    val post: String = "",
    val description: String = "",

    val mentionUsers: ArrayList<String>? = null,
    val parentComment: String? = null
)

data class UserListRequest(
    var offset: Int = 0,
    val limit: Int = 10,
    var search: String = "",
)

data class RegionRequest(
    var offset: Int = 0,
    val limit: Int = 100,
    var search: String = "",
    var filterDate : String ?= null
)

data class UserRequest(
    var id: String = "",
    var status: String = ""
)
data class LinkedAccountRequest(
    var limit: Int = 0,
    var offset: Int = 0,
    var search: String = "",
    var order: Int = 0

)
data class PostRestrict(
    var post: String = "",
    var user: String = ""
)

data class UserRestrict(
    var id: String = ""
)

data class ProfilePostRequest(
    var offset: Int = 0,
    val limit: Int = 10,
    val type: String = "",
)

data class PostPinRequest(
    val type: String = "",
    val post: String = "",
)

data class DeletePost(
    val post: String = "",
)

data class UserBlockedRequest(
    val user: String = "",
)

data class SwitchNotificationRequest(
    val user: String = ""
)

data class SoundPinRequest(
    val sound: String = "",
)

data class LikeListRequest(
    var offset: Int = 0,
    val limit: Int = 10,
    val order: Int = -1,
    val search: String = "",
    val type: String = "",
    val user: String? = null
)

data class mentionUserRequest(
    var offset: Int = 0,
    val limit: Int = 100,
    val search: String = ""
)

data class FcmRequest(
    var fcmToken: String? = ""
)

data class EditPost(
    val disappearTime: String = "",
    val type: String = "",
    val post: String = "",
    val description: String = "",
    val tagUsers: ArrayList<String>? = null,
    val mentionUsers: ArrayList<String>? = null,
    val collabUser: String? = null,
    val options: EditOptions? = null,
    val privacy: String = "",
    val images: String = "",
)


data class EditProfilePrivacy(
    val accountType: String,
    val bioMentionUsers: ArrayList<String>? = null,
    val profile: String,
    val bio: String,
    val accessSetting: AccessSettings
)

data class AccessSettings(
    val seeArchive: String,
    val seeLikeGiven: String,
    val seeMyBio: String,
    val seeMyProfile: String,
    val seeFollowing: String,
    val seeFollower: String,
    val seeLikeReceived: String,
    val message: String,
    val collab: String,
    val tag: String,
    val seeTree: String? = null// Assuming seeTree is part of the structure
)

data class EditPrivacy(
    val accountType: String = "",
    val bio: String = "",
    val accessSetting: AccessSetting? = null,
    val media: ArrayList<ProfileImage>? = null,
)

data class AccessSetting(
    val seeArchive: String = "",
    val seeLikGiven: String = "",
    val seeMyBio: String = "",
    val seeMyProfile: String = "",
    val seeFollowing: String = "",
    val seeFollower: String = "",
    val seeMyReceived: String = "",
    val message: String = "",
    val collab: String = "",
    val tag: String = "",
)

data class ProfileImage(
    val ext: String = "",
    val file: String = "",
)

data class EditOptions(
    val comment: Boolean,
    val download: Boolean,
    val like: Boolean,
    val repost: Boolean
)


data class UserNotificationRequest(
    val setting: Setting? = null
)

data class Setting(
    val accountIFollow: AccountIFollow,
    val followRequest: Boolean,
    val showInNotification: Boolean,
    val myFollower: MyFollower,
    val myFriend: MyFriend,
    val anyOne: AnyOne,
)

data class AccountIFollow(
    val likeMyPost: Boolean,
    val commented: Boolean,
    val mentionedMe: Boolean,
    val taggedMe: Boolean
)

data class MyFollower(
    val likeMyPost: Boolean,
    val commented: Boolean,
    val mentionedMe: Boolean,
    val taggedMe: Boolean
)

data class MyFriend(
    val likeMyPost: Boolean,
    val commented: Boolean,
    val mentionedMe: Boolean,
    val taggedMe: Boolean
)

data class AnyOne(
    val likeMyPost: Boolean,
    val commented: Boolean,
    val mentionedMe: Boolean,
    val taggedMe: Boolean
)

data class EditAccountRequest(
    val name: String? = "",
    val username: String? = "",
    val currentPassword: String? = "",
    val newPassword: String? = "",
)

data class UpdateInterestRequest(
    val categories: ArrayList<String>? = null,
    val fromSetting: Boolean = false,
)

data class ExperienceRequest(
    val setting: Settings? = null,
    val filterWords: String? = "",
)


data class Settings(
    val filterWord: Boolean? = null,
    val breakReminder: Boolean? = null,
    val isRestrictedMode: Boolean? = null,
)

data class CreatePost(
    val disappearTime: String? = null,
    val type: String? = null,
    val options: Options? = null,
    val description: String? = null,
    val privacy: String? = null,
    val country: String? = null,
    var mentionUsers :  ArrayList<String>? = arrayListOf(),
    var tagUsers: ArrayList<String>? = arrayListOf()
)

data class Options(
    val download: Boolean,
    val comment: Boolean,
    val repost: Boolean? = null,
    val like: Boolean
)

data class CreatePostWithMedia(
    val post: String? = null,
    val seekInput: Double? = null,
    val media: ArrayList<MediaData>? = null
)


data class MediaData(val ext: String? = null, val file: String? = null)

//    : Parcelable {
//
//    // Implementing Parcelable methods
//    override fun describeContents(): Int {
//        return 0
//    }
//
//    override fun writeToParcel(dest: Parcel, flags: Int) {
//        dest.writeString(ext)
//        dest.writeString(file)
//    }
//
//    companion object CREATOR : Parcelable.Creator<MediaData> {
//        override fun createFromParcel(parcel: Parcel): MediaData {
//            val ext = parcel.readString()
//            val file = parcel.readString()
//            return MediaData(ext, file)
//        }
//
//        override fun newArray(size: Int): Array<MediaData?> {
//            return arrayOfNulls(size)
//        }
//    }
//}
data class HashTagListResponse(
    val success: Boolean? = null,
    var message: String? = null,
    val data: ArrayList<Hashtag>? = null
)

data class Hashtag(
    val _id: String,
    val tag: String
)

data class HashtagPayload(
    var offset: Int = 0,
    val limit: Int = 10,
    var search: String = "",
    var hashtag: String? = null
)


data class PremiumPurchasePayLoad(
    val blueTickEnable: Boolean? = null,
    val aiEnable: Boolean? = null,
    val noAdsEnable: Boolean? = null,
    val localizedPrice: String? = null,
    val receipt: String? = null,
    val amount: Double? = null,
    val transactionId: String? = null,
    val productId: String? = null
)


data class PurchaseTreesPayload(
    val amount: Double? = null, val tree: Int? = null, val receipt: String? = null,
    val localizedPrice: String? = null, val country: String? = null
)


data class PurchaseSrcPayload(
    val wd: Int? = null, val amount: Double? = null, val receipt: String? = null,
    val localizedPrice: String? = null
)


data class PurchaseDetails(
    val success: Boolean? = null,
    var message: String? = null,
    val data: SubscriptionData? = null
)

data class SubscriptionData(
    val blueTick: SubscriptionDetail,

    val noAds: SubscriptionDetail,
    val ai: SubscriptionDetail? = null
)

data class SubscriptionDetail(
    val _id: String,
    val blueTickEnable: Boolean,
    val noAdsEnable: Boolean,
    val aiEnable: Boolean,
    val createdAt: String,
    val expireTime: String?, // Change the type if it's not a String for expiration date
    val isExpire: Boolean
)







