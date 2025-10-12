package com.wedo8020.app.response

data class MessageListResponse(
    var data: ArrayList<MessageListData>?=null,
    var message: String?=null,
    var error: Int?=null,
    var success: Boolean=false,
)

data class MessageListData(
    val _id: String,
    val deletedAt: Any,
    var lastMessage: LastMessage?=null,
    var noOfUnseenMessages: Int,
    val otherUser: OtherUser?=null,
    val pinTime: String,
    val sortOn: Int

)

data class LastMessage(
    val _id: String,
    var createdAt: String,
    val `file`: String,
    var isMessageHide: Boolean =false,
    var message: String = "",
    val parentMessage: Any,
    val post: Any,
    val reactions: List<Reaction>,
    val readReceipt: Boolean,
    var seenStatus: Any,
    val seenTime: Any,
    val sender: String,
    var type: String
)

data class OtherUser(
    val _id: String,
    val blueTickEnable: Boolean,
    val isOnline: Boolean,
    val name: String,
    val profile: String?=null,
    val username: String
)

data class Reaction(
    val _id: String,
    val date: String,
    val react: String,
    val user: String
)