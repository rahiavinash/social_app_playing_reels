package com.wedo8020.app.response

data class ProfilePrivacyResponse(
    var data: ProfilePrivacyData?=null,
    var message: String?=null,
    var error: Int?=null,
    var success: Boolean=false,
)

data class ProfilePrivacyData(
    val _id: String,
    val accessSetting: ProfilePrivacyAccessSetting?,
    val accountType: String,
    val bio: String?=null,
    val bioMentionHashtags: List<Any>,
    val bioMentionUsers: ArrayList<BioMentionUserResponse>,
    val profile: String?=null,
    val recoveryCode: String
)

data class BioMentionUserResponse(
    val _id: String,
    val name: String,
    val username: String,
    val blueTickEnable: Boolean,
    val profile: String
)

data class ProfilePrivacyAccessSetting(
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