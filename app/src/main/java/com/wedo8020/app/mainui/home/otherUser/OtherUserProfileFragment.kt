package com.wedo8020.app.mainui.home.otherUser

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Html
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.wedo8020.app.R
import com.wedo8020.app.databinding.FragmentOtherUserProfileBinding
import com.wedo8020.app.mainui.adapter.ViewPagerAdapter
import com.wedo8020.app.mainui.base.BaseFragment
import com.wedo8020.app.mainui.home.postFragment.PostViewModel
import com.wedo8020.app.mainui.home.profiletab.ActivePostFragment
import com.wedo8020.app.mainui.home.profiletab.ArchiveAirOffFragment

import com.wedo8020.app.response.MentionUser
import com.wedo8020.app.utils.AppConstant
import com.wedo8020.app.utils.Utils.loadImageAsync
import com.google.android.material.tabs.TabLayout
import com.wedo8020.app.eventBus.ProfilePostItemClick
import com.wedo8020.app.mainui.dialogFragments.OtherOptionBottomSheet
import com.wedo8020.app.mainui.dialogFragments.ReportUserBottomSheet
import com.wedo8020.app.request.PostRequest
import com.wedo8020.app.request.SwitchNotificationRequest
import com.wedo8020.app.request.UserBlockedRequest
import com.wedo8020.app.request.UserRequest
import com.wedo8020.app.response.UserDetailData
import com.wedo8020.app.utils.DeepLinkType
import com.wedo8020.app.utils.Extension.share
import com.wedo8020.app.utils.Hashtag
import com.wedo8020.app.utils.Prefs
import com.wedo8020.app.utils.Utils
import com.wedo8020.app.utils.Utils.capitalizeFirstChar
import com.wedo8020.app.utils.Utils.loadImageAsyncMessage
import com.wedo8020.app.utils.Utils.toJsonString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.util.Objects

private const val ARG_PARAM1 = "mentionUser"

class OtherUserProfileFragment : BaseFragment(), ReportUserBottomSheet.ClickListeners,
    OtherOptionBottomSheet.OtherOptionItemClickListener, Hashtag.onHashtagClick {
    private var mentionUserHash = hashMapOf<String, String>()
    private var hashtagUserHash = hashMapOf<String, String>()
    private var param1: String = ""
    private var binding: FragmentOtherUserProfileBinding? = null
    private val tabIcons = intArrayOf(
        R.drawable.ic_vedio_svg,
        R.drawable.ic_archive,
    )
    var dialogs: Dialog? = null
    private var bundle = Bundle()
    private var userDetailData: UserDetailData? = null
    private val authViewModel by viewModels<PostViewModel>()
    private val fragments = ArrayList<Fragment>()
    private lateinit var adapter: ViewPagerAdapter
    var profile = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1) ?: ""
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentOtherUserProfileBinding.inflate(inflater, container, false)
        hideNavBottom()
        setUpUi()
        return binding!!.root
    }

    private fun setViewPager(canSeeArchive: Boolean, userBlocked: Boolean) {

        binding?.apply {
            fragments.add(ActivePostFragment.newInstance(param1, userBlocked))
            fragments.add(ArchiveAirOffFragment.newInstance(param1, canSeeArchive, userBlocked))
            val titles = listOf("", "", "")
            adapter = ViewPagerAdapter(childFragmentManager, 2, fragments, titles)
            viewPager.adapter = adapter
            viewPager.offscreenPageLimit = 2
            tabLayout.setupWithViewPager(viewPager)
            for (i in 0 until tabLayout.tabCount) {
                val tab = tabLayout.getTabAt(i)
                tab?.setIcon(tabIcons[i])
                if (context != null) {
                    tab?.icon?.setTintList(
                        ColorStateList.valueOf(
                            requireContext().getColor(
                                if (i == 0) {
                                    R.color.pureBlack
                                } else {
                                    R.color.Grey
                                }
                            )
                        )
                    )
                }
            }
            tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    tab.icon?.setColorFilter(
                        requireContext().getColor(R.color.pureBlack), PorterDuff.Mode.SRC_IN
                    )
//                    tab.icon?.setTintList(ColorStateList.valueOf(requireContext().getColor(R.color.pureBlack)))
                }

                override fun onTabUnselected(tab: TabLayout.Tab) {
                    tab.icon?.setColorFilter(
                        requireContext().getColor(R.color.Grey), PorterDuff.Mode.SRC_IN
                    )
//                    tab.icon?.setTintList(ColorStateList.valueOf(requireContext().getColor(R.color.Grey)))

                }

                override fun onTabReselected(tab: TabLayout.Tab) {
                    // Handle tab reselection if needed
                    tab.icon?.setTintList(ColorStateList.valueOf(requireContext().getColor(R.color.pureBlack)))

                }
            })
        }
    }

    private fun setUpUi() {

        binding?.apply {
//            fragments.add()
            if (context != null) {
            dialogs = Dialog(requireActivity())
            dialogs?.requestWindowFeature(Window.FEATURE_CONTENT_TRANSITIONS)
            }
            ivBack.setOnClickListener {
                findNavController().navigateUp()
            }
            btnMsg.setOnClickListener {
                val bundle = Bundle()
                bundle.apply {
                    putString("userChatId", param1)
                    putString("otherUserName", userDetailData?.name)
                    putString("otherUserProfile", userDetailData?.profile)
                    putBoolean("blueTick", userDetailData?.blueTickEnable ?: false)
                    findNavController().navigate(R.id.showChatFragment, this)
                }
            }

        }
        if (context!=null){
            if (Utils.isInternetAvailables(requireContext())){
                callApi()
            }else{
                Utils.showToast(getString(R.string.no_internet_message))
                binding?.apply {
                    userNotFoundContainer.visibility = View.VISIBLE
                    btnMenu.visibility = View.INVISIBLE
                    tabLayout.visibility = View.INVISIBLE
                    viewPager.visibility = View.INVISIBLE
                    animationView.visibility = View.GONE
                }
            }
        }

        setObserver()
        clickListeners()



        binding?.ivUserProfileImage?.setOnClickListener {
            bundle.putString("profile", profile)
            findNavController().navigate(R.id.showProfileFragment, bundle)
        }

    }

    private fun clickListeners() {
        binding?.apply {
            btnMenu.setOnClickListener {
                val bottomSheetFragment = OtherOptionBottomSheet(
                    R.layout.option_bottomsheet_layout,
                    this@OtherUserProfileFragment,
                    userDetailData?.isBlock ?: false
                )
                bottomSheetFragment.height = ViewGroup.LayoutParams.WRAP_CONTENT
                bottomSheetFragment.show(childFragmentManager, bottomSheetFragment.tag)
            }

            tvBlockBtn.setOnClickListener {
                userDetailData?.let { userBlocked(it) }
            }
            tvGiven.setOnClickListener {
                bundle.putString("types", "given")
                bundle.putString("userId", userDetailData?._id)
                findNavController().navigate(
                    R.id.action_other_profile_fragment_to_likeGivenAndReceiveFragment,
                    bundle
                )
            }
            tvReceived.setOnClickListener {
                bundle.putString("types", "received")
                bundle.putString("userId", userDetailData?._id)
                findNavController().navigate(
                    R.id.action_other_profile_fragment_to_likeGivenAndReceiveFragment,
                    bundle
                )
            }

            clFollwer.setOnClickListener {
                bundle.putString("type", "follower")
                bundle.putString("userId", userDetailData?._id)
                findNavController().navigate(
                    R.id.action_other_profile_fragment_to_follower_following_fragment,
                    bundle
                )
            }

            clFollowing.setOnClickListener {
                bundle.putString("type", "following")
                bundle.putString("userId", userDetailData?._id)
                findNavController().navigate(
                    R.id.action_other_profile_fragment_to_follower_following_fragment,
                    bundle
                )
            }

            btnNotification.setOnClickListener {
                if(Utils.isInternetAvailables(requireContext())){
                    authViewModel.switchNotification(
                        SwitchNotificationRequest(
                            userDetailData?._id ?: ""
                        )
                    )
                    userDetailData?.let {
                        it.isNotificationOn = !it.isNotificationOn
                        btnNotification.setImageDrawable(
                            requireContext().getDrawable(
                                if (it.isNotificationOn) {
                                    R.drawable.ic_noti_bell_ring
                                } else {
                                    R.drawable.ic_notification_bell
                                }
                            )
                        )
                    }
                }else{
                    Utils.showToast(getString(R.string.no_internet_message))
                }


            }
            tvFollowBtn.setOnClickListener {
                if(Utils.isInternetAvailables(requireContext())){
                    userDetailData?.let {
                        when (it.followStatus) {
                            "1" -> {
                                it.followStatus = "2"
                                authViewModel.followUnfollowUserRequest = UserRequest(it._id)
                                authViewModel.followUnfollowUser()
                                tvFollowBtn.text = requireContext().getText(R.string.follow)
                            }

                            "2" -> {
                                authViewModel.followUnfollowUserRequest = UserRequest(it._id)
                                authViewModel.followUnfollowUser()
                                if (it.accountType.equals(
                                        AppConstant.PRIVATE,
                                        ignoreCase = true
                                    )
                                ) {
                                    it.followStatus = "3"
                                    tvFollowBtn.text =
                                        requireContext().getText(R.string.requested)
                                } else {
                                    it.followStatus = "1"
                                    tvFollowBtn.text =
                                        requireContext().getText(R.string.following)
                                }
                            }

                            "3" -> {
                                it.followStatus = "2"
                                authViewModel.followUnfollowUserRequest =
                                    UserRequest(it._id, AppConstant.CANCEL)
                                authViewModel.postFollowRequestStatus()
                                tvFollowBtn.text = requireContext().getText(R.string.follow)
                            }

                            else -> {
                                Utils.showToast(requireContext().getString(R.string.something_went_wrong))
                            }
                        }
                    }
                }else{
                    Utils.showToast(getString(R.string.no_internet_message))
                }

            }
        }
    }

    private fun callApi() {
        val isInternetAvailable = isInternetAvailable(requireActivity())
        if (isInternetAvailable) {
            authViewModel.userDertails(mapOf("user" to param1!!))
        }

    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun setObserver() {
        authViewModel.userDetails.observe(viewLifecycleOwner) { response ->
            binding?.animationView?.visibility = View.GONE
            if (response != null) {
                if (response.success) {
//                    dialogs?.dismiss()
                    response.data?.let {
                        Log.e("UserDetails", response.data.toString())
                        userDetailData = it
                        profile = response.data?.profile.toString()
                        binding?.apply {
                            if (!it.isMessageOn) {
                                btnMsg.isEnabled = false
                                btnMsg.setTextColor(requireContext().getColor(R.color.Grey))
                                btnMsg.backgroundTintList =
                                    ColorStateList.valueOf(requireContext().getColor(R.color.unselectedTabColor))
//                                btnMsg.background
                            }

                            val type = when (it.followStatus) {
                                null -> {
                                    "own"
                                }

                                "1" -> {
                                    AppConstant.FOLLOWERS
                                }

                                else -> {
                                    ""
                                }
                            }

                            if (it.isBlock) {
                                binding?.tvBlockBtn?.visibility = View.VISIBLE
                                binding?.followMessageContainer?.visibility = View.GONE
                            } else {
                                binding?.tvBlockBtn?.visibility = View.GONE
                                binding?.followMessageContainer?.visibility = View.VISIBLE
                            }
                            Log.e("afggsdf",it.accessSetting?.seeLikeReceived.toString())
                            it.accessSetting?.let { access ->
                                otherUserDataAccessibility(
                                    access.seeFollower,
                                    type,
                                    it.isFriend,
                                    clFollwer
                                )
                                otherUserDataAccessibility(
                                    access.seeFollowing,
                                    type,
                                    it.isFriend,
                                    clFollowing
                                )
                                otherUserDataAccessibility(
                                    access.seeTree,
                                    type,
                                    it.isFriend,
                                    clTrees
                                )
                                otherUserDataAccessibility(
                                    access.seeLikeGiven,
                                    type,
                                    it.isFriend,
                                    tvGiven
                                )

                                otherUserDataAccessibility(
                                    access.seeLikeReceived,
                                    type,
                                    it.isFriend,
                                    tvReceived
                                )
                                otherUserDataAccessibility(
                                    access.seeMyProfile,
                                    type,
                                    it.isFriend,
                                    clProfileCardView
                                )


                                btnNotification.setImageDrawable(
                                    if (it.isNotificationOn) {
                                        requireContext().getDrawable(R.drawable.ic_noti_bell_ring)
                                    } else {
                                        requireContext().getDrawable(R.drawable.ic_notification_bell)
                                    }
                                )


                                if (tvGiven.visibility == View.GONE && tvReceived.visibility == View.GONE) {
                                    linearLayout.visibility = View.GONE
                                }

                                when (access.seeArchive) {
                                    AppConstant.EVERYONE -> {
                                        setViewPager(true, it.isBlock)
                                    }

                                    AppConstant.NO_ONE -> {
                                        setViewPager(false, it.isBlock)
                                    }

                                    else -> {
                                        if (access.seeArchive.equals(
                                                type,
                                                true
                                            ) || (access.seeArchive.equals(
                                                AppConstant.FRIENDS,
                                                true
                                            ) && it.isFriend)
                                        ) {
                                            setViewPager(true, it.isBlock)
                                        } else {
                                            setViewPager(false, it.isBlock)
                                        }
                                    }
                                }
                            }

                            tvPrivateAccount.text =
                                response.data!!.accountType.capitalizeFirstChar() + " " + "Account"

                            when (it.followStatus) {
                                null -> {
                                    tvFollowBtn.visibility = View.GONE
                                }

                                "1" -> {
                                    tvFollowBtn.visibility = View.VISIBLE
                                    tvFollowBtn.text = requireContext().getText(R.string.following)
                                }

                                "2" -> {
                                    tvFollowBtn.visibility = View.VISIBLE
                                    tvFollowBtn.text = requireContext().getText(R.string.follow)
                                }

                                "3" -> {
                                    tvFollowBtn.visibility = View.VISIBLE
                                    tvFollowBtn.text = requireContext().getText(R.string.requested)
                                }

                                else -> {
                                    tvFollowBtn.visibility = View.GONE
                                }

                            }
                            tvName.text = response.data?.name
                            if (it.blueTickEnable == true) {
                                icVerified.visibility = View.VISIBLE
                            } else {
                                icVerified.visibility = View.GONE
                            }
                            tvUsername.text = response.data?.username

                            tvFriendsCount.text = response.data?.noOfFriends.toString()
                            tvFollowerCount.text = response.data?.noOfFollowers.toString()
                            tvFollowingCount.text = response.data?.noOfFollowings.toString()
                            tvTreesCount.text = response.data?.eTrees.toString()
                            CoroutineScope(Dispatchers.IO).launch {
                                ivUserProfileImage.loadImageAsync(response.data?.profile ?: "")
                            }
                            /** Given & Received value*/
                            val givenValue = response.data?.overallGivenLikes
                            val receivedValue = response.data?.overallLikes
                            val sourceStringGiven = "Given  <b>$givenValue</b> "
                            val sourceStringReceive = "Received  <b>$receivedValue</b> "
                            tvGiven.text = Html.fromHtml(sourceStringGiven)
                            tvReceived.text = Html.fromHtml(sourceStringReceive)
                            profileDetailContainer.visibility = View.VISIBLE
                            if (it.bio != null) {
                                if (it.bio == " " || it.bio.trim().isEmpty()) {
                                    tvProfileBio.visibility = View.GONE
                                } else {
                                    tvProfileBio.visibility = View.VISIBLE
                                    val description = Utils.convertDesc(it.bio)
                                    it.bioMentionUsers.forEach { it ->
                                        if (!mentionUserHash.containsKey("@" + it.username)) {
                                            mentionUserHash["@" + it.username] = it._id
                                        }
                                    }
                                    it.bioMentionHashtags?.forEach { it ->
                                        if (!hashtagUserHash.containsKey("#" + it.tag)) {
                                            hashtagUserHash["#" + it.tag] = it._id
                                        }
                                    }

                                    val ss = SpannableString(description)

                                    val hashtagSpans1: ArrayList<IntArray> =
                                        Utils.getSpans(description, '#')
                                    val callOutSpans1: ArrayList<IntArray> =
                                        Utils.getSpans(description, '@')
                                    Utils.setSpanComment(ss,
                                        hashtagSpans1,
                                        this@OtherUserProfileFragment,
                                        it.bioMentionHashtags?.let { it1 -> ArrayList(it1) }
                                            ?: ArrayList()
                                    )
                                    Utils.setSpanUnameForBio(
                                        ss,
                                        callOutSpans1,
                                        this@OtherUserProfileFragment,
                                        response.data!!.bioMentionUsers
                                    )
                                    tvProfileBio.movementMethod = LinkMovementMethod.getInstance()
                                    tvProfileBio.text = ss

                                }

                                //    mBinding.tvProfileBio.text = description
                            } else {
                                tvProfileBio.visibility = View.GONE
                            }
                        }
                    }
                }
            }
        }

        authViewModel.showMessage.observe(viewLifecycleOwner){
            if (it.isNotEmpty()){
                binding?.apply {
                    animationView.visibility = View.GONE
                    userNotFoundContainer.visibility =  View.VISIBLE
                    tabLayout.visibility = View.GONE
                    btnMenu.visibility = View.GONE

                }
            }
        }
        authViewModel.followUnfollowUserData.observe(viewLifecycleOwner) { response ->
            if (response != null) {

            }
        }
        authViewModel.switchNotificationData.observe(viewLifecycleOwner) { response ->
            if (response != null) {
                if (response.success) {
                    response.data?.let {
                        Utils.showToast(
                            "Post Notification ${
                                if (it.isNotificationOn == true) {
                                    "ON"
                                } else {
                                    "OFF"
                                }
                            }"
                        )
                    }
                }
            }
        }
        authViewModel.postReportData.observe(viewLifecycleOwner) {
            if (!it.message.isNullOrEmpty()) {
                Utils.showToast(it.message ?: "")
            }
        }

        authViewModel.baseResponse.observe(viewLifecycleOwner) { response ->
            if (response != null) {
                if (response.success) {
                    response.data?.isBlock?.let {
                        /*if (it){
                            binding?.tvBlockBtn?.visibility = View.VISIBLE
                            binding?.followMessageContainer?.visibility = View.GONE
                        }else{
                            binding?.followMessageContainer?.visibility = View.VISIBLE
                            binding?.tvBlockBtn?.visibility = View.GONE
                        }*/
                        val bundle = Bundle()
                        bundle.putString("mentionUser", param1)
                        findNavController().popBackStack()
                        findNavController().navigate(R.id.otherProfileFragment, bundle)
                    }
                }
            }
        }

        authViewModel.showLoading.observe(viewLifecycleOwner) {

        }
    }

    private fun otherUserDataAccessibility(
        canSee: String?,
        followStatus: String,
        isFriend: Boolean,
        view: View
    ) {
        when (canSee?.lowercase()) {
            AppConstant.EVERYONE -> {
                view.visibility = View.VISIBLE
            }

            AppConstant.NO_ONE -> {
                view.visibility = View.GONE
            }

            else -> {
                if (canSee.equals(followStatus, true)) {
                    view.visibility = View.VISIBLE
                } else if (canSee.equals(AppConstant.FRIENDS, true) && isFriend) {
                    view.visibility = View.VISIBLE
                } else {
                    view.visibility = View.GONE
                }
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: MentionUser) =
            OtherUserProfileFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_PARAM1, param1)
                }
            }
    }

    override fun shareClick() {
        val link = generateDeepLink(
            DeepLinkType.PROFILE,
            param1,
            binding?.tvName?.text.toString() + "(${binding?.tvUsername?.text}) on Wedo80",
            ""
        )
        share(
            getString(
                R.string.share_profile_link,
                "${Prefs.init().name}", link
            )
        )
    }

    override fun blockClick() {

      userDetailData?.let { userBlocked(it) }

    }

    override fun reportClick() {
        val bottomSheetFragment = ReportUserBottomSheet(R.layout.user_report_option_sheet, this)

        bottomSheetFragment.show(childFragmentManager, bottomSheetFragment.tag)
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Subscribe
    fun onDoubleTap(event: Any) {
        if (event is ProfilePostItemClick) {
            bundle = Bundle()
            bundle.putInt("position", event.position)
            bundle.putString("dataList", event.dataList.toJsonString())
            bundle.putString("type", event.postType)
            findNavController().navigate(R.id.show_profile_post_fragment, bundle)
        }
    }

    override fun reportClick(reason: String) {
        if (isInternetAvailable(requireContext())) {
            authViewModel.postReportRequest = PostRequest(user = param1, reason = reason)
            authViewModel.userReport()
        } else {
            showToast(getString(R.string.no_internet_message))
        }

    }

    override fun hashTagClick(text: String) {
        val bundle = Bundle()
        bundle.putString("hashtag", text)
        bundle.putString("type", "hashtag")
        findNavController().navigate(
            R.id.action_otherProfileFragment_to_show_profile_post_fragment,
            bundle
        )
        /* val bundle = Bundle()
         bundle.putString("hashtag", text)
         findNavController().navigate(
             R.id.searchFragment,
             bundle
         )*/
    }

    override fun mentionClick(text: String) {
        val bundle = Bundle()
        bundle.also {
            it.putString("mentionUser", text)
            findNavController().navigate(
                R.id.action_otherProfileFragment_to_otherProfileFragment,
                it
            )
        }
    }

    @SuppressLint("SetTextI18n")
    private fun userBlocked(data: UserDetailData) {
        dialogs?.setCancelable(false)
        Objects.requireNonNull(dialogs?.window)?.setBackgroundDrawable(
            ColorDrawable(
                Color.TRANSPARENT
            )
        )
        dialogs?.setContentView(R.layout.discard_post)
        dialogs?.setCanceledOnTouchOutside(true)
        val logout = dialogs?.findViewById(R.id.Logout) as TextView
        val mainText = dialogs?.findViewById(R.id.SureLoutout) as TextView
        val ivAlert = dialogs?.findViewById(R.id.ivAlert) as ImageView
        val ivUserProfileImage = dialogs?.findViewById(R.id.ivUserProfileImage) as ImageView

        ivAlert.visibility = View.GONE
        ivUserProfileImage.visibility = View.VISIBLE
        mainText.text = "Are you sure you want to ${if (!data.isBlock){
            "Block"
        } else{
            "Unblock"
        }} \n \"${data.name}\"?"

        logout.text = if (!data.isBlock){
            "Block"
        } else{
            "Unblock"
        }
        CoroutineScope(Dispatchers.Main).launch {
            ivUserProfileImage.loadImageAsyncMessage(data.profile)
        }

        logout.setOnClickListener {
            if (isInternetAvailable(requireActivity())) {
                userDetailData?.let {
                    it.isBlock = !it.isBlock
                    authViewModel.userBlocked(UserBlockedRequest(it._id))
                }
            } else {

                Utils.showToast("Please check your internet connection.")
            }
            dialogs?.dismiss()
        }
        dialogs?.show()
    }
}