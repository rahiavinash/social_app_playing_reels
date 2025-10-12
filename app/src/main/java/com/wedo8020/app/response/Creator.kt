package com.wedo8020.app.response

import java.io.Serializable

data class Creator(
    val _id: String,
    val accessSetting: AccessSetting,
    val accountType: String,
    var followStatus: Int,
    val isOnline: Boolean,
    val name: String,
    val noOfFollowers: Int,
    var overallLikes: Int,
    val profile: String = "",
    val username: String,
    val blueTickEnable: Boolean,
):Serializable