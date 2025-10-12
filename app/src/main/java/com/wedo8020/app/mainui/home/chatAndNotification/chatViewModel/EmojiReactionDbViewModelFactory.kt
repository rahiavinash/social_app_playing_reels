package com.wedo8020.app.mainui.home.chatAndNotification.chatViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.wedo8020.app.mainui.home.chatAndNotification.roomDBReaction.EmoJiReactionRepo

class EmojiReactionDbViewModelFactory (
    private val repository: EmoJiReactionRepo
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        try {
            val constructor = modelClass.getDeclaredConstructor(EmoJiReactionRepo::class.java)
            return constructor.newInstance(repository)
        } catch (e: Exception) {
        }
        return super.create(modelClass)
    }
}