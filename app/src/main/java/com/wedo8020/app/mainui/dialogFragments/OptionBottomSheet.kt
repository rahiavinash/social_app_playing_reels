package com.wedo8020.app.mainui.dialogFragments

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.wedo8020.app.databinding.OptionBottomsheetLayoutBinding
import com.wedo8020.app.postOptionListeners
import com.wedo8020.app.response.Options
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class OptionBottomSheet(
    private val postOptionListeners: postOptionListeners,
    val position: Int,
    val url: String,
    val options: Options,
    private val ownPost: Boolean,private val otherProfile:Boolean = false
) : BottomSheetDialogFragment() {
    private lateinit var binding: OptionBottomsheetLayoutBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext())
        dialog.setOnShowListener { dialogInterface ->
            val bottomSheetDialog = dialogInterface as BottomSheetDialog
            val bottomSheet =
                bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.layoutParams?.height = ViewGroup.LayoutParams.WRAP_CONTENT
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

    private fun setUpUI() {
        binding.apply {
            options.also {
                if (!it.download) {
                    downloadBtn.visibility = View.GONE
                }
                if (!it.repost) {
                    repostBtn.visibility = View.GONE
                }
            }
            if (ownPost) {
                ownPostOptions.visibility = View.VISIBLE
            } else {
                otherPostOption.visibility = View.VISIBLE
            }
            if (otherProfile){
                notInterestedBtn.visibility = View.GONE
                icRestrictBtn.visibility = View.GONE
            }
            ghostModeBtn.setOnClickListener {
                postOptionListeners.ghostMode()
                dismiss()
            }
            swapBtn.setOnClickListener {
                postOptionListeners.swapLayout()
                dismiss()
            }
            downloadBtn.setOnClickListener {

                postOptionListeners.downloadPost(position)
                dismiss()

            }
            repostBtn.setOnClickListener {
                postOptionListeners.userRepostClick()
                dismiss()
            }
            icRestrictBtn.setOnClickListener {
                postOptionListeners.userRestrictClick()
                dismiss()
            }
            reportBtn.setOnClickListener {
                postOptionListeners.userReportClick()
                dismiss()
            }
            notInterestedBtn.setOnClickListener {
                postOptionListeners.notInterestedClick()
                dismiss()
            }
            deleteBtn.setOnClickListener {
                postOptionListeners.deletePost()
                dismiss()
            }
            editBtn.setOnClickListener {
                postOptionListeners.editPost()
                dismiss()
            }
            archiveBtn.setOnClickListener {
                postOptionListeners.archivePost()
                dismiss()
            }
            postInfoBtn.setOnClickListener {
                postOptionListeners.postInfoClick(position)
            }
        }
    }
}