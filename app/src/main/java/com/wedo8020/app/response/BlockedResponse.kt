package com.wedo8020.app.response

data class BlockedResponse(
    var data: ArrayList<BlockedUserData>? = null,
    var message: String? = null,
    var error: Int? = null,
    var success: Boolean = false
)

data class BlockedUserData(
    val _id: String,
    val accountType: String,
    val blueTickEnable: Boolean,
    val name: String,
    val overallLikes: Int,
    val profile: String = "",
    val username: String
)






