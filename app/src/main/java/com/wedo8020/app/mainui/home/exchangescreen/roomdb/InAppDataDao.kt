package com.wedo8020.app.mainui.home.exchangescreen.roomdb

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query


@Dao
interface InAppDataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun sendDataToDb(inApp: InAppDataEntity)


    @Query("SELECT * FROM in_app_purchase_info")
    fun fetchAllData(): List<InAppDataEntity>

    @Query("DELETE FROM in_app_purchase_info")
    suspend fun deleteAllInAppData()

}