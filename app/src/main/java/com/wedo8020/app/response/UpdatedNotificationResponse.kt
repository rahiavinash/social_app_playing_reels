package com.wedo8020.app.response

class UpdatedNotificationResponse(
    var data: ArrayList<UpdatedNotificationData>? = null,
    val createdAt: String,
)

data class UpdatedNotificationData(
    val _id: String,
    val ar: Int,
    val createdAt: String,
    val lastUpdateWithoutRead: String,
    val noOfSender: Int,
    val `receiver`: Receivers,
    val sender: List<Any>,
    val type: String
)

data class Receivers(
    val WD: Double
)