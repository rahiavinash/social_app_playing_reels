package com.wedo8020.app.imagecroper

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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.wedo8020.app.R
import com.wedo8020.app.dataSources.ImagesVideoConverter
import com.wedo8020.app.databinding.FragmentRoundImageCroperBinding
import com.wedo8020.app.mainui.base.BaseFragment
import com.wedo8020.app.mainui.home.postFragment.PostViewModel
import com.wedo8020.app.request.AccessSetting
import com.wedo8020.app.request.EditPrivacy
import com.wedo8020.app.request.ProfileImage
import com.wedo8020.app.utils.InternetChecker
import com.wedo8020.app.utils.ProfileImageEvent
import com.wedo8020.app.utils.Utils
import com.wedo8020.app.utils.Utils.disableMultiTap
import com.wedo8020.app.utils.Utils.loadBitmapFromView
import com.wedo8020.app.utils.Utils.saveImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Objects


class RoundImageCropFragment : BaseFragment() {
    private lateinit var mBinding: FragmentRoundImageCroperBinding
    private var imageUri = ""
    private var uri: Uri? = null
    var dialogs: Dialog? = null
    var dialog: Dialog? = null
    private var tag = ""
    private var bioDescription = ""
    var accountType = ""
    private var collaboration = ""
    private var messageMe = ""
    private var likeGiven = ""
    private var likeReceive = ""
    private var seeMyArchives = ""
    private var seeMyFollowing = ""
    private var seeMyFollower = ""
    private var seeMyBio = ""
    private var seeMyProfile = ""
    private var base64Image = ""
    private val authViewModel by viewModels<PostViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        mBinding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.fragment_round_image_croper,
            container,
            false
        )
        imageUri = arguments?.getString("imageUri")!!
        accountType = arguments?.getString("accountType")!!
        bioDescription = arguments?.getString("bio")!!
        seeMyBio = arguments?.getString("seeMyBio")!!
        seeMyArchives = arguments?.getString("seeArchive")!!
        likeGiven = arguments?.getString("seeLikeGiven")!!
        likeReceive = arguments?.getString("seeLikeReceived")!!
        seeMyProfile = arguments?.getString("seeMyProfile")!!
        seeMyFollowing = arguments?.getString("seeFollowing")!!
        seeMyFollower = arguments?.getString("seeFollower")!!
        messageMe = arguments?.getString("message")!!
        collaboration = arguments?.getString("collar")!!
        tag = arguments?.getString("tag")!!

        uri = imageUri.toUri()
        setObserver()
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog = Dialog(requireActivity())
        dialog?.requestWindowFeature(Window.FEATURE_CONTENT_TRANSITIONS)
       val internetCheck= InternetChecker(requireContext())
        /**  set the image for Crop*/
        mBinding.ivUserProfileImage.setImageURI(uri)
        if (uri != null) {
            uri = Utils.compressImage(requireContext(), uri!!)
        }


        mBinding.let {
            it.tvDone.setOnClickListener {



                mBinding.tvDone.disableMultiTap()
                mBinding.IvBack.disableBtn()
                val data = ArrayList<ProfileImage>()
                var imaUri: Uri? = null
                if (loadBitmapFromView(mBinding.ivUserProfileImage) != null) {
                    imaUri = requireContext().saveImage(loadBitmapFromView(mBinding.cardView))
                }

                /** Convert the single image URI to base64*/
                 base64Image = ImagesVideoConverter.uriToBase64(requireContext(), imaUri!!)!!
                data.add(ProfileImage("png", base64Image))
                internetCheck.startInternetCheck { isConnected ->
                    if (isConnected) {
                            lifecycleScope.launch(Dispatchers.IO) {
                                authViewModel.editProfilePrivacy(
                                    EditPrivacy(
                                        accountType = accountType,
                                        bio = bioDescription,
                                        accessSetting = AccessSetting(
                                            seeArchive = seeMyArchives,
                                            seeLikGiven = likeGiven,
                                            seeMyBio = seeMyBio,
                                            seeMyProfile = seeMyProfile,
                                            seeFollowing = seeMyFollowing,
                                            seeFollower = seeMyFollower,
                                            seeMyReceived = likeReceive,
                                            message = messageMe,
                                            collab = collaboration,
                                            tag = tag
                                        ),
                                        media = data
                                    )
                                )
                            }


                    } else {
                        dialog?.dismiss()
                        Utils.showToast("Please check your internet connection.")
                        Log.e("internet" , "internet")
                        // Internet is not available, show a toast or perform some action
                        // Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            it.IvBack.setOnClickListener {
                findNavController().navigateUp()
            }
        }


    }

    private fun setObserver() {
        authViewModel.baseResponse.observe(viewLifecycleOwner) { response ->
            if (response != null) {
                if (response.success) {
                    dialog?.dismiss()
                    findNavController().navigateUp()
                    Handler(Looper.getMainLooper()).postDelayed({
                        org.greenrobot.eventbus.EventBus.getDefault()
                            .post(ProfileImageEvent(base64Image))
                    }, 10)
                    Utils.showToast("Profile picture updated successfully.")
                    dialog?.dismiss()
                }
            }
        }
        authViewModel.showLoading.observe(viewLifecycleOwner) {
            if (it){
                showProgressDialog()
            }else{
                dialog?.dismiss()
            }

        }
    }


    private fun showProgressDialog() {
        dialog?.setCancelable(false)
        Objects.requireNonNull(dialog?.window)?.setBackgroundDrawable(
            ColorDrawable(
                Color.TRANSPARENT
            )
        )
        dialog?.setContentView(R.layout.progressbar)
        dialog?.show()
    }

}