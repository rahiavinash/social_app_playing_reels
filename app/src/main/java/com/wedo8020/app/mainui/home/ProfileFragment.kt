package com.wedo8020.app.mainui.home


import android.annotation.SuppressLint
import android.app.Dialog
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
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.wedo8020.app.R
import com.wedo8020.app.databinding.FragmentProfileBinding
import com.wedo8020.app.mainui.base.BaseFragment
import com.wedo8020.app.mainui.home.adapter.ProfileViewpagerAdapter
import com.wedo8020.app.mainui.home.postFragment.PostViewModel
import com.wedo8020.app.utils.Hashtag
import com.wedo8020.app.utils.Prefs
import com.wedo8020.app.utils.Utils
import com.bumptech.glide.Glide
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.tabs.TabLayout
import com.wedo8020.app.MainApplication
import com.wedo8020.app.eventBus.ProfilePostItemClick
import com.wedo8020.app.mainui.activities.MainActivity
import com.wedo8020.app.mainui.login.switchaccounts.SwitchAccountFragment
import com.wedo8020.app.utils.ActivePostUpdate
import com.wedo8020.app.utils.ArchivePostUpdate
import com.wedo8020.app.utils.NewMessage
import com.wedo8020.app.utils.NewProfileUpdated
import com.wedo8020.app.utils.OnClickEditPost
import com.wedo8020.app.utils.SaveDropUpdate
import com.wedo8020.app.utils.Utils.toJsonString
import com.wedo8020.zelpyy.SocketProvider.SocketIOImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import java.util.Objects


class ProfileFragment : BaseFragment(), Hashtag.onHashtagClick {
    private lateinit var mBinding: FragmentProfileBinding
    private val authViewModel by viewModels<PostViewModel>()
    private var mentionUserHash = hashMapOf<String, String>()
    private var hashtagUserHash = hashMapOf<String, String>()
    var dialogs: Dialog? = null
    var bundle = Bundle()
    var profile = ""
    private var isCollapseToolbar=false
    private lateinit var socket: SocketIOImpl
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        mBinding =
            DataBindingUtil.inflate(layoutInflater, R.layout.fragment_profile, container, false)
        socket = MainApplication.get().socket
        if (context != null) {
            dialogs = Dialog(requireActivity())
            dialogs?.requestWindowFeature(Window.FEATURE_CONTENT_TRANSITIONS)
        }
        mBinding.ivUserProfileImage.isClickable = false
        selectedTab()
        hideStatusAndNavigationBar()
        hideSoftKeyboard(requireActivity())
        //  setOnGestureBack(mBinding.root)
        /**Show BottomNavigation*/
        showNavBottom()
        callApi()
        setObserver()
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
           // findNavController().navigate(R.id.homeFragment)
            Log.e("asdfsad","ASdgasfg")
            val activity = activity as MainActivity
            activity.homeTabClick()
        }

        mBinding.ivUserProfileImage.setOnClickListener {
            mBinding.ivUserProfileImage.disableBtn()
            bundle.putString("profile", profile)
            findNavController().navigate(R.id.action_profileFragment_to_showProfileFragment, bundle)
        }

        mBinding.ivUserProfileImage.setOnLongClickListener {
            showSwitchAccountBottomSheet()
            true
        }

        mBinding.IvSetting.setOnClickListener {
            findNavController().navigate(R.id.profileSettingsFragment)
        }
        mBinding.ivLinkedAccount.setOnClickListener {
            findNavController().navigate(R.id.linkedAccountFragment)
        }

        mBinding.tvEditProfile.setOnClickListener {
            checkIfFragmentAttached {
                if (isInternetAvailable(requireContext())){
                    findNavController().navigate(R.id.action_profileFragment_to_editProfileFragment)
                }else{
                    showToast(getString(R.string.no_internet_message))
                }
                }


        }
        mBinding.tvGiven.setOnClickListener {
            bundle.putString("types", "given")
            findNavController().navigate(
                R.id.action_profileFragment_to_likeGivenAndReceiveFragment,
                bundle
            )
        }
        mBinding.tvReceived.setOnClickListener {
            bundle.putString("types", "received")
            findNavController().navigate(
                R.id.action_profileFragment_to_likeGivenAndReceiveFragment,
                bundle
            )
        }
        /*    mBinding.llFriendFollower.setOnClickListener {
                findNavController().navigate(R.id.action_profileFragment_to_friendFollowerFragment)
            }*/
        mBinding.clFriend.setOnClickListener {
            bundle.putString("type", "friend")
            findNavController().navigate(
                R.id.action_profileFragment_to_friendFollowerFragment,
                bundle
            )
        }
        mBinding.clFollwer.setOnClickListener {
            bundle.putString("type", "follower")
            findNavController().navigate(
                R.id.action_profileFragment_to_friendFollowerFragment,
                bundle
            )
        }

        mBinding.clFollowing.setOnClickListener {
            bundle.putString("type", "following")
            findNavController().navigate(
                R.id.action_profileFragment_to_friendFollowerFragment,
                bundle
            )
        }
    }

    private fun collapseToolbar() {
        if (isCollapseToolbar) {
            val collapsingToolbarLayout = view?.findViewById<AppBarLayout>(R.id.collapsingToolbar)
            // Collapse the CollapsingToolbarLayout
            collapsingToolbarLayout?.setExpanded(false)
            isCollapseToolbar=false
        }
    }
    private fun showSwitchAccountBottomSheet() {
        val bottomSheetFragment = SwitchAccountFragment(R.layout.fragment_switch_account)
        /* bundle.putString("posting", mBinding.tvPublic.text.toString())
         bottomSheetFragment.arguments = bundle*/
        bottomSheetFragment.height = 1000
        bottomSheetFragment.show(childFragmentManager, bottomSheetFragment.tag)
    }

    fun callApi() {
        if (context != null) {
            if (isInternetAvailable(requireActivity())) {
                // showProgressDialog()
                authViewModel.userDertails()
            } else {
                dialogs?.dismiss()
                Utils.showToast("Please check your internet connection.")
            }
        }
    }

    private fun selectedTab() {
        val adapter = ProfileViewpagerAdapter(childFragmentManager)
        mBinding.viewPager.adapter = adapter

        /** SetIcon For TabLayout and TabColor*/
        mBinding.tabLayout.setupWithViewPager(mBinding.viewPager)
        mBinding.tabLayout.getTabAt(0)?.setIcon(R.drawable.ic_vedio_svg)
        mBinding.tabLayout.getTabAt(0)?.icon?.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN)
        mBinding.tabLayout.getTabAt(1)?.setIcon(R.drawable.ic_save_post)
        mBinding.tabLayout.getTabAt(1)?.icon?.setColorFilter(
            resources.getColor(R.color.Grey),
            PorterDuff.Mode.SRC_IN
        )
        mBinding.tabLayout.getTabAt(2)?.setIcon(R.drawable.ic_archive)
        mBinding.tabLayout.getTabAt(2)?.icon?.setColorFilter(
            resources.getColor(R.color.Grey),
            PorterDuff.Mode.SRC_IN
        )
        /*  mBinding.tabLayout.getTabAt(3)!!.setIcon(R.drawable.ic_archive)
          mBinding.tabLayout.getTabAt(3)!!.icon!!.setColorFilter(
              resources.getColor(R.color.Grey),
              PorterDuff.Mode.SRC_IN
          )*/

        /** TabOnSelectedListener for handle the tab color on select*/
        mBinding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                mBinding.viewPager.currentItem = tab.position
                val tabIconColor = ContextCompat.getColor(requireContext(), R.color.black)
                tab.icon?.setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN)
                if (tab.position == 1) {
                    org.greenrobot.eventbus.EventBus.getDefault()
                        .post(SaveDropUpdate("save"))
                }else if (tab.position==2){
                    org.greenrobot.eventbus.EventBus.getDefault()
                        .post(ArchivePostUpdate("archive"))
                }
              /*  else  if (tab.position==0) {
                 org.greenrobot.eventbus.EventBus.getDefault()
                        .post(ActivePostUpdate(""))
                }*/

            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                val tabIconColor = ContextCompat.getColor(requireContext(), R.color.Grey)
                tab.icon?.setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN)
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
                val tabIconColor = ContextCompat.getColor(requireContext(), R.color.black)
                tab.icon?.setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN)
            }
        })
    }


    @SuppressLint("SetTextI18n", "SuspiciousIndentation")
    private fun setObserver() {
        authViewModel.userDetails.observe(viewLifecycleOwner) { response ->
            dialogs?.dismiss()
            if (response != null) {
                if (response.success) {
                    if (response.data != null) {
                        mBinding.ivUserProfileImage.isClickable = true
                        response.data?.let {
                            mBinding.tvPrivateAccount.text =
                                response.data?.accountType?.capitalize() + " " + "Account"
                            mBinding.tvName.text = response.data?.name
                            if (it.blueTickEnable == true) {
                                mBinding.icVerified.visibility = View.VISIBLE
                            } else {
                                mBinding.icVerified.visibility = View.GONE
                            }
                            mBinding.tvUserNames.text = response.data?.username
                            mBinding.tvFriendCount.text = response.data?.noOfFriends.toString()
                            mBinding.tvFollowerCount.text =
                                response.data?.noOfFollowers.toString()
                            mBinding.tvFollowingCount.text =
                                response.data?.noOfFollowings.toString()
                            mBinding.tvTreesCount.text = response.data?.eTrees.toString()
                            Prefs.init().profileId = response.data?._id.toString()
                            response.data.let {
                                if (it?.bio != null) {
                                    if (it.bio == " ") {
                                        mBinding.tvProfileBio.visibility = View.GONE
                                    } else {
                                        mBinding.tvProfileBio.visibility = View.VISIBLE
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
                                            this,
                                            it.bioMentionHashtags?.let { it1 -> ArrayList(it1) }
                                                ?: ArrayList()
                                        )
                                        response.data?.bioMentionUsers?.let { it1 ->
                                            Utils.setSpanUnameForBio(
                                                ss,
                                                callOutSpans1,
                                                this,
                                                it1
                                            )
                                        }
                                        mBinding.tvProfileBio.movementMethod =
                                            LinkMovementMethod.getInstance()
                                        mBinding.tvProfileBio.text = ss.trim()

                                    }

                                    //    mBinding.tvProfileBio.text = description
                                } else {
                                    mBinding.tvProfileBio.visibility = View.GONE
                                }
                            }

                            if (!response.data?.profile.isNullOrEmpty()) {
                                profile = response.data?.profile.toString()
                                if (context != null) {
                                    if (context != null) {
                                        Glide.with(requireContext()).load(response.data?.profile)
                                            .error(R.drawable.ic_greyuser)
                                            .into(mBinding.ivUserProfileImage)

                                        Prefs.init().userProfile = response.data?.profile.toString()
                                        org.greenrobot.eventbus.EventBus.getDefault()
                                            .post(NewProfileUpdated("NewProfileUpdated"))
                                    }
                                }

                            }


                            /** Given & Received value*/
                            val givenValue = response.data?.overallGivenLikes
                            val receivedValue = response.data?.overallLikes
                            val sourceStringGiven = "Given  <b>$givenValue</b> "
                            val sourceStringReceive = "Received  <b>$receivedValue</b> "
                            mBinding.tvGiven.text = Html.fromHtml(sourceStringGiven)
                            mBinding.tvReceived.text = Html.fromHtml(sourceStringReceive)

                        }
                    }
                }
            }
        }

        authViewModel.showLoading.observe(viewLifecycleOwner) {
            if (it == false) {
                dialogs?.dismiss()
            } else {
                //  showProgressDialog()
            }

        }
    }

    private fun showProgressDialog() {
        dialogs?.setCancelable(false)
        Objects.requireNonNull(dialogs?.window)?.setBackgroundDrawable(
            ColorDrawable(
                Color.TRANSPARENT
            )
        )
        dialogs?.setContentView(R.layout.progressbar)
        dialogs?.show()
    }

    override fun hashTagClick(text: String) {
//                setLastVisibleItemPos()
        val bundle = Bundle()
        bundle.putString("hashtag", text)
        bundle.putString("type", "hashtag")
        findNavController().navigate(
            R.id.action_profileFragment_to_show_profile_post_fragment,
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
                R.id.action_profileFragment_to_otherProfileFragment,
                it
            )
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
            collapseToolbar()
        if (!socket.isConnected()) {
            socket.connect()

        } else {
            setSocketListenON()
            getNotificationCount()
        }

    }


    private fun getNotificationCount() {
        socket.emitMessage(
            "totalNotification", "Bearer " + Prefs.init()
                .accessToken
        ) { _, _ ->
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setSocketListenON() {
        socket.listenOff("totalNotificationSuccess")
        socket.listenOff("error")
        socket.listenOn("totalNotificationSuccess") { args ->
            CoroutineScope(Dispatchers.Main).launch {
                val obj = JSONObject(args[0].toString())
                val dataObject = obj.getJSONObject("data")
                Log.e("totalNotificationSuccess", dataObject.toString())
                val messageValue = dataObject.optInt("messages", 0)
                org.greenrobot.eventbus.EventBus.getDefault()
                    .post(NewMessage(messageValue.toString()))
            }
        }
        socket.listenOn("error") { args ->
            CoroutineScope(Dispatchers.Main).launch {

            }
        }
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onActivePostUpdate(event: OnClickEditPost?) {
        isCollapseToolbar=true
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Subscribe
    fun onDoubleTap(event: Any) {
        if (event is ProfilePostItemClick) {
            bundle = Bundle()
            bundle.putInt("position", event.position)
            bundle.putString("dataList", event.dataList.toJsonString())
            bundle.putString("type", event.postType)
            bundle.putBoolean("ownProfile", true)
            findNavController().navigate(R.id.show_profile_post_fragment, bundle)
            isCollapseToolbar=true
        }
    }
}