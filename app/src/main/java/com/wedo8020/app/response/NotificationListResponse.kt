package com.wedo8020.app.response

data class NotificationListResponse(
    var data: ArrayList<NotificationListData>?=null,
    var message: String?=null,
    var error: Int?=null,
    var success: Boolean=false,
)

data class NotificationListData(
    val _id: String,
    val ar: Int,
    val createdAt: String,
    val message: String,
    var isSame: Boolean,
    val post: Post? = null,
    val lastUpdateWithoutRead: String,
    val noOfSender: Int,
    val `receiver`: Receiver,
    val sender: ArrayList<SenderItem>,
    val type: String,
    val comment: Comment?,
    var otherUser: OtherUser,
    var createdDate: String
)

data class Receiver(
    val WD: Double
)

data class SenderItem(
    val _id: String,
    val name: String,
    val username: String,
    val blueTickEnable: Boolean,
    val index: Int,
    val profile: String?
)