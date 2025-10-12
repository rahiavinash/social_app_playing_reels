package com.wedo8020.app.mainui.home.chatAndNotification

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.wedo8020.app.MainApplication
import com.wedo8020.app.R
import com.wedo8020.app.databinding.FragmentMessagingSettingBottomSheetBinding
import com.wedo8020.app.mainui.base.BaseBottomSheetFragment
import com.wedo8020.app.utils.Prefs
import com.wedo8020.app.utils.ReadReceipt
import com.wedo8020.zelpyy.SocketProvider.SocketIOImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject



class MessagingSettingBottomSheet(override val layoutId: Int) : BaseBottomSheetFragment<FragmentMessagingSettingBottomSheetBinding>() {
    private var pushOnOffSwitch = false
    // private val authViewModel by viewModels<PostViewModel>()
    private lateinit var socket: SocketIOImpl
var bundle=Bundle()
    @SuppressLint("SuspiciousIndentation")
    override fun onSetUpUi(binding: FragmentMessagingSettingBottomSheetBinding) {
        socket = MainApplication.get().socket
      var switch=arguments?.getString("pushOnOffSwitch")!!
        if (switch=="true"){
            pushOnOffSwitch=true
            binding.PushOnOffSwitch.isChecked=pushOnOffSwitch
        }else{
            pushOnOffSwitch=false
            binding.PushOnOffSwitch.isChecked=pushOnOffSwitch
        }
       /* callApi()
        detailsObserver(binding)*/
        binding.PushOnOffSwitch.setOnClickListener {
            pushOnOffSwitch = binding.PushOnOffSwitch.isChecked
            Log.e("pushOnOffSwitch", pushOnOffSwitch.toString())

            if (!socket.isConnected()) {
                socket.connect()
                Log.e("socketConnect", "notConnected")
            } else {
                Log.e("socketConnect", "Connected")
                setSocketListenON()
                postReadReceipt()
            }
            org.greenrobot.eventbus.EventBus.getDefault()
                .post(ReadReceipt("Read"))

        }
        /** Switch color change blue to grey on click on/off*/
         val checkedColor = ContextCompat.getColorStateList(requireActivity(), R.color.appBlue)
         val uncheckedColor = ContextCompat.getColorStateList(requireActivity(), R.color.Grey)
        binding.PushOnOffSwitch.trackTintList =
            if (binding.PushOnOffSwitch.isChecked) checkedColor else uncheckedColor
        binding.PushOnOffSwitch.setOnCheckedChangeListener { _, isChecked ->
            binding.PushOnOffSwitch.trackTintList = if (isChecked) checkedColor else uncheckedColor
        }

        binding.clBlockedList.setOnClickListener {
            bundle.putString("clBlockedList" ,"clBlockedList")
            findNavController().navigate(R.id.blockedUserFragment ,bundle)
            dismiss()
        }


    }

  /*      fun callApi() {
        if (Utils.isInternetAvailables(requireActivity())) {
            authViewModel.userDertails()
        } else {
            Utils.showToast("Please check your internet connection.")
        }
    }*/

/*    @SuppressLint("NotifyDataSetChanged", "SuspiciousIndentation")
    private fun detailsObserver(binding: FragmentMessagingSettingBottomSheetBinding) {
        authViewModel.userDetails.observe(viewLifecycleOwner) { response ->
            if (response != null) {
                if (response.success) {
                    pushOnOffSwitch=response.data!!.readReceipt
                    binding.PushOnOffSwitch.isChecked=pushOnOffSwitch
                    Log.e("readReceipted",response.data!!.readReceipt.toString())
                }
            }
        }
    }*/



    @SuppressLint("NotifyDataSetChanged")
    private fun setSocketListenON() {
        socket.listenOff("readReceiptSuccess")
        socket.listenOff("error")
        socket.listenOn("readReceiptSuccess") { args ->
            CoroutineScope(Dispatchers.Main).launch {

                val obj = JSONObject(args[0].toString())
            }
        }


        socket.listenOn("error") { args ->
            CoroutineScope(Dispatchers.Main).launch {
            }
        }

    }
    private fun postReadReceipt() {
        val obj = "Bearer "+Prefs.init()
            .accessToken
        socket.emitMessage("readReceipt", obj) { _, _ ->
        }
    }



}

