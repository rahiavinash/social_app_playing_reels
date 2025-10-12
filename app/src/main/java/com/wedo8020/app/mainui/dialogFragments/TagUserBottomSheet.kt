package com.wedo8020.app.mainui.dialogFragments

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.wedo8020.app.R
import com.wedo8020.app.databinding.TagUserBottomsheetLayoutBinding
import com.wedo8020.app.mainui.adapter.TagUserAdapter
import com.wedo8020.app.response.TagUser
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class TagUserBottomSheet(private val taguserList: ArrayList<TagUser>,clickListener: TagUserAdapter.ClickListener): BottomSheetDialogFragment(),TagUserAdapter.ClickListener {
    lateinit var binding: TagUserBottomsheetLayoutBinding
    lateinit var tagUserAdapter: TagUserAdapter
    val followinClickListener = clickListener

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if(!::binding.isInitialized){
            binding = TagUserBottomsheetLayoutBinding.inflate(inflater,container,false)
        }

        setUpUI()
        return binding.root
    }
    private fun setUpUI(){
        binding.apply {
            tagUserAdapter = TagUserAdapter(R.layout.tag_user_list_item,this@TagUserBottomSheet)
            tagUserRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            tagUserRecyclerView.adapter = tagUserAdapter
            tagUserAdapter.setNewItems(taguserList)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext())
        dialog.setOnShowListener { dialogInterface ->
            val bottomSheetDialog = dialogInterface as BottomSheetDialog
            val bottomSheet = bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.layoutParams?.height = 900
            val behaviour = BottomSheetBehavior.from(bottomSheet!!)
            behaviour.state = BottomSheetBehavior.STATE_HALF_EXPANDED
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


    override fun followClick(position:Int) {
        followinClickListener.followClick(position)
    }
}