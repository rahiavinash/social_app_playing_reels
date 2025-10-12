package com.wedo8020.app.response

data class Options(
    val comment: Boolean = false,
    val download: Boolean = false,
    val like: Boolean=false,
    val repost: Boolean=false
)