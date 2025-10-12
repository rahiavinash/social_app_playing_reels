package com.wedo8020.app.utils

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager

import com.wedo8020.app.MainApplication
import com.wedo8020.app.R
import com.wedo8020.app.databinding.CustomMessageNotificationBinding
import com.wedo8020.app.utils.Utils.loadImageAsync
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch




object ShowCustomDialog {

    fun showClickableDialog(
        message: String,
        heading: String = "",
        imageUrl: String = "",
        color: Int = R.color.card_notification_color,
        run: () -> Unit
    ) {
        val context = MainApplication.get().appLifecycleObserver.activity

        context?.let { activity ->
            val binding = CustomMessageNotificationBinding.inflate(
                LayoutInflater.from(activity),
                null,
                false
            )

            binding.apply {
                txtMsg.text = message
                profileName.text = heading.ifBlank { activity.getString(R.string.app_name) }

                if (imageUrl.startsWith("https")) {
                    CoroutineScope(Dispatchers.IO).launch {
                        profileImg.loadImageAsync(imageUrl)
                    }
                }

                mainCard.setCardBackgroundColor(
                    activity.applicationContext.getColor(
                        color
                    )
                )

                // Add an onClickListener to the custom dialog
                mainLayout.setOnClickListener {
                    run()
//                    customDialog.dismiss()
                }
            }

            val customDialog = Dialog(activity)
            customDialog.setContentView(binding.root)

            // Set the dialog to be non-cancelable to prevent dismissal by clicking outside
            customDialog.setCancelable(false)
            customDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            val window = customDialog.window
            window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            window?.setGravity(Gravity.TOP)
            // Add the FLAG_NOT_TOUCH_MODAL flag to allow interaction with views outside the dialog
            window?.setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_DIM_BEHIND
            )
            window?.setDimAmount(0.0f)
            // Show the custom dialog
            customDialog.show()

            // Dismiss the custom dialog after a delay (adjust duration as needed)
            Handler().postDelayed({ customDialog.dismiss() }, 2000)
        }
    }
}

/*object ShowCustomSnackbar {

    fun showClickableSnackbar(
        message: String,
        heading: String = "",
        imageUrl: String = "",
        color: Int = R.color.card_notification_color,
        run: () -> Unit
    ) {
        val rootView = MainApplication.get().appLifecycleObserver.activity?.findViewById<ViewGroup>(android.R.id.content)

        rootView?.let { viewGroup ->
            val binding = CustomMessageNotificationBinding.inflate(
                LayoutInflater.from(viewGroup.context),
                null,
                false
            )

            binding.apply {
                txtMsg.text = message
                profileName.text = heading.ifBlank { viewGroup.context.getString(R.string.app_name) }

                if (imageUrl.startsWith("https")) {
                    CoroutineScope(Dispatchers.IO).launch {
                        profileImg.loadImageAsync(imageUrl)
                    }
                }

                mainCard.setCardBackgroundColor(
                    MainApplication.get().applicationContext.getColor(
                        color
                    )
                )

                // Add an overlay button to capture click events
                rootView.setOnClickListener {
                    run()
//                    customSnackbar.dismiss()
                }
            }

            val customSnackbar = PopupWindow(
                binding.root,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            // Set elevation to appear above BottomSheet
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                customSnackbar.elevation = viewGroup.resources.getDimension(R.dimen.activity_horizontal_margin)
            }

            // Show the custom Snackbar above the BottomSheet
            customSnackbar.showAtLocation(viewGroup, Gravity.TOP, 0, 0)

            // Dismiss the custom Snackbar after a delay (adjust duration as needed)
            Handler().postDelayed({ customSnackbar.dismiss() }, 2000)
        }
    }
}*/

/*object ShowCustomSnackbar {

    @SuppressLint("RestrictedApi")
    fun showClickableSnackbar(
        message: String,
        heading: String = "",
        imageUrl: String = "",
        color: Int = R.color.card_notification_color,
        run: () -> Unit
    ) {
        val rootView = MainApplication.get().appLifecycleObserver.activity?.findViewById<ViewGroup>(android.R.id.content)

        rootView?.let { viewGroup ->
            val binding = CustomMessageNotificationBinding.inflate(
                LayoutInflater.from(viewGroup.context),
                null,
                false
            )

            binding.apply {
                root.setOnClickListener {
                    Toast.makeText(rootView.context,"sadf",Toast.LENGTH_SHORT).show()
                    run()
                }
                txtMsg.text = message
                profileName.text = heading.ifBlank { viewGroup.context.getString(R.string.app_name) }

                if (imageUrl.startsWith("https")) {
                    CoroutineScope(Dispatchers.IO).launch {
                        profileImg.loadImageAsync(imageUrl)
                    }
                }

                mainCard.setCardBackgroundColor(
                    MainApplication.get().applicationContext.getColor(
                        color
                    )
                )
            }

            val snackbar = Snackbar.make(viewGroup, "", Snackbar.LENGTH_SHORT)
            val snackbarView = snackbar.view
            val snackLayout = snackbarView as Snackbar.SnackbarLayout

            // Remove any previous views from the Snackbar layout
            for (i in 0 until snackLayout.childCount) {
                snackLayout.removeViewAt(i)
            }

            // Add the custom view to the Snackbar layout
            snackLayout.addView(binding.root, 0)

            // Show the Snackbar
            snackbar.show()
        }
    }
}*/
