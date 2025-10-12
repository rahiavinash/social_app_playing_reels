package com.wedo8020.app.response

data class ShowChatResponse(
    val `data`: ShowChatResponseData ?=null,
    var message: String?=null,
    val success: Boolean=false
)

data class ShowChatResponseData(
    var history: ArrayList<UserMessageData>?=null,
    val isMessageOn: Boolean=false
)

data class UserMessageData(
    val _id: String,
    var createdAt: String,
    val file: String,
    val isMessageHide: Boolean,
    var message: String,
    val parentMessage: ParentMessage?=null,
    val post: Post?= null,
    var reactions: ArrayList<Reactions>?,
    val readReceipt: Boolean,
    val receiver: String,
    var seenStatus:String?=null,
    val seenTime: String?=null,
    val sender: String,
    var type: String,
    var date: String = "",
    val deletedAt: String? = null,
    val lastMessage: LastMessage ?= null,
    val otherUser: OtherUsersData ?=null,
    var showSeen: Boolean = false,
    var deleteMessage: Boolean = false,

)



data class Reactions(
    val _id: String,
    val date: String,
    var react: String,
    val user: String
)
data class ParentMessage(
    val _id: String,
    val message: String,
    val sender: String,
    val receiver: String,
    val type: String,
    val createdAt: String,
    val file: String?,
    val post: Post?
)

data class Post(
    val _id: String,
    val type: String,
    val creator: UserCreators,
    val hasSound: Boolean,
    val isVisible: Boolean,
    val video: String,
    val images: ArrayList<String>,
    val videoThumbnail: String,
    val options: Options,
)

data class UserCreators(
    val _id: String,
    val name: String,
    val username: String,
    val profile: Any?
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
)
