package com.wedo8020.app.mainui.home.chatAndNotification

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayout
import com.wedo8020.app.MainApplication
import com.wedo8020.app.R
import com.wedo8020.app.databinding.FragmentMessageAndNotificationsBinding
import com.wedo8020.app.eventBus.isSocketConnected
import com.wedo8020.app.mainui.activities.MainActivity
import com.wedo8020.app.mainui.adapter.ViewPagerAdapter
import com.wedo8020.app.mainui.base.BaseFragment
import com.wedo8020.app.mainui.home.chatAndNotification.chat.MessageUserListFragment
import com.wedo8020.app.mainui.home.chatAndNotification.notifications.NotificationUserListFragment
import com.wedo8020.app.mainui.home.postFragment.PostViewModel
import com.wedo8020.app.utils.ChangeTab
import com.wedo8020.app.utils.NewMessage
import com.wedo8020.app.utils.NotificationListener
import com.wedo8020.app.utils.OnClickNotification
import com.wedo8020.app.utils.Prefs
import com.wedo8020.app.utils.ReadReceipt
import com.wedo8020.app.utils.Utils
import com.wedo8020.zelpyy.SocketProvider.SocketIOImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject

/**
 * A simple [Fragment] subclass.
 * Use the [MessageAndNotificationsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MessageAndNotificationsFragment : BaseFragment() {
private lateinit var mBinding:FragmentMessageAndNotificationsBinding
    private val fragments = ArrayList<Fragment>()
    private val authViewModel by viewModels<PostViewModel>()
    private var adapter: ViewPagerAdapter? =null
    private lateinit var socket: SocketIOImpl
    private val titles = listOf(
        "Messages","Notifications"
    )
    private var notificationsValue=0
    private var  messageValue=0
    var bundle=Bundle()
    private var pushOnOffSwitch = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mBinding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.fragment_message_and_notifications,
            container,
            false
        )
        socket=MainApplication.get().socket
        setUpUi()
        callApi()
        detailsObserver()

        mBinding.IvSetting.setOnClickListener {
            mBinding.IvSetting.disableBtn()
            showMessagingSettingBottomSheet()
        }

        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner){
          //  findNavController().navigate(R.id.homeFragment)
            val activity = activity as MainActivity
            activity.homeTabClick()
        }
    }

        private fun setUpUi(){
                mBinding.apply {
                    fragments.add(MessageUserListFragment())
                    fragments.add(NotificationUserListFragment())
                  /** Replace with your actual tab titles*/
                    adapter = ViewPagerAdapter(childFragmentManager, 2, fragments, titles)
                    viewPager.adapter = adapter
                    viewPager.offscreenPageLimit = 2
                    if(arguments != null && requireArguments().containsKey("type")){
                        viewPager.currentItem=1
                        mBinding.tvReceiveTab.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                        mBinding.ivReceivedBar.visibility=View.VISIBLE
                        mBinding.ivGivenBar.visibility=View.GONE
                        Looper.myLooper()?.let { looper ->
                            android.os.Handler(looper).postDelayed({
                                org.greenrobot.eventbus.EventBus.getDefault().post(OnClickNotification("OnClickNotification"))
                            }, 500)
                        }
                    }
                    tabLayout.setupWithViewPager(viewPager)
                    viewPager.setSwipeable(false)
                    tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                        override fun onTabSelected(tab: TabLayout.Tab) {
                            when (tab.position){
                                0 -> {
                                    mBinding.ivReceivedBar.visibility=View.GONE
                                    mBinding.ivGivenBar.visibility=View.VISIBLE
                                    mBinding.tvGivenTab.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                                }
                                1-> {
                                    mBinding.tvReceiveTab.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                                    mBinding.ivReceivedBar.visibility=View.VISIBLE
                                    mBinding.ivGivenBar.visibility=View.GONE
                                    org.greenrobot.eventbus.EventBus.getDefault()
                                        .post(OnClickNotification("OnClickNotification"))
                                    org.greenrobot.eventbus.EventBus.getDefault()
                                        .post(ChangeTab("Message"))
//
                                }
                            }
                        }
                        override fun onTabUnselected(tab: TabLayout.Tab) {
                            mBinding.tvGivenTab.setTextColor(ContextCompat.getColor(requireContext(), R.color.Grey))
                            mBinding.tvReceiveTab.setTextColor(ContextCompat.getColor(requireContext(), R.color.Grey))
                        }

                        override fun onTabReselected(tab: TabLayout.Tab) {
                            when (tab.position){
                                0 -> {
                                    mBinding.ivReceivedBar.visibility=View.GONE
                                    mBinding.ivGivenBar.visibility=View.VISIBLE
                                    mBinding.tvGivenTab.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                                }
                                1-> {
                                    mBinding.tvReceiveTab.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                                    mBinding.ivReceivedBar.visibility=View.VISIBLE
                                    mBinding.ivGivenBar.visibility=View.GONE
                                }
                            }
                        }
                    })
                  /*  if (arguments!=null&&requireArguments().containsKey("hashtag")){
                        Utils.runWithDelay(1000){
                            viewPager.setCurrentItem(3,false)
                            //   searchEt.setText(requireArguments().getString("hashtag"))
                        }
                    }else if(arguments != null && requireArguments().containsKey("type")){
                        Utils.runWithDelay(1000){
                            viewPager.setCurrentItem(1,false)
                            //   searchEt.setText(requireArguments().getString("hashtag"))
                        }
                    }*/
                }
        }
    private fun showMessagingSettingBottomSheet() {
        val bottomSheetFragment = MessagingSettingBottomSheet(R.layout.fragment_messaging_setting_bottom_sheet)
         bundle.putString("pushOnOffSwitch", pushOnOffSwitch.toString())
         bottomSheetFragment.arguments = bundle
        bottomSheetFragment.height = 600
        bottomSheetFragment.show(childFragmentManager , bottomSheetFragment.tag)
    }
        override fun onDestroyView() {
            super.onDestroyView()
            //  mBinding = null
            adapter = null

        }
    override fun onResume() {
        super.onResume()
        if (!socket.isConnected()) {
            socket.connect()
        } else {
            setSocketListenON()
            getNotificationCount()
        }

        if (notificationsValue>0){
            if (notificationsValue<100){
                mBinding.tvNotification.text=notificationsValue.toString()
            }else{
                mBinding.tvNotification.setText(R.string.more_than_99)
            }
            mBinding.tvNotification.visibility=View.VISIBLE

        }else{
            mBinding.tvNotification.visibility=View.GONE
        }

        if (messageValue>0){
            if (messageValue>99){
                mBinding.tvMessage.setText(R.string.more_than_99)
            }else{
                mBinding.tvMessage.text=messageValue.toString()

            }
            mBinding.tvMessage.visibility=View.VISIBLE

        }else{
            mBinding.tvMessage.visibility=View.GONE
        }
    }



    private fun getNotificationCount() {
        socket.emitMessage("totalNotification", "Bearer " + Prefs.init()
            .accessToken) { _, _ ->
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
                Log.e("totalNotificationSuccess" ,dataObject.toString())
                notificationsValue = dataObject.optInt("notifications", 0)
                messageValue = dataObject.optInt("messages", 0)
                org.greenrobot.eventbus.EventBus.getDefault()
                    .post(NewMessage(messageValue.toString()))
                if (notificationsValue>0){
                    if (notificationsValue<100){
                        mBinding.tvNotification.text=notificationsValue.toString()
                    }else{
                        mBinding.tvNotification.setText(R.string.more_than_99)
                    }
                    mBinding.tvNotification.visibility=View.VISIBLE

                }else{
                    mBinding.tvNotification.visibility=View.GONE
                }

                if (messageValue>0){
                    if (messageValue>99){
                        mBinding.tvMessage.setText(R.string.more_than_99)
                    }else{
                        mBinding.tvMessage.text=messageValue.toString()

                    }
                    mBinding.tvMessage.visibility=View.VISIBLE

                }else{
                    mBinding.tvMessage.visibility=View.GONE
                }
                org.greenrobot.eventbus.EventBus.getDefault()
                    .post(NotificationListener("totalNotificationSuccess"))


            }
        }
        socket.listenOn("error"){
            CoroutineScope(Dispatchers.Main).launch {

            }
        }
    }


    override fun onStart() {
        super.onStart()
        org.greenrobot.eventbus.EventBus.getDefault().register(this)
        //initializePlayer()
    }

    override fun onStop() {
        super.onStop()
        org.greenrobot.eventbus.EventBus.getDefault().unregister(this)

    }

    @SuppressLint("SetTextI18n")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvents(event: ReadReceipt?) {
      if(event?.text=="Read"){
          callApi()
      }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun isSocketConnected(event: isSocketConnected){
        if (event.isConnected){
            setSocketListenON()
            getNotificationCount()
        }
    }

    fun callApi() {
        if (Utils.isInternetAvailables(requireActivity())) {
            authViewModel.userDertails()
        } else {
            Utils.showToast("Please check your internet connection.")
        }
    }
        @SuppressLint("NotifyDataSetChanged", "SuspiciousIndentation")
        private fun detailsObserver() {
            authViewModel.userDetails.observe(viewLifecycleOwner) { response ->
                if (response != null) {
                    if (response.success) {
                        pushOnOffSwitch=response.data!!.readReceipt

                    }
                }
            }
        }

}