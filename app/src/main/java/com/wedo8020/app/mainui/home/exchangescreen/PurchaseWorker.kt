package com.wedo8020.app.mainui.home.exchangescreen

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.Window
import android.widget.TextView
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchaseHistoryParams
import com.android.billingclient.api.consumePurchase
import com.android.billingclient.api.queryPurchaseHistory
import com.google.common.collect.ImmutableList
import com.google.gson.Gson
import com.wedo8020.app.MainApplication
import com.wedo8020.app.R
import com.wedo8020.app.repo.RemoteRepositaryImp
import com.wedo8020.app.request.PurchaseSrcPayload
import com.wedo8020.app.response.BaseResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Objects

class
PurchaseWorker(val appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {
    private lateinit var billingClient: BillingClient
    private val authenticationRepository = RemoteRepositaryImp()
    var formattedPriceDetail: String? = null
    var price: Double? = null
    var countryCode: String? = null
    var dialog: Dialog? = null
    private var aiAvailable: Boolean? = null
    private var blueTickAvailable: Boolean? = null
    private var noAdsAvailable: Boolean? = null
   // private var purchaseTypeIs: PurchaseType? = null
    private var treesPlanted: Int? = null
    private var countryName: String? = null
    private var wdCount: Int? = null
    private var purchaseId: String = ""
    var dialogs: Dialog? = null
    var receiptDetailsInString : String ?=null
    private var apiResponse : Boolean = false

    private var receiptDetails: PlantItem? = null



    override fun doWork(): Result {
        Log.d("workerWorking", "Worker class is called")



        val srcValue = inputData.getString("srcValue")
        val id = inputData.getString("id")
        var purchaseTypeIs = inputData.getString("purchaseType")
        if (id != null) {
            purchaseId = id
        }
        wdCount = srcValue?.toInt()



        Log.d("workerWorkingPurchases", "Worker class data $srcValue $purchaseId $purchaseTypeIs")



        val purchasesUpdatedListener =
            PurchasesUpdatedListener { billingResult, purchases ->
                Result.retry()
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                    Log.d("workerWorkingResponse", "Response code is success")
                        CoroutineScope(Dispatchers.IO).launch {
                            handlePurchase(purchases[0])



                        }
                    receiptDetails =
                        PlantItem(
                            productId = purchases[0].products.joinToString(),
                            orderId = purchases[0].purchaseToken,
                            transactionDate = purchases[0].purchaseTime.toString(),
                            transactionId = purchases[0].orderId
                        )
                    Log.d("workerWorkingPurchases", "Purchases updated io $receiptDetails")
                    when (purchaseTypeIs) {
                        PurchaseType.SrcPurchases.name -> {
                            fun getPlantItemsJson(plantItems: PlantItem): String {
                                val gson = Gson()
                                return gson.toJson(plantItems)
                            }
                            CoroutineScope(Dispatchers.Main).launch {
                               // showProgressDialog()
                              //  receiptDetailsInString = receiptDetails?.let { getPlantItemsJson(it) }
                            }
                           receiptDetailsInString = receiptDetails?.let { getPlantItemsJson(it) }

                            Log.d("workerWorkingPurchases", "I am in Src Purchases $receiptDetailsInString")
                            authenticationRepository.purchaseSrcApi(
                                PurchaseSrcPayload(
                                    wd = wdCount,
                                    localizedPrice = "$countryCode $formattedPriceDetail",

                                    receipt = getPlantItemsJson(receiptDetails!!),
                                    amount = price
                                )
                            )


                            { isSuccess: Boolean, baseResponse: BaseResponse ->

                                if (isSuccess) {
                                    apiResponse = true
                                    billingClient.endConnection()
                                    Result.success()

                                    Log.d("workerWorkingPurchases", " Src purchase is success")
                                    // mutableBaseResponse.postValue(baseResponse)
                                } else {
                                    Log.d("workerWorkingPurchases", " ${baseResponse.message}Src purchase is not success ${baseResponse.data}")
                                    Result.retry()


                                }
                            }
                        }

                        else -> {
                            purchaseTypeIs = PurchaseType.None.name
                        }
                    }

                    }

                else if(billingResult.responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED && purchases != null){
                    Log.e("PurchasesUpdatedListener", "Failed to update purchases: ${ purchases[0].purchaseState}")
                    val params = QueryPurchaseHistoryParams.newBuilder()
                        .setProductType(BillingClient.ProductType.INAPP)

                    CoroutineScope(Dispatchers.IO).launch {
                        val purchaseHistoryResult = billingClient.queryPurchaseHistory(params.build())
                        Log.e("PurchasesUpdatedListener", "Purchase history is $purchaseHistoryResult")
                    }




                    Result.retry()
                }


                else {
                    Log.e("PurchasesUpdatedListener", "Failed to update purchases: ${billingResult.debugMessage}")
                    Result.retry()
                    Log.e("PurchasesUpdatedListener", "Failed to update purchases: ${billingResult.debugMessage}")
                }
            }

        billingClient = BillingClient.newBuilder(appContext)
            .setListener(purchasesUpdatedListener)
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
                                        .setProductId(purchaseId)
                                        .setProductType(BillingClient.ProductType.INAPP)
                                        .build()
                                )
                            )
                            .build()
                    Log.d("productDetailList", queryProductDetailsParams.toString())


                    billingClient.queryProductDetailsAsync(queryProductDetailsParams) { billingResult,
                                                                                        productDetailsList ->

                        Log.d(
                            "DetailList",
                            "i am here ${productDetailsList[0].oneTimePurchaseOfferDetails?.formattedPrice}"
                        )
                        val productDetailsParamsList = listOf(
                            BillingFlowParams.ProductDetailsParams.newBuilder()

                                .setProductDetails(productDetailsList[0])


                                .build()
                        )
                        formattedPriceDetail =
                            productDetailsList[0].oneTimePurchaseOfferDetails?.formattedPrice


                        Log.d(
                            "productDetailList",
                            " param list is ${productDetailsList.size}"
                        )



                        val billingFlowParams = BillingFlowParams.newBuilder()
                            .setProductDetailsParamsList(productDetailsParamsList)
                            .build()

                        val amountWithRupees =
                            productDetailsList[0].oneTimePurchaseOfferDetails?.formattedPrice
                        val amountWithoutRupees =
                            amountWithRupees?.replace("₹", "")?.replace(",", "")
                        price = amountWithoutRupees?.toDouble()

                        countryCode =
                            productDetailsList[0].oneTimePurchaseOfferDetails?.priceCurrencyCode
                        //wdCount = wd


                        MainApplication.get().appLifecycleObserver.activity?.let {
                            billingClient.launchBillingFlow(
                                it,
                                billingFlowParams
                            )
                        }




                    }






                }
            }

            override fun onBillingServiceDisconnected() {
                Log.d("error", "there is some error processing it ")
            }
        })
        return  Result.success()

    }


//    private fun getPlantItemsJson(plantItems: PlantItem): String {
//        val gson = Gson()
//        return gson.toJson(plantItems)
//    }



    private suspend fun handlePurchase(purchase: Purchase) {
        Log.d("purchaseType11", "Purchases $purchase")
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            val consumeParams =
                ConsumeParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
            val consumeResult = withContext(Dispatchers.IO) {
                billingClient.consumePurchase(consumeParams)
            }
        } else if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {

            Log.d("purchaseType11", "Purchases pending")
        }

    }
    private fun thanksDialogForDownload(text: String) {
        dialog = Dialog(MainApplication.get().applicationContext)
        dialog?.requestWindowFeature(Window.FEATURE_CONTENT_TRANSITIONS)
        dialog?.setCancelable(false)
        Objects.requireNonNull(dialog?.window)?.setBackgroundDrawable(
            ColorDrawable(
                Color.TRANSPARENT
            )
        )
        dialog?.setContentView(R.layout.congratulations_pop_up)

        val logout = dialog?.findViewById(R.id.Logout) as TextView
        val plantText = dialog?.findViewById(R.id.ivAlert) as TextView

        plantText.text = "Thank you for planting trees. $text has been added to your wallet."

        logout.setOnClickListener {
            dialog?.dismiss()
        }
        dialog?.show()
    }

    interface PurchaseViewCallback {
        fun noAdsLifeTimeClick()
        fun noAdsOneYear()

        fun verifiedTickOneYear()

        fun verifiedTickLifeTime()
    }

    private fun showProgressDialog() {
        dialogs = Dialog(appContext)
        dialogs?.requestWindowFeature(Window.FEATURE_CONTENT_TRANSITIONS)
        dialogs?.apply {
            setCancelable(false)
            window?.apply {
                setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            }
            setContentView(R.layout.progressbar)
            show()
        }
    }
    data class PlantItem(
        val productId: String,
        val transactionId: String?,
        val transactionDate: String?,
        val orderId: String?
    )

    interface PurchasePriceCallback {
        fun onPriceUpdated(formattedPrice: String)
    }



}



