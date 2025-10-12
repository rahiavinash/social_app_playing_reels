package com.wedo8020.app.response

data class FollowingListResponse(
    val `data`: List<Data>,
    val message: String,
    val success: Boolean
)

data class FollowingListData(
    val _id: String,
    val accountType: String,
    val blueTickEnable: Boolean,
    val followStatus: Int,
    val isFriend: Boolean,
    val name: String,
    val noOfLikes: Int,
    val overallLikes: Int,
    val profile: String,
    val username: String
)