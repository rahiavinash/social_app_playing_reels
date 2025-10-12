package com.wedo8020.app.mainui.home.createpost

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.wedo8020.app.R
import com.wedo8020.app.mainui.base.BaseFragment
import android.provider.Settings
import android.text.Html
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.wedo8020.app.databinding.FragmentPermissionBinding
import com.wedo8020.app.mainui.activities.MainActivity

class PermissionFragment : BaseFragment() {

    private lateinit var mBinding : FragmentPermissionBinding

    @SuppressLint("SuspiciousIndentation")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding =
            DataBindingUtil.inflate(layoutInflater, R.layout.fragment_permission, container, false)


            return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val callback = requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {

            val activity = activity
            activity?.let {
                if (activity is MainActivity){
                    activity.homeTabClick()
                }
            }
        }
        mBinding.settingTV.setOnClickListener {
            checkIfFragmentAttached {


                val builder = AlertDialog.Builder(requireContext())


                val boldText = "<b>WeDo 80</b>"
                val message = "WeDo 80 would like to access your gallery"

                builder.setTitle(Html.fromHtml(boldText, Html.FROM_HTML_MODE_LEGACY))

                builder.setMessage(message)

                builder.setPositiveButton("OK") { dialog, which ->
                    // Open settings after user clicks OK
                    openAppSettings()
                }
                builder.setNegativeButton("Cancel") { dialog, which ->
                    dialog.dismiss()
                }
                val dialog = builder.create()
                dialog.show()
            }
        }
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri: Uri = Uri.fromParts("package", requireActivity().packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            // Check if permission is granted or not
            if (isGalleryPermissionGranted(requireContext())) {
                // Permission granted, update the view
                updateViewAfterPermissionGranted()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun updateViewAfterPermissionGranted() {
        findNavController().navigate(R.id.uploadFragment)

    }

    private fun isGalleryPermissionGranted(context: Context): Boolean {
        val readImagePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            Manifest.permission.READ_MEDIA_IMAGES
        else
            Manifest.permission.READ_EXTERNAL_STORAGE


        if(ContextCompat.checkSelfPermission(context, readImagePermission) == PackageManager.PERMISSION_GRANTED){
            return true
        }
        return false
    }

    override fun onResume() {
        super.onResume()
        // Update the view when returning to this fragment
        if (isGalleryPermissionGranted(requireContext())) {
            updateViewAfterPermissionGranted()
        }
    }

    companion object {
        const val PERMISSION_REQUEST_CODE = 1001
    }
}