package com.wedo8020.app.response


data class NewMessageListResponse(
    val success: Boolean,
    val code: Int,

)

/*
data class ReportData(
    val deletedAt: String?,
    val lastMessage: LastMessages,
    val otherUser: OtherUsersData
)

data class LastMessages(
    val message: String,
    val sender: String,
    val isMessageHide: Boolean,
    val seenTime: String?,
    val readReceipt: Boolean,
    val file: String,
    val post: String,
    val type: String,
    val reactions: List<Any>,
    val seenStatus: String
)

data class OtherUsersData(
    val _id: String,
    val name: String,
    val username: String,
    val blueTickEnable: Boolean,
    val profile: String
)*/
