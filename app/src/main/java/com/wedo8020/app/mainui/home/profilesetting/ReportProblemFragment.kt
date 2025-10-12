
package com.wedo8020.app.mainui.home.profilesetting

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.wedo8020.app.R
import com.wedo8020.app.databinding.FragmentRepportProblemBinding

/**
 * A simple [Fragment] subclass.
 * Use the [ReportProblemFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ReportProblemFragment : Fragment() {
    private lateinit var mBinding:FragmentRepportProblemBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mBinding=DataBindingUtil.inflate(inflater,R.layout.fragment_repport_problem, container, false)

        return mBinding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mBinding.IvBack.setOnClickListener {
            findNavController().navigateUp()
        }

        mBinding.clExistingFeature.setOnClickListener {
            if (mBinding.ivRadioSelect.visibility==View.VISIBLE){
                mBinding.ivRadioSelect.visibility=View.GONE
            }else{
                mBinding.ivRadioSelect.visibility=View.VISIBLE
            }
        }

        mBinding.clNewFeature.setOnClickListener {
            if (mBinding.ivRadioSelect1.visibility==View.VISIBLE){
                mBinding.ivRadioSelect1.visibility=View.GONE
            }else{
                mBinding.ivRadioSelect1.visibility=View.VISIBLE
            }
        }

        mBinding.clContent.setOnClickListener {
            if (mBinding.ivRadioSelect3.visibility==View.VISIBLE){
                mBinding.ivRadioSelect3.visibility=View.GONE
            }else{
                mBinding.ivRadioSelect3.visibility=View.VISIBLE
            }
        }

        mBinding.clXChange.setOnClickListener {
            if (mBinding.ivRadioSelect4.visibility==View.VISIBLE){
                mBinding.ivRadioSelect4.visibility=View.GONE
            }else{
                mBinding.ivRadioSelect4.visibility=View.VISIBLE
            }
        }


        mBinding.clPositive.setOnClickListener {
            if (mBinding.ivRadioSelect5.visibility==View.VISIBLE){
                mBinding.ivRadioSelect5.visibility=View.GONE
            }else{
                mBinding.ivRadioSelect5.visibility=View.VISIBLE
            }
        }


        mBinding.clNegative.setOnClickListener {
            if (mBinding.ivRadioSelect6.visibility==View.VISIBLE){
                mBinding.ivRadioSelect6.visibility=View.GONE
            }else{
                mBinding.ivRadioSelect6.visibility=View.VISIBLE
            }
        }




    }

}