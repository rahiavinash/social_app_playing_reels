package com.wedo8020.app.response

import com.google.gson.annotations.SerializedName

data class LeaderboardResponse(
    val success: Boolean? = null,
    var message: String? = null,
    val data: Map<String, ArrayList<LeaderboardItem>>? = null
)

data class LeaderboardItem(
    val _id: String? = null,
    val name: String? = null,
    val username: String? = null,
    @SerializedName("blueTickEnable")
    val isBlueTickEnabled: Boolean? = null,
    val monthLikes: Int? = null,
    val lastLike: String? = null,
    val rank: Int? = null,
    val profile: String? = null,
    val overallLikes: Int? = null
)

data class LeaderboardRequest(
    val search: String? = null,
    val filterDate: String? = null
)
data class DropsDataRequest(
    val search: String? = null,
    val filterDate: String? = null
)

data class DropsData(
    val _id: String? = null,
    val description: String? = null,
    val mentionUsers: List<String>? = null,
    val type: String? = null,
    val creator: CreatorDataList? = null,
    val video: String? = null,
    val images: List<String>? = null,
    val videoThumbnail: String? = null,
    val hashtags: List<String>? = null,
    val monthLikes: Int? = null,
    val lastLike: String? = null,
    val rank: Int? = null,
    val overallLikes: Int? = null
)

data class CreatorDataList(
    val username: String? = null,
    val name: String? = null,
    val profile: String? = null
)

data class DropsDataResponse(
    val success: Boolean? = null,
    var message: String? = null,
    val data: Map<String, ArrayList<Data>>? = null
)
