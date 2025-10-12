package com.wedo8020.app.mainui.home.createpost

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.wedo8020.app.R
import com.wedo8020.app.databinding.FragmentDisappearsInBottomSheetBinding

class DisappearsTimeBottomSheet(onClick:OnClickListener) : BottomSheetDialogFragment() {
    private var onClickDisappear:OnClickListener=onClick
    private lateinit var mBinding: FragmentDisappearsInBottomSheetBinding
    private var disappearData=""
    private var disappearSendData=""
    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding= DataBindingUtil.inflate(layoutInflater,
            R.layout.fragment_disappears_in_bottom_sheet, container, false)

        Log.d("diappearTime", arguments?.getString("disappearTime").toString())
        checkIfFragmentAttached {
            disappearData = arguments?.getString("disappearTime").toString()
            when (arguments?.getString("disappearTime")) {

                "24 Hours" -> {
                    mBinding.tv24hours.setBackgroundDrawable(requireContext().getDrawable(R.drawable.blue_tag))
                    mBinding.tv24hours.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.white
                        )
                    )
                }

                "48 Hours" -> {
                    mBinding.tv48hours.setBackgroundDrawable(requireContext().getDrawable(R.drawable.blue_tag))
                    mBinding.tv48hours.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.white
                        )
                    )
                }

                "7 days" -> {
                    mBinding.tv7days.setBackgroundDrawable(requireContext().getDrawable(R.drawable.blue_tag))
                    mBinding.tv7days.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.white
                        )
                    )
                }

                "30 days" -> {
                    mBinding.tv30days.setBackgroundDrawable(requireContext().getDrawable(R.drawable.blue_tag))
                    mBinding.tv30days.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.white
                        )
                    )
                }

                "90 days" -> {
                    mBinding.tv90days.setBackgroundDrawable(requireContext().getDrawable(R.drawable.blue_tag))
                    mBinding.tv90days.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.white
                        )
                    )
                }

                "Never" -> {
                    mBinding.tvNever.setBackgroundDrawable(requireContext().getDrawable(R.drawable.blue_tag))
                    mBinding.tvNever.setTextColor(
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

        checkIfFragmentAttached {
            mBinding.tv24hours.setOnClickListener {
                disappearData = "24 Hours"
                disappearSendData = mBinding.tv24hours.contentDescription.toString()
                onClickDisappear.onClickDisappearClick(disappearData, disappearSendData)
                mBinding.tv24hours.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )
                dismiss()
            }
            mBinding.tv48hours.setOnClickListener {
                disappearData = "48 Hours"
                disappearSendData = mBinding.tv48hours.contentDescription.toString()
                onClickDisappear.onClickDisappearClick(disappearData, disappearSendData)
                mBinding.tv48hours.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )
                dismiss()
            }
            mBinding.tv7days.setOnClickListener {
                disappearData = "7 days"
                disappearSendData = mBinding.tv7days.contentDescription.toString()
                onClickDisappear.onClickDisappearClick(disappearData, disappearSendData)
                mBinding.tv7days.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )
                dismiss()
            }
            mBinding.tv30days.setOnClickListener {
                disappearData = "30 days"
                disappearSendData = mBinding.tv30days.contentDescription.toString()
                onClickDisappear.onClickDisappearClick(disappearData, disappearSendData)
                mBinding.tv30days.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )
                dismiss()
            }
            mBinding.tv90days.setOnClickListener {
                disappearData = "90 days"
                disappearSendData = mBinding.tv90days.contentDescription.toString()
                onClickDisappear.onClickDisappearClick(disappearData, disappearSendData)
                mBinding.tv90days.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )
                dismiss()
            }

            mBinding.tvNever.setOnClickListener {
                disappearData = "Never"
                disappearSendData = mBinding.tvNever.contentDescription.toString()
                onClickDisappear.onClickDisappearClick(disappearData, disappearSendData)
                mBinding.tvNever.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )
                dismiss()
            }
        }
    }

    interface  OnClickListener{
        fun onClickDisappearClick(disappearData:String,disappearSendData: String)
    }
    private fun checkIfFragmentAttached(operation: Context.() -> Unit) {
        if (isAdded && context != null) {
            operation(requireContext())
        }
    }
}