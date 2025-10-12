package com.wedo8020.app.response

data class Comment(
    val _id: String,
    val createdAt: String,
    val creator: Creator,
    val description: String,
    val hashtags: ArrayList<Hashtag>?=null,
    var isLike: Boolean,
    val isTop: Int,
    val mentionUsers: List<MentionUser>,
    var noOfLikes: Int,
    var noOfSubComments: Int,
    val parentComment: String?,
    val pinTime: String ? =null,
    var subComments:ArrayList<Comment>?,
    val post: String,
    val isReported: Boolean? = false
)