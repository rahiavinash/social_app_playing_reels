package com.wedo8020.app.eventBus

import android.widget.ImageView
import android.widget.TextView
import com.wedo8020.app.response.Data
import org.json.JSONObject

data class isInnerPageScroll(
    val isScroll: Boolean
)
data class HomePagerScroll(
    val isScroll: Boolean
)
data class StartStopAR(
    val isStart: Boolean
)
data class HomeTabClick(
    val isClicked: Boolean
)
data class isDoubleTap(
    val position: Int,
    val x: Float,
    val y: Float,
    val postId: String,
    val imgView: ImageView,
    val textView: TextView,
    val overallTxt: TextView,
    val targetX: Float,
    val targetY: Float
)

data class ProfilePostItemClick(
    val position: Int,
    val dataList: ArrayList<Data>,
    val postType: String
)

data class SearchHashtag(
    val hashtag: String
)

data class isSocketConnected(
    var isConnected: Boolean
)


data class isGhostMode(
    var isGhostMode: Boolean
)

data class Repost(var post: String)

data class UploadStatus(var isSuccess: Boolean)
data class DialogShown(var dialog: String)

data class InAppNotification(var jsonObject: JSONObject)