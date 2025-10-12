package com.wedo8020.app.mainui.home.exchangescreen

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.Window
import androidx.navigation.Navigation
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.Purchase.PurchaseState
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.consumePurchase
import com.google.common.collect.ImmutableList
import com.google.gson.Gson
import com.wedo8020.app.R
import com.wedo8020.app.mainui.activities.MainActivity
import com.wedo8020.app.mainui.home.exchangescreen.roomdb.InAppDataEntity
import com.wedo8020.app.mainui.home.exchangescreen.roomdb.InAppDbCreation
import com.wedo8020.app.mainui.home.exchangescreen.roomdb.InAppDbRepo
import com.wedo8020.app.repo.RemoteRepositaryImp
import com.wedo8020.app.request.PremiumPurchasePayLoad
import com.wedo8020.app.request.PurchaseSrcPayload
import com.wedo8020.app.request.PurchaseTreesPayload
import com.wedo8020.app.response.BaseResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Objects


class PurchaseManager(private val context: Context) : PurchasesUpdatedListener {
    private lateinit var billingClient: BillingClient
    private val authenticationRepository = RemoteRepositaryImp()
    var formattedPriceDetail: String? = null
    var price: Double? = null
    var countryCode: String? = null
    var dialog: Dialog? = null
    private var aiAvailable: Boolean? = null
    private var blueTickAvailable: Boolean? = null
    private var noAdsAvailable: Boolean? = null
    private var purchaseTypeIs: PurchaseType? = null
    private var treesPlanted: Int? = null
    private var countryName: String? = null
    private var wdCount: Int? = null
    private var purchaseId: String? = null
    var dialogs: Dialog? = null
    var receiptDetails: PlantItem? = null
    var receiptDetail : PlantItem? = null
    private lateinit var database: InAppDbCreation
    private lateinit var repository: InAppDbRepo
    var purchaseData : PurchaseManager.PurchaseDetailsValue?=null

    var priceCallback: PurchasePriceCallback? = null
    var purchaseViewCallback: PurchaseViewCallback? = null
    private val mainActivity: MainActivity? = context as? MainActivity

    fun purchaseItem(
        id: String,
        purchaseType: PurchaseType? = null,
        aiEnable: Boolean? = null,
        blueTickEnable: Boolean? = null,
        noAdsEnable: Boolean? = null,
        trees: Int? = null,
        country: String? = null,
        wd: Int? = null
    ) {

        dialog = Dialog(context)
        dialog?.requestWindowFeature(Window.FEATURE_CONTENT_TRANSITIONS)



        aiAvailable = aiEnable
        blueTickAvailable = blueTickEnable
        noAdsAvailable = noAdsEnable
        purchaseTypeIs = purchaseType
        treesPlanted = trees
        countryName = country
        purchaseId = id
        database = InAppDbCreation(context)
        repository = InAppDbRepo(database)
        dialogs = Dialog(context)
        dialogs?.requestWindowFeature(Window.FEATURE_CONTENT_TRANSITIONS)
        database= InAppDbCreation(context)
        repository = InAppDbRepo(database)
        Log.d(
            "productDetailList",
            "ai is : $aiAvailable, noAds is : $noAdsEnable, blue tick is: $blueTickAvailable "
        )
        mainActivity?.endBillingClientConnection()
        billingClient = BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases()
            .build()
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d("productDetailList", "Response code match")


                    val queryProductDetailsParams =
                        QueryProductDetailsParams.newBuilder()
                            .setProductList(
                                ImmutableList.of(
                                    QueryProductDetailsParams.Product.newBuilder()
                                        .setProductId(id)
                                        .setProductType(BillingClient.ProductType.INAPP)
                                        .build()
                                )
                            )
                            .build()
                    Log.d("productDetailList", queryProductDetailsParams.toString())

                    CoroutineScope(Dispatchers.Main).launch {
                        showProgressDialog()
                    }
                    billingClient.queryProductDetailsAsync(queryProductDetailsParams) { billingResult,
                                                                                        productDetailsList ->

                        Log.d(
                            "DetailList",
                            "i am here ${productDetailsList[0].oneTimePurchaseOfferDetails?.formattedPrice}"
                        )
                        val productDetailsParamsList = listOf(
                            BillingFlowParams.ProductDetailsParams.newBuilder()
                                // retrieve a value for "productDetails" by calling queryProductDetailsAsync()
                                .setProductDetails(productDetailsList[0])
                                // to get an offer token, call ProductDetails.subscriptionOfferDetails()
                                // for a list of offers that are available to the user

                                .build()
                        )
                        formattedPriceDetail =
                            productDetailsList[0].oneTimePurchaseOfferDetails?.formattedPrice


                        Log.d(
                            "productDetailList",
                            " param list is ${productDetailsList.size}"
                        )




// Check if the latest purchase is not null
//                        if (latestPurchase != null) {
//                            // Access the details of the latest purchase
//                            val productId = latestPurchase.productId
//                            val purchaseToken = latestPurchase.purchaseToken
//                            val purchaseTime = latestPurchase.purchaseTime
//                            val quantity = latestPurchase.quantity
//                            val developerPayload = latestPurchase.developerPayload

                        val billingFlowParams = BillingFlowParams.newBuilder()
                            .setProductDetailsParamsList(productDetailsParamsList)
                            .build()

                        val amountWithRupees =
                            productDetailsList[0].oneTimePurchaseOfferDetails?.formattedPrice
                        val amountWithoutRupees =
                            amountWithRupees?.replace("₹", "")?.replace(",", "")
                        price = amountWithoutRupees?.toDouble()
                        // formattedPriceDetail?.let { purchaseViewModel.setFormattedPrice(it) }
                        countryCode =
                            productDetailsList[0].oneTimePurchaseOfferDetails?.priceCurrencyCode
                        wdCount = wd



                        billingClient.launchBillingFlow(
                            this@PurchaseManager.context as Activity,
                            billingFlowParams
                        )



                            CoroutineScope(Dispatchers.IO).launch {


                                    repository.inAppDataDetails(
                                        InAppDataEntity(
                                            id = purchaseId.toString(),
                                            purchaseType = purchaseTypeIs,
                                            aiEnable = aiAvailable,
                                            blueTickEnable = blueTickAvailable,
                                            noAdsEnable = noAdsAvailable,
                                            trees = treesPlanted,
                                            country = countryName,
                                            formattedPrice = "$countryCode $formattedPriceDetail",
                                            price = amountWithoutRupees?.toDouble(),
                                            wd = wdCount,
                                            orderId = "",
                                            productId = "",
                                            transactionId = "",
                                            transactionTime = ""
                                        )
                                    )


                                }
                            }






                }
            }

            override fun onBillingServiceDisconnected() {
                Log.d("error", "there is some error processing it ")
            }
        })
    }


    private suspend fun handlePurchase(purchase: Purchase) {
        Log.d("purchaseType11", "Purchases $purchase")
        if (purchase.purchaseState == PurchaseState.PURCHASED) {
            val consumeParams =
                ConsumeParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
            val consumeResult = withContext(Dispatchers.IO) {
                billingClient.consumePurchase(consumeParams)
            }
        } else if (purchase.purchaseState == PurchaseState.PENDING) {

            Log.d("purchaseType11", "Purchases pending")
        }

    }



    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {


//        val myData = WorkerData(blueTickEnable  = blueTickAvailable, aiEnable = aiAvailable,
//            noAdsEnable = noAdsAvailable, localizedPrice = "$countryCode $formattedPriceDetail", amount = price,  )
//
//        if (purchases != null) {
//            for (purchase in purchases) {
//
//                purchaseData = PurchaseDetailsValue(purchase)
//                receiptDetail =
//                    PlantItem(
//                        productId = purchase.products.joinToString(),
//                        orderId = purchase.purchaseToken,
//                        transactionDate = purchase.purchaseTime.toString(),
//                        transactionId = purchase.orderId
//                    )
//            }
//        }
//
//
//
//        val gson = Gson()
//        val myDataJson = gson.toJson(myData)
//        val myReceiptDetails = gson.toJson(receiptDetail)
//        val purchasesData = gson.toJson(purchaseData)
//        val inputData = Data.Builder()
//            .putString("purchaseTypeIs", purchaseTypeIs.toString())
//            .putString("responseCode", result.responseCode.toString())
//            .putString("myDataJson", myDataJson)
//            .putString("purchase", purchasesData)
//            .putString("myReceiptDetails", myReceiptDetails)
//            .putString("purchaseId", purchaseId)
//            .build()
//
//        val constraints = Constraints.Builder()
//            .setRequiredNetworkType(NetworkType.CONNECTED)
//            .build()
//
//        val workRequest = OneTimeWorkRequest.Builder(PurchaseWorker::class.java)
//            .setInputData(inputData)
//            .setConstraints(constraints)
//            .build()
//
//        WorkManager.getInstance(context).enqueue(workRequest)

        Log.d("purchaseType11", "Purchases updated hahahahah ${result.responseCode} ${BillingClient.BillingResponseCode.OK}")
        when (result.responseCode) {

                    BillingClient.BillingResponseCode.OK -> {

                Log.d("successApi", purchases.toString())
                if (purchases != null) {

                    CoroutineScope(Dispatchers.Main).launch {
                        showProgressDialog()
                    }
                    Log.d("purchaseType11", "Purchases updated 1")
                    Log.d("PurchasesLoop", "Purchases updated ${purchases[0]}")
                    CoroutineScope(Dispatchers.IO).launch {


                            handlePurchase(purchases[0])
                            receiptDetails =
                                PlantItem(
                                    productId = purchases[0].products.joinToString(),
                                    orderId = purchases[0].purchaseToken,
                                    transactionDate = purchases[0].purchaseTime.toString(),
                                    transactionId = purchases[0].orderId
                                )

                        Log.d("purchaseType11", "Purchases updated 2")
                        fun getPlantItemsJson(plantItems: PlantItem): String {
                            val gson = Gson()
                            return gson.toJson(plantItems)
                        }


                        when (purchaseTypeIs) {


                            PurchaseType.PremiumPurchases -> {
                                CoroutineScope(Dispatchers.Main).launch {
                                    showProgressDialog()
                                }

                                priceCallback?.onPriceUpdated(formattedPriceDetail.toString())
                                Log.d("purchaseType", "I am in Premium and receipt is ${
                                    receiptDetails?.let {
                                        getPlantItemsJson(
                                            it
                                        )
                                    }
                                }")

                                authenticationRepository.premiumPurchasesApi(
                                    PremiumPurchasePayLoad(
                                        blueTickEnable = blueTickAvailable,
                                        aiEnable = aiAvailable,
                                        noAdsEnable = noAdsAvailable,
                                        localizedPrice = "$countryCode $formattedPriceDetail",

                                        receipt = receiptDetails?.let { getPlantItemsJson(it) },
                                        amount = price
                                    )
                                )


                                { isSuccess: Boolean, baseResponse: BaseResponse ->

                                    if (isSuccess) {
                                        billingClient.endConnection()
                                        Log.d(
                                            "purchase111111111",
                                            "premium purchase done $baseResponse"
                                        )
                                        CoroutineScope(Dispatchers.IO).launch {
                                            repository.deleteAllInAppData()
                                        }


                                        CoroutineScope(Dispatchers.Main).launch {
                                            dialogs?.dismiss()






                                            Log.d("purchaseId", purchaseId.toString())
                                            if (purchaseId == "com.wedo.noads.yearly") {
                                                Log.d("purchaseId", "1 year only")
                                                purchaseViewCallback?.noAdsOneYear()
                                            } else if (purchaseId == "com.wedo.noads.lifetime") {
                                                Log.d("purchaseId", "lifetime")
                                                purchaseViewCallback?.noAdsLifeTimeClick()
                                            } else if (purchaseId == "com.wedo.verify.yearly") {
                                                Log.d("purchaseId", "lifetime")
                                                purchaseViewCallback?.verifiedTickOneYear()
                                            } else if (purchaseId == "com.wedo.verify.lifetime") {
                                                Log.d("purchaseId", "lifetime")
                                                purchaseViewCallback?.verifiedTickLifeTime()
                                            }


                                        }

                                    }
                                    else {
                                        CoroutineScope(Dispatchers.Main).launch {
                                            dialogs?.dismiss()


                                        }
                                    }
                                }
                            }

                            PurchaseType.PlantPurchases -> {
                                CoroutineScope(Dispatchers.Main).launch {
                                    showProgressDialog()
                                }
                                Log.d("purchaseType", "I am in Plant Purchases")
                                authenticationRepository.purchaseTreeApi(
                                    PurchaseTreesPayload(
                                        tree = treesPlanted,
                                        country = countryName,
                                        localizedPrice = "$countryCode $formattedPriceDetail",

                                        receipt = receiptDetails?.let { getPlantItemsJson(it) },
                                        amount = price
                                    )
                                )

                                //val amount : Double?=null, val tree : Int? = null,val receipt : String?=null,
                                // val localizedPrice : String?= null, val country: String?=null

                                { isSuccess: Boolean, baseResponse: BaseResponse ->

                                    if (isSuccess) {

                                        billingClient.endConnection()
                                        CoroutineScope(Dispatchers.IO).launch {
                                            repository.deleteAllInAppData()
                                        }
                                        CoroutineScope(Dispatchers.Main).launch {
                                            dialogs?.dismiss()

                                            val navController = Navigation.findNavController(context as Activity, R.id.fragmentContainerView)
                                            navController.navigate(R.id.xchangeFragment)
                                        }

                                        Log.d("successApi1", " Plant purchases is success")
                                        // mutableBaseResponse.postValue(baseResponse)
                                    } else {
                                        Log.d("successApi1", " Plant purchase is not success")
                                        CoroutineScope(Dispatchers.Main).launch {
                                            dialogs?.dismiss()


                                        }
                                    }
                                }

                            }

                            PurchaseType.SrcPurchases -> {
                                CoroutineScope(Dispatchers.Main).launch {
                                    showProgressDialog()
                                }
                                Log.d("purchaseType", "I am in Src Purchases")
                                authenticationRepository.purchaseSrcApi(
                                    PurchaseSrcPayload(
                                        wd = wdCount,
                                        localizedPrice = "$countryCode $formattedPriceDetail",

                                        receipt = receiptDetails?.let { getPlantItemsJson(it) },
                                        amount = price
                                    )
                                )


                                { isSuccess: Boolean, baseResponse: BaseResponse ->

                                    if (isSuccess) {

                                        billingClient.endConnection()
                                        CoroutineScope(Dispatchers.IO).launch {
                                            repository.deleteAllInAppData()
                                        }
                                        CoroutineScope(Dispatchers.Main).launch {
                                            dialogs?.dismiss()


                                        }

                                        Log.d("successApi1", " Src purchase is success")
                                        // mutableBaseResponse.postValue(baseResponse)
                                    } else {
                                        Log.d("successApi1", " Src purchase is not success")
                                        CoroutineScope(Dispatchers.Main).launch {
                                            dialogs?.dismiss()


                                        }
                                    }
                                }
                            }

                            else -> {
                                purchaseTypeIs = PurchaseType.None
                            }
                        }


                    }
                }


            }


            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                dialogs?.dismiss()
                Log.d("TransactionSuccess", "item already owned")
                CoroutineScope(Dispatchers.IO).launch {
                    repository.deleteAllInAppData()
                }

            }

            else -> {
                dialogs?.dismiss()
                Log.d("TransactionSuccess", "Billing response not OK")
                CoroutineScope(Dispatchers.IO).launch {
                    repository.deleteAllInAppData()
                }
            }
        }
    }




    interface PurchaseViewCallback {
        fun noAdsLifeTimeClick()
        fun noAdsOneYear()

        fun verifiedTickOneYear()

        fun verifiedTickLifeTime()
    }

    private fun showProgressDialog() {
        dialogs?.setCancelable(false)
        Objects.requireNonNull(dialogs?.window)?.setBackgroundDrawable(
            ColorDrawable(
                Color.TRANSPARENT
            )
        )
        dialogs?.setContentView(R.layout.progressbar)
        dialogs?.show()
    }

    interface PurchasePriceCallback {
        fun onPriceUpdated(formattedPrice: String)
    }




    data class PlantItem(
        val productId: String,
        val transactionId: String?,
        val transactionDate: String?,
        val orderId: String?
    )


    data class PurchaseDetailsValue(val purchase : Purchase?=null)

    data class WorkerData(val blueTickEnable : Boolean? = null, val aiEnable : Boolean?=null, val purchase : Purchase?=null,
                          val noAdsEnable : Boolean?= null, val localizedPrice : String?= null,
                          val receipt : String?=null, val amount : Double?=null, val transactionId: String?=null, val productId:String?=null)
}
