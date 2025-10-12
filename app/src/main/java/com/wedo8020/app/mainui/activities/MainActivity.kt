package com.wedo8020.app.mainui.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchaseHistoryRecord
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.wedo8020.app.MainApplication
import com.wedo8020.app.R
import com.wedo8020.app.databinding.ActivityMainBinding
import com.wedo8020.app.mainui.base.BaseActivity
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.wedo8020.app.eventBus.HomeTabClick
import com.wedo8020.app.eventBus.InAppNotification
import com.wedo8020.app.mainui.dialogFragments.RemindMeBottomSheet
import com.wedo8020.app.mainui.home.createpost.CreatePostViewModel
import com.wedo8020.app.mainui.home.createpost.CreatePostViewModelFactory
import com.wedo8020.app.mainui.home.exchangescreen.InAppConstants
import com.wedo8020.app.mainui.home.exchangescreen.PurchaseManager
import com.wedo8020.app.mainui.home.exchangescreen.PurchaseType
import com.wedo8020.app.mainui.home.exchangescreen.PurchasesViewModel
import com.wedo8020.app.mainui.home.exchangescreen.roomdb.InAppDataEntity
import com.wedo8020.app.mainui.home.exchangescreen.roomdb.InAppDbCreation
import com.wedo8020.app.mainui.home.exchangescreen.roomdb.InAppDbRepo
import com.wedo8020.app.mainui.home.postFragment.PostViewModel
import com.wedo8020.app.mainui.login.switchaccounts.SwitchAccountFragment
import com.wedo8020.app.repo.RemoteRepositaryImp
import com.wedo8020.app.request.FcmRequest
import com.wedo8020.app.request.PremiumPurchasePayLoad
import com.wedo8020.app.response.BaseResponse
import com.wedo8020.app.response.Data
import com.wedo8020.app.response.ImpactData
import com.wedo8020.app.roomdb.CreatePostDbCreation
import com.wedo8020.app.roomdb.CreatePostDbRepo
import com.wedo8020.app.utils.ActivePostUpdate
import com.wedo8020.app.utils.AppConstant
import com.wedo8020.app.utils.AppConstant.REQUEST_CODE_APP_UPDATE
import com.wedo8020.app.utils.NewMessage
import com.wedo8020.app.utils.NewProfileUpdated
import com.wedo8020.app.utils.NotificationType
import com.wedo8020.app.utils.Prefs
import com.wedo8020.app.utils.Utils
import com.wedo8020.app.utils.Utils.disableMultiTap
import com.wedo8020.app.utils.Utils.loadImageAsync
import com.wedo8020.zelpyy.SocketProvider.SocketIOImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import java.util.Objects


class MainActivity : BaseActivity(), PurchasesUpdatedListener,
    RemindMeBottomSheet.RemindMeBottomSheetListener {
    private val TAG = MainActivity::class.java.name
    private lateinit var binding: ActivityMainBinding
    lateinit var navController: NavController

    //    lateinit var wl: WakeLock
    private lateinit var database: CreatePostDbCreation
    private lateinit var repository: CreatePostDbRepo
    private lateinit var factory: CreatePostViewModelFactory
    private lateinit var viewModel: CreatePostViewModel
    private lateinit var purchaseManager: PurchaseManager
    private lateinit var viewModel2: PurchasesViewModel
    private lateinit var billingClient: BillingClient
    private lateinit var inAppDatabase: InAppDbCreation
    private lateinit var inAppRepository: InAppDbRepo
    var databaseData: ArrayList<InAppDataEntity>? = arrayListOf()
    var impactData: ImpactData? = null
    private lateinit var socket: SocketIOImpl
    var dialog: Dialog? = null
    var dialogs: Dialog? = null
    var productId: String? = null
    private var runningDialog: Dialog? = null
    private var sortedData: List<PurchaseHistoryRecord>? = listOf()
    private val authenticationRepository = RemoteRepositaryImp()
    private var purchaseViewCallback: MainActivity.PurchaseViewCallback? = null
    var inAppProductsPrice: HashMap<String, String> = hashMapOf()
    var registerNetworkConnected = true
    val durationMillis: Long = 3600000
    var dropList = ArrayList<Data?>()
    var followingList = ArrayList<Data?>()
    private var profileTabImage: String? = null
    var currentDrop = "drop"
    var currentDropPosition = 0
    var currentFollowingPosition = 0
    private val postViewModel by lazy {
        ViewModelProvider(this)[PostViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        adjustFontScale(resources.configuration, 1.0f)
        setStatusBarTransparent()
        purchaseManager = PurchaseManager(this)
        viewModel2 = ViewModelProvider(this)[PurchasesViewModel::class.java]

        dialogs = Dialog(this)
        dialogs?.requestWindowFeature(Window.FEATURE_CONTENT_TRANSITIONS)

        dialog = Dialog(this)
        dialog?.requestWindowFeature(Window.FEATURE_CONTENT_TRANSITIONS)

        runningDialog = Dialog(this)
        runningDialog?.requestWindowFeature(Window.FEATURE_CONTENT_TRANSITIONS)
        viewModel2.impactDetails()
        setUpUI(this)

        inAppDatabase = InAppDbCreation(this)
        inAppRepository = InAppDbRepo(inAppDatabase)
        /** Check for update availability*/
        // Inside your activity or fragment
        val appUpdateManager = AppUpdateManagerFactory.create(this)

// Returns an intent object that you use to check for an update.
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo

// Checks that the platform will allow the specified type of update.
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                // This example applies an immediate update. To apply a flexible update
                // instead, pass in AppUpdateType.FLEXIBLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            ) {
                updateAvailable()
            }
        }
        CoroutineScope(Dispatchers.IO).launch {
            databaseData = inAppRepository.getAllData() as ArrayList<InAppDataEntity>
        }


        billingClient = BillingClient.newBuilder(this)
            .enablePendingPurchases()
            .setListener(this)
            .build()


        val productIds = listOf(
            InAppConstants.VERIFIED_ACCOUNT_ONE_YEAR,
            InAppConstants.VERIFIED_ACCOUNT_LIFE_TIME,
            InAppConstants.NO_ADS_ONE_YEAR,
            InAppConstants.NO_ADS_LIFE_TIME,
            InAppConstants.AI_ONE_YEAR,
            InAppConstants.AI_LIFE_TIME,
            InAppConstants.ONE_PLANT_ID,
            InAppConstants.FIVE_PLANTS_ID,
            InAppConstants.TEN_PLANTS_ID,
            InAppConstants.ONE_SRC_ID,
            InAppConstants.FIVE_SRC_ID,
            InAppConstants.TEN_SRC_ID
        )
        CoroutineScope(Dispatchers.Main).launch {
            binding.profileImg.loadImageAsync(Prefs.init().userProfile)
        }


        fetchProductDetails(productIds)

        /**  here we set the image for profile */


//        binding.profileImg.setImageURI(Uri.parse(Prefs.init().userProfile))

//        billingClient.startConnection(object : BillingClientStateListener {
//
//
//            override fun onBillingSetupFinished(billingResult: BillingResult) {
//                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
//
//                    val params = QueryPurchaseHistoryParams.newBuilder()
//                        .setProductType(BillingClient.ProductType.INAPP)
//                    CoroutineScope(Dispatchers.IO).launch {
//                        val purchaseHistoryResult =
//                            billingClient.queryPurchaseHistory(params.build())
//
//                        val purchaseHistoryList =
//                            purchaseHistoryResult.purchaseHistoryRecordList
//
//                        sortedData =
//                            purchaseHistoryList?.sortedByDescending { it.purchaseTime }
//
//                        Log.d(
//                            "PurchaseRestarted",
//                            "${sortedData}"
//                        )
//
//
//                    }
//
//
//
//
//
//                }
//            }
//
//            override fun onBillingServiceDisconnected() {
//                TODO("Not yet implemented")
//            }
//        })


        retryPayment()


        viewModel2.mutableBaseResponse.observe(this) { impactDetails ->

            if (impactDetails.success == true) {

                impactData = impactDetails.data
                Log.d("ImpactData", impactDetails.data.toString())


            } else {

            }
        }
        if (!checkCameraPermission()) {
            // Permissions are already granted, proceed with your logic
            // ...
            requestCameraPermission()

        }

        callApi()
        generateFcmToken()
    }


    /*    override fun onBackPressed() {
            navController.let {

                Log.e("235435", "fdfsdfsdf")
                if (it.currentDestination?.id == R.id.profileFragment) {

                } else {
                    super.onBackPressed()
                }

            }
        }*/

    private fun checkCameraPermission(): Boolean {
        // Check if the CAMERA permission has been granted
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun requestCameraPermission() {
        // Request CAMERA permission
        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
            101
        )
    }

    private fun fetchProductDetails(ids: List<String>) {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                val productDetailsParamsList = ids.map { id ->
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(id)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
                }

                val queryProductDetailsParams = QueryProductDetailsParams.newBuilder()
                    .setProductList(productDetailsParamsList)
                    .build()

                billingClient.queryProductDetailsAsync(queryProductDetailsParams) { _, productDetailsList ->
                    val productPrices = productDetailsList.map {
                        Log.d(
                            "PurchasesListDoneHaa",
                            "${it.productId}  ${it.oneTimePurchaseOfferDetails?.formattedPrice}"
                        )
                        inAppProductsPrice[it.productId] =
                            it.oneTimePurchaseOfferDetails?.formattedPrice.toString()
                        it.oneTimePurchaseOfferDetails?.formattedPrice
                    }

                    Log.d("PurchasesListDoneHaa", "$productPrices")

                }

            }

            override fun onBillingServiceDisconnected() {
                Log.d("error", "There is some error processing it")

            }
        })
    }

    fun callApi() {
        val isInternetAvailable = isInternetAvailable(this)
        if (isInternetAvailable) {
            postViewModel.userDertails()
        }
    }

    fun endBillingClientConnection() {
        billingClient.endConnection()
    }


    private fun retryPayment() {
        databaseData?.let { data ->
            if (data.isNotEmpty()) {
                Log.d(
                    "PurchaseRestarted",
                    "Premium Purchase as there is pending purchase ${data.size}"
                )
                val firstPurchase = data.firstOrNull()

                when (firstPurchase?.purchaseType) {
                    PurchaseType.PremiumPurchases -> {
                        Log.d("PurchaseRestarted", "Premium Purchase is being done")

                        CoroutineScope(Dispatchers.Main).launch {
                            showProgressDialog()
                        }



                        authenticationRepository.premiumPurchasesApi(
                            PremiumPurchasePayLoad(
                                blueTickEnable = firstPurchase.blueTickEnable,
                                aiEnable = firstPurchase.aiEnable,
                                noAdsEnable = firstPurchase.noAdsEnable,
                                localizedPrice = firstPurchase.formattedPrice,
                                receipt = getPlantItemsJson(
                                    PlantItem(
                                        productId = firstPurchase.productId.toString(),
                                        transactionId = sortedData?.get(0)?.purchaseToken,
                                        transactionDate = sortedData?.get(0)?.purchaseTime.toString(),
                                        orderId = sortedData?.get(0)?.purchaseToken
                                    )
                                ),
                                amount = firstPurchase.price
                            )
                        ) { isSuccess: Boolean, baseResponse: BaseResponse ->
                            if (isSuccess) {
                                Log.d("purchase111111111", "premium purchase done $baseResponse")

                                CoroutineScope(Dispatchers.IO).launch {
                                    inAppRepository.deleteAllInAppData()
                                }

                                billingClient.endConnection()
                                CoroutineScope(Dispatchers.Main).launch {
                                    dialogs?.dismiss()
                                    thanksDialogForDownload("")
                                    productId?.let { Log.d("purchaseId", it) }
                                    when (productId) {
                                        "com.wedo.noads.yearly" -> purchaseViewCallback?.noAdsOneYear()
                                        "com.wedo.noads.lifetime" -> purchaseViewCallback?.noAdsLifeTimeClick()
                                        "com.wedo.verify.yearly" -> purchaseViewCallback?.verifiedTickOneYear()
                                        "com.wedo.verify.lifetime" -> purchaseViewCallback?.verifiedTickLifeTime()
                                    }
                                }
                            }
                        }
                    }


                    else -> {}
                }

            }
        }

    }


    private fun getPlantItemsJson(plantItems: PlantItem): String {
        val gson = Gson()
        return gson.toJson(plantItems)
    }

    private fun generateFcmToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            if (!it.isSuccessful) {
                Log.w(
                    "LoginFragment",
                    "Fetching FCM registration token failed",
                    it.exception
                )
                return@addOnCompleteListener
            }

            val token = it.result
            Log.w(
                "LoginFragment",
                "Fetching FCM registration token $token",

                )
            postViewModel.postFcmToken(FcmRequest((token)))
        }
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

    interface ProductPriceCallback {
        fun onPricesFetched(prices: List<String?>)
    }

    override fun onPause() {
        super.onPause()


    }

    override fun onResume() {
        super.onResume()
        Log.d("ImageUrlIs", "Image is : $profileTabImage")


    }


    @SuppressLint("SetTextI18n")
    private fun thanksDialogForDownload(text: String) {
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


    data class PlantItem(
        val productId: String,
        val transactionId: String?,
        val transactionDate: String?,
        val orderId: String?
    )
//    private fun fetchProductDetails(id: String, callback: ProductPriceCallback) {
//        billingClient.startConnection(object : BillingClientStateListener {
//            override fun onBillingSetupFinished(billingResult: BillingResult) {
//                val queryProductDetailsParams = QueryProductDetailsParams.newBuilder()
//                    .setProductList(
//                        ImmutableList.of(
//                            QueryProductDetailsParams.Product.newBuilder()
//                                .setProductId(id)
//                                .setProductType(BillingClient.ProductType.IN_APP)
//                                .build()
//                        )
//                    )
//                    .build()
//
//                billingClient.queryProductDetailsAsync(queryProductDetailsParams) { _, productDetailsList ->
//                    val productPrice = productDetailsList.firstOrNull()?.oneTimePurchaseOfferDetails?.formattedPrice
//                    callback.onPriceFetched(productPrice)
//                }
//            }
//
//            override fun onBillingServiceDisconnected() {
//                Log.d("error", "there is some error processing it ")
//                callback.onPriceFetched(null)
//            }
//        })
//    }

// How to use it:

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_APP_UPDATE) {
            if (resultCode != RESULT_OK) {
                // If the update is cancelled or fails, you might retry or handle it accordingly
                //   updateAvailable()
            }
        }
    }

    fun getPurchaseManager(): PurchaseManager {
        return purchaseManager
    }

    private fun setUpUI(context: Context) {
        socket = MainApplication.get().socket


        val navHostController =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment

        navController = navHostController.navController

        navController.let {
            Log.e("asd", it.currentDestination?.id.toString())
        }
        // handle redirection for deep link or notifications
        handleRedirection()
        setUpBottomNavigation(context)
        database = CreatePostDbCreation(this)
        repository = CreatePostDbRepo(database)
        factory = CreatePostViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[CreatePostViewModel::class.java]
        CoroutineScope(Dispatchers.IO).launch {
            if (database.getCreatePostData().fetchAllPost().isNotEmpty()) {
                database.getCreatePostData().fetchAllPost().forEach {
                    Utils.printLog(tag = "getCreatePostData", msg = it.toString())
                    viewModel.compressAndUploadVideoOrImages(it)
                }
            }
        }
    }

    private fun handleRedirection() {
        if (intent.hasExtra(AppConstant.CANONICAL_IDENTIFIER)) {
            val url = intent.getStringExtra(AppConstant.CANONICAL_IDENTIFIER)
            if (url?.contains("/") == true) {
                val str = url.split("/")
                val bundle = Bundle()
                when (str[0]) {
                    "post" -> {
                        bundle.putInt("position", 0)
                        bundle.putString("dataList", str[1])
                        navController.navigate(R.id.show_profile_post_fragment, bundle)
                    }

                    "user" -> {
                        if (str[1] != Prefs.init().userId) {
                            bundle.putString("mentionUser", str[1])
                            navController.navigate(R.id.otherProfileFragment, bundle)
                        } else {
                            navController.navigate(R.id.profileFragment)
                        }

                    }
                }

            }
        } else if (intent.hasExtra(AppConstant.NOTIFICATION_TYPE)) {
            val intentExtras = intent.extras
            val bundle = Bundle()
            when (intentExtras?.getString(AppConstant.NOTIFICATION_TYPE)) {
                NotificationType.FOLLOW_USER,
                NotificationType.MENTION_BIO -> {

                    if (intentExtras.getString("sender") == Prefs.init().userId) {
                        navController.navigate(R.id.profileFragment)
                    } else {
                        val userId = intentExtras.getString("sender")
                        bundle.putString("mentionUser", userId)
                        navController.navigate(R.id.otherProfileFragment, bundle)
                    }
                }

                NotificationType.FOLLOW_REQUEST -> {
                    bundle.putString("requestReceive", "requestReceive")
                    navController.navigate(R.id.requestSentAndReceiveFragment, bundle)
                }

                NotificationType.DAILY_AR,
                NotificationType.PERSONAL_INFO,
                NotificationType.BLUE_TICK_PURCHASE,
                NotificationType.WD_PURCHASE,
                NotificationType.WD_TRANSFER,
                NotificationType.TREE_BONUS,
                NotificationType.AI_BONUS,
                NotificationType.NO_ADS_BONUS -> {
                    bundle.putString("type", NotificationType.DAILY_AR)
                    navController.navigate(R.id.messageAndNotificationsFragment, bundle)
                }

                NotificationType.POST_LIKE,
                NotificationType.TAG_POST,
                NotificationType.MENTION_POST,
                NotificationType.COMMENT,
                NotificationType.MENTION_COMMENT,
                NotificationType.COMMENT_LIKE,
                NotificationType.COMMENT_REPLY,
                NotificationType.NEW_POST,
                -> {

                    val postId = intentExtras.getString("post")
                    bundle.putInt("position", 0)
                    bundle.putString("dataList", postId)
                    navController.navigate(R.id.show_profile_post_fragment, bundle)
                }

                NotificationType.MESSAGE,
                NotificationType.REACT -> {
                    bundle.apply {
                        putString("userChatId", intentExtras.getString("userChatId"))
                        putString("otherUserName", intentExtras.getString("otherUserName"))
                        putString("otherUserProfile", intentExtras.getString("otherUserProfile"))
                        putBoolean("blueTick", intentExtras.getBoolean("blueTick"))
                        navController.navigate(R.id.showChatFragment, this)
                    }
                }
            }
        }
    }

    private fun updateAvailable() {
        dialog = Dialog(this)
        dialog?.requestWindowFeature(Window.FEATURE_CONTENT_TRANSITIONS)
        dialog?.setCancelable(false)
        Objects.requireNonNull(dialog?.window)?.setBackgroundDrawable(
            ColorDrawable(
                Color.TRANSPARENT
            )
        )
        dialog?.setContentView(R.layout.update_available)

        val updateNow = dialog?.findViewById(R.id.updateNow) as TextView
        val tvNotNow = dialog?.findViewById(R.id.tvNotNow) as TextView

        tvNotNow.setOnClickListener {
            dialog?.dismiss()
        }
        updateNow.setOnClickListener {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=com.wedo8020.app")
            )
            startActivity(intent)
            dialog?.dismiss()
        }
        dialog?.show()
    }

    fun setUpBottomNavigation(context: Context) {

        binding.apply {
            homeTab.setOnClickListener {
                it.disableMultiTap()
                homeTabClick()
            }
            xChangeTab.setOnClickListener {
                xChangeTab.disableMultiTap()
                xChangeTabClick()
            }
            uploadTab.setOnClickListener {
                uploadTabClick(context)
            }
            messagingTab.setOnClickListener {
                messagingTabClick()
            }
            profileTab.setOnClickListener {
                profileTabClick()

            }

            profileTab.setOnLongClickListener {
                showSwitchAccountBottomSheet()
                true
            }
        }
    }


    private fun showSwitchAccountBottomSheet() {
        val bottomSheetFragment = SwitchAccountFragment(R.layout.fragment_switch_account)
        bottomSheetFragment.height = 1000
        bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
    }

    fun homeTabClick(bundle: Bundle? = null) {
        if (navController.currentDestination?.id == R.id.homeFragment) {
            EventBus.getDefault().post(HomeTabClick(true))
        } else {
            binding.apply {
                homeTitle.visibility = View.VISIBLE
                homeImg.imageTintList = ColorStateList.valueOf(getColor(R.color.pureWhite))
                xChangeTitle.visibility = View.INVISIBLE
//            xChangeImg.imageTintList = ColorStateList.valueOf(getColor(R.color.unselectedColor))
                uploadImg.imageTintList = ColorStateList.valueOf(getColor(R.color.unselectedColor))
                uploadTitle.visibility = View.INVISIBLE
                messagingImg.imageTintList =
                    ColorStateList.valueOf(getColor(R.color.unselectedColor))
                messagineTitle.visibility = View.INVISIBLE
                profileImg.imageTintList = ColorStateList.valueOf(getColor(R.color.unselectedColor))
                profileTitle.visibility = View.INVISIBLE

                navController.navigate(R.id.homeFragment, bundle)
            }
        }

    }

    interface PurchaseViewCallback {
        fun noAdsLifeTimeClick()
        fun noAdsOneYear()

        fun verifiedTickOneYear()

        fun verifiedTickLifeTime()
    }


    fun xChangeTabClick() {
        binding.apply {
            homeTitle.visibility = View.INVISIBLE
            homeImg.imageTintList = ColorStateList.valueOf(getColor(R.color.unselectedColor))
            xChangeTitle.visibility = View.VISIBLE
//            xChangeImg.imageTintList = ColorStateList.valueOf(getColor(R.color.pureWhite))
            uploadImg.imageTintList = ColorStateList.valueOf(getColor(R.color.unselectedColor))
            uploadTitle.visibility = View.INVISIBLE
            messagingImg.imageTintList = ColorStateList.valueOf(getColor(R.color.unselectedColor))
            messagineTitle.visibility = View.INVISIBLE
            profileImg.imageTintList = ColorStateList.valueOf(getColor(R.color.unselectedColor))
            profileTitle.visibility = View.INVISIBLE
            navController.navigate(R.id.xchangeFragment)
        }
    }

    fun uploadTabClick(context: Context) {
        binding.apply {
            homeTitle.visibility = View.INVISIBLE
            homeImg.imageTintList = ColorStateList.valueOf(getColor(R.color.unselectedColor))
            xChangeTitle.visibility = View.INVISIBLE
//            xChangeImg.imageTintList = ColorStateList.valueOf(getColor(R.color.unselectedColor))
            uploadImg.imageTintList = ColorStateList.valueOf(getColor(R.color.pureWhite))
            uploadTitle.visibility = View.VISIBLE
            messagingImg.imageTintList = ColorStateList.valueOf(getColor(R.color.unselectedColor))
            messagineTitle.visibility = View.INVISIBLE
            profileImg.imageTintList = ColorStateList.valueOf(getColor(R.color.unselectedColor))
            profileTitle.visibility = View.INVISIBLE
            val readImagePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                Manifest.permission.READ_MEDIA_IMAGES
            else
                Manifest.permission.READ_EXTERNAL_STORAGE


            if (ContextCompat.checkSelfPermission(
                    context,
                    readImagePermission
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                navController.navigate(R.id.uploadFragment)
            } else {
                navController.navigate(R.id.permissionFragment)

            }

        }
    }

    fun messagingTabClick() {
        binding.apply {
            homeTitle.visibility = View.INVISIBLE
            homeImg.imageTintList = ColorStateList.valueOf(getColor(R.color.unselectedColor))
            xChangeTitle.visibility = View.INVISIBLE
//            xChangeImg.imageTintList = ColorStateList.valueOf(getColor(R.color.unselectedColor))
            uploadImg.imageTintList = ColorStateList.valueOf(getColor(R.color.unselectedColor))
            uploadTitle.visibility = View.INVISIBLE
            messagingImg.imageTintList = ColorStateList.valueOf(getColor(R.color.pureWhite))
            messagineTitle.visibility = View.VISIBLE
            profileImg.imageTintList = ColorStateList.valueOf(getColor(R.color.unselectedColor))
            profileTitle.visibility = View.INVISIBLE
            navController.navigate(R.id.messageAndNotificationsFragment)
        }
    }

    fun profileTabClick() {
        binding.apply {
            homeTitle.visibility = View.INVISIBLE
            homeImg.imageTintList = ColorStateList.valueOf(getColor(R.color.unselectedColor))
            xChangeTitle.visibility = View.INVISIBLE
//            xChangeImg.imageTintList = ColorStateList.valueOf(getColor(R.color.unselectedColor))
            uploadImg.imageTintList = ColorStateList.valueOf(getColor(R.color.unselectedColor))
            uploadTitle.visibility = View.INVISIBLE
            messagingImg.imageTintList = ColorStateList.valueOf(getColor(R.color.unselectedColor))
            messagineTitle.visibility = View.INVISIBLE
            profileImg.imageTintList = ColorStateList.valueOf(getColor(R.color.pureWhite))
            profileTitle.visibility = View.VISIBLE
            navController.navigate(R.id.profileFragment)
        }
        Prefs.init().removeActivePostId=""
        Prefs.init().removePostType=""
        Prefs.init().removeSavePostId=""
        Prefs.init().removeArchivePostId=""
        /* Handler(Looper.myLooper()!!).postDelayed({
             org.greenrobot.eventbus.EventBus.getDefault()
                 .post(ActivePostUpdate(""))
         },1000)*/
    }

    //Todo Hide BottomNavigationView
    fun setBottomNavigationVisibility(visibility: Int) {
        binding.bottomNavigation.visibility = visibility
    }


    override fun onPurchasesUpdated(p0: BillingResult, p1: MutableList<Purchase>?) {
        Log.d("purchasesDetails", p1.toString())
    }

    override fun onStart() {
        super.onStart()
        countDownTimer.start()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        countDownTimer.cancel()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onChatEvents(event: NewMessage?) {
        var messageValue = event?.text.toString().toInt()
        if (messageValue > 0) {
            if (messageValue > 99) {
                binding.tvNotification.setText(R.string.more_than_99)
            } else {
                binding.tvNotification.text = messageValue.toString()
            }
            binding.tvNotification.visibility = View.VISIBLE

        } else {
            binding.tvNotification.visibility = View.INVISIBLE
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onNewProfileUpdate(event: NewProfileUpdated?) {
        /**  here we set the image for profile */
        CoroutineScope(Dispatchers.Main).launch {
            binding.profileImg.loadImageAsync(Prefs.init().userProfile)
        }
    }

    @Subscribe()
    fun showInAppNotification(events: InAppNotification) {
        val dataObject = events.jsonObject
        val notificationDetail = dataObject.getJSONObject("notificationDetail")
        val title = dataObject.getString("message")
        val message = dataObject.getString("description")
        var profile = ""
        if (notificationDetail.has("otherUser")) {
            profile = notificationDetail.getJSONObject("otherUser").getString("profile")
        }

        runOnUiThread {
            Utils.showToast(message, title, profile) {
                lifecycleScope.launch(Dispatchers.Main) {
                    Utils.printLog(tag = "createNotificationChannelin", msg = dataObject.toString())
                }
                redirectViaInAppNotification(dataObject)
            }
        }
    }

    private fun redirectViaInAppNotification(dataObject: JSONObject) {
        val bundle = Bundle()
        val notificationDetail = dataObject.getJSONObject("notificationDetail")
        Utils.printLog(tag = "createNotificationChannel", msg = notificationDetail.toString())
        val type = notificationDetail.getString("type")
        Utils.printLog(tag = "createNotificationChannel", msg = type)

        val senderArray = if (notificationDetail.has("sender")) {
            notificationDetail.getJSONArray("sender")
        } else {
            null
        }

        when (type) {
            NotificationType.FOLLOW_USER,
            NotificationType.MENTION_BIO -> {
                bundle.apply {
                    var id = ""
                    if (senderArray != null) {
                        for (i in 0 until senderArray.length()) {
                            id = senderArray.getString(i)
                        }
                    }
//                    putString(AppConstant.NOTIFICATION_TYPE, type)
//                    putString("sender", id)
                    if (id == Prefs.init().userId) {
                        navController.navigate(R.id.profileFragment, bundle)
                    } else {
                        bundle.putString("mentionUser", id)
                        navController.navigate(R.id.otherProfileFragment, bundle)
                    }

                }
            }

            NotificationType.FOLLOW_REQUEST -> {
                bundle.apply {
                    var id = ""
                    /*   if (senderArray != null) {
                        for (i in 0 until senderArray.length()) {
                            id = senderArray.getString(i)
                        }
                    }*/

                    bundle.putString("type", "request")
                    navController.navigate(R.id.requestSentAndReceiveFragment, bundle)
//                    putString(AppConstant.NOTIFICATION_TYPE, type)
//                    putString("sender", id)
                }
            }

            NotificationType.DAILY_AR,
            NotificationType.PERSONAL_INFO,
            NotificationType.BLUE_TICK_PURCHASE,
            NotificationType.WD_PURCHASE,
            NotificationType.WD_TRANSFER,
            NotificationType.TREE_BONUS,
            NotificationType.AI_BONUS,
            NotificationType.NO_ADS_BONUS -> {
                bundle.putString("type", NotificationType.DAILY_AR)
                navController.navigate(R.id.messageAndNotificationsFragment, bundle)
            }

            NotificationType.MESSAGE,
            NotificationType.REACT -> {
                bundle.apply {
                    val otherUser = notificationDetail.getJSONObject("otherUser")
                    putString(AppConstant.NOTIFICATION_TYPE, type)
                    putString("userChatId", otherUser.getString("_id"))
                    putString("otherUserName", otherUser.getString("name"))
                    putString("otherUserProfile", otherUser.getString("profile"))
                    putBoolean("blueTick", false)
                    navController.navigate(R.id.showChatFragment, this)
                }
                /*bundle.apply {
                    putString("userChatId", intentExtras.getString("userChatId"))
                    putString("otherUserName", intentExtras.getString("otherUserName"))
                    putString("otherUserProfile", intentExtras.getString("otherUserProfile"))
                    putBoolean("blueTick", intentExtras.getBoolean("blueTick"))

                }*/
            }

            NotificationType.POST_LIKE,
            NotificationType.TAG_POST,
            NotificationType.MENTION_POST,
            NotificationType.COMMENT,
            NotificationType.MENTION_COMMENT,
            NotificationType.COMMENT_LIKE,
            NotificationType.COMMENT_REPLY,
            NotificationType.NEW_POST -> {
                bundle.apply {
                    bundle.putInt("position", 0)
                    bundle.putString("dataList", notificationDetail.getString("post"))
                    navController.navigate(R.id.show_profile_post_fragment, bundle)
//                    putString("post", notificationDetail.getString("post"))
//                    putString(AppConstant.NOTIFICATION_TYPE, type)
                }
            }
        }
//        return bundle
    }


    private val countDownTimer = object : CountDownTimer(durationMillis.toLong(), 1000) {
        override fun onTick(millisUntilFinished: Long) {
        }

        override fun onFinish() {
            showRunningTimeDialog()
        }
    }

    private fun showSettingDialog() {
        MaterialAlertDialogBuilder(
            this,
            com.google.android.material.R.style.MaterialAlertDialog_Material3
        )
            .setTitle("Notification Permission")
            .setMessage("Notification permission is required, Please allow notification permission from setting")
            .setPositiveButton("Ok") { _, _ ->
                val intent = Intent(ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showRunningTimeDialog() {
        runningDialog?.setCancelable(false)
        Objects.requireNonNull(runningDialog?.window)?.setBackgroundDrawable(
            ColorDrawable(
                Color.TRANSPARENT
            )
        )
        runningDialog?.setContentView(R.layout.running_dialog_layout)

        val dismiss = runningDialog?.findViewById(R.id.tvDismiss) as TextView
        val edit = runningDialog?.findViewById(R.id.tvEdit) as TextView

        dismiss.setOnClickListener {
            edit.disableMultiTap()
            hideRunningTimeDialog()
        }

        edit.setOnClickListener {
            edit.disableMultiTap()
            val bottomSheetFragment =
                RemindMeBottomSheet(R.layout.fragment_remind_me_bottom_sheet, this)
            bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
        }


        runningDialog?.show()

    }

    private fun hideRunningTimeDialog() {
        runningDialog?.dismiss()
        countDownTimer.cancel()
        countDownTimer.start()
    }

    override fun onClick60Mins() {
        hideRunningTimeDialog()
    }

    override fun onClickNever() {
        runningDialog?.dismiss()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onActivePostUpdate(event: ActivePostUpdate?) {
        Log.e("removeSavePostId" ,event?.text.toString())
        Log.e("removeSavePostId" ,event?.type.toString())
        Prefs.init().removeActivePostId = event?.text.toString()
        Prefs.init().removePostType = event?.type.toString()
        Prefs.init().removeSavePostId = event?.text.toString()
        Prefs.init().removeArchivePostId = event?.text.toString()
    }

}


/**
user/fcm (PUT)

Req- {
fcmToken: "fcmToken"
}
 */