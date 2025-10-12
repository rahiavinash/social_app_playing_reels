package com.wedo8020.app.mainui.dialogFragments

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.wedo8020.app.databinding.OptionBottomsheetLayoutBinding

class OtherUserOptionBottomSheet(
private val otherOptionItemClickListener: OtherOptionItemClickListener,private val isBlock: Boolean
) : BottomSheetDialogFragment() {
    private lateinit var binding: OptionBottomsheetLayoutBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext())
        dialog.setOnShowListener { dialogInterface ->
            val bottomSheetDialog = dialogInterface as BottomSheetDialog
            val bottomSheet =
                bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.layoutParams?.height = 1200
            val behaviour = BottomSheetBehavior.from(bottomSheet!!)
            behaviour.state = BottomSheetBehavior.STATE_EXPANDED
        }
        val window = dialog.window

        // Add the FLAG_NOT_TOUCH_MODAL flag to allow interaction with views outside the dialog
        window?.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_DIM_BEHIND
        )
        window?.setDimAmount(0.0f)
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (!::binding.isInitialized) {
            binding = OptionBottomsheetLayoutBinding.inflate(inflater, container, false)
        }
        setUpUI()
        return binding.root
    }
    private fun setUpUI(){
        binding.apply {
            ghostModeBtn.visibility = View.GONE
            swapBtn.visibility = View.GONE
            downloadBtn.visibility = View.GONE
            otherUserOption.visibility = View.VISIBLE

            btnShareProfile.setOnClickListener {
                otherOptionItemClickListener.shareClick()
                dismiss()
            }

            tvBlock.text = if (isBlock){
                "Block"
            }else {
                "Unblock"
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