package com.wedo8020.app.response

data class ProfileAccountResponse(
    var data: AccountData?=null,
    var message: String?=null,
    var error: Int?=null,
    var success: Boolean=false,
)

data class AccountData(
    val name: String,
    val username: String
)