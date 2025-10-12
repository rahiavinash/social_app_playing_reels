package com.wedo8020.app.mainui.home.profilesetting

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.wedo8020.app.R
import com.wedo8020.app.databinding.FragmentUserExperienceBinding
import com.wedo8020.app.mainui.base.BaseFragment
import com.wedo8020.app.mainui.home.postFragment.PostViewModel
import com.wedo8020.app.request.ExperienceRequest
import com.wedo8020.app.request.Settings
import com.wedo8020.app.utils.Utils
import java.util.Objects

class UserExperienceFragment : BaseFragment() {
    private lateinit var mBinding: FragmentUserExperienceBinding
    private val authViewModel by viewModels<PostViewModel>()
    private var pushOnOffSwitch = false
    private var switchForRestrictMode = false
    private var breakReminderSwitch = false
    var dialogs: Dialog? = null
    var switchClickable = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        mBinding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.fragment_user_experience,
            container,
            false
        )
        dialogs = Dialog(requireActivity())
        dialogs?.requestWindowFeature(Window.FEATURE_CONTENT_TRANSITIONS)
        callApiForGetExperience()
        setObserver()
        updateExperienceObserver()
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding.IvSetting.setOnClickListener {
            findNavController().navigateUp()
        }
        mBinding.PushOnOffSwitch.setOnClickListener {
            mBinding.PushOnOffSwitch.isEnabled=true
            mBinding.PushOnOffSwitch.isChecked = true
            if (switchClickable) {
                mBinding.PushOnOffSwitch.isEnabled = true
                pushOnOffSwitch = mBinding.PushOnOffSwitch.isChecked
            }else{
                mBinding.PushOnOffSwitch.isEnabled = false
            }
        }
        /** Switch color change blue to grey on click on/off*/
        val checkedColor = ContextCompat.getColorStateList(requireActivity(), R.color.appBlue)
        val uncheckedColor = ContextCompat.getColorStateList(requireActivity(), R.color.Grey)
        mBinding.PushOnOffSwitch.trackTintList =
            if (mBinding.PushOnOffSwitch.isChecked) checkedColor else uncheckedColor
        mBinding.PushOnOffSwitch.setOnCheckedChangeListener { _, isChecked ->
            mBinding.PushOnOffSwitch.trackTintList = if (isChecked) checkedColor else uncheckedColor
        }

        mBinding.SwitchForRestrictMode.setOnClickListener {
            switchForRestrictMode = mBinding.SwitchForRestrictMode.isChecked
            Log.e("pushOnOffSwitch", switchForRestrictMode.toString())
        }

        /** Switch color change blue to grey on click on/off*/
        mBinding.SwitchForRestrictMode.trackTintList =
            if (mBinding.SwitchForRestrictMode.isChecked) checkedColor else uncheckedColor
        mBinding.SwitchForRestrictMode.setOnCheckedChangeListener { _, isChecked ->
            mBinding.SwitchForRestrictMode.trackTintList =
                if (isChecked) checkedColor else uncheckedColor
        }
        mBinding.BreakReminderSwitch.setOnClickListener {
            breakReminderSwitch = mBinding.BreakReminderSwitch.isChecked
            Log.e("pushOnOffSwitch", breakReminderSwitch.toString())
        }

        /** Switch color change blue to grey on click on/off*/
        mBinding.BreakReminderSwitch.trackTintList =
            if (mBinding.BreakReminderSwitch.isChecked) checkedColor else uncheckedColor
        mBinding.BreakReminderSwitch.setOnCheckedChangeListener { _, isChecked ->
            mBinding.BreakReminderSwitch.trackTintList =
                if (isChecked) checkedColor else uncheckedColor
        }
        /** Done Button and Api for Update user Experience*/
        mBinding.tvSaveBtn.setOnClickListener {
            mBinding.tvSaveBtn.disableBtn()

            authViewModel.saveUserExperience(
                ExperienceRequest(
                    setting = Settings(
                        filterWord = pushOnOffSwitch,
                        breakReminder = breakReminderSwitch,
                        isRestrictedMode = switchForRestrictMode,
                    ),
                    filterWords = mBinding.edFilterWord.text.toString()
                )
            )

        }
        mBinding.edFilterWord.also {
            it.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    val length: Int = mBinding.edFilterWord.text.trim().length
                    mBinding.tvZero.text = length.toString()
                    mBinding.PushOnOffSwitch.isChecked = length>0

                }

                override fun afterTextChanged(p0: Editable?) {

                }
            })
        }
    }

    private fun callApiForGetExperience() {
        if (isInternetAvailable(requireContext())) {
            showProgressDialogs()
            authViewModel.getUserExperience()
        } else {
            dialogs?.dismiss()
            Utils.showToast("Please check your internet connection.")
        }

    }

    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    private fun setObserver() {
        authViewModel.experienceResponseData.observe(requireActivity()) { response ->
            if (response != null) {
                if (response.success) {
                    dialogs?.dismiss()
                    response.data?.let {
                        response.data!!.let {
                            if (!it.filterWords.isNullOrEmpty()){
                                mBinding.edFilterWord.setText(it.filterWords)
                            }

                            if (it.experienceSetting.isRestrictedMode) {
                                mBinding.SwitchForRestrictMode.isChecked = true
                                switchForRestrictMode = it.experienceSetting.isRestrictedMode
                            }
                            if (it.experienceSetting.filterWord) {
                                mBinding.PushOnOffSwitch.isChecked = true
                                pushOnOffSwitch = it.experienceSetting.filterWord
                            }
                            if (it.experienceSetting.breakReminder) {
                                mBinding.BreakReminderSwitch.isChecked = true
                                breakReminderSwitch = it.experienceSetting.breakReminder
                            }


                        }

                    }
                }
            }
        }

        authViewModel.showLoading.observe(requireActivity()) {
            if (it){
                showProgressDialogs()
            }else{
                dialogs!!.dismiss()
            }
        }

        authViewModel.showMessage.observe(requireActivity()) {
            dialogs?.dismiss()
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

    private fun updateExperienceObserver() {
        authViewModel.baseResponse.observe(requireActivity()) { response ->
            if (response != null) {
                if (response.success) {
                    dialogs?.dismiss()
                    Utils.showToast(response.message!!)
                    findNavController().navigateUp()

                }
            }
        }
        authViewModel.showLoading.observe(requireActivity()) {
        }
    }
}