package com.wedo8020.app.mainui.base

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Rect
import android.location.Location
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.view.GestureDetectorCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.wedo8020.app.R
import com.wedo8020.app.mainui.activities.MainActivity
import com.wedo8020.app.utils.DeepLinkType
import com.wedo8020.app.utils.ShowCustomMessage
import com.wedo8020.app.utils.Utils
import io.branch.indexing.BranchUniversalObject
import io.branch.referral.util.LinkProperties


open class BaseFragment : Fragment() {
    lateinit var gestureDetector: GestureDetectorCompat
    lateinit var rootView: View
    private var layout: View? = null
    private val SWIPE_THRESHOLD = 100
    private val SWIPE_VELOCITY_THRESHOLD = 100
    private var progressDialog: Dialog? = null
    protected open var bottomNavigationVisible= View.VISIBLE
    private val permissionId = 2

    var bottomNavigationGone = View.GONE
    var toast: Toast? = null



   /* override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
        }
        onViewCreatedWithSwipe(view, savedInstanceState)
    }*/

    fun setOnGestureBack(view:View){
        /*rootView = view
        gestureDetector = GestureDetectorCompat(requireContext(), SwipeGestureListener())
        setTouchListenerRecursive(view)*/
    }
       fun checkIfFragmentAttached(operation: Context.() -> Unit) {
        if (isAdded && context != null) {
            operation(requireContext())
        }
    }
    fun showToast(message: String,
                  heading: String = "",
                  imageUrl: String = "",
                  color: Int = R.color.card_notification_color,run: (() -> Unit)? = null){
        if (run == null){
            ShowCustomMessage.addCustomLayout(message, heading, imageUrl, color)
        } else{
            ShowCustomMessage.addCustomLayout(message, heading, imageUrl, color){
                run()
            }
        }
    }
    private fun setTouchListenerRecursive(view: View) {
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val child = view.getChildAt(i)
                setTouchListenerRecursive(child)
            }
        }

        view.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event) // Consume the touch event
        }
        view.isClickable = true
        view.isFocusable = true
    }

    open fun onViewCreatedWithSwipe(view: View, savedInstanceState: Bundle?) {
        // This method can be overridden in child fragments to add additional logic
        // after setting up swipe functionality.
    }
    inner class SwipeGestureListener : GestureDetector.SimpleOnGestureListener() {
        private val SWIPE_THRESHOLD = 100
        private val SWIPE_VELOCITY_THRESHOLD = 100


        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            val distanceX = e2.x - e1!!.x
            val distanceY = e2.y - e1.y

            if (Math.abs(distanceX) > Math.abs(distanceY) &&
                Math.abs(distanceX) > SWIPE_THRESHOLD &&
                Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD &&
                distanceX > 0 // Only for right swipe
            ) {
                // Right swipe, perform animation or action

                    // Use Navigation Component to navigate up with custom animations
                    val navController = findNavController()

                    navController.navigateUp()


                return true
            }
            return false
        }

    }
    //check for fullScreen fragment
    protected open fun getStatusBarHeight(): Int {
        return if (requireActivity() is BaseActivity) {
            (requireActivity() as BaseActivity).getStatusBarHeight()
        } else {
            0
        }
    }
    fun hideStatusAndNavigationBar(){
     /*   requireActivity().window?.apply {
            decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                var flags: Int = decorView.systemUiVisibility
                flags = flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                decorView.systemUiVisibility = flags
            }

            navigationBarColor = Color.TRANSPARENT
            statusBarColor = Color.TRANSPARENT
        }*/

    }

    open fun hideStatusBar() {
        if (Build.VERSION.SDK_INT < 16) {
            requireActivity().window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        } else {
            val decorView = requireActivity().window.decorView
            val uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN
            decorView.systemUiVisibility = uiOptions
        }
    }

    fun runOnUiThread(action: Runnable?) {
        throw RuntimeException("Stub!")
    }


    protected open fun onKeyBoardClosed() {}
    protected open fun onGlobalLayoutCompleted() {}
    protected open fun onKeyBoardOpen(diff: Int) {}

    protected open fun setGlobalLayoutListener() {
        layout = requireActivity().findViewById<View>(Window.ID_ANDROID_CONTENT)
        val observer = layout?.viewTreeObserver
        observer?.addOnGlobalLayoutListener(globalLayoutListener)
    }




    private val globalLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
        layout?.let {
            onGlobalLayoutCompleted()
            val decorView = activity?.window?.decorView
            val r = Rect()
            decorView?.let {
                //r will be populated with the coordinates of your view that area still visible.
                it.getWindowVisibleDisplayFrame(r)
                //get screen height and calculate the difference with the useable area from the r
                val height = it.context.resources.displayMetrics.heightPixels
                val diff = height - r.bottom

                //if it could be a keyboard add the padding to the view
                if (diff > 0) {
                    //keyboard open
                    onKeyBoardOpen(diff)
                } else {
                    //keyboard hide
                    onKeyBoardClosed()
                }
            }
        }
    }
  /*  fun generateDeepLink(type: DeepLinkType, id: String, description: String, imgUrl: String): String{
        val branchUniversalObject = BranchUniversalObject()
            .setCanonicalIdentifier(when(type){
                DeepLinkType.POST -> "post/$id"
                DeepLinkType.PROFILE -> "user/$id"
            })
            .setTitle(description)
            .setContentDescription(description)
            .setContentImageUrl(imgUrl)
            .setContentIndexingMode(BranchUniversalObject.CONTENT_INDEX_MODE.PUBLIC)
            .setLocalIndexMode(BranchUniversalObject.CONTENT_INDEX_MODE.PUBLIC)

        val linkProperties = when(type){
            DeepLinkType.POST -> {
                LinkProperties()
                    .setChannel("Post Sharing")
                    .setFeature("PostLink")
                    .setCampaign("PostCampaign")
            }
            DeepLinkType.PROFILE -> {
                LinkProperties()
                    .setChannel("User Profile Sharing")
                    .setFeature("UserProfileLink")
                    .setCampaign("UserProfileCampaign")
            }
        }
        val url = branchUniversalObject.getShortUrl(requireContext(),linkProperties)
        Utils.printLog(tag = "Branch Deep Link", msg = url)
        return url

    }*/
  /*  @SuppressLint("ServiceCast")
    private fun vibrateOnce(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =  MainApplication.get().applicationContext.getSystemService(
                Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            val vib  = vibratorManager.defaultVibrator;
            vib.vibrate(VibrationEffect.createOneShot(100,1 ))
        } else {
            val vib  = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vib.vibrate(500)
        }
    }*/

 /*   fun hideKeyboard(v: View) {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(v.windowToken, 0)
    }
*/
   /* fun showKeyboard(){
        *//*   val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE!!) as InputMethodManager?

           imm!!.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)*//*
    }*/
    fun View.showSoftKeyboard() {
        post {
            if (this.requestFocus()) {
                val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm?.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
            }
        }
    }
    fun View.hideTheSoftKeyboard() {
        post {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(windowToken, 0)
        }
    }

    fun hideSoftKeyboard(activity: Activity) {
        try {
            val inputMethodManager: InputMethodManager = activity.getSystemService(
                Activity.INPUT_METHOD_SERVICE
            ) as InputMethodManager
            if (inputMethodManager.isAcceptingText) {
                inputMethodManager.hideSoftInputFromWindow(
                    activity.currentFocus!!.windowToken,
                    0
                )
            }
        }catch (e:Exception){ }

    }

    fun showNavBottom() {
        var mainActivity = activity as MainActivity
        mainActivity.setBottomNavigationVisibility(bottomNavigationVisible)
    }
    fun hideNavBottom() {
        var mainActivity = activity as MainActivity
        mainActivity.setBottomNavigationVisibility(bottomNavigationGone)
    }


    @SuppressLint("ClickableViewAccessibility", "SuspiciousIndentation")
    fun hideKeyboardOnOutSideClick(view: View) {
        // Set up touch listener for non-text box views to hide keyboard.
        if (view !is EditText) {
            if(view !is ImageView)
                view.setOnTouchListener { v, event ->
                    hideSoftKeyboard(requireActivity())
                    false
                }
        }
        //If a layout container, iterate over children and seed recursion.
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val innerView = view.getChildAt(i)
                hideKeyboardOnOutSideClick(innerView)
            }
        }
    }
    fun clearRequestFocus(view: View) {
        view.setOnClickListener {
            view.clearFocus()
        }
    }
 /*   fun clearRequestFocus2(view: View){
        if (view is EditText){
            view.setOnClickListener {
                view.isCursorVisible = false
            }
        }
    }
*/



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }


    protected fun hideProgress() {
        progressDialog?.let {
            if (it.isShowing) {
                it.cancel()
                it.dismiss()
            }
        }
    }





    fun hide() {
        val decorView = requireActivity().window.decorView // Hide the status bar.

        val uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        decorView.systemUiVisibility = uiOptions
    }


    fun View.disableBtn() {
        try {
            isClickable = false
            Handler(Looper.getMainLooper()).postDelayed({
                isClickable = true
            }, 3000)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun View.disableSendBtn() {
        try {
            isClickable = false
            Handler(Looper.getMainLooper()).postDelayed({
                isClickable = true
            }, 500)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    open fun onLocationChanged(location: Location) {}


    fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val networkCapabilities = connectivityManager.activeNetwork ?: return false
            val actNw =
                connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false

            return when {
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            // For devices below Android M
            val networkInfo = connectivityManager.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnected
        }
    }
    fun generateDeepLink(type: DeepLinkType, id: String, description: String, imgUrl: String): String{
        val branchUniversalObject = BranchUniversalObject()
            .setCanonicalIdentifier(when(type){
                DeepLinkType.POST -> "post/$id"
                DeepLinkType.PROFILE -> "user/$id"
                DeepLinkType.SHARE_WEDO -> "referral/$id"
            })
            .setTitle(description)
            .setContentDescription(description)
            .setContentImageUrl(imgUrl)
            .setContentIndexingMode(BranchUniversalObject.CONTENT_INDEX_MODE.PUBLIC)
            .setLocalIndexMode(BranchUniversalObject.CONTENT_INDEX_MODE.PUBLIC)

        val linkProperties = when(type){
            DeepLinkType.POST -> {
                LinkProperties()
                    .setChannel("Post Sharing")
                    .setFeature("PostLink")
                    .setCampaign("PostCampaign")
            }
            DeepLinkType.PROFILE -> {
                LinkProperties()
                    .setChannel("User Profile Sharing")
                    .setFeature("UserProfileLink")
                    .setCampaign("UserProfileCampaign")
            }
            DeepLinkType.SHARE_WEDO -> {
                LinkProperties()
                    .setChannel("User Profile Sharing")
                    .setFeature("UserProfileLink")
                    .setCampaign("UserProfileCampaign")
            }
        }
        val url = branchUniversalObject.getShortUrl(requireContext(),linkProperties)
        Utils.printLog(tag = "Branch Deep Link", msg = url)
        return url

    }


}