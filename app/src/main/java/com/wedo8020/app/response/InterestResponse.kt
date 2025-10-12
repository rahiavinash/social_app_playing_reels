package com.wedo8020.app.response


data class InterestResponse(
    var data: UserInterestData ?=null,
    var message: String?=null,
    var error: Int?=null,
    var success: Boolean=false,
)

data class UserInterestData(
    val categories: ArrayList<Category>,
    val onBoardVideo: OnBoardVideo
)

data class Category(
    val _id: String,
    val name: String,
    val selected: Boolean,
    var isSelect:Boolean=false,
)
data class OnBoardVideo(
    val video: String,
    val videoThumbnail: String
)