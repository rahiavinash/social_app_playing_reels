package com.wedo8020.app.notificationservice

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.core.app.RemoteInput
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.IconCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.squareup.picasso.Picasso
import com.wedo8020.app.R
import com.wedo8020.app.eventBus.InAppNotification
import com.wedo8020.app.mainui.activities.MainActivity
import com.wedo8020.app.utils.AppConstant
import com.wedo8020.app.utils.NotificationType
import com.wedo8020.app.utils.Prefs
import com.wedo8020.app.utils.Utils
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject
import java.util.Date

class FireMsgService : FirebaseMessagingService() {
    val TAG = "FireMsgService"
    private var notificationManager: NotificationManagerCompat? = null
    var notificationId = 111
    private var currentMsgUserId = ""
    override fun onNewToken(token: String) {
        super.onNewToken(token)

    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Utils.printLog(tag = "onMessageReceived", msg = message.notification.toString())

       createNotificationChannel(message)
    }

    private fun createNotificationChannel(remoteMessage: RemoteMessage?) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val name = getString(R.string.new_channel_name)
                val description = getString(R.string.new_channel_description)
                val importance = NotificationManager.IMPORTANCE_HIGH
                val channel = NotificationChannel(getString(R.string.channel_id), name, importance)
                channel.description = description
                val notificationManager = getSystemService(NotificationManager::class.java)
                notificationManager!!.createNotificationChannel(channel)
            }
            val intent = Intent(this, MainActivity::class.java)

            var data: Map<String, String>? = null
            data = remoteMessage?.data
            println("datatierpewr = $data" + "------------------" + remoteMessage?.notification?.title)
            val dataObject = JSONObject(JSONObject(data).get("data").toString())

//            val type = dataObject.geTO("type").toString()
            val bundle = createBundleToSendViaIntent(dataObject)
            val title = remoteMessage?.notification?.title

            intent.putExtras(bundle)

            /*pending intent to pass in notification*/
            val pIntent: PendingIntent = PendingIntent.getActivity(
                this,
                0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )




            val inboxStyle: NotificationCompat.InboxStyle = NotificationCompat.InboxStyle()
            inboxStyle.setBigContentTitle("")

            notificationManager = NotificationManagerCompat.from(this)
            val notificationDetail = dataObject.getJSONObject("notificationDetail")
            Utils.printLog(tag = "createNotificationChannel", msg = notificationDetail.toString())
            val type = notificationDetail.getString("type")

            val userID = if (notificationDetail.has("otherUser")) {
                notificationDetail.getJSONObject("otherUser").getString("_id")
            }else{""}

            if (!Prefs.init().isAppBackground){
                if (userID.isEmpty()||Prefs.init().messageSenderId != userID){

                    EventBus.getDefault().post(InAppNotification(dataObject))
                }
                return
            }
            if (type == NotificationType.MESSAGE) {

                val replyIntent = Intent(this, NotificationActionService::class.java)
                replyIntent.putExtra("userID",userID)


                replyIntent.setAction(NotificationType.REPLY_ACTION)
                val replyPendingIntent = PendingIntent.getService(
                    this, 0, replyIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                )

                // Create a RemoteInput for the reply input field

                // Create a RemoteInput for the reply input field
                val remoteInput: RemoteInput = RemoteInput.Builder(NotificationType.KEY_TEXT_REPLY)
                    .setLabel("Reply")
                    .build()

                // Create an action for the reply

                // Create an action for the reply
                val replyAction = NotificationCompat.Action.Builder(
                    android.R.drawable.ic_menu_send,
                    "Reply", replyPendingIntent
                )
                    .addRemoteInput(remoteInput)
                    .build()

                val likeIntent = Intent(this, NotificationActionReceiver::class.java)
                likeIntent.setAction(NotificationType.LIKE_ACTION)
                val likePendingIntent = PendingIntent.getBroadcast(
                    this, 0, likeIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                )

                // Create an action for the like

                // Create an action for the like
                val likeAction = NotificationCompat.Action.Builder(
                    android.R.drawable.btn_star_big_on,
                    "Like", likePendingIntent
                )
                    .build()


                val message1 = NotificationCompat.MessagingStyle.Message(
                    remoteMessage?.notification?.body.toString(),
                    System.currentTimeMillis(),
                    title
                )

                val person = Person.Builder()
                person.setName(title)
                if (notificationDetail.getString("profile")!="null"){
                    val bitmap = Picasso.with(this).load(notificationDetail.getString("profile")).get()
                    person.setIcon(IconCompat.createWithBitmap(bitmap))

                }
                val message2 = NotificationCompat.MessagingStyle.Message(
                    remoteMessage?.notification?.body.toString(),
                    System.currentTimeMillis(),
                    person.build())


                val mBuilder = NotificationCompat.Builder(this, getString(R.string.channel_id))
                    .setSmallIcon(R.drawable.notification_logo)
//                    .addAction(replyAction)
//                    .addAction(likeAction)
                    .setStyle(
                        NotificationCompat.MessagingStyle(Prefs.init().name)
                            .addMessage(message2)
//                            .addMessage(message2)
                    )
                    .setContentIntent(pIntent)
                    .setAutoCancel(true)

                    .setDefaults(Notification.DEFAULT_ALL)
                    .setAutoCancel(true)
                val id = notificationDetail.getJSONObject("otherUser").getString("_id")
                notificationManager?.notify(id, 100, mBuilder.build())
                currentMsgUserId = id

            }
            else {

                val mBuilder = NotificationCompat.Builder(this, getString(R.string.channel_id))
                    .setSmallIcon(R.drawable.notification_logo)
                    .setContentText(
                        remoteMessage?.notification?.body.toString()
                    )
                    .setContentTitle(title)
                    .setContentIntent(pIntent)
                    .setColor(ContextCompat.getColor(this, R.color.black))
                    .setAutoCancel(true)

                    .setDefaults(Notification.DEFAULT_ALL)
                val m = (Date().time / 1000L % Int.MAX_VALUE).toInt()
                notificationManager?.notify(m, mBuilder.build())
            }


        } catch (e: Exception) {
            Utils.printLog(tag = "createNotificationChannel", msg = e.toString())
        }
    }


    private fun createBundleToSendViaIntent(dataObject: JSONObject): Bundle {
        val bundle = Bundle()
        val notificationDetail = dataObject.getJSONObject("notificationDetail")
        Utils.printLog(tag = "createNotificationChannel", msg = notificationDetail.toString())
        val type = notificationDetail.getString("type")
        Utils.printLog(tag = "createNotificationChannel", msg = type)

        val senderArray = if (notificationDetail.has("sender")) {
            notificationDetail.getJSONArray("sender")
        } else {
            null
        }

        when (type) {
            NotificationType.FOLLOW_USER -> {
                bundle.apply {
                    var id = ""
                    if (senderArray != null) {
                        for (i in 0 until senderArray.length()) {
                            id = senderArray.getString(i)
                        }
                    }
                    putString(AppConstant.NOTIFICATION_TYPE, type)
                    putString("sender", id)
                }
            }

            NotificationType.FOLLOW_REQUEST,
            NotificationType.MENTION_BIO -> {
                bundle.apply {
                    var id = ""
                    if (senderArray != null) {
                        for (i in 0 until senderArray.length()) {
                            id = senderArray.getString(i)
                        }
                    }

                    putString(AppConstant.NOTIFICATION_TYPE, type)
                    putString("sender", id)
                }
            }

            NotificationType.DAILY_AR,
            NotificationType.PERSONAL_INFO,
            NotificationType.BLUE_TICK_PURCHASE,
            NotificationType.WD_PURCHASE,
            NotificationType.WD_TRANSFER,
            NotificationType.TREE_BONUS,
            NotificationType.AI_BONUS,
            NotificationType.NO_ADS_BONUS -> {
                bundle.apply {
                    putString(AppConstant.NOTIFICATION_TYPE, type)
                }
            }

            NotificationType.MESSAGE,
            NotificationType.REACT-> {
                bundle.apply {
                    val otherUser = notificationDetail.getJSONObject("otherUser")
                    putString(AppConstant.NOTIFICATION_TYPE, type)
                    putString("userChatId",otherUser.getString("_id"))
                    putString("otherUserName", otherUser.getString("name"))
                    putString("otherUserProfile", otherUser.getString("profile"))
                    putBoolean("blueTick", false)
                }
            }

            NotificationType.POST_LIKE,
            NotificationType.TAG_POST,
            NotificationType.MENTION_POST,
            NotificationType.COMMENT,
            NotificationType.COMMENT_LIKE,
            NotificationType.COMMENT_REPLY,
            NotificationType.NEW_POST-> {
                bundle.apply {
                    putString("post", notificationDetail.getString("post"))
                    putString(AppConstant.NOTIFICATION_TYPE, type)
                }
            }
        }
        return bundle
    }

    override
    fun handleIntent(intent: Intent?) {
        try {
            if (intent?.extras != null) {
                val builder = RemoteMessage.Builder("MessagingService")
                for (key in intent.extras!!.keySet()) {
                    builder.addData(key, intent.extras!!.get(key).toString())
                }
                onMessageReceived(builder.build());
            } else {
                super.handleIntent(intent)
            }
        } catch (e: Exception) {
            super.handleIntent(intent)
        }
    }
}