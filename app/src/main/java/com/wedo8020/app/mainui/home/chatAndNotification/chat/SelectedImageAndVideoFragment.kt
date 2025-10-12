package com.wedo8020.app.mainui.home.chatAndNotification.chat


import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.wedo8020.app.MainApplication
import com.wedo8020.app.R
import com.wedo8020.app.dataSources.ImagesVideoConverter
import com.wedo8020.app.databinding.FragmentSelectedImageAndVideoBinding
import com.wedo8020.app.mainui.base.BaseFragment
import com.wedo8020.app.mainui.home.chatAndNotification.chatViewModel.ChatViewModel
import com.wedo8020.app.response.ChatImageUpload
import com.wedo8020.app.utils.ImageUploaded
import com.wedo8020.app.utils.InternetChecker
import com.wedo8020.app.utils.Utils
import com.wedo8020.app.utils.Utils.disableMultiTap
import com.wedo8020.zelpyy.SocketProvider.SocketIOImpl
import java.util.Objects


class SelectedImageAndVideoFragment : BaseFragment() {
private lateinit var mBinding:FragmentSelectedImageAndVideoBinding
    private val authViewModel by viewModels<ChatViewModel>()
    private var chatImageUri: Uri = Uri.EMPTY
    private var userChatId = ""
    private var base64Image = ""
    var dialog : Dialog?=null
    private lateinit var socket: SocketIOImpl
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        mBinding=DataBindingUtil.inflate(layoutInflater,R.layout.fragment_selected_image_and_video, container, false )
        dialog = Dialog(requireActivity())
        dialog?.requestWindowFeature(Window.FEATURE_CONTENT_TRANSITIONS)
        val uri=arguments?.getString("chatImageUri").toString()
        userChatId=arguments?.getString("chatUserID").toString()
        chatImageUri=uri.toUri()
        mBinding.ivChatImage.setImageURI(chatImageUri)
        socket = MainApplication.get().socket


        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        imageUploadObserver()
        mBinding.IvBack.setOnClickListener {
            mBinding.ivSendButton.disableMultiTap()
            findNavController().navigateUp()
        }
        mBinding.ivSendButton.setOnClickListener {
            mBinding.IvBack.disableMultiTap()
/** Start checking internet periodically*/
            if (isInternetAvailable(requireContext())){
                /** Convert the single image URI to base64*/
               // base64Image = ImagesVideoConverter.uriToBase64(requireContext(), chatImageUri!!)!!
                base64Image = ImagesVideoConverter.uriToBase64(requireContext(), chatImageUri).toString()
                preparingPopUp()
                InternetChecker(requireContext()).startInternetCheck { isConnected ->
                    if (isConnected) {
                        authViewModel.chatImageUpload(
                            ChatImageUpload(
                                file = base64Image,
                                ext = "jpg",
                                localId = "1234"
                            )
                        )

                    } else {
                        dialog?.dismiss()
                        Utils.showToast("Please check your internet connection.")
                        Log.e("internet" , "internet")
                        // Internet is not available, show a toast or perform some action
                        // Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show()
                    }
                }

            }else{
                dialog?.dismiss()
           //     Utils.showToast("Please check your internet connection.")
            }



        }
    }
    private fun imageUploadObserver() {
        authViewModel.chatImageUploadResponse.observe(viewLifecycleOwner) { response ->
            if (response != null) {
                if (response.success) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        org.greenrobot.eventbus.EventBus.getDefault()
                            .post(ImageUploaded(response.data?.url.toString()))
                    },500)
                    dialog?.dismiss()
                    findNavController().navigateUp()
                }
            }
        }
        authViewModel.showLoading.observe(requireActivity()) {
        }
    }
    private fun preparingPopUp() {
        if (context!=null){
            if (!requireActivity().isFinishing) {
                dialog?.setCancelable(false)
                Objects.requireNonNull(dialog?.window)?.setBackgroundDrawable(
                    ColorDrawable(
                        Color.TRANSPARENT
                    )
                )
                dialog?.setContentView(R.layout.preparing_please_wait)
                dialog?.show()
            }
        }

    }

}