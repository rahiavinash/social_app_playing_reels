package com.wedo8020.app.response

import com.google.gson.annotations.SerializedName

data class RegisterUserResponse(
    val success: Boolean?=null,
    var message: String?=null,
    val data: ApiData?=null
)

data class ApiData(
    var accessToken: String?=null,
    val refreshToken: String?= null,
    val recoveryCode: String?= null
)

data class RegisterUserRequest(
    val name: String? = null,
    val username: String? = null,
    val password: String? = null,
    val deviceType: String? = null,
    val deviceToken: String? = null,
    val referralId: String? = null
)


data class RecoveryCodeRequest(val recoveryCode : String ?= null, val deviceToken: String?=null)

data class RecoveryCodeResponse( val success: Boolean?=null,
                                 var message: String?=null,
                                 val data: RecoveryResponse?=null)

data class RecoveryResponse(
    var username: String?=null,
    val password: String?= null
)

data class MessageRequest( var offset:Int = 0,
                           val limit: Int = 0,
                           val order: Int = -1,
                           var search : String = "")


data class MessageResponse(var success: Boolean?=null,
                           var message : String?=null,var data : ArrayList<MessageData>?=null)

data class MessageData(
    @SerializedName("_id")val id: String? = null,
    val name: String? = null,
    val username: String? = null,
    val accountType: String? = null,
    val blueTickEnable: Boolean? = null,
    val profile: String? = null,
    val overallLikes: Int? = null,
    val noOfLikes: Int? = null,
    val isFriend: Boolean? = null,
    val followStatus: Int? = null
)
data class AcceptRejectRequest(var status : String?=null, var id : String?=null)

data class AcceptRejectResponse(var message : String?=null, var success : Boolean ?=null)