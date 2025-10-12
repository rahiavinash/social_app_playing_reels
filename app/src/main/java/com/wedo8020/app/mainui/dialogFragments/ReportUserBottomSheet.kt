package com.wedo8020.app.mainui.dialogFragments

import com.wedo8020.app.databinding.UserReportOptionSheetBinding
import com.wedo8020.app.mainui.base.BaseBottomSheetFragment

class ReportUserBottomSheet(
    override val layoutId: Int,
    private val clickListeners: ClickListeners
): BaseBottomSheetFragment<UserReportOptionSheetBinding>() {


    override fun onSetUpUi(binding: UserReportOptionSheetBinding) {
        binding.apply {
            meBtn.setOnClickListener {
                clickListeners.reportClick(meText.text.toString())
                dismiss()
            }
            someoneBtn.setOnClickListener {
                clickListeners.reportClick(someoneText.text.toString())
                dismiss()
            }
            publicFigureBtn.setOnClickListener {
                clickListeners.reportClick(publicFigureText.text.toString())
                dismiss()
            }
            businessBtn.setOnClickListener {
                clickListeners.reportClick(businessText.text.toString())
                dismiss()
            }
            politcalBtn.setOnClickListener {
                clickListeners.reportClick(politicalText.text.toString())
                dismiss()
            }
            hateSpeechBtn.setOnClickListener {
                clickListeners.reportClick(hateText.text.toString())
                dismiss()
            }
            nudityBtn.setOnClickListener {
                clickListeners.reportClick(nudityText.text.toString())
                dismiss()
            }
            bullyingBtn.setOnClickListener {
                clickListeners.reportClick(bullyingText.text.toString())
                dismiss()
            }

        }
    }
    interface ClickListeners{
        fun reportClick(reason: String)
    }
}