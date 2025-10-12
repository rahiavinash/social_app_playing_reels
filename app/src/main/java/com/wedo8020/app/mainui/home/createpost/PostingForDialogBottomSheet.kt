package com.wedo8020.app.mainui.home.createpost

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.wedo8020.app.R
import com.wedo8020.app.databinding.FragmentPostingFoDialogBinding

@Suppress("DEPRECATION")
class PostingForDialogBottomSheet(onClickListener:OnClickListener): BottomSheetDialogFragment() {
    private var onClickListeners:OnClickListener= onClickListener
    private lateinit var mBinding: FragmentPostingFoDialogBinding
    var bundle = Bundle()
    private var postingFor=""
    private var postingForText=""
    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        mBinding= DataBindingUtil.inflate(layoutInflater,
            R.layout.fragment_posting_fo_dialog, container, false )
        disableBottomSheetDraggableBehavior()
        checkIfFragmentAttached {
            when (arguments?.getString("postText")) {
                "Friends" -> {
                    mBinding.tvFriends.setBackgroundDrawable(requireContext().getDrawable(R.drawable.blue_tag))
                    mBinding.tvFriends.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.white
                        )
                    )
                }

                "Followers" -> {
                    mBinding.tvFollower.setBackgroundDrawable(requireContext().getDrawable(R.drawable.blue_tag))
                    mBinding.tvFollower.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.white
                        )
                    )
                }

                "Public" -> {
                    mBinding.tvPublic.setBackgroundDrawable(requireContext().getDrawable(R.drawable.blue_tag))
                    mBinding.tvPublic.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.white
                        )
                    )
                }

                "Only Me" -> {
                    mBinding.tvOnlyMe.setBackgroundDrawable(requireContext().getDrawable(R.drawable.blue_tag))
                    mBinding.tvOnlyMe.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.white
                        )
                    )
                }
            }
        }
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding.tvOnlyMe.setOnClickListener {
            postingFor="onlyMe"
            postingForText="Only Me"
            onClickListeners.onClickTags(postingFor,postingForText)
            dismiss()
        }
        mBinding.tvFriends.setOnClickListener {
            postingFor="friends"
            postingForText="Friends"
            onClickListeners.onClickTags(postingFor,postingForText)
            dismiss()
        }
        mBinding.tvFollower.setOnClickListener {
            postingFor="followers"
            postingForText="Followers"
            onClickListeners.onClickTags(postingFor,postingForText)
            dismiss()
        }
        mBinding.tvPublic.setOnClickListener {
            postingFor="everyOne"
            postingForText="Public"
            onClickListeners.onClickTags(postingFor,postingForText)
            dismiss()
        }
    }



    private fun disableBottomSheetDraggableBehavior() {
        this.isCancelable = false
        this.dialog?.setCanceledOnTouchOutside(true)
    }

    interface  OnClickListener{
        fun onClickTags(data:String,postingForText:String)
    }
    private fun checkIfFragmentAttached(operation: Context.() -> Unit) {
        if (isAdded && context != null) {
            operation(requireContext())
        }
    }
}