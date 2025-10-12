package com.wedo8020.app.response

data class UserListResponse(
    var data: ArrayList<UserList>?=null,
    var message: String?=null,
    var error: Int?=null,
    var success: Boolean=false
)

data class UserList(
    val _id: String,
    val blueTickEnable: Boolean,
    val isCollabActive: Boolean,
    var isTagActive: Boolean,
    val mostTagged: Int,
    val name: String,
    val overallLikes: Int,
    val profile: String="",
    val username: String,
    var isTagSelected: Boolean
)
