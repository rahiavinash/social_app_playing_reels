package com.wedo8020.app.response

data class FriendListResponse(
    var data: ArrayList<FriendListData>?=null,
    var message: String?=null,
    var error: Int?=null,
    var success: Boolean=false,
)

data class FriendListData(
    val _id: String,
    val accountType: String,
    val blueTickEnable: Boolean,
    var followStatus: Int,
    val isFriend: Boolean,
    val name: String,
    val noOfLikes: Int,
    val overallLikes: Int,
    var profile: String?=null,
    val username: String
)