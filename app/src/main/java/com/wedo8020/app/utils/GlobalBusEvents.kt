package com.wedo8020.app.utils

import com.wedo8020.app.response.UserMessageData

data class MessageEvent(var text: String)
data class ProfileImageEvent(var text: String)
data class ReadReceipt(var text: String)
data class OnClickNotification(var text: String)
data class NotificationListener(var text: String)
data class ImageUploaded(var text: String)
data class NewEmoJi(var text: String ,var messageIDs:String )
data class NewMessage(var text: String)
data class ChangeTab(var text: String)
data class NewProfileUpdated(var text: String)
data class LastMessageUpdate(var data:UserMessageData)
data class UnsentMessageUpdate(var message:String?=null ,var  time:String?=null , var type:String?=null)
data class SaveDropUpdate(var text:String)
data class ArchivePostUpdate(var text:String)
data class ActivePostUpdate(var text:String, var type:String)
data class OnClickEditPost(var text:String)
