package com.wedo8020.app.response

data class ProfilePostResponse(
    var data: ArrayList<Data>?=null,
    var message: String?=null,
    var error: Int?=null,
    var success: Boolean=false,

)

