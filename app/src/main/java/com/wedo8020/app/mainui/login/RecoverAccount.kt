package com.wedo8020.app.mainui.login

import android.annotation.SuppressLint
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.wedo8020.app.R
import com.wedo8020.app.databinding.FragmentRecoverAccountBinding
import com.wedo8020.app.mainui.base.BaseFragment
import com.wedo8020.app.response.RecoveryCodeRequest
import com.wedo8020.app.utils.Utils


class RecoverAccount : BaseFragment() {

    private lateinit var binding : FragmentRecoverAccountBinding
    private val viewModel by viewModels<LoginViewModel>()
    private var androidId : String?=null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        binding =  FragmentRecoverAccountBinding.inflate(inflater,container,false)
        //binding.recoveryCode.showSoftKeyboard()
        return binding.root
    }

    @SuppressLint("HardwareIds")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }
        androidId = Settings.Secure.getString(
            requireActivity().contentResolver,
            Settings.Secure.ANDROID_ID
        )

        binding.recoverButton.isEnabled =  false
        binding.recoveryCode.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {

                if(s?.isEmpty() == false) {
                    if (s.isNotEmpty() && s.startsWith(" ")) {

                        binding.recoveryCode.setText(s.trimStart())
                        binding.recoveryCode.setSelection(binding.recoveryCode.text.length)
                    }
                    if(isInternetAvailable(requireContext())){

                        viewModel.recoveryCode(RecoveryCodeRequest(recoveryCode = s.toString(), deviceToken = androidId))
                        Log.d("authenticationCode", "Api is hitting")
                    }
                    else {
                        Utils.showToast("Please check your internet connection.")
                    }


                }
                else {
                    binding.recoveryCode.clearFocus()
                    binding.recoverButton.isEnabled = false
                    binding.recoverButton.setBackgroundResource(R.drawable.white_button_background_two)
                    binding.recoverButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.Grey))
                }

            }
        })

        viewModel.recoveryCodeData.observe(viewLifecycleOwner, Observer {
            response ->
            if(response.success == true){
                Log.d("responseSuccess", "Success : ${response.data}")
                binding.recoverButton.isEnabled =  true
                binding.recoverButton.setBackgroundResource(R.drawable.blue_button_background_two)
                binding.recoverButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                Log.d("successToken", " Token : ${response.data?.username.toString()} ${response.data?.password.toString()}")
                val updatedBundle = Bundle()
                updatedBundle.putString("username", response.data?.username.toString())
                updatedBundle.putString("password", response.data?.password.toString())


                bundle = updatedBundle
            }
            else {

                binding.recoverButton.isEnabled = false
                binding.recoverButton.setBackgroundResource(R.drawable.white_button_background_two)
                binding.recoverButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.Grey))
            }
        })

        binding.recoverButton.setOnClickListener {
            findNavController().navigate(R.id.recoveredAccountFragment, bundle)
        }
    }

    companion object {
        var bundle = Bundle()
    }



}