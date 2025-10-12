package com.wedo8020.app.mainui.dialogFragments

import android.view.View
import com.wedo8020.app.R
import com.wedo8020.app.databinding.OptionBottomsheetLayoutBinding
import com.wedo8020.app.mainui.base.BaseBottomSheetFragment

class OtherOptionBottomSheet(
    override val layoutId: Int,
    private val otherOptionItemClickListener: OtherOptionItemClickListener,
    private val isBlock: Boolean
) : BaseBottomSheetFragment<OptionBottomsheetLayoutBinding>() {
    override fun onSetUpUi(binding: OptionBottomsheetLayoutBinding) {
        binding.apply {

            ghostModeBtn.visibility = View.GONE
            swapBtn.visibility = View.GONE
            downloadBtn.visibility = View.GONE
            postInfoBtn.visibility = View.GONE
            mainLayout.setBackgroundColor(requireContext().getColor(R.color.pureWhite))
            otherUserOption.visibility = View.VISIBLE

            btnShareProfile.setOnClickListener {
                otherOptionItemClickListener.shareClick()
                dismiss()
            }

            tvBlock.text = if (isBlock) {
                "Unblock"
            } else {
                "Block"
            }
            btnBlock.setOnClickListener {
                otherOptionItemClickListener.blockClick()
                dismiss()
            }
            btnReport.setOnClickListener {
                otherOptionItemClickListener.reportClick()
                dismiss()
            }
        }
    }
    interface OtherOptionItemClickListener{
        fun shareClick()
        fun blockClick()
        fun reportClick()
    }
}