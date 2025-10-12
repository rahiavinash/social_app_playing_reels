package com.wedo8020.app.response

data class DeleteMessageResponse(
    val code: Int = 0,
    val message: String = "",
    val messageId: String = "",
    val success: Boolean = false
)