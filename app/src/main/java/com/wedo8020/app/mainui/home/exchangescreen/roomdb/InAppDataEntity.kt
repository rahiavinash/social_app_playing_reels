package com.wedo8020.app.mainui.home.exchangescreen.roomdb

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wedo8020.app.mainui.home.exchangescreen.PurchaseType

@Entity(tableName = "in_app_purchase_info")
data class InAppDataEntity(
    @PrimaryKey(autoGenerate = true)
    val _id : Int = 0,
    val id: String,
    val purchaseType: PurchaseType? = null,
    val aiEnable: Boolean? = null,
    val blueTickEnable: Boolean? = null,
    val noAdsEnable: Boolean? = null,
    val trees: Int? = null,
    val country: String? = null,
    val wd: Int? = null,
    val formattedPrice : String?= null,
    val price : Double? = null,
    val countryCode: String?= null,
    val orderId : String?=null,
    val productId : String?=null,
    val transactionId : String?=null,
    val transactionTime : String?=null,


)