package com.wedo8020.app.response

data class MessageCountResponse(
    val code: Int,
    val `data`: MessageAndNotificationCountData,
    val success: Boolean
)

data class MessageAndNotificationCountData(
    val messages: Int,
    val notifications: Int,
    val socketId: String
)