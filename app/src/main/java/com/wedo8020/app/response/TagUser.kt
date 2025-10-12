package com.wedo8020.app.response

data class TagUser(
    val _id: String,
    val accountType: String,
    var followStatus: Int,
    val name: String,
    val noOfFollowers: Int,
    val overallLikes: Int,
    val overallPosts: Int,
    val profile: String,
    val username: String
){

}