package com.wedo8020.app.response

data class ReactionListResponse(
    val code: Int,
    val `data`: ReactionData,
    val message: String,
    val success: Boolean
)

data class ReactionData(
    val _id: String?=null,
    val reactions: ArrayList<Reactions>?=null
)
//data class ReactionData(
//    val type: String,
//    val otherUser: OtherUserData
//)
//
//data class OtherUserData(
//    val _id: String,
//    val profile: String,
//    val username: String,
//    val name: String,
//    val isOnline: Boolean
//)

