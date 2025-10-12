package com.wedo8020.app.mainui.home.exchangescreen


import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.wedo8020.app.base.BaseViewModel
import com.wedo8020.app.repo.RemoteRepositaryImp
import com.wedo8020.app.request.RegionRequest
import com.wedo8020.app.request.UserListRequest
import com.wedo8020.app.response.BaseResponse
import com.wedo8020.app.response.CountryListImpactScreen
import com.wedo8020.app.response.DropsDataRequest
import com.wedo8020.app.response.DropsDataResponse
import com.wedo8020.app.response.ImpactDetail
import com.wedo8020.app.response.LeaderboardRequest
import com.wedo8020.app.response.LeaderboardResponse
import com.wedo8020.app.response.MostTreesPlantedByRegionResponse
import com.wedo8020.app.response.MostTreesPlantedRequest
import com.wedo8020.app.response.MostTreesPlantedResponse
import com.wedo8020.app.response.PrizeDetailsResponse
import com.wedo8020.app.response.PurchaseHistoryList
import com.wedo8020.app.response.TransferSrcDetails
import com.wedo8020.app.response.TreesCountData
import com.wedo8020.app.response.UserDataList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class PurchasesViewModel() : BaseViewModel(){
    private val authenticationRepository = RemoteRepositaryImp()
    var mutableBaseResponse  = MutableLiveData<ImpactDetail>()
    var userListResponse = MutableLiveData<UserDataList>()
    var purchaseHistoryResponse = MutableLiveData<PurchaseHistoryList?>()
    val srcSendResponse =  MutableLiveData<BaseResponse>()
    var leaderBoardResponse = MutableLiveData<LeaderboardResponse>()
    var dropsResponse = MutableLiveData<DropsDataResponse>()

    var countryList = MutableLiveData<CountryListImpactScreen>()
    var prizeDetailsResponse = MutableLiveData<PrizeDetailsResponse>()
    var mostPlantedTreesResponse = MutableLiveData<MostTreesPlantedResponse>()
    var mostPlantedTreesResponseByRegion = MutableLiveData<MostTreesPlantedByRegionResponse>()
    fun impactDetails() {
        viewModelScope.launch(Dispatchers.IO) {
            authenticationRepository.getImpactDetails { isSuccess: Boolean, response: ImpactDetail ->
                if (isSuccess) {
                    Log.d("successApi", " hashtagListResponse : ${response.data}")
                    mutableBaseResponse.postValue(response)
                } else {
                    showMessage.postValue(response.message)

                }
            }
        }
    }

    fun prizeDetails() {
        viewModelScope.launch(Dispatchers.IO) {
            authenticationRepository.getPrizeDetails { isSuccess: Boolean, response: PrizeDetailsResponse ->
                if (isSuccess) {
                    Log.d("successApiResponse", " prize details : ${response.data}")
                    prizeDetailsResponse.postValue(response)
                } else {
                    showMessage.postValue(response.message)

                }
            }
        }
    }




    fun getUserDetails(payload : UserListRequest){

        viewModelScope.launch(Dispatchers.IO) {
            authenticationRepository.getUserDetails(payload) { isSuccess: Boolean, response:UserDataList ->
                if (isSuccess) {
                    Log.d("successApi", " hashtagListResponse : ${response.data}")
                   userListResponse.postValue(response)
                } else {
                    showMessage.postValue(response.message)

                }
            }
        }
    }

    fun getPurchasesHistory(payload : UserListRequest){

        viewModelScope.launch(Dispatchers.IO) {
            authenticationRepository.getPurchaseHistory(payload) { isSuccess: Boolean, response:PurchaseHistoryList ->
                if (isSuccess) {
                    Log.d("successApi", " hashtagListResponse : ${response.data}")
                    purchaseHistoryResponse.postValue(response)
                } else {
                    showMessage.postValue(response.message)

                }
            }
        }
    }

    fun sendSrc(payload : TransferSrcDetails){

        viewModelScope.launch(Dispatchers.IO) {
            authenticationRepository.sendSrc(payload) { isSuccess: Boolean, response:BaseResponse ->
                if (isSuccess) {
                    Log.d("successApi", "sendSrc : ${response.data}")
                    srcSendResponse.postValue(response)

                } else {
                    showMessage.postValue(response.message)

                }
            }
        }
    }
    fun getLeaderBoardDetails(payload : LeaderboardRequest){

        viewModelScope.launch(Dispatchers.IO) {
            authenticationRepository.getLeaderBoardDetails(payload) { isSuccess: Boolean, response:LeaderboardResponse ->
                if (isSuccess) {
                    Log.d("leaderBoardSuccess", "leaderBoardData : ${response.success} ${response.data}")

                    response.data?.forEach { (key1, value) ->
                        Log.d("leaderBoardSuccess", "$key1 ${value}")
                    }
                    leaderBoardResponse.postValue(response)

                } else {
                    showMessage.postValue(response.message)

                }
            }
        }
    }

    fun getMostPlantedTrees(payload : MostTreesPlantedRequest){

        viewModelScope.launch(Dispatchers.IO) {
            authenticationRepository.getMostPlantedTrees(payload) { isSuccess: Boolean, response:MostTreesPlantedResponse ->
                if (isSuccess) {
                    Log.d("leaderBoardSuccess", "leaderBoardData : ${response.success} ${response.data}")

                    response.data?.forEach { (key1, value) ->
                        Log.d("leaderBoardSuccess", "$key1 ${value}")
                    }
                    mostPlantedTreesResponse.postValue(response)

                } else {
                    showMessage.postValue(response.message)

                }
            }
        }
    }

    fun getMostPlantedTreesByRegion(payload : RegionRequest){

        viewModelScope.launch(Dispatchers.IO) {
            authenticationRepository.getMostPlantedTreesByRegion(payload) { isSuccess: Boolean, response: MostTreesPlantedByRegionResponse ->
                if (isSuccess) {
                    Log.d("leaderBoardSuccessForRegion", "leaderBoardData : ${response.success} ${response.data}")

                    response.data?.forEach { (key1, value) ->
                        Log.d("leaderBoardSuccess", "$key1 ${value}")
                    }
                    mostPlantedTreesResponseByRegion.postValue(response)

                } else {
                    showMessage.postValue(response.message)

                }
            }
        }
    }




    fun getDropsDetails(payload : DropsDataRequest){

        viewModelScope.launch(Dispatchers.IO) {
            authenticationRepository.getDropsDetails(payload) { isSuccess: Boolean, response:DropsDataResponse ->
                if (isSuccess) {
                    Log.d("leaderBoardSuccess", "leaderBoardData : ${response.success} ${response.data?.get("Jan 2024")}")
                    dropsResponse.postValue(response)

                } else {
                    showMessage.postValue(response.message)

                }
            }
        }
    }


    fun getCountryDetails(){

        viewModelScope.launch(Dispatchers.IO) {
            authenticationRepository.getCountryDetails { isSuccess: Boolean, response:CountryListImpactScreen ->
                if (isSuccess) {
                    Log.d("countryListSuccess", "$response")
                    countryList.postValue(response)

                } else {
                    showMessage.postValue(response.message)

                }
            }
        }
    }



}