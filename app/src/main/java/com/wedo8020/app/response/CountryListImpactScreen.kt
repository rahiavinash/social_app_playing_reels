package com.wedo8020.app.response

import com.google.gson.annotations.SerializedName

data class Country(
    val _id: String? = null,
    val name: String? = null,
    val code: String? = null
)

data class CountryListImpactScreen(
    val success: Boolean? = null,
    var message: String? = null,
    val data: ArrayList<Country>? = null
)
data class TreeDetails(
    val label: String? = null,
    val value: Int? = null
)

data class Tree(
    @SerializedName("1") val one: TreeDetails? = null,
    @SerializedName("5") val five: TreeDetails? = null,
    @SerializedName("10") val ten: TreeDetails? = null,
    val info : String?=null
)

data class PremiumPurchaseDetail(
    val label: String? = null,
    @SerializedName("lifeTimeLabel") val lifetimeLabel: String? = null,
    val year: Int? = null,
    val lifetime: Int? = null,
    val info: String? = null
)

data class ArBonus(
    val _id: String? = null,
    val tree: Tree? = null,
    val info: String? = null,
    @SerializedName("premiumPurchase") val premiumPurchase: PremiumPurchase? = null,
    val wdInfo: String? = null,
    val treesPlanted: Int? = null
)

data class PremiumPurchase(
    val blueTick: PremiumPurchaseDetail? = null,
    val noAds: PremiumPurchaseDetail? = null,
    val ai: PremiumPurchaseDetail? = null
)

data class ImpactData(
    val _id: String? = null,
    val WD: Double? = null,
    val eTrees: Int? = null,
    val arBonus: ArBonus? = null,
    val overallLikes: Int? = null,
    val overallGivenLikes: Int? = null,
    val wallet: String? = null,
    val remainingWD: String? = null,
    val totalTreesByWeDo: Int? = null
)

data class ImpactDetail(
    val success: Boolean? = null,
    var message: String? = null,
    val data: ImpactData? = null
)


data class UserDataList(
    val success: Boolean? = null,
    var message: String? = null,
    val data: ArrayList<User>? = null
)

data class User(
    val _id: String? = null,
    val name: String? = null,

    val username: String? = null,
    val blueTickEnable: Boolean? = null,
    val profile: String? = null,
    val mostTagged: Int? = null,
    val overallLikes: Int? = null,
    val isCollabActive: Boolean? = null,
    val isTagActive: Boolean? = null,
    val ARWithoutfloor: Int,
    val createdAt: String,
    val totalDailyAR: Int,


)
data class PurchaseHistoryList(
    val success: Boolean? = null,
    var message: String? = null,
    val data: TransactionData? = null
)
data class OtherUsers(
    @SerializedName("_id")
    val id: String? = null,
    val name: String? = null,
    val username: String? = null,
    @SerializedName("blueTickEnable")
    val isBlueTickEnable: Boolean? = null
)

data class Transaction(
    @SerializedName("_id")  val id: String? = null,
    val date: String? = null,
    val wd: Double? = null,
    val action: String? = null,
    val deletedAt: String? = null,
    val createdAt: String? = null,
    val otherUser: OtherUsers? = null,
    val reason: String? = null,
    val ar: Int? = null
)

data class TransactionData(
    @SerializedName("WD") val wd: Double? = null,
    val transactions: ArrayList<Transaction>? = null
)
data class TransferSrcDetails(val username : String ?=null, val ar : Int ?= null)
data class ChatImageUpload(val file : String ?=null, val ext : String ?= null ,val localId:String ?=null)

data class PrizeDetailsResponse(
    val success: Boolean = false,
    var message: String? = null,
    val data: PrizeDetail? = null

)

data class PrizeDetail(
    val account: WinnerDetail? = null,
    val drop: WinnerDetail? = null,
    val tree: WinnerDetail? = null,
    val business: WinnerDetail? = null,
    val wallet : String?=null,
    val shareAppText: String? = null
)

data class WinnerDetail(
    val leftText: String? = null,
    val rightText: String? = null
)

data class MostTreesPlantedResponse(
    val success: Boolean? = null,
    var message: String? = null,
    val data: Map<String, ArrayList<UserType>>? = null
)
data class MostTreesPlantedByRegionResponse(
    val success: Boolean? = null,
    var message: String? = null,
    val data: ArrayList<TreesCountData>? = null
)

data class TreesCountData(val totalTrees : Int ?=null, val name : String?=null, val _id : String ?=null )

data class UserType(
    val _id: String? = null,
    val name: String? = null,
    val username: String? = null,
    val eTrees: Int? = null,
    val blueTickEnable: Boolean? = null,
    val lastPurchase: String? = null,
    val rank: Int? = null,
    val profile: String? = null
)

data class MostTreesPlantedRequest(
    val search: String? = null,
    val filterDate: String? = null,
    val type: String? = null
)

data class AccountAvailabilityResponse(var success : Boolean?=null, var message : String?=null, var data : WholeData?=null)

data class WholeData(var status : Boolean?=null)