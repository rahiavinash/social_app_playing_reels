package com.wedo8020.app.notificationservice

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import com.wedo8020.app.MainApplication
import com.wedo8020.app.eventBus.isSocketConnected
import com.wedo8020.app.utils.NotificationType
import com.wedo8020.app.utils.Prefs
import com.wedo8020.app.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe


class NotificationActionService : Service() {
    val socket = MainApplication.get().socket
    var intent: Intent? = null
    var userID = ""
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        EventBus.getDefault().register(this)
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if (intent != null) {
            this.intent = intent
            userID = this.intent?.getStringExtra("userID")?:""
            Log.e("NotificationActionService", "user id ---------- "+userID)
            if (!socket.isConnected()) {
                socket.connect()
                Log.e("NotificationActionService", "notConnected")
            } else {
                Log.e("NotificationActionService", "Connected")
                setSocketListener()
                sendMessage()
            }
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Utils.printLog(tag = "NotificationActionService", msg = "destroy")
        socket.disconnect()
        EventBus.getDefault().unregister(this)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Subscribe
    fun isSocketConnected(isSocketConnected: Any) {
        if (isSocketConnected is isSocketConnected) {
            if (isSocketConnected.isConnected) {
                Log.e("NotificationActionService", "emit login socket")
                setSocketListener()
                sendMessage()
            }
        }
    }

    private fun sendMessage(replyMessage:String? =null) {
        val remoteInput = RemoteInput.getResultsFromIntent(intent!!)


        if (remoteInput != null) {
            val replyText = remoteInput.getCharSequence(NotificationType.KEY_TEXT_REPLY)

            Utils.printLog(tag = "NotificationActionService", msg = replyText.toString())

            // Handle the reply text here
            val obj = Utils.toJsonObject(
                "token" to "Bearer ${Prefs.init().accessToken}",
                "message" to replyText.toString(),
                "replyMessage" to replyMessage,
                "user" to userID,
                "type" to "text",
            )
            Utils.printLog(tag = "NotificationActionService", msg = obj.toString())
            socket.emitMessage("newMessage", obj) { _, _ ->

            }
//            stopSelf()
        }

    }
    private fun setSocketListener(){
        socket.listenOff("error")
        socket.listenOff("newMessageSuccess")
        socket.listenOn("error") { args ->
            CoroutineScope(Dispatchers.Main).launch {
                Log.e("NotificationActionService", args[0].toString())

                updateNotification("")
                stopSelf()
            }
        }
        socket.listenOn("newMessageSuccess") { args ->
            CoroutineScope(Dispatchers.Main).launch {
                Log.e("NotificationActionService", args[0].toString())

                updateNotification("")
                stopSelf()
            }
        }
    }
    private fun emoJiReactionToMessage(userID: String, message: String, reaction: String) {
        val obj = Utils.toJsonObject(
            "token" to "Bearer ${Prefs.init().accessToken}",
            "message" to message,
            "reaction" to reaction,
            "user" to userID
        )
        socket.emitMessage("reactMessage", obj) { _, _ ->
        }
    }
    private fun updateNotification(replyText: String) {
        // Assuming you have the original notificationId used when creating the notification
        val notificationManager = NotificationManagerCompat.from(this)

        // Retrieve the existing notification
        val existingNotification = notificationManager.activeNotifications
    }
}