package com.wedo8020.app.response

data class LinkedAccountResponse(
    var data: LinkedAccountData?=null,
    var message: String?=null,
    var error: Int?=null,
    var success: Boolean=false,
    var postType: String? = null
)

data class LinkedAccountData(
    val totalBonus: Double,
    val users: ArrayList<Users>
)
data class Users(
    val _id: String,
    val name: String,
    val username: String,
    val profile: String,
    val createdAt: String,
    val ARWithoutfloor: Double,
    val totalDailyAR: Int
)
/*
data class User(
    val ARWithoutfloor: Int,
    val _id: String,
    val createdAt: String,
    val name: String,
    val profile: String,
    val totalDailyAR: Int,
    val username: String
)*/
