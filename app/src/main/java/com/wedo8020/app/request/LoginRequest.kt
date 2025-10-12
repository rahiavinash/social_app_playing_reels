package com.wedo8020.app.request

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    var deviceToken: String?=null,
    var deviceType: String="android",
    var password: String="",
    var isRecover : Boolean ?=null,
    var username: String="",
    var refreshToken:String?=null,
    var payload:String?= null,
    var appVersion : String ?=null
){

}