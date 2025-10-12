package com.wedo8020.app.mainui.login.switchaccounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.wedo8020.app.mainui.login.switchaccounts.roomdB.SwitchAccountsRepo

class SwitchAccountsViewModelFactory (
    private val repository: SwitchAccountsRepo
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        try {
            val constructor = modelClass.getDeclaredConstructor(SwitchAccountsRepo::class.java)
            return constructor.newInstance(repository)
        } catch (e: Exception) {
        }
        return super.create(modelClass)
    }
}
