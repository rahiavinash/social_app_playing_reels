package com.wedo8020.app.mainui.home.profiletab

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.wedo8020.app.R
import com.wedo8020.app.databinding.FragmentShowProfileBinding
import com.wedo8020.app.mainui.base.BaseFragment
import com.wedo8020.app.mainui.home.postFragment.PostViewModel
import com.wedo8020.app.utils.Utils
import com.wedo8020.app.utils.Utils.loadImageAsync
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class ShowProfileFragment : BaseFragment() {
    private lateinit var mBinding: FragmentShowProfileBinding
    private val authViewModel by viewModels<PostViewModel>()
    var dialogs: Dialog? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        mBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_show_profile, container, false)
        hideNavBottom()
        if (context!=null){
            dialogs = Dialog(requireActivity())
            dialogs?.requestWindowFeature(Window.FEATURE_CONTENT_TRANSITIONS)
        }
        if (!arguments?.getString("profile").isNullOrEmpty()){
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    delay(1000)
                    mBinding.animationView.visibility = View.GONE
                    mBinding.userProfile.loadImageAsync(arguments?.getString("profile").toString())
                } catch (e: Exception) {
                    mBinding.animationView.visibility = View.GONE
                }

            }
        }else{
            callApi()
            setObserver()
        }



        mBinding.animationView.visibility = View.VISIBLE
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding.IvSetting.setOnClickListener {
            findNavController().navigateUp()
        }
    }
    fun callApi() {
        if (context != null) {
            if (isInternetAvailable(requireActivity())) {
                // showProgressDialog()
                authViewModel.userDertails()
            } else {
                dialogs?.dismiss()
                Utils.showToast("Please check your internet connection.")
            }
        }
    }

    private fun setObserver() {
        authViewModel.userDetails.observe(viewLifecycleOwner) { response ->
            dialogs?.dismiss()
            mBinding.animationView.visibility = View.GONE
            if (response != null) {
                if (response.success) {
                    if (response.data != null) {
                        CoroutineScope(Dispatchers.Main).launch {
                            mBinding.userProfile.loadImageAsync(
                                response.data?.profile.toString()
                            )
                        }
                    }
                }
            }
        }
                    }
}