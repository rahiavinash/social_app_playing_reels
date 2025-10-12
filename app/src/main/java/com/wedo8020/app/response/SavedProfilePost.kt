package com.wedo8020.app.response

data class SavedProfilePost(
    var data: ArrayList<Data>?=null,
    var message: String?=null,
    var error: Int?=null,
    var success: Boolean=false,

    )


data class ProfilePostData(
    val _id: String,
    val description: String?,
    val tagUsers: List<String>,
    val mentionUsers: List<String>,
    val options: PostOptions,
    val type: String,
    val sound: String,
    val expireTime: String?,
    val disappearTime: String,
    val creator: PostCreator,
    val privacy: String,
    val initialPrivacy: String?,
    val hashtags: List<String>,
    val repostedBy: String?,
    val deletedAt: String?,
    val createdAt: String,
    val hasSound: Boolean,
    val postId: String,
    val pinTime: String?,
    var video: String,
    val videoThumbnail: String,
    var images: List<String>,
    val noOfViews: Int,
    val noOfLikes: Int,
    val noOfComments: Int,
    val noOfSaved: Int,
    val noOfShares: Int,
    val isSaved: Boolean,
    val isSoundSaved: Boolean,
    val isLike: Boolean,
    val isReported: Boolean,
    val isNotInterested: Boolean,
    var isSelect: Boolean
)

data class PostOptions(
    val like: Boolean,
    val repost: Boolean,
    val comment: Boolean,
    val download: Boolean
)

data class PostCreator(
    val _id: String,
    val name: String,
    val username: String,
    val accountType: String,
    val accessSetting: PostAccessSetting,
    val blueTickEnable: Boolean,
    val profile: String?,
    val overallLikes: Int,
    val followers: List<String>,
    val blockedUsers: List<String>,
    val followStatus: Int,
    val noOfFollowers: Int,
    val isOnline: Boolean
)

data class PostAccessSetting(
    val tag: String,
    val collab: String,
    val message: String,
    val seeTree: String,
    val seeLikeGiven: String,
    val seeLikeReceived: String,
    val seeFollower: String,
    val seeFollowing: String,
    val seeMyBio: String,
    val seeMyProfile: String,
    val seeArchive: String
)
