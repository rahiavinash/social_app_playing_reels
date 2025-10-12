package com.wedo8020.app.notificationservice

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.RemoteInput
import com.wedo8020.app.utils.NotificationType

class NotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action: String? = intent.action


        action?.let {
            when (action) {
                NotificationType.REPLY_ACTION -> {
                    val replyText = getMessageText(intent)
//                    Toast.makeText(context, "Replied: $replyText", Toast.LENGTH_SHORT).show()
                }
                NotificationType.LIKE_ACTION -> {
//                    Toast.makeText(context, "Liked!", Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }
    }


    private fun getMessageText(intent: Intent): CharSequence? {
        val remoteInput = RemoteInput.getResultsFromIntent(intent)
        return remoteInput?.getCharSequence(NotificationType.KEY_TEXT_REPLY)
    }
}