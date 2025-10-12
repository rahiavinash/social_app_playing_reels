package com.wedo8020.app.mainui.home.profilesetting

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.wedo8020.app.R
import com.wedo8020.app.databinding.FragmentDeleteBinding
import com.wedo8020.app.mainui.activities.LoginActivity
import com.wedo8020.app.mainui.base.BaseFragment
import com.wedo8020.app.mainui.home.postFragment.PostViewModel
import com.wedo8020.app.utils.Prefs
import java.util.Objects


/**
 * A simple [Fragment] subclass.
 * Use the [DeleteFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DeleteFragment : BaseFragment() {
private lateinit var mBinding:FragmentDeleteBinding
private val authViewModel by viewModels<PostViewModel>()
    var dialogs: Dialog? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        mBinding=DataBindingUtil.inflate(layoutInflater,R.layout.fragment_delete, container, false)
        dialogs = Dialog(requireActivity())
        dialogs?.requestWindowFeature(Window.FEATURE_CONTENT_TRANSITIONS)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
       deleteAccountObserver()
        mBinding.IvSetting.setOnClickListener {
            findNavController().navigateUp()
        }
        mBinding.tvDeleteButton.setOnClickListener {
            authViewModel.userDeleteAccount()
        }

    }

    private fun deleteAccountObserver() {
        authViewModel.userLogoutData.observe(requireActivity()) { response ->
            if (response != null) {
                if (response.success) {
                    dialogs?.dismiss()
                    Prefs.init().clear()
                    val intent = Intent(requireActivity(), LoginActivity::class.java)
                    startActivity(intent)
                    requireActivity().finish()
                    showToast(response.message.toString())
                }
            }
        }
        authViewModel.showLoading.observe(requireActivity()) {
            if (it){
                showProgressDialogs()
            }
        }
    }

    private fun showProgressDialogs() {
        dialogs = Dialog(requireActivity())
        dialogs?.requestWindowFeature(Window.FEATURE_CONTENT_TRANSITIONS)
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