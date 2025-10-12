package com.wedo8020.app.roomdb

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.google.gson.annotations.SerializedName

@Entity(tableName = "create_post_table")
@TypeConverters(UriConverter::class, ImageListConverter::class, StringListConverter::class)
data class CreatePostEntity(
    @PrimaryKey
    @SerializedName("id") val id: String = "", // Assuming your primary key is named 'id' and is of type Long
    @SerializedName("disappearTime") val disappearTime: String? = null,
    @SerializedName("type") val type: String? = null,
    @SerializedName("download") val download: Boolean? = null,
    @SerializedName("comment") val comment: Boolean? = null,
    @SerializedName("repost") val repost: Boolean? = null,
    @SerializedName("like") val like: Boolean? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("privacy") val privacy: String? = null,
    @SerializedName("tagUsers") val tagUsers: List<String>? = null,
    @SerializedName("images") val images: List<String>?= null,
    @SerializedName("video") val video: String? = null,
    @SerializedName("seekTime") val seekTime: String? = null,
)



