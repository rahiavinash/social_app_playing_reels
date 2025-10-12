package com.wedo8020.app.response

import java.io.Serializable

data class MentionUser(
    val _id: String="",
    val name: String="",
    val profile: String="",
    val username: String=""
):Serializable