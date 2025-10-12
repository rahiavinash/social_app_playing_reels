package com.wedo8020.app.mainui.home.profilesetting

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.wedo8020.app.R
import com.wedo8020.app.databinding.FragmentUpdateAccountDetailsBinding


/**
 * A simple [Fragment] subclass.
 * Use the [UpdateAccountDetailsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class UpdateAccountDetailsFragment : Fragment() {
private lateinit var mBinding:FragmentUpdateAccountDetailsBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mBinding=DataBindingUtil.inflate(inflater,R.layout.fragment_update_account_details, container, false)
        
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding.IvSetting.setOnClickListener {
            findNavController().navigateUp()
        }
    }
}