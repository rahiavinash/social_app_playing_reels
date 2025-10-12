package com.wedo8020.app.mainui.login

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.wedo8020.app.R
import com.wedo8020.app.databinding.FragmentLoginSignUpBinding
import com.wedo8020.app.mainui.base.BaseFragment
import com.wedo8020.app.utils.Utils


class LoginSignUpFragment : BaseFragment() {

    private lateinit var binding : FragmentLoginSignUpBinding
    private val viewModel by viewModels<LoginViewModel>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginSignUpBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.loginButton.setOnClickListener {
            findNavController().navigate(R.id.action_loginSignUpFragment_to_loginFragment)
        }
        viewModel.accountAvailability()
        binding.createAccountButton.setOnClickListener {

            if(isInternetAvailable(requireContext())){
                binding.constraintLayout.visibility = View.VISIBLE


                viewModel.accountAvailability.observe(viewLifecycleOwner){
                        response ->

                    if(response.success == true && response.data?.status==true){
                        binding.constraintLayout.visibility = View.GONE
                        Log.d("accountAvailability"," Api is success ${response}")
                        findNavController().navigate(R.id.action_loginSignUpFragment_to_createAccountFragment)
                    }
                    else if(response.data?.status== false){
                        binding.constraintLayout.visibility = View.GONE
                        Log.d("accountAvailability"," Api is not success ${response}")
                        findNavController().navigate(R.id.action_loginSignUpFragment_to_noAccountAvailable)
                    }
                    else {
                        binding.constraintLayout.visibility = View.GONE
                        Utils.showToast(response.message.toString())
                    }



                }
            }
            else {
                Utils.showToast("Please check your internet connection.")
            }

        }




        binding.recoverMyAccount.setOnClickListener {
            findNavController().navigate(R.id.action_loginSignUpFragment_to_recoverAccount)
        }
    }


}