package com.wedo8020.app.mainui.home.createpost

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.wedo8020.app.roomdb.CreatePostDbRepo

class CreatePostViewModelFactory(
    private val repository: CreatePostDbRepo
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        try {
            val constructor = modelClass.getDeclaredConstructor(CreatePostDbRepo::class.java)
            return constructor.newInstance(repository)
        } catch (e: Exception) {
        }
        return super.create(modelClass)
    }
}