package com.wedo8020.app.mainui.home.exchangescreen

import android.app.Dialog
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.consumePurchase
import com.google.common.collect.ImmutableList
import com.google.gson.Gson
import com.wedo8020.app.mainui.activities.MainActivity
import com.wedo8020.app.repo.RemoteRepositaryImp
import com.wedo8020.app.request.PurchaseSrcPayload
import com.wedo8020.app.response.BaseResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.Serializable

private const val FOREGROUND_NOTIFICATION_ID = 1001
class PurchaseService() : Service() {

    companion object {
        const val ACTION_START_PURCHASE = "ACTION_START_PURCHASE"
        // Define more actions if needed
    }

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
    private var receiptDetails: PlantItem? = null
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val serviceContext = this
        Log.d("service!23333", "Service is getting selected")
        intent?.extras.let {
            wdCount = it?.get("wdCount") as Int?
            purchaseTypeIs= it?.get("purchaseType") as PurchaseType?
            purchaseId= it?.get("id") as String
        }
        when (intent?.action) {

            ACTION_START_PURCHASE -> handleStartPurchase(this)

        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun handleStartPurchase(context : Context) {

        Log.d("serviceInApp", "In App connection is started")
        billingClient = BillingClient.newBuilder(this)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()

        startForegroundService(context)
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {

                    val queryProductDetailsParams =
                        QueryProductDetailsParams.newBuilder()
                            .setProductList(
                                ImmutableList.of(
                                    QueryProductDetailsParams.Product.newBuilder()
                                        .setProductId(purchaseId.toString())
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


                        Log.d("productDetailList", "$countryCode")

//                        billingClient.launchBillingFlow(
//                            context as Activity,
//                            billingFlowParams
//                        )
                        createBillingFlowIntent(context, billingFlowParams)
                        Log.d("productDetailList", " after $countryCode")
                    }
                }
            }

            override fun onBillingServiceDisconnected() {
                // Handle billing service disconnected
            }
        })
    }

    // Handle purchase result
    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            CoroutineScope(Dispatchers.IO).launch {
                handlePurchase(purchases[0])
                receiptDetails = PlantItem(
                        productId = purchases[0].products.joinToString(),
                        orderId = purchases[0].purchaseToken,
                        transactionDate = purchases[0].purchaseTime.toString(),
                        transactionId = purchases[0].orderId
                    )

                Log.d("purchaseType11", "Purchases updated 2")

            }

            when (purchaseTypeIs) {
                PurchaseType.SrcPurchases -> {
//                    CoroutineScope(Dispatchers.Main).launch {
//                        showProgressDialog()
//                    }
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


                            Log.d("successApi1", " Src purchase is success")
                            // mutableBaseResponse.postValue(baseResponse)
                        } else {
                            Log.d("successApi1", " Src purchase is not success")

                        }
                    }
                }

                else -> {
                    purchaseTypeIs = PurchaseType.None
                }
            }

        }


        else {
            // Handle failed purchases or other errors
            Log.e("PurchasesUpdatedListener", "Failed to update purchases: ${billingResult.debugMessage}")
        }

    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    data class BillingFlowParamsWrapper(val billingFlowParamsJson: String) : Serializable

    private fun createBillingFlowIntent(context: Context, billingFlowParams: BillingFlowParams): PendingIntent {
        val intent = Intent(context, MainActivity::class.java)


        val gson = Gson()
        val paramsJson = gson.toJson(billingFlowParams)

        val wrapper = BillingFlowParamsWrapper(paramsJson)
        intent.putExtra("BILLING_FLOW_PARAMS", wrapper)

        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }
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
    private fun startForegroundService(context: Context) {
        val notification = createNotification(context)
        startForeground(FOREGROUND_NOTIFICATION_ID, notification)
    }
    private fun createNotification(context: Context): Notification {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "Your_Channel_ID"
            val channelName = "Your_Channel_Name"
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val pendingIntent = PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_ONE_SHOT)


        val notificationBuilder = NotificationCompat.Builder(context, "Your_Channel_ID")
            .setContentTitle("Your Notification Title")
            .setContentText("Your Notification Text")
            //.setSmallIcon(R.drawable.ic_notification_icon) // Replace with your notification icon
            .setContentIntent(pendingIntent)
            .setAutoCancel(true) // Dismiss the notification when tapped
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)


        return notificationBuilder.build()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up resources if needed
    }

    data class PlantItem(
        val productId: String,
        val transactionId: String?,
        val transactionDate: String?,
        val orderId: String?
    )

    fun getPlantItemsJson(plantItems: PlantItem): String {
        val gson = Gson()
        return gson.toJson(plantItems)
    }

}
