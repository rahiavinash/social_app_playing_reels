package com.wedo8020.app.mainui.base

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import com.wedo8020.app.R
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.wedo8020.app.utils.ShowCustomMessage

abstract class


BaseActivity : AppCompatActivity() {


    private var progressDialog: Dialog? = null
    private lateinit var dialog: BottomSheetDialog
    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
    }


    fun setStatusBarTransparent() {
      /*  window.apply {
            decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            statusBarColor = Color.TRANSPARENT
        }*/
    }

    protected fun hideProgress() {
        progressDialog?.let {
            if (it.isShowing) {
                it.cancel()
                it.dismiss()
            }
        }
    }

    fun askPermission(){

    }


    protected fun showToast(
        message: String,
        heading: String = "",
        imageUrl: String = "",
        color: Int = R.color.card_notification_color,run: () -> Unit = {}
    ) {
        ShowCustomMessage.addCustomLayout(message, heading, imageUrl, color){
            run()
        }
    }
    open fun hideStatusBar() {
        if (Build.VERSION.SDK_INT < 16) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        } else {
            val decorView = window.decorView
            val uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN
            decorView.systemUiVisibility = uiOptions
        }
    }
    fun setStatusBarTransparent(activity: Activity, view: View) {
        activity.apply {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = ContextCompat.getColor(this, R.color.transparent)
            WindowCompat.setDecorFitsSystemWindows(window, false)
            ViewCompat.setOnApplyWindowInsetsListener(view) { root, windowInset ->
                val inset = windowInset.getInsets(WindowInsetsCompat.Type.systemBars())
                root.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    leftMargin = inset.left
                    bottomMargin = inset.bottom
                    rightMargin = inset.right
                    topMargin = 0
                }
                WindowInsetsCompat.CONSUMED
            }
        }
    }


    fun hideStatusAndNavigationBar(){
        window?.apply {
            decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                var flags: Int = decorView.systemUiVisibility
                flags = flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                decorView.systemUiVisibility = flags
            }

            navigationBarColor = Color.TRANSPARENT
            statusBarColor = Color.TRANSPARENT
        }
    }
    open fun adjustFontScale(configuration: Configuration, scale: Float) {
        configuration.fontScale = scale
        val metrics = resources.displayMetrics
        val wm = getSystemService(WINDOW_SERVICE) as WindowManager
        wm.defaultDisplay.getMetrics(metrics)
        metrics.scaledDensity = configuration.fontScale * metrics.density
        baseContext.resources.updateConfiguration(configuration, metrics)
    }
    override fun attachBaseContext(baseContext: Context) {
        val newContext: Context
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val displayMetrics = baseContext.resources.displayMetrics
            val configuration = baseContext.resources.configuration
            if (displayMetrics.densityDpi != DisplayMetrics.DENSITY_DEVICE_STABLE) {
                // Current density is different from Default Density. Override it
                configuration.densityDpi = DisplayMetrics.DENSITY_DEVICE_STABLE
                newContext = baseContext.createConfigurationContext(configuration)
            } else {
                // Same density. Just use same context
                newContext = baseContext
            }
        } else {
            // Old API. Screen zoom not supported
            newContext = baseContext
        }
        super.attachBaseContext(baseContext)
    }


    /*      protected fun showProgressDialog(context: Context?) {
              if (progressDialog == null) progressDialog =
                  Dialog(applicationContext, R.style.progressDialogStyle)
              progressDialog?.let {
                  it.setContentView(R.layout.progress_dialog)
                  it.window?.apply {
                      setBackgroundDrawableResource(android.R.color.transparent)
                      clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                  }
                  it.dismiss()
                  it.setCancelable(false)
                  it.show()
              }
          }*/
    /*

            override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
                if (currentFocus != null) {
                    val imm: InputMethodManager =
                        getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
                }
                return super.dispatchTouchEvent(ev)
            }

            protected fun showNoInternetAlert() {
                Alerter.create(this)
                    .setText(R.string.connection_lost)
                    .setIcon(R.drawable.ic_network_check)
                    .setBackgroundColorRes(R.color.my_statusbar_color)
                    .show()
            }*/

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    protected open fun hideStatusBar(darkText: Boolean) {
        hideStatusBar(window, darkText)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    protected open fun hideStatusBar(window: Window, darkText: Boolean) {
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.TRANSPARENT
        var flag = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        if (darkText) {
            flag = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        window.decorView.systemUiVisibility = flag or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
    }

    open fun getStatusBarHeight(): Int {
        val id = resources!!.getIdentifier(
            "status_bar_height", "dimen", "android"
        )
        return if (id > 0) resources!!.getDimensionPixelSize(id) else id
    }

    fun changeStatusBarColor() {
        val window: Window = window
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        window.statusBarColor = ContextCompat.getColor(this, com.google.android.material.R.color.mtrl_btn_transparent_bg_color)
    }

    fun changeStatusBarFullScreen() {
        val w = window
        val v: View = w.decorView
        w.navigationBarColor = resources.getColor(android.R.color.transparent)
        v.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        w.statusBarColor = Color.TRANSPARENT
        w.navigationBarColor = ContextCompat.getColor(this, android.R.color.black)
    }

    open fun hideNavigationBar() {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
    }
    fun hideStatus(){
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
    }

    fun dismissDialog() {
        if (::dialog.isInitialized) {
            dialog.dismiss()
        }
    }
    open fun hideSystemUI() {
        // Set the IMMERSIVE flag.
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.
        this.window
            .setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }
    fun keybord(){
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN );
        //window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE )
    }

    fun hideSoftKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
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

}