package com.wedo8020.app.response

data class PostDetailsResponse(
    var data: Data?=null,
    var message: String?=null,
    var error: Int?=null,
    var success: Boolean=false
)

data class PostDetailData(
    val _id: String,
    val createdAt: String,
    val creator: Creators,
    val deletedAt: Any,
    val description: String?=null,
    val disappearTime: String,
    val expireTime: Any,
    val hashtags: List<Any>,
    val images: List<String>,
    val isLike: Boolean,
    val isNotInterested: Boolean,
    val isReported: Boolean,
    val isSaved: Boolean,
    val isSoundSaved: Boolean,
    val mentionUsers: List<MentionUsers>,
    val noOfComments: Int,
    val noOfLikes: Int,
    val noOfSaved: Int,
    val noOfShares: Int,
    val noOfViews: Int,
    val options: DetailsOptions,
    val pinTime: Any,
    val postId: String,
    val privacy: String,
    val repostedBy: Any,
    val tagUsers: ArrayList<TagUsers>,
    val type: String,
    val video: String?=null,
    val videoThumbnail: Any
)

data class Creators(
    val _id: String,
    val accessSetting: AccessSettings,
    val accountType: String,
    val blueTickEnable: Boolean,
    val followStatus: Int,
    val isOnline: Boolean,
    val name: String,
    val noOfFollowers: Int,
    val overallLikes: Int,
    val profile: String,
    val username: String
)

data class MentionUsers(
    val _id: String,
    val blueTickEnable: Boolean,
    val name: String,
    val profile: String,
    val username: String
)

data class DetailsOptions(
    val comment: Boolean,
    val download: Boolean,
    val like: Boolean,
    val repost: Boolean
)

data class TagUsers(
    val _id: String,
    val accountType: String,
    val blueTickEnable: Boolean,
    val followStatus: Int,
    val name: String,
    val noOfFollowers: Int,
    val overallLikes: Int,
    val overallPosts: Int,
    val profile: String,
    val username: String
)

data class AccessSettings(
    val collab: String,
    val message: String,
    val seeArchive: String,
    val seeFollower: String,
    val seeFollowing: String,
    val seeLikeGiven: String,
    val seeLikeReceived: String,
    val seeMyBio: String,
    val seeMyProfile: String,
    val seeTree: String,
    val tag: String
)