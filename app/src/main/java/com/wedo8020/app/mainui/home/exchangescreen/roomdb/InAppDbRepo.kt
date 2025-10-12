package com.wedo8020.app.mainui.home.exchangescreen.roomdb


class InAppDbRepo(
    private val inAppData : InAppDbCreation) {

    suspend fun inAppDataDetails(inAppPayload : InAppDataEntity) = inAppData.getInAppData().sendDataToDb(inAppPayload)


    suspend fun deleteAllInAppData() = inAppData.getInAppData().deleteAllInAppData()


    suspend fun getAllData() = inAppData.getInAppData().fetchAllData()



}