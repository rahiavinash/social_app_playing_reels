package com.wedo8020.app.response

data class ProfileNotificationResponse(
    var data: NotificationData?=null,
    var message: String?=null,
    var error: Int?=null,
    var success: Boolean=false,
)

data class NotificationData(
    val accountIFollow: AccountIFollow?,
    val anyOne: AnyOne?,
    val followRequest: Boolean,
    val myFollower: MyFollower?,
    val myFriend: MyFriend?,
    val showInNotification: Boolean
)

data class AccountIFollow(
    val commented: Boolean,
    val likeMyPost: Boolean,
    val mentionedMe: Boolean,
    val taggedMe: Boolean
)

data class AnyOne(
    val commented: Boolean,
    val likeMyPost: Boolean,
    val mentionedMe: Boolean,
    val taggedMe: Boolean
)

data class MyFollower(
    val commented: Boolean,
    val likeMyPost: Boolean,
    val mentionedMe: Boolean,
    val taggedMe: Boolean
)

data class MyFriend(
    val commented: Boolean,
    val likeMyPost: Boolean,
    val mentionedMe: Boolean,
    val taggedMe: Boolean
)