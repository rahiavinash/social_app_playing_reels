package com.wedo8020.app.response

data class UserDetailsResponse(
    var data: UserDetailData?=null,
    var message: String?=null,
    var error: Int?=null,
    var success: Boolean=false,
    var postType: String? = null

)

data class UserDetailData(
    val _id: String,
    val accessSetting: UserDetailAccessSetting? = null,
    val accountType: String,
    val bio: String?=null,
    val bioMentionHashtags: List<Hashtag>?,
    val bioMentionUsers: List<BioMentionUser>,
    val blueTickEnable: Boolean?,
    val deletedAt: Any,
    val dob: String,
    val eTrees: Int,
    val email: String,
    val experienceSetting: ExperienceSetting,
    val name: String,
    val noOfFollowers: Int,
    val noOfFollowings: Int,
    val noOfFriends: Int,
    val overallGivenLikes: Int,
    val overallLikes: Int,
    val profile: String ="",
    val readReceipt: Boolean,
    val selectedCategories: List<String>,
    val username: String,
    var followStatus: String?=null,
    val isOtherFollowing: Boolean,
    var isNotificationOn: Boolean,
    val isMessageOn: Boolean,
    var isBlock: Boolean,
    val isFriend: Boolean
)

data class UserDetailAccessSetting(
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

data class BioMentionHashtag(
    val _id: String,
    val tag: String
)

data class BioMentionUser(
    val _id: String,
    val blueTickEnable: Boolean,
    val name: String,
    val profile: String,
    val username: String
)

data class ExperienceSetting(
    val breakReminder: Boolean,
    val filterWord: Boolean,
    val isRestrictedMode: Boolean
)