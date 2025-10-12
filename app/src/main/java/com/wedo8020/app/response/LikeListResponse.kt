package com.wedo8020.app.response


data class LikeListResponse(
    var data: ArrayList<LikeListResponseItem>?=null,
    var message: String?=null,
    var error: Int?=null,
    var success: Boolean=false,
    var postType: String? = null
)
data class LikeListResponseItem(
    val _id: String,
    val accountType: String,
    val actionLikes: Int,
    val blueTickEnable: Boolean,
    val followStatus: Int,
    val isFriend: Boolean,
    val isOnline: Boolean,
    val isOtherFollowing: Boolean,
    val name: String,
    val overallLikes: Int,
    val profile: String?=null,
    val username: String
)