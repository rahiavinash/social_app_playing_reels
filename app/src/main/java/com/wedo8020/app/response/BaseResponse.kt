package com.wedo8020.app.response

data class BaseResponse(
    var data: Data?=null,
    var message: String?=null,
    var error: Int?=null,
    var success: Boolean=false
)
data class CommentBaseResponse(
    var data: DataX?=null,
    var message: String?=null,
    var error: Int?=null,
    var success: Boolean=false
)
data class DataX(
    var isChecked: Boolean = false,
    val _id: String,
    val creator: String,
    val comments: ArrayList<Comment>,
    val totalComments: Int,
    val noOfComments: Int
)

