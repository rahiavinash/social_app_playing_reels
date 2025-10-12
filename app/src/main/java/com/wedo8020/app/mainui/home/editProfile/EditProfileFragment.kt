package com.wedo8020.app.mainui.home.editProfile

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.text.Editable
import android.text.Html
import android.text.InputFilter
import android.text.SpannableString
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.RadioButton
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.wedo8020.app.MainApplication
import com.wedo8020.app.R
import com.wedo8020.app.databinding.FragmentEditProfileBinding
import com.wedo8020.app.mainui.base.BaseFragment
import com.wedo8020.app.mainui.home.createpost.adapter.HashTagListAdapter
import com.wedo8020.app.mainui.home.postFragment.PostViewModel
import com.wedo8020.app.mainui.home.profiletab.mentionAdapter.MentionListAdapter
import com.wedo8020.app.mainui.login.switchaccounts.SwitchAccountsViewModel
import com.wedo8020.app.mainui.login.switchaccounts.SwitchAccountsViewModelFactory
import com.wedo8020.app.mainui.login.switchaccounts.roomdB.SwitchAccountsDbCreation
import com.wedo8020.app.mainui.login.switchaccounts.roomdB.SwitchAccountsRepo
import com.wedo8020.app.request.AccessSettings
import com.wedo8020.app.request.AccountIFollow
import com.wedo8020.app.request.AnyOne
import com.wedo8020.app.request.EditProfilePrivacy
import com.wedo8020.app.request.Hashtag
import com.wedo8020.app.request.HashtagPayload
import com.wedo8020.app.request.MyFollower
import com.wedo8020.app.request.MyFriend
import com.wedo8020.app.request.Setting
import com.wedo8020.app.request.UserNotificationRequest
import com.wedo8020.app.request.mentionUserRequest
import com.wedo8020.app.response.MentionUserData
import com.wedo8020.app.utils.CustomFont
import com.wedo8020.app.utils.Prefs
import com.wedo8020.app.utils.ProfileImageEvent
import com.wedo8020.app.utils.Utils
import com.wedo8020.app.utils.Utils.findLastWord
import com.wedo8020.app.utils.Utils.loadImageAsync
import com.wedo8020.zelpyy.SocketProvider.SocketIOImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.Objects
import java.util.regex.Pattern


class EditProfileFragment : BaseFragment() {
    private lateinit var mBinding: FragmentEditProfileBinding
    private val authViewModel by viewModels<PostViewModel>()
    var dialog: Dialog? = null
    private var galleryPermissionClick = 0
    private var imageUri: Uri? = null
    val bundle = Bundle()
    private var profileUrl = ""
    private var tag = ""
    private var accountType = ""
    private var collaboration = ""
    private var messageMe = ""
    private var likeGiven = ""
    private var likeReceive = ""
    private var seeMyArchives = ""
    private var seeMyFollowing = ""
    private var seeMyFollower = ""
    private var seeMyBio = ""
    private var seeMyTrees = ""
    private var seeMyProfile = ""
    private var followRequest = false
    private var anyoneLike = false
    private var ckCommented = false
    private var ckTagMe = false
    private var ckMention = false
    private var anyoneLikeMyFriend = false
    private var ckCommentMyFriend = false
    private var ckTaggedMeFriends = false
    private var ckMentionFriends = false
    private var anyoneLikeFollow = false
    private var ckCommentedFollow = false
    private var ckTaggedMeFollow = false
    private var ckMentionFollow = false
    private var likeAccountFollower = false
    private var ckCommentedFollower = false
    private var ckTagMeFollower = false
    private var ckMentionedFollower = false
    private var pushOnOffSwitch = false
    private var base64 = ""
    private var recoveryCode = ""
    private var userId = ""
    private var mentionSearch = ""
    private lateinit var manager: RecyclerView.LayoutManager
    private var adapter: MentionListAdapter? = null
    private lateinit var adapterHashtag: HashTagListAdapter
    private var mentionLists: ArrayList<MentionUserData> = ArrayList()
    private var hashtagUserHash = hashMapOf<String, String>()
    private var startIndex = -1
    private var endIndex = -1
    private lateinit var viewModel: SwitchAccountsViewModel
    private lateinit var database: SwitchAccountsDbCreation
    private lateinit var repository: SwitchAccountsRepo
    private lateinit var factory: SwitchAccountsViewModelFactory
    private var stringForApi = ""
    private var mentionUserIdList = ArrayList<String>()
    var hashTagList: ArrayList<Hashtag> = ArrayList()
    var handler = Handler(Looper.getMainLooper())
    private var mentionUserHash = hashMapOf<String, String>()
    private lateinit var socket: SocketIOImpl
    var success: String? = null
    var data: String? = null
    var userName: String? = null
    private var isUserName: Boolean = false
    var accountUserName = ""
    private var isOpen = false
    private var isOpenAccount = false
    private var isOpenNotification = false
    private val usernameValidationLiveData = MutableLiveData<String>()
    var fileSizeInMB: Float = 0F
    private var imageCompressed:Uri?=Uri.EMPTY
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        mBinding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.fragment_edit_profile,
            container,
            false
        )
        dialog = Dialog(requireActivity())
        dialog?.requestWindowFeature(Window.FEATURE_CONTENT_TRANSITIONS)
        hideNavBottom()
        callApiForProfileDetail()
        profileDetailObserver()
        getAccountObserver()
        getNotificationDetailObserver()
        callAccountApi()
        callNotificationApi()
        mBinding.rvMentionList.visibility = View.GONE
        val allowedChars =
            "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz.-!@#\$%^&*_"
        val filter = InputFilter { source, start, end, _, _, _ ->
            for (i in start until end) {
                if (!allowedChars.contains(source[i])) {
                    return@InputFilter ""
                }
            }
            null
        }
        mBinding.etCurrentPassword2.filters = arrayOf(filter)
        mBinding.etNewPassword2.filters = arrayOf(filter)
        mBinding.etConfirmPassword2.filters = arrayOf(filter)
        // Ensure the keyboard doesn't adjust the window layout
      //  activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
        return mBinding.root
    }


    @SuppressLint("SuspiciousIndentation")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        socket = MainApplication.get().socket
        setUpUi()
        mentionUserCallApi(mentionSearch)
        mentionObserver()
        hashTagObserver()
        hideKeyboardOnOutSideClick(mBinding.clProfileAndProfile)
        database = SwitchAccountsDbCreation(requireContext())
        repository = SwitchAccountsRepo(database)
        factory = SwitchAccountsViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[SwitchAccountsViewModel::class.java]

        /** Bold the Text and Change Font Family */
        val customFont: Typeface? = try {
            ResourcesCompat.getFont(requireActivity(), R.font.poppins_bold)
        } catch (e: Exception) {
            null
        }
        val originalString =
            "Recovery Code: If you forget your username or password,you will need this code to get back into your account.There is no other way. Please copy it and keep it somewhere safe."
        val wordToCustomize = "Recovery Code:"

        /** Finding the index of the word in the string*/
        val startIndex = originalString.indexOf(wordToCustomize)
        val endIndex = startIndex + wordToCustomize.length
        val spannableString = SpannableString(originalString)

        /** Apply custom font family using CustomTypefaceSpan*/
        customFont?.let {
            spannableString.setSpan(
                CustomFont(it),
                startIndex,
                endIndex,
                SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        mBinding.tvRecoverCode.text = spannableString
        //     mBinding.etUserName.filters = arrayOf(filter)
        //    mBinding.etUserName.setText(accountUserName)
        mBinding.etUserName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (isUserName) {
                    if (s.toString() == "") {
                        mBinding.usernameValidation.visibility = View.INVISIBLE
                        //  binding.usernameValidation.text=""
                    } else if (s.toString() == accountUserName) {
                        mBinding.usernameValidation.visibility = View.INVISIBLE
                        mBinding.tvSave.isEnabled = true
                    } else {
                        mBinding.usernameValidation.visibility = View.VISIBLE
                        userName = s.toString()
                        getUserName(s.toString())
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {


            }
        })


        mBinding.etConfirmPassword2.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun afterTextChanged(editable: Editable) {
                val newPassword = mBinding.etNewPassword2.text.toString()
                val confirmPassword = editable.toString()
                if (newPassword != confirmPassword) {
                    mBinding.tvWrongPassword.visibility = View.VISIBLE
                } else if (confirmPassword.isEmpty()) {
                    mBinding.tvWrongPassword.visibility = View.GONE
                } else if (mBinding.etConfirmPassword2.text.toString() == mBinding.etNewPassword2.text.toString()) {
                    mBinding.tvWrongPassword.visibility = View.VISIBLE
                    mBinding.tvWrongPassword.setText(R.string.password_match)
                    mBinding.tvWrongPassword.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.green
                        )
                    )
                } else {
                    mBinding.tvWrongPassword.visibility = View.GONE
                }
            }
        })

        mBinding.clPrivacy.setOnClickListener {
            mBinding.clAccountAndSecurity.isClickable = false
            mBinding.clNotification.isClickable = false
            Handler(Looper.myLooper()!!).postDelayed({
                mBinding.clAccountAndSecurity.isClickable = true
                mBinding.clNotification.isClickable = true
            }, 1000)
            if (mBinding.clPrivacyOption.visibility == View.VISIBLE) {
                isOpen = false
                mBinding.clPrivacyOption.visibility = View.GONE
                mBinding.ivDownArrow.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireActivity(),
                        R.drawable.ic_arrow_down
                    )
                )
            } else {
                isOpen = true

                mBinding.clPrivacyOption.visibility = View.VISIBLE
                mBinding.ivDownArrow.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireActivity(),
                        R.drawable.ic_arrow_up
                    )
                )
            }
            if (!isOpen && !isOpenAccount && !isOpenNotification) {
                mBinding.clBottombar.visibility = View.VISIBLE
            } else {
                mBinding.clBottombar.visibility = View.GONE
            }

            Log.e("isOpen", isOpen.toString())
        }

        mBinding.clAccountAndSecurity.setOnClickListener {

            mBinding.clPrivacy.isClickable = false
            mBinding.clNotification.isClickable = false
            Handler(Looper.myLooper()!!).postDelayed({
                mBinding.clPrivacy.isClickable = true
                mBinding.clNotification.isClickable = true
            }, 1000)
            if (mBinding.clAccountOption.visibility == View.VISIBLE) {
                isOpenAccount = false
                mBinding.clAccountOption.visibility = View.GONE
                mBinding.ivDownArrowAccount.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireActivity(),
                        R.drawable.ic_arrow_down
                    )
                )
            } else {
                isOpenAccount = true
                mBinding.clAccountOption.visibility = View.VISIBLE
                mBinding.ivDownArrowAccount.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireActivity(),
                        R.drawable.ic_arrow_up
                    )
                )
            }
            if (!isOpen && !isOpenAccount && !isOpenNotification) {
                mBinding.clBottombar.visibility = View.VISIBLE
            } else {
                mBinding.clBottombar.visibility = View.GONE
            }
        }




        mBinding.clNotification.setOnClickListener {

            mBinding.clPrivacy.isClickable = false
            mBinding.clAccountAndSecurity.isClickable = false
            Handler(Looper.myLooper()!!).postDelayed({
                mBinding.clPrivacy.isClickable = true
                mBinding.clAccountAndSecurity.isClickable = true
            }, 1000)
            if (mBinding.NotificationOption.visibility == View.VISIBLE) {
                isOpenNotification = false
                mBinding.NotificationOption.visibility = View.GONE
                mBinding.ivDownArrowNotification.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireActivity(),
                        R.drawable.ic_arrow_down
                    )
                )
            } else {
                isOpenNotification = true
                mBinding.NotificationOption.visibility = View.VISIBLE
                mBinding.ivDownArrowNotification.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireActivity(),
                        R.drawable.ic_arrow_up
                    )
                )
            }
            if (!isOpen && !isOpenAccount && !isOpenNotification) {
                mBinding.clBottombar.visibility = View.VISIBLE
            } else {
                mBinding.clBottombar.visibility = View.GONE
            }
        }

        mBinding.tvAllAnyOne.setOnClickListener {
            if (mBinding.AnyoneLiked.isChecked && mBinding.ckCommented.isChecked
                && mBinding.ckTagMe.isChecked && mBinding.ckMentioned.isChecked
            ) {
                mBinding.AnyoneLiked.isChecked = false
                mBinding.ckCommented.isChecked = false
                mBinding.ckTagMe.isChecked = false
                mBinding.ckMentioned.isChecked = false
                anyoneLike = false
                ckCommented = false
                ckMention = false
                ckTagMe = false
            } else {
                mBinding.AnyoneLiked.isChecked = true
                mBinding.ckCommented.isChecked = true
                mBinding.ckTagMe.isChecked = true
                mBinding.ckMentioned.isChecked = true
                anyoneLike = true
                ckCommented = true
                ckMention = true
                ckTagMe = true
            }
        }

        mBinding.tvAllMyFriends.setOnClickListener {
            if (mBinding.AnyoneLikedMyFriend.isChecked && mBinding.ckCommentedMyFriend.isChecked
                && mBinding.ckTaggedMeFriends.isChecked && mBinding.ckMentionedFriends.isChecked
            ) {
                mBinding.AnyoneLikedMyFriend.isChecked = false
                mBinding.ckCommentedMyFriend.isChecked = false
                mBinding.ckTaggedMeFriends.isChecked = false
                mBinding.ckMentionedFriends.isChecked = false
                anyoneLikeMyFriend = false
                ckCommentMyFriend = false
                ckMentionFriends = false
                ckTaggedMeFriends = false
            } else {
                mBinding.AnyoneLikedMyFriend.isChecked = true
                mBinding.ckCommentedMyFriend.isChecked = true
                mBinding.ckTaggedMeFriends.isChecked = true
                mBinding.ckMentionedFriends.isChecked = true
                anyoneLikeMyFriend = true
                ckCommentMyFriend = true
                ckMentionFriends = true
                ckTaggedMeFriends = true
            }
        }

        mBinding.tvAllFollow.setOnClickListener {
            if (mBinding.AnyoneLikedAccountFollow.isChecked && mBinding.ckCommentedFollow.isChecked
                && mBinding.ckTaggedMeFollow.isChecked && mBinding.ckMentionedFollow.isChecked
            ) {
                mBinding.AnyoneLikedAccountFollow.isChecked = false
                mBinding.ckCommentedFollow.isChecked = false
                mBinding.ckTaggedMeFollow.isChecked = false
                mBinding.ckMentionedFollow.isChecked = false
                anyoneLikeFollow = false
                ckCommentedFollow = false
                ckMentionFollow = false
                ckTaggedMeFollow = false
            } else {
                mBinding.AnyoneLikedAccountFollow.isChecked = true
                mBinding.ckCommentedFollow.isChecked = true
                mBinding.ckTaggedMeFollow.isChecked = true
                mBinding.ckMentionedFollow.isChecked = true
                anyoneLikeFollow = true
                ckCommentedFollow = true
                ckMentionFollow = true
                ckTaggedMeFollow = true
            }


        }

        mBinding.tvAllFollowers.setOnClickListener {
            if (mBinding.AnyoneLikedAccountFollower.isChecked && mBinding.ckCommentedFollower.isChecked
                && mBinding.ckTaggedMeFollower.isChecked && mBinding.ckMentionedFollower.isChecked
            ) {
                mBinding.AnyoneLikedAccountFollower.isChecked = false
                mBinding.ckCommentedFollower.isChecked = false
                mBinding.ckTaggedMeFollower.isChecked = false
                mBinding.ckMentionedFollower.isChecked = false
                likeAccountFollower = false
                ckCommentedFollower = false
                ckMentionedFollower = false
                ckTagMeFollower = false
            } else {
                mBinding.AnyoneLikedAccountFollower.isChecked = true
                mBinding.ckCommentedFollower.isChecked = true
                mBinding.ckTaggedMeFollower.isChecked = true
                mBinding.ckMentionedFollower.isChecked = true
                likeAccountFollower = true
                ckCommentedFollower = true
                ckMentionedFollower = true
                ckTagMeFollower = true
            }
        }

        mBinding.IvBack.setOnClickListener {
            findNavController().navigateUp()
        }

        mBinding.tvCodeCopy.setOnClickListener {
            val clipboardManager =
                requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("label", mBinding.etRecoverCode.text.toString())
            clipboardManager.setPrimaryClip(clipData)
        }

        mBinding.rgPrivacy.setOnCheckedChangeListener { _, checkedId ->
            val radioButton: RadioButton = view.findViewById(checkedId)
            if (radioButton.isChecked) {
                accountType = radioButton.text.toString().lowercase()

                if (accountType == "private") {
                    mBinding.followerButton.isChecked = true
                    mBinding.followerButtonMessageMe.isChecked = true
                    mBinding.followerButtonLikeGiven.isChecked = true
                    mBinding.followerButtonMylike.isChecked = true
                    mBinding.followerButtonSeeMyFollower.isChecked = true
                    mBinding.followerButtonSeeMyFollowing.isChecked = true
                    mBinding.followerButtonSeeMyTrees.isChecked = true
                    mBinding.followerButtonSeeMyBio.isChecked = true
                    mBinding.followerButtonProfilePicture.isChecked = true
                } else {
                    mBinding.EveryoneButton.isChecked = true
                    mBinding.EveryoneButtonMessageMe.isChecked = true
                    mBinding.EveryoneButtonLikeGiven.isChecked = true
                    mBinding.EveryoneButtonMylike.isChecked = true
                    mBinding.EveryoneButtonSeeMyFollower.isChecked = true
                    mBinding.EveryoneButtonSeeMyFollowing.isChecked = true
                    mBinding.EveryoneButtonSeeMyTrees.isChecked = true
                    mBinding.EveryoneButtonSeeMyBio.isChecked = true
                    mBinding.EveryoneButtonProfilePicture.isChecked = true
                }

            }
        }

        /**  Get TagMe selectionValue*/
        mBinding.repostToGroup.setOnCheckedChangeListener { _, checkedId ->
            val radioButton: RadioButton = view.findViewById(checkedId)
            if (radioButton.isChecked) {
                tag = radioButton.text.toString().lowercase()
                if (tag == "no one") {
                    tag = "noOne"
                } else if (tag == "everyone") {
                    tag = "everyOne"
                }
            }
        }

        /**  Get Collaboration select Value*/
        /*   mBinding.llCollabWithMe.setOnCheckedChangeListener { _, checkedId ->
               val radioButton: RadioButton = view.findViewById(checkedId)
               if (radioButton.isChecked) {
                   collaboration = radioButton.text.toString().lowercase()
                   if (collaboration == "no one") {
                       collaboration = "noOne"
                   } else if (collaboration == "everyone") {
                       collaboration = "everyOne"
                   }
               }
           }*/

        /**  Get Message select Value*/
        mBinding.llMessageMe.setOnCheckedChangeListener { _, checkedId ->
            val radioButton: RadioButton = view.findViewById(checkedId)
            if (radioButton.isChecked) {
                messageMe = radioButton.text.toString().lowercase()
                if (messageMe == "no one") {
                    messageMe = "noOne"
                } else if (messageMe == "everyone") {
                    messageMe = "everyOne"
                }
            }
        }

        /**  Get :Like Given select Value*/
        mBinding.llMylikeReceived.setOnCheckedChangeListener { _, checkedId ->
            val radioButton: RadioButton = view.findViewById(checkedId)
            if (radioButton.isChecked) {
                likeGiven = radioButton.text.toString().lowercase()
                if (likeGiven == "no one") {
                    likeGiven = "noOne"
                } else if (likeGiven == "everyone") {
                    likeGiven = "everyOne"
                }
            }
        }

        /**  Get :Like Received select Value*/
        mBinding.llMylikeGiven.setOnCheckedChangeListener { _, checkedId ->
            val radioButton: RadioButton = view.findViewById(checkedId)
            if (radioButton.isChecked) {
                likeReceive = radioButton.text.toString().lowercase()
                if (likeReceive == "no one") {
                    likeReceive = "noOne"
                } else if (likeReceive == "everyone") {
                    likeReceive = "everyOne"
                }
            }
        }


        /**  Get See My Follower select Value*/
        mBinding.llSeeMyFollower.setOnCheckedChangeListener { _, checkedId ->
            val radioButton: RadioButton = view.findViewById(checkedId)
            if (radioButton.isChecked) {
                seeMyFollower = radioButton.text.toString().lowercase()
                if (seeMyFollower == "no one") {
                    seeMyFollower = "noOne"
                } else if (seeMyFollower == "everyone") {
                    seeMyFollower = "everyOne"
                }
            }
        }

        /**  Get to See My Following select Value*/
        mBinding.llSeeMyFollowing.setOnCheckedChangeListener { _, checkedId ->
            val radioButton: RadioButton = view.findViewById(checkedId)
            if (radioButton.isChecked) {
                seeMyFollowing = radioButton.text.toString().lowercase()
                if (seeMyFollowing == "no one") {
                    seeMyFollowing = "noOne"
                } else if (seeMyFollowing == "everyone") {
                    seeMyFollowing = "everyOne"
                }
            }
        }

        /**  Get to See MyTrees select Value*/
        mBinding.llSeeMyTrees.setOnCheckedChangeListener { _, checkedId ->
            val radioButton: RadioButton = view.findViewById(checkedId)
            if (radioButton.isChecked) {
                seeMyTrees = radioButton.text.toString().lowercase()
                if (seeMyTrees == "no one") {
                    seeMyTrees = "noOne"
                } else if (seeMyTrees == "everyone") {
                    seeMyTrees = "everyOne"
                }
            }
        }

        /**  Get to See Archives select Value*/
        mBinding.llSeeMyArchives.setOnCheckedChangeListener { _, checkedId ->
            val radioButton: RadioButton = view.findViewById(checkedId)
            if (radioButton.isChecked) {
                seeMyArchives = radioButton.text.toString().lowercase()
                if (seeMyArchives == "no one") {
                    seeMyArchives = "noOne"
                } else if (seeMyArchives == "everyone") {
                    seeMyArchives = "everyOne"
                }
            }
        }


        /**  Get to See My Bio select Value*/
        mBinding.rgSeeMyBio.setOnCheckedChangeListener { _, checkedId ->
            val radioButton: RadioButton = view.findViewById(checkedId)
            if (radioButton.isChecked) {
                seeMyBio = radioButton.text.toString().lowercase()
                if (seeMyBio == "no one") {
                    seeMyBio = "noOne"
                } else if (seeMyBio == "everyone") {
                    seeMyBio = "everyOne"
                }
            }
        }


        /**  Get to See My Profile Picture select Value*/
        mBinding.rgSeeMyProfilePicture.setOnCheckedChangeListener { _, checkedId ->
            val radioButton: RadioButton = view.findViewById(checkedId)
            if (radioButton.isChecked) {
                seeMyProfile = radioButton.text.toString().lowercase()
                if (seeMyProfile == "no one") {
                    seeMyProfile = "noOne"
                } else if (seeMyProfile == "everyone") {
                    seeMyProfile = "everyOne"
                }
            }
        }


        mBinding.ivEditImage.setOnClickListener {
            val readImagePermission =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.READ_MEDIA_IMAGES else Manifest.permission.READ_EXTERNAL_STORAGE
            if (Prefs.init().permissionClickGallery < 2) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(
                            requireContext(),
                            readImagePermission
                        ) == PackageManager.PERMISSION_DENIED
                    ) {
                        ActivityCompat.requestPermissions(
                            requireActivity(),
                            arrayOf(
                                Manifest.permission.READ_MEDIA_IMAGES,
                                Manifest.permission.READ_EXTERNAL_STORAGE
                            ),
                            1
                        )
                        galleryPermissionClick++
                        Prefs.init().permissionClickGallery = galleryPermissionClick
                        Log.e("picturePath", "13")
                    } else {

                        val values = ContentValues()
                        values.put(
                            MediaStore.Images.Media.TITLE,
                            System.currentTimeMillis().toString()
                        )
                        values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera")
                        /*   imageUri = requireActivity().contentResolver.insert(
                               MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values
                           )!!*/
                        val i = Intent(
                            Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        )
                        startActivityForResult(i, 1)

                        Log.e("picturePath", "GALLERY")

                    }
                } else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.TIRAMISU) {


                    if (ActivityCompat.checkSelfPermission(
                            requireActivity(),
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        )
                        != PackageManager.PERMISSION_GRANTED
                    ) {
                        ActivityCompat.requestPermissions(
                            requireActivity(),
                            arrayOf(
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                            ), 1
                        )


                        galleryPermissionClick++
                        Prefs.init().permissionClickGallery = galleryPermissionClick
                        Log.e("picturePath", "11")
                    } else {
                        val i = Intent(
                            Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        )
                        startActivityForResult(i, 1)

                        Log.e("picturePath", "GALLERY")
                    }
                }

            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ActivityCompat.checkSelfPermission(
                            requireContext(),
                            readImagePermission
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        callCustomFunction()
                    } else {
                        val values = ContentValues()
                        values.put(
                            MediaStore.Images.Media.TITLE,
                            System.currentTimeMillis().toString()
                        )
                        values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera")
                        /* imageUri = requireActivity().contentResolver.insert(
                             MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values
                         )!!*/
                        val i = Intent(
                            Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        )
                        startActivityForResult(i, 1)

                    }
                } else
                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.TIRAMISU) {
                        if (ActivityCompat.checkSelfPermission(
                                requireContext(),
                                Manifest.permission.READ_EXTERNAL_STORAGE
                            )
                            != PackageManager.PERMISSION_GRANTED ||
                            ActivityCompat.checkSelfPermission(
                                requireActivity(),
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                            )
                            != PackageManager.PERMISSION_GRANTED
                        ) {
                            callCustomFunction()
                        } else {
                            val values = ContentValues()
                            values.put(
                                MediaStore.Images.Media.TITLE,
                                System.currentTimeMillis().toString()
                            )
                            values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera")
                            /* imageUri = requireActivity().contentResolver.insert(
                                 MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values
                             )!!*/
                            val i = Intent(
                                Intent.ACTION_PICK,
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                            )
                            startActivityForResult(i, 1)
                        }
                    }
            }
        }

        mBinding.rbFollowRequest.setOnClickListener {
            followRequest = mBinding.rbFollowRequest.isChecked
        }

        mBinding.AnyoneLiked.setOnClickListener {
            anyoneLike = mBinding.AnyoneLiked.isChecked
        }

        mBinding.ckCommented.setOnClickListener {
            ckCommented = mBinding.ckCommented.isChecked
        }
        mBinding.ckTagMe.setOnClickListener {
            ckTagMe = mBinding.ckTagMe.isChecked
        }
        mBinding.ckMentioned.setOnClickListener {
            ckMention = mBinding.ckMentioned.isChecked
        }

        mBinding.AnyoneLikedMyFriend.setOnCheckedChangeListener { _, isChecked ->
            anyoneLikeMyFriend = isChecked
        }

        mBinding.ckCommentedMyFriend.setOnClickListener {
            ckCommentMyFriend = mBinding.ckCommentedMyFriend.isChecked
        }

        mBinding.ckTaggedMeFriends.setOnCheckedChangeListener { _, isChecked ->
            ckTaggedMeFriends = isChecked
        }
        mBinding.ckMentionedFriends.setOnCheckedChangeListener { _, isChecked ->
            ckMentionFriends = isChecked
        }
        mBinding.AnyoneLikedAccountFollow.setOnCheckedChangeListener { _, isChecked ->
            anyoneLikeFollow = isChecked
        }
        mBinding.ckCommentedFollow.setOnCheckedChangeListener { _, isChecked ->
            ckCommentedFollow = isChecked
        }
        mBinding.ckTaggedMeFollow.setOnCheckedChangeListener { _, isChecked ->
            ckTaggedMeFollow = isChecked
        }
        mBinding.ckMentionedFollow.setOnCheckedChangeListener { _, isChecked ->
            ckMentionFollow = isChecked
        }
        mBinding.AnyoneLikedAccountFollower.setOnCheckedChangeListener { _, isChecked ->
            likeAccountFollower = isChecked
        }
        mBinding.ckCommentedFollower.setOnCheckedChangeListener { _, isChecked ->
            ckCommentedFollower = isChecked
        }
        mBinding.ckTaggedMeFollower.setOnCheckedChangeListener { _, isChecked ->
            ckTagMeFollower = isChecked
        }
        mBinding.ckMentionedFollower.setOnCheckedChangeListener { _, isChecked ->
            ckMentionedFollower = isChecked
        }


        mBinding.tvSave.setOnClickListener {
            mBinding.tvSave.disableBtn()
            if (isInternetAvailable(requireActivity())) {
                val map = HashMap<String, String>()
                if (!mBinding.etCurrentPassword2.text.isNullOrEmpty()) {
                    map["name"] = mBinding.etName.text.toString()
                    map["username"] = mBinding.etUserName.text.toString()
                    map["currentPassword"] = mBinding.etCurrentPassword2.text.toString()
                    map["newPassword"] = mBinding.etConfirmPassword2.text.toString()
                } else {
                    map["name"] = mBinding.etName.text.toString()
                    map["username"] = mBinding.etUserName.text.toString()
                }
                Log.d(
                    "username",
                    "Details are username ${mBinding.etUserName.text} password : ${mBinding.etCurrentPassword2.text.toString()}"
                )
                Log.d("username", "Details are username ${Prefs.init().username} ")


                authViewModel.editProfileAccount(
                    map
                )
            } else {
                showToast("Please check your internet connection.")
            }
            accountPostObserver()

        }

        mBinding.PushOnOffSwitch.setOnClickListener {
            pushOnOffSwitch = mBinding.PushOnOffSwitch.isChecked
            Log.e("pushOnOffSwitch", pushOnOffSwitch.toString())
        }

        /** Switch color change blue to grey on click on/off*/
        val checkedColor = ContextCompat.getColorStateList(requireActivity(), R.color.appBlue)
        val uncheckedColor = ContextCompat.getColorStateList(requireActivity(), R.color.Grey)
        mBinding.PushOnOffSwitch.trackTintList =
            if (mBinding.PushOnOffSwitch.isChecked) checkedColor else uncheckedColor
        mBinding.PushOnOffSwitch.setOnCheckedChangeListener { _, isChecked ->
            mBinding.PushOnOffSwitch.trackTintList = if (isChecked) checkedColor else uncheckedColor
        }

    }

    private fun setUpUi() {
        // setEdittextForKeyboard()
        mBinding.apply {
            mBinding.tvDescription.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    //                    Log.e("commentEt","before "+p0.toString())
                }

                @SuppressLint("SuspiciousIndentation")
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    val cursorPosition = if (mBinding.tvDescription.selectionStart < 0) {
                        0
                    } else {
                        mBinding.tvDescription.selectionStart
                    }
                    val text = mBinding.tvDescription.text.toString()

                    startIndex = cursorPosition
                    endIndex = cursorPosition

                    while (startIndex > 0 && !text[startIndex - 1].isWhitespace()) {
                        startIndex--
                    }

                    while (endIndex < text.length && !text[endIndex].isWhitespace()) {
                        endIndex++
                    }

                    val selectedWord = text.substring(startIndex, endIndex)
                    Log.d("selectedWord", selectedWord)

                    if (selectedWord.startsWith("@")) {
                        setRecyclerViewAdapter()
                        rvMentionList.visibility = View.VISIBLE
                        clPrivacy.visibility = View.GONE
                        clAccountAndSecurity.visibility = View.GONE
                        clNotification.visibility = View.GONE
                        if (Utils.isInternetAvailables(requireActivity())) {
                            handler.removeCallbacksAndMessages(null)
                            handler.postDelayed({
                                Handler(Looper.getMainLooper()).postDelayed({
                                    mentionUserCallApi(selectedWord.removePrefix("@"))

                                }, 0)

                            }, 200)

                        }
                    } else if (selectedWord.startsWith("#")) {
                        Log.d("hashSelected", "hash has been written")
                        if (Utils.isInternetAvailables(requireActivity())) {
                            handler.removeCallbacksAndMessages(null)
                            handler.postDelayed({
                                //  setHashTagRecyclerViewAdapter()
                                Handler(Looper.getMainLooper()).postDelayed({
                                    hashTagApiCall(selectedWord.removePrefix("#"))
                                }, 0)

                            }, 200)

                        }
                        setHashTagRecyclerViewAdapter()
                        rvMentionList.visibility = View.VISIBLE
                        clPrivacy.visibility = View.GONE
                        clAccountAndSecurity.visibility = View.GONE
                        clNotification.visibility = View.GONE
                    } else {
                        rvMentionList.visibility = View.GONE
                        clPrivacy.visibility = View.VISIBLE
                        clAccountAndSecurity.visibility = View.VISIBLE
                        clNotification.visibility = View.VISIBLE
                    }

                    val length: Int = mBinding.tvDescription.text.length
                    mBinding.tvZero.text = length.toString()


                }

                override fun afterTextChanged(p0: Editable?) {
                    if (!p0.isNullOrEmpty())
                        if (p0.toString().last() == ' ') {
                            //                        Log.e("commentEt","after "+findLastWord(p0.toString()))
                            val lastWord = findLastWord(p0.toString().trim())
                            if (lastWord.startsWith("@")) {
                                if (!mentionUserHash.containsKey(lastWord)) {
                                    if (mentionLists.size > 0 &&
                                        mentionLists[0].username == lastWord.substring(1)
                                    ) {

                                        mentionUserHash[lastWord] = mentionLists[0]._id
                                    }
                                }
                            } else if (lastWord.startsWith("#")) {
                                if (!hashtagUserHash.containsKey(lastWord)) {
                                    if (hashTagList.size > 0 &&
                                        hashTagList[0].tag == lastWord.substring(1)
                                    ) {

                                        hashtagUserHash[lastWord] = hashTagList[0]._id
                                    }
                                }
                            }
                        }
                    highlightTagsAndMentions(p0)
                }
            })
            /*mBinding!!.postBT.setOnClickListener {
                    if (!sfTagListData.isNullOrEmpty()){
                        sfTagListData!!.forEach {
                            finalTag.add(it._id  )
                        }
                    }
                    convertStringFormatToSendApi(mBinding?.editText3!!.text.toString())

                    Prefs.init().TagList = ""
                }*/

        }
        // callApi()
        //   paginationScroll()
    }

    private fun hashTagApiCall(search: String) {
        if (Utils.isInternetAvailables(requireActivity())) {
            authViewModel.hashTagList(
                HashtagPayload(offset = 0, limit = 0, search = search)
            )

        }
    }

    private fun setRecyclerViewAdapter() {
        mBinding.apply {
            manager = LinearLayoutManager(requireContext())
            rvMentionList.layoutManager = manager
            adapter = MentionListAdapter(
                R.layout.mention_list_recycleview,
                object : MentionListAdapter.mentionClick {
                    override fun onClick(id: String, username: String) {
                        mentionUserHash["@${username}"] = id
                        tvDescription.text =
                            tvDescription.text.replace(startIndex, endIndex, "@${username} ")
                        tvDescription.setSelection(tvDescription.text.toString().length)
                        mBinding.rvMentionList.visibility = View.GONE
                        clPrivacy.visibility = View.VISIBLE
                        clAccountAndSecurity.visibility = View.VISIBLE
                        clNotification.visibility = View.VISIBLE

                    }
                }, true
            )
            rvMentionList.adapter = adapter
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    override fun onStart() {
        super.onStart()
        org.greenrobot.eventbus.EventBus.getDefault().register(this)


        //initializePlayer()
    }

    override fun onStop() {
        super.onStop()
        org.greenrobot.eventbus.EventBus.getDefault().unregister(this)
        if (getActivity() != null) {
            getActivity()?.getWindow()?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
    }

    @SuppressLint("SetTextI18n")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: ProfileImageEvent?) {
        base64 = event.toString()
    }

    private fun callApiForProfileDetail() {
        if (isInternetAvailable(requireContext())) {
            //  showProgressDialog()
            authViewModel.userProfilePrivacy()
        } else {
            dialog!!.dismiss()
        }
    }


    private fun callAccountApi() {
        if (isInternetAvailable(requireContext())) {
            authViewModel.userProfileAccount()
        } else {
            dialog!!.dismiss()
        }
    }

    private fun callNotificationApi() {
        if (isInternetAvailable(requireContext())) {
            //  showProgressDialog()
            authViewModel.userProfileNotification()
        } else {
            dialog!!.dismiss()
        }
    }

    private fun callApiForUpdatePrivacy() {
        if (isInternetAvailable(requireContext())) {
            lifecycleScope.launch(Dispatchers.IO) {
                authViewModel.finalEditProfilePrivacy(
                    EditProfilePrivacy(
                        accountType = accountType,
                        bioMentionUsers = mentionUserIdList,
                        bio = stringForApi,
                        profile = profileUrl,
                        accessSetting = AccessSettings(
                            seeArchive = seeMyArchives,
                            seeLikeGiven = likeGiven,
                            seeMyBio = seeMyBio,
                            seeMyProfile = seeMyProfile,
                            seeFollowing = seeMyFollowing,
                            seeFollower = seeMyFollower,
                            seeLikeReceived = likeReceive,
                            message = messageMe,
                            collab = collaboration,
                            tag = tag,
                            seeTree = seeMyTrees
                        )
                    )
                )
            }

        } else {
            showToast("Please check your internet connection.")
        }
    }


    private fun callApiForUpdateNotification() {
        if (isInternetAvailable(requireActivity())) {
            authViewModel.editProfileNotification(
                UserNotificationRequest(
                    setting = Setting(
                        accountIFollow = AccountIFollow(
                            likeMyPost = anyoneLikeFollow,
                            commented = ckCommentedFollow,
                            mentionedMe = ckMentionFollow,
                            taggedMe = ckTaggedMeFollow
                        ),
                        followRequest = followRequest,
                        myFollower = MyFollower(
                            likeMyPost = likeAccountFollower,
                            commented = ckCommentedFollower,
                            mentionedMe = ckMentionedFollower,
                            taggedMe = ckTagMeFollower
                        ),
                        myFriend = MyFriend(
                            likeMyPost = anyoneLikeMyFriend,
                            commented = ckCommentMyFriend,
                            mentionedMe = ckMentionFriends,
                            taggedMe = ckTaggedMeFriends
                        ),
                        anyOne = AnyOne(
                            likeMyPost = anyoneLike,
                            commented = ckCommented,
                            mentionedMe = ckMention,
                            taggedMe = ckTagMe
                        ),
                        showInNotification = pushOnOffSwitch
                    )

                )
            )
        } else {
            showToast("Please check your internet connection.")
        }
    }

    fun mentionUserCallApi(search: String) {
        if (Utils.isInternetAvailables(requireActivity())) {
            authViewModel.mentionUserRequests =
                mentionUserRequest(offset = 0, limit = 100, search = search)
            authViewModel.mentionUser()
        }
    }

    private fun mentionObserver() {
        authViewModel.mentionUserData.observe(requireActivity()) { response ->
            //  dialog?.dismiss()
            if (response != null) {
                if (response.success) {
                    //    dialog?.dismiss()
                    if (response.data != null) {
                        mentionLists = response.data!!
                        adapter?.setNewItems(mentionLists)
                    }
                }
            }
        }
        authViewModel.showLoading.observe(requireActivity()) {
        }
    }

    @SuppressLint("SuspiciousIndentation", "Recycle")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {

            lifecycleScope.launch(Dispatchers.IO) {

                imageCompressed = async {
                    data?.data?.let { compressImagesWithGlide(requireContext(), it) }
                }.await()
                lifecycleScope.launch(Dispatchers.Main) {

                    if (imageCompressed != null) {
                        /** Here get the size of image In MB*/
                   /*     val returnCursor: Cursor =
                            requireContext().contentResolver.query(imageCompressed!!, null, null, null, null)!!
                        val sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE)
                        returnCursor.moveToFirst()
                        val sizeInMB: Float = returnCursor.getLong(sizeIndex).toFloat() / (1024f * 1024f)
                        val size = returnCursor.getLong(sizeIndex) / 1024*/

                       /* if (sizeInMB < 15) {*/
                            bundle.putString("imageUri", imageCompressed.toString())
                            bundle.putString("accountType", accountType)
                            bundle.putString("bio", mBinding.tvDescription.text.toString())
                            bundle.putString("seeMyBio", seeMyBio)
                            bundle.putString("seeArchive", seeMyArchives)
                            bundle.putString("seeLikeGiven", likeGiven)
                            bundle.putString("seeLikeReceived", likeReceive)
                            bundle.putString("seeMyProfile", seeMyProfile)
                            bundle.putString("seeFollowing", seeMyFollowing)
                            bundle.putString("seeFollower", seeMyFollower)
                            bundle.putString("seeTree", seeMyTrees)
                            bundle.putString("message", messageMe)
                            bundle.putString("collar", collaboration)
                            bundle.putString("tag", tag)
                            if (data?.data != null) {
                                findNavController().navigate(R.id.roundImageCropFragment, bundle)
                            }
                        /* }else {
                            Handler(Looper.myLooper()!!).postDelayed({
                                Utils.showToast("Please select an image less than 15MB.")
                            }, 1000)

                            Log.e("546645", "SizeLog kdlfdsadsadsadsadsadsad")
                            Log.e("TAG", "SizeLog: $size")
                            Log.e("TAG", "SizeLog: $sizeInMB")
                        }*/
                    }


                }


            }



           // imageUri = data?.data



        }
    }

    fun getFileSize(uri: Uri): Long {
        val cursor: Cursor? = requireContext().contentResolver!!.query(
            uri, null, null, null, null
        )

        cursor?.use {
            val sizeColumn =
                it.getColumnIndexOrThrow(android.provider.MediaStore.MediaColumns.SIZE)
            if (it.moveToNext()) {
                return it.getLong(sizeColumn)
            }
        }
        return 0L
    }

    private fun profileDetailObserver() {
        authViewModel.userProfilePrivacyData.observe(requireActivity()) { response ->
            dialog?.dismiss()
            if (response != null) {
                if (response.success) {
                    if (response.data != null) {
                        /** set the image Cover */
                        if (!response.data!!.profile.isNullOrEmpty()) {
                            CoroutineScope(Dispatchers.IO).launch {
                                mBinding.ivUserProfileImage.loadImageAsync(response.data!!.profile!!)
                                viewModel.updateProfile(
                                    Prefs.init().username,
                                    response.data?.profile.toString()
                                )
                            }
                            profileUrl = response.data!!.profile!!
                        }

                        userId = response.data!!._id
                        recoveryCode = response.data!!.recoveryCode
                        //   mBinding.tvDescription.setText(response.data!!.bio.toString())

                        /**To Show the Description user */
                        response.data.let { it ->
                            if (it!!.bio != null) {
                                val description = Utils.convertDesc(it.bio!!).trim()
                                mBinding.tvDescription.movementMethod =
                                    LinkMovementMethod.getInstance()
                                it.bioMentionUsers.forEach {
                                    if (!mentionUserHash.containsKey("@" + it.username)) {
                                        mentionUserHash["@" + it.username] = it._id
                                    }
                                }
                                mBinding.tvDescription.setText(description)


                                mBinding.rvMentionList.visibility = View.GONE
                                mBinding.clPrivacy.visibility = View.VISIBLE
                                mBinding.clAccountAndSecurity.visibility = View.VISIBLE
                                mBinding.clNotification.visibility = View.VISIBLE


                                mBinding.tvZero.text = mBinding.tvDescription.text.length.toString()
                            }
                        }
                        /** set the Recovery Code */
                        mBinding.etRecoverCode.setText(response.data!!.recoveryCode)
                        when (response.data!!.accountType) {
                            "private" -> {
                                mBinding.rgPrivacy.check(mBinding.tvPrivate.id)
                            }

                            "public" -> {
                                mBinding.rgPrivacy.check(mBinding.tvPublic.id)
                            }

                            else -> {
                                mBinding.rgPrivacy.check(mBinding.tvBusiness.id)
                            }
                        }

                        when (response.data!!.accessSetting?.tag) {
                            "noOne" -> {
                                mBinding.repostToGroup.check(mBinding.noOneButton.id)
                            }

                            "friends" -> {
                                mBinding.repostToGroup.check(mBinding.friendsButton.id)
                            }

                            "followers" -> {
                                mBinding.repostToGroup.check(mBinding.followerButton.id)
                            }

                            else -> {
                                mBinding.repostToGroup.check(mBinding.EveryoneButton.id)
                            }
                        }

                        when (response.data!!.accessSetting?.collab) {
                            "noOne" -> {
                                mBinding.llCollabWithMe.check(mBinding.noOneButtonCollab.id)
                            }

                            "friends" -> {
                                mBinding.llCollabWithMe.check(mBinding.friendsButtonCollab.id)
                            }

                            "followers" -> {
                                mBinding.llCollabWithMe.check(mBinding.followerButtonCollab.id)
                            }

                            else -> {
                                mBinding.llCollabWithMe.check(mBinding.EveryoneButtonCollab.id)
                            }
                        }

                        when (response.data!!.accessSetting?.message) {
                            "noOne" -> {
                                mBinding.llMessageMe.check(mBinding.noOneButtonMessageMe.id)
                            }

                            "friends" -> {
                                mBinding.llMessageMe.check(mBinding.friendsButtonMessageMe.id)
                            }

                            "followers" -> {
                                mBinding.llMessageMe.check(mBinding.followerButtonMessageMe.id)
                            }

                            else -> {
                                mBinding.llMessageMe.check(mBinding.EveryoneButtonMessageMe.id)
                            }
                        }
                        if (response.data!!.accessSetting != null) {
                            when (response.data!!.accessSetting?.seeLikeGiven) {
                                "noOne" -> {
                                    mBinding.llMylikeReceived.check(mBinding.noOneButtonMylike.id)
                                }

                                "friends" -> {
                                    mBinding.llMylikeReceived.check(mBinding.friendsButtonMylike.id)
                                }

                                "followers" -> {
                                    mBinding.llMylikeReceived.check(mBinding.followerButtonMylike.id)
                                }

                                else -> {
                                    mBinding.llMylikeReceived.check(mBinding.EveryoneButtonMylike.id)
                                }
                            }
                        }



                        when (response.data!!.accessSetting?.seeLikeReceived) {
                            "noOne" -> {
                                mBinding.llMylikeGiven.check(mBinding.noOneButtonLikeGiven.id)
                            }

                            "friends" -> {
                                mBinding.llMylikeGiven.check(mBinding.friendsButtonLikeGiven.id)
                            }

                            "followers" -> {
                                mBinding.llMylikeGiven.check(mBinding.followerButtonLikeGiven.id)
                            }

                            else -> {
                                mBinding.llMylikeGiven.check(mBinding.EveryoneButtonLikeGiven.id)
                            }
                        }

                        when (response.data!!.accessSetting?.seeFollower) {
                            "noOne" -> {
                                mBinding.llSeeMyFollower.check(mBinding.noOneButtonSeeMyFollower.id)
                            }

                            "friends" -> {
                                mBinding.llSeeMyFollower.check(mBinding.friendsButtonSeeMyFollower.id)
                            }

                            "followers" -> {
                                mBinding.llSeeMyFollower.check(mBinding.followerButtonSeeMyFollower.id)
                            }

                            else -> {
                                mBinding.llSeeMyFollower.check(mBinding.EveryoneButtonSeeMyFollower.id)
                            }
                        }

                        when (response.data!!.accessSetting?.seeFollowing) {
                            "noOne" -> {
                                mBinding.llSeeMyFollowing.check(mBinding.noOneButtonSeeMyFollowing.id)
                            }

                            "friends" -> {
                                mBinding.llSeeMyFollowing.check(mBinding.friendsButtonSeeMyFollowing.id)
                            }

                            "followers" -> {
                                mBinding.llSeeMyFollowing.check(mBinding.followerButtonSeeMyFollowing.id)
                            }

                            else -> {
                                mBinding.llSeeMyFollowing.check(mBinding.EveryoneButtonSeeMyFollowing.id)
                            }
                        }


                        when (response.data!!.accessSetting?.seeTree) {
                            "noOne" -> {
                                mBinding.llSeeMyTrees.check(mBinding.noOneButtonSeeMyTrees.id)
                            }

                            "friends" -> {
                                mBinding.llSeeMyTrees.check(mBinding.friendsButtonSeeMyTrees.id)
                            }

                            "followers" -> {
                                mBinding.llSeeMyTrees.check(mBinding.followerButtonSeeMyTrees.id)
                            }

                            else -> {
                                mBinding.llSeeMyTrees.check(mBinding.EveryoneButtonSeeMyTrees.id)
                            }
                        }


                        when (response.data!!.accessSetting?.seeArchive) {
                            "noOne" -> {
                                mBinding.llSeeMyArchives.check(mBinding.noOneButtonArchives.id)
                            }

                            "friends" -> {
                                mBinding.llSeeMyArchives.check(mBinding.friendsButtonArchives.id)
                            }

                            "followers" -> {
                                mBinding.llSeeMyArchives.check(mBinding.followerButtonArchives.id)
                            }

                            else -> {
                                mBinding.llSeeMyArchives.check(mBinding.EveryoneButtonArchives.id)
                            }
                        }


                        when (response.data!!.accessSetting?.seeMyBio) {
                            "noOne" -> {
                                mBinding.rgSeeMyBio.check(mBinding.noOneButtonSeeMyBio.id)
                            }

                            "friends" -> {
                                mBinding.rgSeeMyBio.check(mBinding.friendsButtonSeeMyBio.id)
                            }

                            "followers" -> {
                                mBinding.rgSeeMyBio.check(mBinding.followerButtonSeeMyBio.id)
                            }

                            else -> {
                                mBinding.rgSeeMyBio.check(mBinding.EveryoneButtonSeeMyBio.id)
                            }
                        }


                        when (response.data!!.accessSetting?.seeMyProfile) {
                            "noOne" -> {
                                mBinding.rgSeeMyProfilePicture.check(mBinding.noOneButtonProfilePicture.id)
                            }

                            "friends" -> {
                                mBinding.rgSeeMyProfilePicture.check(mBinding.friendsButtonProfilePicture.id)
                            }

                            "followers" -> {
                                mBinding.rgSeeMyProfilePicture.check(mBinding.followerButtonProfilePicture.id)
                            }

                            else -> {
                                mBinding.rgSeeMyProfilePicture.check(mBinding.EveryoneButtonProfilePicture.id)
                            }
                        }
                    }
                }
                authViewModel.showLoading.observe(requireActivity()) {
                    dialog?.dismiss()
                }

                authViewModel.showMessage.observe(requireActivity()) {
                    // dialog!!.dismiss()
                }
            }
        }
    }

    private fun getAccountObserver() {
        authViewModel.userProfileAccountData.observe(requireActivity()) { response ->
            dialog?.dismiss()
            if (response != null) {
                if (response.success) {
                    // dialog?.dismiss()
                    if (response.data != null) {
                        mBinding.etName.setText(response.data?.name)
                        mBinding.etUserName.setText(response.data?.username)
                        accountUserName = response.data?.username.toString()
                        isUserName = true
                    }
                }
                authViewModel.showLoading.observe(requireActivity()) {

                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!socket.isConnected()) {
            socket.connect()
            Log.e("socketIsConnect", "notConnected")
        } else {
            Log.e("socketIsConnect", "Connected")

            setSocketListenOn()


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
                val obj = JSONObject(args[0].toString())

                success = obj.getString("success")
                data = obj.getString("data")

                if (success == "true" && data == "true") {
                    mBinding.usernameValidation.setTextColor(Color.parseColor("#0AB210"))
                    mBinding.usernameValidation.setText(R.string.available)
                    usernameValidationLiveData.value = "Available"
                    mBinding.tvSave.isEnabled = true
                } else if (data == "false") {
                    mBinding.usernameValidation.setTextColor(Color.parseColor("#FF0340"))
                    mBinding.usernameValidation.setText(R.string.not_available)
                    usernameValidationLiveData.value = "Not available"
                    mBinding.tvSave.isEnabled = false
                }
            }
        }


    }

    /*    private fun check() {

            usernameValidationLiveData.observe(*//* owner = *//* viewLifecycleOwner) { validationText ->


            // Ensure the current username matches the validation result
            val currentUsername = mBinding.etUserName.text.toString()

            if (validationText == "Not available"){
                mBinding.tvSave.isEnabled=false

            }else{
                mBinding.tvSave.isEnabled=true

            }

        }

    }*/

    private fun getNotificationDetailObserver() {
        authViewModel.userProfileNotification.observe(viewLifecycleOwner) { response ->
            dialog?.dismiss()
            if (response != null) {
                if (response.success) {
                    if (response.data != null) {
                        if (response.data!!.followRequest) {
                            mBinding.rbFollowRequest.isChecked = true
                            followRequest = true
                        }
                        if (response.data!!.showInNotification) {
                            mBinding.PushOnOffSwitch.isChecked = true
                            pushOnOffSwitch = true
                        }

                        if (response.data?.anyOne != null) {
                            response.data?.anyOne.let {
                                if (it?.likeMyPost == true) {
                                    anyoneLike = true
                                    mBinding.AnyoneLiked.isChecked = true
                                }
                                if (it?.commented == true) {
                                    mBinding.ckCommented.isChecked = true
                                    ckCommented = true
                                }
                                if (it?.taggedMe == true) {
                                    mBinding.ckTagMe.isChecked = true
                                    ckTagMe = true
                                }
                                if (it?.mentionedMe == true) {
                                    mBinding.ckMentioned.isChecked = true
                                    ckMention = true
                                }
                            }
                        }

                        if (response.data!!.myFriend != null) {
                            if (response.data!!.myFriend!!.likeMyPost) {
                                anyoneLikeMyFriend = true
                                mBinding.AnyoneLikedMyFriend.isChecked = true
                            }
                            if (response.data!!.myFriend!!.commented) {
                                ckCommentMyFriend = true
                                mBinding.ckCommentedMyFriend.isChecked = true
                            }
                            if (response.data!!.myFriend!!.taggedMe) {
                                ckTaggedMeFriends = true
                                mBinding.ckTaggedMeFriends.isChecked = true
                            }
                            if (response.data!!.myFriend!!.mentionedMe) {
                                ckMentionFriends = true
                                mBinding.ckMentionedFriends.isChecked = true
                            }
                        }



                        if (response.data!!.accountIFollow != null) {
                            if (response.data!!.accountIFollow!!.likeMyPost) {
                                anyoneLikeFollow = true
                                mBinding.AnyoneLikedAccountFollow.isChecked = true
                            }
                            if (response.data!!.accountIFollow!!.commented) {
                                ckCommentedFollow = true
                                mBinding.ckCommentedFollow.isChecked = true
                            }
                            if (response.data!!.accountIFollow!!.taggedMe) {
                                ckTaggedMeFollow = true
                                mBinding.ckTaggedMeFollow.isChecked = true
                            }
                            if (response.data!!.accountIFollow!!.mentionedMe) {
                                mBinding.ckMentionedFollow.isChecked = true
                                ckMentionFollow = true
                            }
                        }

                        if (response.data!!.myFollower != null) {
                            if (response.data!!.myFollower!!.likeMyPost) {
                                likeAccountFollower = true
                                mBinding.AnyoneLikedAccountFollower.isChecked = true
                            }
                            if (response.data!!.myFollower!!.commented) {
                                ckCommentedFollower = true
                                mBinding.ckCommentedFollower.isChecked = true
                            }
                            if (response.data!!.myFollower!!.taggedMe) {
                                ckTagMeFollower = true
                                mBinding.ckTaggedMeFollower.isChecked = true
                            }
                            if (response.data!!.myFollower!!.mentionedMe) {
                                mBinding.ckMentionedFollower.isChecked = true
                                ckMentionedFollower = true
                            }
                        }
                    }
                }
                authViewModel.showLoading.observe(viewLifecycleOwner) {
                }
            }
        }
    }

    private fun accountPostObserver() {
        authViewModel.baseResponseForAccount.observe(viewLifecycleOwner) { response ->
            dialog?.dismiss()
            if (response != null) {
                if (response.success) {
                    if (mBinding.etConfirmPassword2.text.toString().isNotEmpty()) {
                        CoroutineScope(Dispatchers.IO).launch {
                            viewModel.updatePassword(
                                Prefs.init().username,
                                mBinding.etConfirmPassword2.text.toString()
                            )
                        }

                    }
                    if (mBinding.etUserName.text.toString().isNotEmpty()) {
                        viewModel.updateUsername(
                            username = Prefs.init().username,
                            updatedUserName = mBinding.etUserName.text.toString()
                        )
                    }
                    Prefs.init().username = mBinding.etUserName.text.toString()
                    callApiForUpdateNotification()
                    notificationPostObserver()
                }
            }
        }
        authViewModel.showLoading.observe(viewLifecycleOwner) {
            if (it) {
                //   showProgressDialog()
            } else {
                dialog?.dismiss()
            }
        }
        authViewModel.showMessage.observe(viewLifecycleOwner){response->
            if (!response.isNullOrEmpty()){
             //   showToast(response.toString())
            }
        }
    }

    private fun notificationPostObserver() {
        authViewModel.baseResponseForNotification.observe(
            viewLifecycleOwner
        ) { response ->
            dialog?.dismiss()
            if (response != null) {
                if (response.success) {
                    convertStringFormatToSendApi(mBinding.tvDescription.text.toString())
                    callApiForUpdatePrivacy()
                    observerForPrivacy()
                }
            }
        }

    }

    private fun observerForPrivacy() {
        authViewModel.baseResponse.observe(viewLifecycleOwner) { response ->
            //dialog?.dismiss()
            if (response != null) {
                if (response.success) {
                    //    dialog?.dismiss()
                    showToast("Profile updated successfully.")
                    findNavController().navigateUp()
                }
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

    private fun callCustomFunction() {
        val builder = AlertDialog.Builder(requireContext())
        val boldText = "<b>WeDo 80</b>"
        val message = "WeDo 80 would like to access your gallery"

        builder.setTitle(Html.fromHtml(boldText, Html.FROM_HTML_MODE_LEGACY))
        builder.setMessage(message)
        builder.setPositiveButton("OK") { _, _ ->
            // Open settings after user clicks OK
            openAppSettings()
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        with(intent) {
            data = Uri.fromParts("package", requireContext().packageName, null)
            addCategory(Intent.CATEGORY_DEFAULT)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
        }
        startActivity(intent)
    }

    private fun highlightTagsAndMentions(editable: Editable?) {
        val text = editable.toString()
        // Highlight hashtags
        val hashtagPattern = Pattern.compile("\\B#([a-zA-Z0-9_\\p{Punct}]+)\\b")
        val hashtagMatcher = hashtagPattern.matcher(text)
        while (hashtagMatcher.find()) {
            val start = hashtagMatcher.start()
            val end = hashtagMatcher.end()

            editable?.setSpan(
                android.text.style.ForegroundColorSpan(requireContext().getColor(R.color.appBlue)),
                start,
                end,
                0
            )
        }

        // Highlight mentions
        val mentionPattern = Pattern.compile("\\B@([a-zA-Z0-9_\\p{Punct}]+)\\b")
        val mentionMatcher = mentionPattern.matcher(text)

        while (mentionMatcher.find()) {
            val start = mentionMatcher.start()
            val end = mentionMatcher.end()
            val mention = text.substring(start, end)
            if (mentionUserHash.containsKey(mention)) {
                editable?.setSpan(
                    android.text.style.ForegroundColorSpan(requireContext().getColor(R.color.appBlue)),
                    start,
                    end,
                    0
                )
            } else {
                editable?.setSpan(
                    android.text.style.ForegroundColorSpan(requireContext().getColor(R.color.LightBlack)),
                    start,
                    end,
                    0
                )
            }
        }
    }

    private fun hashTagObserver() {
        authViewModel.hashTagListResponse.observe(viewLifecycleOwner) { response ->
            if (response != null) {
                if (response.success == true) {
                    //   dialogs?.dismiss()

                    if (response.data != null) {
                        hashTagList = response.data
                        adapterHashtag.setNewItems(hashTagList)
                    }
                }
            }

        }
    }

    private fun setHashTagRecyclerViewAdapter() {
        mBinding.apply {

            manager = LinearLayoutManager(requireContext())
            this.rvMentionList.layoutManager = manager
            adapterHashtag = HashTagListAdapter(
                R.layout.recycler_hashtag_list,
                object : HashTagListAdapter.HashTagClick {
                    override fun onClick(id: String, username: String) {
                        hashtagUserHash["#${username}"] = id
                        mBinding.tvDescription.text =
                            mBinding.tvDescription.text.replace(
                                startIndex,
                                endIndex,
                                "#${username} "
                            )
                        //mBinding?.editText3!!.text.replace(startIndex, endIndex, "@${username} ")
                        mBinding.tvDescription.setSelection(mBinding.tvDescription.text.toString().length)
                        highlightTagsAndMentions(mBinding.tvDescription.text)
                        rvMentionList.visibility = View.GONE
                        clPrivacy.visibility = View.VISIBLE
                        clAccountAndSecurity.visibility = View.VISIBLE
                        clNotification.visibility = View.VISIBLE


                    }
                }, true
            )
            rvMentionList.adapter = adapterHashtag

        }
    }
    fun compressImagesWithGlide(context: Context, originalImageUri: Uri): Uri {
        var compressedImageUris : Uri = Uri.EMPTY
        val displayMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)

        val screenWidth = displayMetrics.widthPixels



        try {
            // Compress the image using Glide and obtain the compressed bitmap
            val compressedBitmap = Glide.with(context)
                .asBitmap()
                .load(originalImageUri)
                .apply(
                    RequestOptions()
                        .format(DecodeFormat.PREFER_RGB_565)
                        .override(screenWidth, 1024)
                        .fitCenter()
                        .diskCacheStrategy(DiskCacheStrategy.NONE) // Disable disk caching
                )
                .submit()
                .get()


            val compressedImageFile = File.createTempFile("compressed_image", ".jpg", context.cacheDir)


            FileOutputStream(compressedImageFile).use { outputStream ->
                compressedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            }


            val compressedImageUri = Uri.fromFile(compressedImageFile)


            compressedImageUris = compressedImageUri
        } catch (e: Exception) {
            e.printStackTrace()
        }


        return compressedImageUris
    }
    private fun convertStringFormatToSendApi(text: String) {
        for (i in text.split(" ")) {
            if (mentionUserHash.containsKey(i)) {
                stringForApi += "@[${i.substring(1)}](${mentionUserHash[i]}) "
                if (mentionUserHash[i] != null && !mentionUserIdList.contains(mentionUserHash[i])) {
                    mentionUserIdList.add(mentionUserHash[i]!!)
                }
            } else {
                stringForApi += "$i "
            }
        }
    }
}