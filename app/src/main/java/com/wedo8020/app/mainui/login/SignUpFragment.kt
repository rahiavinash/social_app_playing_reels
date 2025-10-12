package com.wedo8020.app.mainui.login

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import com.wedo8020.app.MainApplication
import com.wedo8020.app.R
import com.wedo8020.app.databinding.FragmentSignUpBinding
import com.wedo8020.app.eventBus.isSocketConnected
import com.wedo8020.app.mainui.base.BaseFragment
import com.wedo8020.app.utils.Utils
import com.wedo8020.zelpyy.SocketProvider.SocketIOImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.json.JSONObject


class

SignUpFragment : BaseFragment() {
    private lateinit var binding : FragmentSignUpBinding
    private lateinit var socket: SocketIOImpl
    var success : String?=null
    var data : String ?=null
    var userName : String ?= null
    private val viewModel by viewModels<LoginViewModel>()
    private val usernameValidationLiveData = MutableLiveData<String>()
    private var enteredUsername: String = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        binding =  FragmentSignUpBinding.inflate(inflater,container,false)
        //binding.usernameSearch.requestFocus()
        binding.usernameSearch.showSoftKeyboard()
        socket = MainApplication.get().socket

        return binding.root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("username", enteredUsername)
        super.onSaveInstanceState(outState)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hideKeyboardOnOutSideClick(binding.root)


        binding.nextButton.isEnabled = false
        val allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890.-_"

        val filter = InputFilter { source, _, _, dest, _, _ ->
            if (source != null && !source.all { allowedChars.contains(it) }) {
                ""
            } else null
        }
        binding.usernameSearch.filters = arrayOf(filter)



//        binding.usernameSearch.showSoftKeyboard()
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }
        viewModel.username?.let { storedUsername ->
            binding.usernameSearch.setText(storedUsername)
        }

        binding.nextButton.setOnClickListener {
            findNavController().navigate(R.id.createPasswordFragment, bundle)
        }
        val maxLength = 20
        binding.usernameSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(isInternetAvailable(requireContext())){
                    if (s.toString()==""){
                        binding.usernameValidation.visibility = View.INVISIBLE

                    }else{
                        binding.usernameValidation.visibility = View.VISIBLE
                        bundle.putString("userName", s.toString())
                        userName = s.toString()
                        getUserName(s.toString())

                        check(userName = s.toString(), accountName = binding.accountName.text.toString())
                    }
                    check(userName = s.toString(), accountName = binding.accountName.text.toString())
                }
                else {
                    Utils.showToast("Please check your internet connection.")
                }

            }

            override fun afterTextChanged(s: Editable?) {
                viewModel.username = s?.toString()
                enteredUsername = s.toString()
                val textWithoutSpaces = s.toString().replace(" ", "")


                if (textWithoutSpaces.length > maxLength) {
                    val truncatedText = textWithoutSpaces.substring(0, maxLength)
                    binding.usernameSearch.setText(truncatedText)
                    binding.usernameSearch.setSelection(truncatedText.length)
                }

            }
        })



            setSocketListenOn()



        binding.accountName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {


            }

            override fun afterTextChanged(s: Editable?) {
//                if((s?.length ?:0)> 20){
//                    val truncatedText = s?.subSequence(0, 20)
//                    binding.accountName.setText(truncatedText)
//                    binding.accountName.setSelection(truncatedText?.length ?: 0)
//                }
                if (s?.isNotEmpty() == true && s.startsWith(" ")) {

                    binding.accountName.setText(s.trimStart())
                    binding.accountName.text?.let { binding.accountName.setSelection(it.length) }
                }
                else {
                    bundle.putString("accountName", s.toString())
                    check(userName = binding.usernameSearch.text.toString(), accountName = s.toString())
                }


            }
        })
        binding.accountName.setOnKeyListener { _, keyCode, event ->
            handleBackPress(keyCode, event)
        }

        binding.usernameSearch.setOnKeyListener { _, keyCode, event ->
            handleBackPress(keyCode, event)
        }

    }

    private fun handleBackPress(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && event?.action == KeyEvent.ACTION_UP) {
            //updateNextButtonState(binding.usernameValidation.text.toString())
            return true
        }
        return false
    }
    private fun check(userName: String, accountName : String) {

        usernameValidationLiveData.observe(viewLifecycleOwner) { validationText ->
            Log.d("validationText", "Validation : $validationText")

            // Ensure the current username matches the validation result


            if (validationText == "Available" && userName.isNotEmpty() && accountName.isNotEmpty()) {
                binding.nextButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                binding.nextButton.setBackgroundResource(R.drawable.blue_button_background_two)
                binding.nextButton.isEnabled = true
            } else {
                binding.nextButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.Grey))
                binding.nextButton.setBackgroundResource(R.drawable.white_button_background_two)
                binding.nextButton.isEnabled = false
            }
        }

    }




    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }
    override fun onResume() {
        super.onResume()
        if(isInternetAvailable(requireContext())){
            if (!socket.isConnected()) {
                socket.connect()
                Log.e("socketIsConnect", "notConghnected")
            } else {
                Log.e("socketIsConnect", "Connected")

                setSocketListenOn()


            }
        }
        else {
            Utils.showToast("Please check your internet connection")
        }

    }

    private fun getUserName(userName: String) {

        socket.emitMessage("checkUsername", userName) { _, _ ->
        }
    }

    private fun setSocketListenOn() {
        socket.listenOff("usernameSuccess")

        socket.listenOff("error")


        socket.listenOn("usernameSuccess") { args ->
            CoroutineScope(Dispatchers.Main).launch {


                Log.e("usernameSuccess", args[0].toString())
                val obj = JSONObject(args[0].toString())

                success = obj.getString("success")
                data = obj.getString("data")

                if(success == "true" && data =="true"){
                    binding.usernameValidation.setTextColor(Color.parseColor("#0AB210"))
                    binding.usernameValidation.text="Available"
                    usernameValidationLiveData.value = "Available"
                }
                else if(data == "false"){
                    binding.usernameValidation.setTextColor(Color.parseColor("#FF0340"))
                    binding.usernameValidation.text="Not Available"
                    usernameValidationLiveData.value = "Not Available"

                }
            }
        }


    }


    @Subscribe
    fun isSocketConnected(event: Any){
        if(isInternetAvailable(requireContext())){
            if (event is isSocketConnected){
                if (event.isConnected){
                    Log.e("socketIsConnect", "connected")

                    setSocketListenOn()


                    // getUserName(enteredUserName)

                }else{
                    socket.connect()
                }
            }
        }
        else {
            Utils.showToast("Please check your internet connection.")
        }

    }

    companion object{
        val bundle = Bundle()
    }

}