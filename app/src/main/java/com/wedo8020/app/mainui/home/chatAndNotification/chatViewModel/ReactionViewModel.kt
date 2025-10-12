package com.wedo8020.app.mainui.home.chatAndNotification.chatViewModel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.wedo8020.app.base.BaseViewModel
import com.wedo8020.app.mainui.home.chatAndNotification.roomDBReaction.EmoJiReactionRepo
import com.wedo8020.app.mainui.home.chatAndNotification.roomDBReaction.ReactionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class ReactionViewModel(val repository : EmoJiReactionRepo) : BaseViewModel() {

    var allEmojiReaction = MutableLiveData<ArrayList<ReactionEntity>>()
    var dataCount = MutableLiveData<Int>()

    fun addEmoJi(payload : ReactionEntity){
        viewModelScope.launch(Dispatchers.IO){
            repository.addReaction(payload)
        }

    }

    fun getAllEmoJiList(){
        viewModelScope.launch(Dispatchers.IO){
            allEmojiReaction.postValue(repository.getAllEmoJi() as ArrayList<ReactionEntity>?)
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun updateSecondRow(entity : ReactionEntity){
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateRowIfNeeded(entity)
        }
    }

    fun getRowsCount(){
        viewModelScope.launch(Dispatchers.IO) {
            dataCount.postValue(repository.getRows())
        }
    }



}