package com.wedo8020.app.response

data class PostBaseResponse(
    var data: ArrayList<Data?>?=null,
    var message: String?=null,
    var error: Int?=null,
    var success: Boolean=false,
    var postType: String? = null
)

data class SearchBaseResponse(
    var data: ArrayList<Data?>?=null,
    var message: String?=null,
    var error: Int?=null,
    var success: Boolean=false,
    var postType: String? = null
)