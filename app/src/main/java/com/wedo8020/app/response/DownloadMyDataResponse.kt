package com.wedo8020.app.response

data class DownloadMyDataResponse(
    val `data`: String="",
    var message: String?=null,
    var error: Int?=null,
    val success: Boolean=false
)