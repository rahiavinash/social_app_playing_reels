package com.wedo8020.app.mainui.dialogFragments

import com.wedo8020.app.databinding.FragmentRemindMeBottomSheetBinding
import com.wedo8020.app.mainui.base.BaseBottomSheetFragment

class RemindMeBottomSheet(override val layoutId: Int,private val clickListeners: RemindMeBottomSheetListener) : BaseBottomSheetFragment<FragmentRemindMeBottomSheetBinding>() {

    override fun onSetUpUi(binding: FragmentRemindMeBottomSheetBinding) {

        binding.tvMints.setOnClickListener {
            clickListeners.onClick60Mins()
            dismiss()

        }

        binding.tvNever.setOnClickListener {
            clickListeners.onClickNever()
            dismiss()

        }
    }

    interface  RemindMeBottomSheetListener {
        fun onClick60Mins ()
        fun onClickNever ()
    }


}