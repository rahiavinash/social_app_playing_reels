package com.wedo8020.app.response

data class MentionUserResponse(
    var data: ArrayList<MentionUserData>?=null,
    var message: String?=null,
    var error: Int?=null,
    var success: Boolean=false,
)

data class MentionUserData(
    val _id: String,
    val blueTickEnable: Boolean,
    val isOnline: Boolean,
    val name: String,
    val profile: Any,
    val username: String
)