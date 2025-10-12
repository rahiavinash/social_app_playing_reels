package com.wedo8020.app.mainui.home.chatAndNotification.chatViewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.wedo8020.app.base.BaseViewModel
import com.wedo8020.app.repo.RemoteRepositaryImp
import com.wedo8020.app.request.ChatHistoryRequest
import com.wedo8020.app.request.ChatList
import com.wedo8020.app.request.UserDecryptFile
import com.wedo8020.app.response.AcceptRejectRequest
import com.wedo8020.app.response.AcceptRejectResponse
import com.wedo8020.app.response.BaseResponse
import com.wedo8020.app.response.ChatImageUpload
import com.wedo8020.app.response.MessageListResponse
import com.wedo8020.app.response.MessageRequest
import com.wedo8020.app.response.MessageResponse
import com.wedo8020.app.response.ShowChatResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ChatViewModel: BaseViewModel() {
    val TAG = ChatViewModel::class.java.name
    private val authenticationRepository by lazy {
        RemoteRepositaryImp()
    }

    var chatImageUploadResponse = MutableLiveData<BaseResponse>()
    var receivedMessagesResponse = MutableLiveData<MessageResponse>()
    var sentMessagesResponse = MutableLiveData<MessageResponse>()
    var acceptRequestResponse = MutableLiveData<AcceptRejectResponse>()
    val chatListResponse: MutableLiveData<com.wedo8020.app.utils.Event<MessageListResponse>> by lazy{ MutableLiveData<com.wedo8020.app.utils.Event<MessageListResponse>>() }
    val chatHistoryResponse: MutableLiveData<com.wedo8020.app.utils.Event<ShowChatResponse>> by lazy{ MutableLiveData<com.wedo8020.app.utils.Event<ShowChatResponse>>() }


    fun chatImageUpload(payload: ChatImageUpload) {
        viewModelScope.launch(Dispatchers.IO) {
            authenticationRepository.chatImageUpload(payload) { isSuccess: Boolean, response: BaseResponse ->
                if (isSuccess) {
                    chatImageUploadResponse.postValue(response)

                } else {
                    showMessage.postValue(response.message)

                }
            }
        }
    }

    fun sentMessageRequest(payload: MessageRequest) {
        viewModelScope.launch(Dispatchers.IO) {
            showLoading.postValue(true)
            authenticationRepository.sentMessagesRequest(payload) { isSuccess: Boolean, response: MessageResponse ->
                if (isSuccess) {
                    showLoading.postValue(false)
                    sentMessagesResponse.postValue(response)

                } else {
                    showLoading.postValue(false)
                    sentMessagesResponse.postValue(response)
                    showMessage.postValue(response.message)

                }
            }
        }
    }

    fun receiveMessageRequest(payload: MessageRequest) {
        viewModelScope.launch(Dispatchers.IO) {
            showLoading.postValue(true)
            authenticationRepository.receivedMessagesRequest(payload) { isSuccess: Boolean, response: MessageResponse ->
                if (isSuccess) {
                    showLoading.postValue(false)
                    receivedMessagesResponse.postValue(response)

                } else {
                    showLoading.postValue(false)
                    receivedMessagesResponse.postValue(response)
                    showMessage.postValue(response.message)

                }
            }
        }
    }
    fun userDecryptFile(userDecryptFile: UserDecryptFile) {
        showLoading.postValue(true)
        viewModelScope.launch {
            authenticationRepository.userDecrypt(userDecryptFile)
            { isSuccess: Boolean, response: BaseResponse ->
                showLoading.postValue(false)
                if (isSuccess) {
                    chatImageUploadResponse.postValue(response)

                } else {
                    showMessage.postValue(response.message)
                }
            }
        }
    }

    fun acceptReject(payload: AcceptRejectRequest) {
        viewModelScope.launch(Dispatchers.IO) {
            showLoading.postValue(true)
            authenticationRepository.acceptRejectRequests(payload) { isSuccess: Boolean, response: AcceptRejectResponse ->
                if (isSuccess) {
                    showLoading.postValue(false)
                    acceptRequestResponse.postValue(response)

                } else {
                    showLoading.postValue(false)
                    acceptRequestResponse.postValue(response)
                    showMessage.postValue(response.message)

                }
            }
        }
    }

    fun chatList(payload :ChatList) {
        viewModelScope.launch(Dispatchers.IO) {
            showLoading.postValue(true)
            authenticationRepository.chatList(payload) { isSuccess: Boolean, response: MessageListResponse ->
                if (isSuccess) {
                    showLoading.postValue(false)
                    chatListResponse.postValue(com.wedo8020.app.utils.Event(response))

                } else {
                    showLoading.postValue(false)
                    chatListResponse.postValue(com.wedo8020.app.utils.Event(response))
                    showMessage.postValue(response.message)

                }
            }
        }
    }

    fun chatHistory(payload :ChatHistoryRequest) {
        viewModelScope.launch(Dispatchers.IO) {
            showLoading.postValue(true)
            authenticationRepository.chatHistory(payload) { isSuccess: Boolean, response: ShowChatResponse ->
                if (isSuccess) {
                    showLoading.postValue(false)
                    chatHistoryResponse.postValue(com.wedo8020.app.utils.Event(response))

                } else {
                    showLoading.postValue(false)
                    chatHistoryResponse.postValue(com.wedo8020.app.utils.Event(response))
                    showMessage.postValue(response.message)

                }
            }
        }
    }
}