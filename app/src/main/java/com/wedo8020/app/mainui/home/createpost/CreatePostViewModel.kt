package com.wedo8020.app.mainui.home.createpost

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.wedo8020.app.CompressionUploadWorker
import com.wedo8020.app.MainApplication
import com.wedo8020.app.base.BaseViewModel
import com.wedo8020.app.eventBus.UploadStatus
import com.wedo8020.app.repo.RemoteRepositaryImp
import com.wedo8020.app.request.CreatePost
import com.wedo8020.app.request.CreatePostWithMedia
import com.wedo8020.app.request.HashTagListResponse
import com.wedo8020.app.request.HashtagPayload
import com.wedo8020.app.response.BaseResponse
import com.wedo8020.app.roomdb.CreatePostDbRepo
import com.wedo8020.app.roomdb.CreatePostEntity
import com.wedo8020.app.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus


class CreatePostViewModel(private val repository: CreatePostDbRepo) : BaseViewModel() {
    private val authenticationRepository = RemoteRepositaryImp()
    var mutableBaseResponse  = MutableLiveData<BaseResponse>()
    var mediaResponse  = MutableLiveData<BaseResponse>()
    var hashTagListResponse = MutableLiveData<HashTagListResponse>()

    private val workManager = WorkManager.getInstance(MainApplication.get().applicationContext)

    fun compressAndUploadVideoOrImages(
        createPostEntity: CreatePostEntity
    ) {

        val data = Data.Builder()
            .putString("videoUri", createPostEntity.video)
            .putString("seekTime",createPostEntity.seekTime.toString())
            .putStringArray(
                "images",
                createPostEntity.images?.toTypedArray() ?: ArrayList<String>().toTypedArray()
            )
            .putString("postId", createPostEntity.id)
            .build()
        val compressionUploadWorkRequest =
            OneTimeWorkRequest.Builder(CompressionUploadWorker::class.java)
                .setInputData(data)
                .build()


        workManager.enqueue(compressionUploadWorkRequest)
        // Observe the work status and post events accordingly
        viewModelScope.launch(Dispatchers.Main) {
            workManager.getWorkInfoByIdLiveData(compressionUploadWorkRequest.id)
                .observeForever { workInfo ->
                    when (workInfo.state) {
                        WorkInfo.State.SUCCEEDED -> {
                            Utils.printLog(tag = "getCreatePostData", msg = "success")


                            EventBus.getDefault().post(UploadStatus(true))
                        }

                        WorkInfo.State.FAILED -> {
//                        val error = workInfo.outputData.getString("error")
                            Utils.printLog(tag = "getCreatePostData", msg = "failed")

                            EventBus.getDefault().post(UploadStatus(false))
                        }
                        // Handle other states if necessary
                        else -> {}
                    }
                }
        }

    }

    suspend fun createPostInDB(data: CreatePostEntity) = repository.createPost(data)


    suspend fun deleteAll() = repository.deleteAll()


    fun createPost(createPost: CreatePost) {
        viewModelScope.launch(Dispatchers.IO) {
            authenticationRepository.createPost(createPost) { isSuccess: Boolean, baseResponse: BaseResponse ->
                if (isSuccess) {
                    Log.d("successApi", " Api without media is success")
                    mutableBaseResponse.postValue(baseResponse)
                } else {
                    showMessage.postValue(baseResponse.message)

                }
            }
        }
    }

    fun createPostWithMedia(mediaCreatePost: CreatePostWithMedia) {
        viewModelScope.launch(Dispatchers.IO) {
            authenticationRepository.createPostWithMedia(mediaCreatePost) { isSuccess: Boolean, baseResponse: BaseResponse ->
                if (isSuccess) {
                    Log.d("successApi", " Api without media is success")
                    mediaResponse.postValue(baseResponse)
                } else {
                    showMessage.postValue(baseResponse.message)

                }
            }
        }
    }

    fun hashTagList(payload: HashtagPayload) {
        viewModelScope.launch(Dispatchers.IO) {
            authenticationRepository.hashTagList(payload) { isSuccess: Boolean, response: HashTagListResponse ->
                if (isSuccess) {
                    Log.d("successApi", " hashtagListResponse : ${response.data?.size}")
                    hashTagListResponse.postValue(response)
                } else {
                    showMessage.postValue(response.message)

                }
            }
        }
    }


}