package com.wedo8020.app.response

data class RestrictResponse(
    var data: ArrayList<UserData>?=null,
    var message: String?=null,
    var error: Int?=null,
    var success: Boolean=false
)

data class UserData(
    val _id: String,
    val blueTickEnable: Boolean,
    val createdAt: String,
    val creator: CreatorData ?=null,
    val images: List<String>,
    val isRestricted: Boolean,
    val name: String,
    val overallLikes: Int,
    val profile: String?=null,
    val reportedType: String,
    val type: String,
    val username: String,
    val video: Any,
    val reason: String,
    val videoThumbnail: String?=null
)

data class CreatorData(
    val _id: String,
    val blueTickEnable: Boolean,
    val name: String,
    val overallLikes: Int,
    val profile: String ="",
    val username: String
)