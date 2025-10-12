package com.wedo8020.app.mainui.login.switchaccounts

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.wedo8020.app.base.BaseViewModel
import com.wedo8020.app.mainui.login.switchaccounts.roomdB.SwitchAccountsEntity
import com.wedo8020.app.mainui.login.switchaccounts.roomdB.SwitchAccountsRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SwitchAccountsViewModel(val repository : SwitchAccountsRepo) : BaseViewModel() {

    var allAccounts = MutableLiveData<ArrayList<SwitchAccountsEntity>>()
    var dataCount = MutableLiveData<Int>()
    var userExists = MutableLiveData<Int>()
    var userDetails = MutableLiveData<SwitchAccountsEntity>()

    fun addAccount(payload : SwitchAccountsEntity){
        viewModelScope.launch(Dispatchers.IO){
            repository.addAccount(payload)
        }

    }

    fun getAllAccounts(){
        viewModelScope.launch(Dispatchers.IO){
           allAccounts.postValue(repository.getAllAccounts() as ArrayList<SwitchAccountsEntity>?)
        }

    }

    fun getRowsCount(){
        viewModelScope.launch(Dispatchers.IO) {
            dataCount.postValue(repository.getRows())
        }
    }
    fun deleteAll(){
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAll()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun updateSecondRow(entity : SwitchAccountsEntity){
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateRowIfNeeded(entity)
        }
    }

    fun userExists(username : String){
        viewModelScope.launch(Dispatchers.IO) {
            userExists.postValue(repository.userExists(username))
        }
    }

    fun updateProfile(username: String, profile:String){
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateProfile(username,profile)
        }
    }
    fun updatePassword(username: String, password:String){
        viewModelScope.launch(Dispatchers.IO) {
            repository.updatePassword(username,password)
        }
    }
    fun updateUsername(username: String, updatedUserName:String){
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateUsername(username,updatedUserName)
        }
    }

    fun getUserByUsername(username : String){
        viewModelScope.launch(Dispatchers.IO) {
            userDetails.postValue(repository.getUserByUsername(username))
        }
    }


}