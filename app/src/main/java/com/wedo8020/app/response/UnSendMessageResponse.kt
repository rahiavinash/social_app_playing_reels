package com.wedo8020.app.response

data class UnSendMessageResponse(
    val code: Int,
    val `data`: UnSendData,
    val message: String,
    val success: Boolean
)

data class UnSendData(
    val isMessageHide: Boolean = false,
    val messageId: String = "",
    val userId: String = ""
)