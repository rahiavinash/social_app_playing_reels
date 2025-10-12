package com.wedo8020.app.mainui.login

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.wedo8020.app.base.BaseViewModel
import com.wedo8020.app.repo.RemoteRepositaryImp
import com.wedo8020.app.request.LoginRequest
import com.wedo8020.app.response.AccountAvailabilityResponse
import com.wedo8020.app.response.BaseResponse
import com.wedo8020.app.response.RecoveryCodeRequest
import com.wedo8020.app.response.RecoveryCodeResponse
import com.wedo8020.app.response.RegisterUserRequest
import com.wedo8020.app.response.RegisterUserResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginViewModel : BaseViewModel() {

    var isCheckBoxChecked: Boolean = false
    var username: String? = null
    private val authenticationRepository by lazy{
        RemoteRepositaryImp()
    }
    var loginData = MutableLiveData<BaseResponse>()
    var registerUser = MutableLiveData<RegisterUserResponse>()
    var recoveryCodeData = MutableLiveData<RecoveryCodeResponse>()
    var accountAvailability = MutableLiveData<AccountAvailabilityResponse>()


    fun login(loginRequest: LoginRequest) {
        showLoading.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
            authenticationRepository.login(loginRequest)
            { isSuccess: Boolean, response: BaseResponse ->
                showLoading.postValue(false)
                if (isSuccess) {
                    Log.d("loginResponse", " ${response.success} ${response.data}")
                    loginData.postValue(response)

                } else {
                   loginData.postValue(response)
                    showMessage.postValue(response.message)
                }
            }
        }
    }

    fun registerUser(payload : RegisterUserRequest){
        showLoading.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
            authenticationRepository.registerUser(payload)
            { isSuccess: Boolean, response: RegisterUserResponse ->
                showLoading.postValue(false)
                if (isSuccess) {
                   registerUser.postValue(response)
                } else {
                    registerUser.postValue(response)
                    showMessage.postValue(response.message)
                }
            }
        }
    }


    fun recoveryCode(payload : RecoveryCodeRequest){

        viewModelScope.launch(Dispatchers.IO) {
            authenticationRepository.recoveryCode(payload)
            { isSuccess: Boolean, response: RecoveryCodeResponse ->
                showLoading.postValue(false)
                if (isSuccess) {
                    Log.d("recoveryCode" , " ${response.data?.username.toString()}")
                   recoveryCodeData.postValue(response)

                } else {
                    recoveryCodeData.postValue(response)
                }
            }
        }
    }

    fun accountAvailability() {
        viewModelScope.launch(Dispatchers.IO) {
            authenticationRepository.accountAvailability()
            { isSuccess: Boolean, response: AccountAvailabilityResponse ->
                showLoading.postValue(false)
                if (isSuccess) {
                    Log.d("accountAvailability", " ${response}")
                    accountAvailability.postValue(response)

                } else {
                    accountAvailability.postValue(response)
                }
            }
        }
    }


}