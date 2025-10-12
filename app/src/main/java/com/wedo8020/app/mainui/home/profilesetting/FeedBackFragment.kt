package com.wedo8020.app.mainui.home.profilesetting

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.wedo8020.app.R
import com.wedo8020.app.databinding.FragmentFeedBackBinding
import com.wedo8020.app.mainui.base.BaseFragment
import com.wedo8020.app.mainui.home.postFragment.PostViewModel
import com.wedo8020.app.request.FeedBackRequest
import com.wedo8020.app.utils.Utils
import com.wedo8020.app.utils.Utils.disableMultiTap


class FeedBackFragment : BaseFragment() {

    private lateinit var mBinding: FragmentFeedBackBinding
    private val authViewModel by viewModels<PostViewModel>()
    private var titleText = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_feed_back, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateFeedbackObserver()
        hideKeyboardOnOutSideClick(mBinding.clFeedback)
        mBinding.edFeedbackMessage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (p0!!.trim().isNotEmpty() && !titleText.isNullOrEmpty()) {
                    mBinding.tvBlueSend.visibility = View.VISIBLE
                } else {
                    mBinding.tvBlueSend.visibility = View.INVISIBLE
                }
                mBinding.tvZero.text = p0.length.toString()
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })

        mBinding.let {
            val clickableViews = listOf(
                it.clExistingFeature,
                it.clNewFeature,
                it.clContent,
                it.clXChange,
                it.clPositive,
                it.clNegative,
                it.clDesign,
                it.clOther
            )
            val radioSelectViews = listOf(
                it.ivRadioSelect,
                it.ivRadioSelect1,
                it.ivRadioSelect3,
                it.ivRadioSelect4,
                it.ivRadioSelect5,
                it.ivRadioSelect6,
                it.ivRadioSelect7,
                it.ivRadioSelect8
            )
            val text = listOf(
                it.tvPopular,
                it.tvPopular2,
                it.tvPopular3,
                it.tvPopular4,
                it.tvPopular5,
                it.tvPopular6,
                it.tvPopular7,
                it.tvPopular8
            )



            clickableViews.forEachIndexed { index, v ->
                v.setOnClickListener {
                    radioSelectViews.forEach { it.visibility = View.GONE }
                    radioSelectViews[index].visibility = View.VISIBLE
                    titleText = text[index].text.toString()
                    if (titleText.isNotEmpty() && !mBinding.edFeedbackMessage.text.isNullOrEmpty()) {
                        mBinding.tvBlueSend.visibility = View.VISIBLE
                    }
                }
            }

        }


        mBinding.IvBack.setOnClickListener {
            findNavController().navigateUp()
        }


        mBinding.tvBlueSend.setOnClickListener {
            mBinding.tvBlueSend.disableMultiTap()
            if (titleText.isNotEmpty() && !mBinding.edFeedbackMessage.text.isNullOrEmpty()) {
                callApiForUpdateFeedback()
            }
        }
    }

    private fun callApiForUpdateFeedback() {
        if (isInternetAvailable(requireContext())) {
            authViewModel.updateFeedback(
                FeedBackRequest(
                    title = titleText,
                    message = mBinding.edFeedbackMessage.text.toString()
                )
            )
        } else {
            Utils.showToast("Please check your internet connection.")
        }

    }


    @SuppressLint("SuspiciousIndentation")
    private fun updateFeedbackObserver() {
        authViewModel.baseResponse.observe(requireActivity()) { response ->
            if (response != null) {
                if (response.success) {
                    com.wedo8020.app.utils.Utils.showToast(response.message!!)
                    findNavController().navigateUp()
                }
            }
        }
        authViewModel.showLoading.observe(requireActivity()) {
        }

    }


}