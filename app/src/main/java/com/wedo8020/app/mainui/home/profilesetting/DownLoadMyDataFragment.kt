package com.wedo8020.app.mainui.home.profilesetting

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.wedo8020.app.R
import com.wedo8020.app.databinding.FragmentDownLoadMyDataBinding
import com.wedo8020.app.mainui.base.BaseFragment
import com.wedo8020.app.mainui.home.postFragment.PostViewModel
import com.wedo8020.app.utils.Utils
import com.wedo8020.app.utils.Utils.disableMultiTap
import java.util.Objects


class DownLoadMyDataFragment : BaseFragment() {
    private lateinit var mBinding: FragmentDownLoadMyDataBinding
    var dialog: Dialog? = null
    var dialogs: Dialog? = null
    private val authViewModel by viewModels<PostViewModel>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        mBinding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.fragment_down_load_my_data,
            container,
            false
        )
        dialogs = Dialog(requireActivity())
        dialogs?.requestWindowFeature(Window.FEATURE_CONTENT_TRANSITIONS)
        dialog = Dialog(requireActivity())
        dialog?.requestWindowFeature(Window.FEATURE_CONTENT_TRANSITIONS)
        observer()
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mBinding.tvDownLoadButton.setOnClickListener {
            mBinding.tvDownLoadButton.disableMultiTap()
            if (isInternetAvailable(requireActivity())) {
                authViewModel.downloadMyData()
                showProgressDialogs()
            } else {
                dialogs?.dismiss()
                Utils.showToast("Please check your internet connection.")
            }
        }

        mBinding.IvBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun thanksDialogForDownload() {

        dialog?.setCancelable(false)
        Objects.requireNonNull(dialog?.window)?.setBackgroundDrawable(
            ColorDrawable(
                Color.TRANSPARENT
            )
        )
        dialog?.setContentView(R.layout.download_thanku)
        val logout = dialog?.findViewById(R.id.Logout) as TextView
        logout.setOnClickListener {
            dialog?.dismiss()
        }
        dialog?.show()
    }

    private fun observer() {
        authViewModel.downloadMyData.observe(viewLifecycleOwner) { response ->
            dialogs?.dismiss()
            thanksDialogForDownload()
            if (response != null) {
                if (response.success) {
                    Utils.showToast(response.message.toString())
                }
            }
        }

        authViewModel.showLoading.observe(requireActivity()) {
        }
        authViewModel.showMessage.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()){
                dialogs?.dismiss()
                Utils.showToast(it)
            }

        }

    }
    private fun showProgressDialogs() {
        dialogs?.setCancelable(false)
        Objects.requireNonNull(dialogs?.window)?.setBackgroundDrawable(
            ColorDrawable(
                Color.TRANSPARENT
            )
        )
        dialogs?.setContentView(R.layout.progressbar)
        dialogs?.show()
    }
}