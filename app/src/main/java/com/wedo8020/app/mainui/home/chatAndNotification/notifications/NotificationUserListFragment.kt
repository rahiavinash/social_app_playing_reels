package com.wedo8020.app.mainui.home.chatAndNotification.notifications

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.wedo8020.app.MainApplication
import com.wedo8020.app.R
import com.wedo8020.app.databinding.FragmentNotificationUserListBinding
import com.wedo8020.app.eventBus.isSocketConnected
import com.wedo8020.app.mainui.base.BaseFragment
import com.wedo8020.app.mainui.home.chatAndNotification.adapter.NotificationAdapter
import com.wedo8020.app.mainui.home.chatAndNotification.chatViewModel.ChatViewModel
import com.wedo8020.app.mainui.home.postFragment.PostViewModel
import com.wedo8020.app.request.DeleteNotification
import com.wedo8020.app.request.NotificationRequest
import com.wedo8020.app.response.AcceptRejectRequest
import com.wedo8020.app.response.NotificationListData
import com.wedo8020.app.utils.NotificationListener
import com.wedo8020.app.utils.OnClickNotification
import com.wedo8020.app.utils.Prefs
import com.wedo8020.app.utils.SocketListenerConstant
import com.wedo8020.app.utils.Utils
import com.wedo8020.app.utils.Utils.disableMultiTap
import com.wedo8020.zelpyy.SocketProvider.SocketIOImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import java.util.Objects
import java.util.TimeZone


class NotificationUserListFragment : BaseFragment(), NotificationAdapter.OnClickListener {
    private lateinit var mBinding: FragmentNotificationUserListBinding
    private val authViewModel by viewModels<PostViewModel>()
    private val viewModel by viewModels<ChatViewModel>()
    private val list: ArrayList<NotificationListData> = ArrayList()
    private val uniqueList = arrayListOf<NotificationListData>()
    private lateinit var adapter: NotificationAdapter
    private var yearMonthDate = ""
    var isPagination = false
    private var allClear = false
    var offset = 0
    var bundle = Bundle()
    var dialog: Dialog? = null
    var dialogs: Dialog? = null
    private var dialogProgress: Dialog? = null
    private var refreshItems = false
    private lateinit var socket: SocketIOImpl


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment

        mBinding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.fragment_notification_user_list,
            container,
            false
        )
        socket = MainApplication.get().socket
        showNavBottom()
        // callApiForNotificationList()

        return mBinding.root
    }

    @SuppressLint("SuspiciousIndentation")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        notificationObserver()
        /** Refresh the Data */
        mBinding.refresh.setOnRefreshListener {
            if (isInternetAvailable(requireContext())) {
                // showProgressDialog()
                /* list.clear()
                 uniqueList.clear()*/
                refreshItems = true
                /* mBinding.rvNotificationList.visibility = View.GONE*/
                mBinding.animationView.visibility = View.GONE
                mBinding.TvNoData.visibility = View.GONE
                Handler(Looper.myLooper()!!).postDelayed({
                    callApiForNotificationList()
                }, 1000)
            } else {
                dialogProgress?.dismiss()
                mBinding.refresh.isRefreshing = false
                showToast(getString(R.string.no_internet_message))
            }
        }

        mBinding.tvRequest.setOnClickListener {
            findNavController().navigate(R.id.action_messageAndNotificationsFragment_to_requestSentAndReceiveFragment)
        }
        mBinding.tvClearAll.setOnClickListener {
            mBinding.tvClearAll.disableMultiTap()
            if (list.size > 0) {
                clearNotification()
            }

        }

        mBinding.rvNotificationList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (!recyclerView.canScrollVertically(1)) {
                    if (dy > 0) { // Check if scrolling down
                        offset += 20
                        isPagination = true
                        mBinding.ivPaginationLoader.visibility = View.VISIBLE
                        if (context != null) {
                            if (isInternetAvailable(requireContext())) {
                                authViewModel.notificationList(
                                    NotificationRequest(
                                        offset = offset,
                                        limit = 20
                                    )
                                )
                            } else {
                                Utils.showToast("Please check your internet connection.")
                            }
                        }
                    }
                }
            }

        })
        /*  mBinding.nestedScroll.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, _, scrollY, _, _ ->
              if (scrollY == v.getChildAt(0).measuredHeight - v.measuredHeight) {
                  offset += 10
                  authViewModel.notificationList(
                      NotificationRequest(
                          offset = offset,
                          limit = 15
                      )
                  )
              }
          })*/

    }

    private fun callApiForNotificationList() {
        if (context != null) {
            if (isInternetAvailable(requireActivity())) {
                lifecycleScope.launch(Dispatchers.IO) {
                    authViewModel.notificationList(
                        NotificationRequest(
                            offset = 0,
                            limit = 20
                        )
                    )
                }

            } else {
                Utils.showToast("Please check your internet connection.")
                mBinding.refresh.isRefreshing = false
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvents(event: NotificationListener?) {
        if (event?.text == "totalNotificationSuccess") {
            if (context != null) {
                if (isInternetAvailable(requireContext())) {
                    getRequestCount()
                } else {
                    Utils.showToast("Please check your internet connection.")
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged", "SuspiciousIndentation", "SetTextI18n")
    private fun notificationObserver() {
        authViewModel.notificationListResponse.observe(viewLifecycleOwner) { response ->
            mBinding.animationView.visibility = View.GONE
            mBinding.ivPaginationLoader.visibility = View.GONE
            mBinding.refresh.isRefreshing = false
            dialogProgress?.dismiss()
            if (response != null) {
                if (response.success) {
                    if (!response.data.isNullOrEmpty()) {
                        mBinding.tvClearAll.isEnabled = true
                        Log.e("responseData", response.data.toString())
                        mBinding.TvNoData.visibility = View.GONE
                        if (refreshItems) {
                            list.clear()
                            refreshItems = false
                            mBinding.rvNotificationList.scrollToPosition(0)
                        }
                        list.addAll(response.data!!)
                        //   mBinding.rvNotificationList.reStoreState()
                        var previousDate = ""
                        list.sortByDescending { it.createdAt }
                        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                        val timeZone = TimeZone.getTimeZone("UTC")
                        val currentDateTime: ZonedDateTime =
                            ZonedDateTime.now(timeZone.toZoneId())
                        val formattedDateTime = currentDateTime.format(formatter)

                        /** Get the previous day's date*/
                        val previousDayDateTime: ZonedDateTime =
                            currentDateTime.minus(1, ChronoUnit.DAYS)

                        /** Format the previous day's date using the specified pattern*/
                        val formattedPreviousDay = previousDayDateTime.format(formatter)


                        list.forEach { list ->
                            Utils.convertDateAndTime(list.createdAt)
                            convertDateAndTime2(previousDate)

                            if (Utils.yearMonthDate == yearMonthDate && yearMonthDate != "") {
                                list.isSame = true
                            } else {
                                if (formattedDateTime == Utils.yearMonthDate) {
                                    list.createdDate = "Today"
                                } else if (formattedPreviousDay == Utils.yearMonthDate) {
                                    list.createdDate = "Yesterday"
                                } else {
                                    list.createdDate = Utils.finalDateFormat
                                }
                            }
                            previousDate = list.createdAt


                            when {
                                uniqueList.any { it._id == list._id } -> {
                                }

                                else -> {
                                    uniqueList.add(list)
                                }
                            }
                        }
                        uniqueList.sortByDescending { it.lastUpdateWithoutRead }
                        mBinding.rvNotificationList.reStoreState()
                        if (!::adapter.isInitialized) {
                            adapter =
                                NotificationAdapter(mBinding.rvNotificationList, uniqueList, this)
                            mBinding.rvNotificationList.adapter = adapter
                            adapter.notifyDataSetChanged()
                        } else {
                            mBinding.rvNotificationList.adapter = adapter
                            adapter.notifyDataSetChanged()
                        }
                        mBinding.rvNotificationList.visibility = View.VISIBLE
                    } else {
                        if (uniqueList.size == 0) {
                            mBinding.tvClearAll.isEnabled = false
                            if (!::adapter.isInitialized) {
                                adapter =
                                    NotificationAdapter(
                                        mBinding.rvNotificationList,
                                        uniqueList,
                                        this
                                    )
                                mBinding.rvNotificationList.adapter = adapter
                                adapter.notifyDataSetChanged()
                            } else {
                                mBinding.rvNotificationList.adapter = adapter
                                adapter.notifyDataSetChanged()
                            }
                            if (list.size == 0) {
                                mBinding.TvNoData.visibility = View.VISIBLE
                                mBinding.TvNoData.text =
                                    "You have not received any \nNotification yet"
                            } else {
                                mBinding.TvNoData.visibility = View.GONE
                            }
                        }
                    }


                }
            }
        }
        authViewModel.showLoading.observe(viewLifecycleOwner) {
            if (isPagination) {
                if (it) {
                    mBinding.ivPaginationLoader.visibility = View.VISIBLE
                } else {
                    mBinding.ivPaginationLoader.visibility = View.GONE
                    dialogProgress?.dismiss()
                }
            } else {
                if (!it) {
                    mBinding.animationView.visibility = View.GONE
                    dialogProgress?.dismiss()
                } else {
                    mBinding.animationView.visibility = View.VISIBLE
                }

            }
        }


    }


    private fun getRequestCount() {
        socket.emitMessage(
            "requestCount", "Bearer " + Prefs.init()
                .accessToken
        ) { _, _ ->
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setSocketListenON() {
        socket.listenOff("requestCountSuccess")
        socket.listenOff("error")
        /* socket.listenOn("totalNotificationSuccess") { args ->
                  CoroutineScope(Dispatchers.Main).launch {
                      Log.e("NotificationMessageIts", args[0].toString())
                      val obj = JSONObject(args[0].toString())
                      val dataObject = obj.getJSONObject("data")
                      val notificationsValue = dataObject.getInt("notifications")
                      val messageValue = dataObject.getInt("messages")
                  }
              }*/
        socket.listenOn("requestCountSuccess") { args ->
            CoroutineScope(Dispatchers.Main).launch {
                val obj = JSONObject(args[0].toString())
                val dataObject = obj.getJSONObject("data")
                val requestsCount = dataObject.getInt("requestsCount")
                if (requestsCount > 0) {
                    if (requestsCount > 99) {
                        mBinding.tvMessage.setText(R.string.more_than_99)
                    } else {
                        mBinding.tvMessage.text = requestsCount.toString()
                    }
                    mBinding.tvMessage.visibility = View.VISIBLE

                } else {
                    mBinding.tvMessage.visibility = View.GONE
                }
            }
        }
        socket.listenOn(SocketListenerConstant.ERROR) { args ->
        }
    }

    override fun onResume() {
        super.onResume()
        if (!socket.isConnected()) {
            socket.connect()
        } else {
            setSocketListenON()
            getRequestCount()
        }
    }

    private fun RecyclerView.reStoreState() {
        val recyclerViewState = this.layoutManager?.onSaveInstanceState()
        this.layoutManager?.onRestoreInstanceState(recyclerViewState)
    }

    private fun convertDateAndTime2(dateTimeString: String) {
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH)
            val outputFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss a", Locale.ENGLISH)

            /** Parse the input timestamp to Date object*/
            val data = inputFormat.parse(dateTimeString)
            val formattedDate = data?.let { outputFormat.format(it) }

            /** Convert UTC formatted date to local time*/
            val df = SimpleDateFormat("yyyy-MM-dd hh:mm:ss a", Locale.ENGLISH)
            df.timeZone = TimeZone.getTimeZone("UTC")
            val utcDate = formattedDate?.let { df.parse(it) }
            df.timeZone = TimeZone.getDefault()
            val localFormattedDate = utcDate?.let { df.format(it) }

            /** Get year information*/
            val yearMonth = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
            yearMonthDate = localFormattedDate.let {
                df.parse(it)
                    ?.let { it1 -> yearMonth.format(it1) }
            }!!


            /*   */
            /**Calculate time differences*//*
            val cal1: Calendar = Calendar.getInstance()
            val cal2: Calendar = Calendar.getInstance()
            cal2.time = df.parse(localFormattedDate)!!
            val msDiff: Long = cal1.timeInMillis - cal2.timeInMillis
            *//*Utils.daysDiff = TimeUnit.MILLISECONDS.toDays(msDiff)
            Utils.monthsDiff = (Utils.daysDiff / 30.44).toLong()
            Utils.seconds = TimeUnit.MILLISECONDS.toSeconds(msDiff)
            Utils.minutes = TimeUnit.MILLISECONDS.toMinutes(msDiff)*//*
            hours = TimeUnit.MILLISECONDS.toHours(msDiff)*/
        } catch (e: Exception) {
            e.printStackTrace()
            // Handle the parsing exception
        }
    }

    @SuppressLint("SetTextI18n")
    fun clearNotification() {
        if (context != null) {
            dialog = Dialog(requireActivity())
            dialog?.requestWindowFeature(Window.FEATURE_CONTENT_TRANSITIONS)
            dialog?.setCancelable(false)
            dialog?.setCanceledOnTouchOutside(true)
            Objects.requireNonNull(dialog?.window)?.setBackgroundDrawable(
                ColorDrawable(
                    Color.TRANSPARENT
                )
            )
            dialog?.setContentView(R.layout.discard_post)
            val logout = dialog?.findViewById(R.id.Logout) as TextView
            val clearAll = dialog?.findViewById(R.id.SureLoutout) as TextView
            /*    val ivClose = dialog?.findViewById(R.id.ivClose) as ImageView
        ivClose.visibility = View.GONE*/
            clearAll.text = "Clear All Notifications?"
            logout.text = "Clear"

            logout.setOnClickListener {
                if (isInternetAvailable(requireActivity())) {
                    allClear = true
                    lifecycleScope.launch(Dispatchers.IO) {
                        authViewModel.deleteNotification(
                            DeleteNotification(
                                notification = null
                            )
                        )
                    }

                    deleteNotificationObserver()
                } else {
                    Utils.showToast("Please check your internet connection.")
                }
                dialog?.dismiss()
            }
            dialog?.show()
        }
    }

    override fun onDeleteClick(position: Int, data: NotificationListData) {
        val notificationId = data._id
        Log.e("adapterSize", adapter.list.size.toString())
        if (adapter.list.size == 1) {
            allClear = true
            list.clear()
            uniqueList.clear()
            callApiForNotificationList()
        }
        if (context != null) {
            if (isInternetAvailable(requireActivity())) {
                authViewModel.deleteNotification(
                    DeleteNotification(
                        notification = notificationId
                    )
                )
                deleteNotificationObserver()
            } else {
                Utils.showToast("Please check your internet connection.")
            }
        }
    }

    override fun onClickUserNotification(position: Int, data: NotificationListData) {

        findNavController().navigate(R.id.walletFragment)
    }

    override fun onClickProfile(position: Int, data: NotificationListData) {
        if (data.sender[0]._id == Prefs.init().userId) {
            findNavController().navigate(R.id.profileFragment)
        } else {
            bundle.putString("mentionUser", data.sender[0]._id)
            findNavController().navigate(R.id.otherProfileFragment, bundle)
        }
    }

    override fun onClickLikePost(position: Int, data: NotificationListData) {
        bundle.putInt("position", 0)
        bundle.putString("dataList", data.post?._id)
        findNavController().navigate(
            R.id.show_profile_post_fragment,
            bundle
        )
    }

    override fun onClickAccept(position: Int, data: NotificationListData) {
        bundle.putString("requestReceive", "requestReceive")
        //   findNavController().navigate(R.id.requestSentAndReceiveFragment , bundle)
        if (isInternetAvailable(requireActivity())) {
            viewModel.acceptReject(AcceptRejectRequest(status = "accept", id = data.sender[0]._id))
        } else {
            Utils.showToast("Please check your internet connection.")
        }
    }

    override fun onClickAdminNotification(position: Int, data: NotificationListData) {
        Log.e(" onClickAdminNotification", " onClickAdminNotification")
        adminNotificationPopUp(data.message)
    }

    private fun adminNotificationPopUp(message: String) {
        dialogs = Dialog(requireActivity())
        dialogs?.requestWindowFeature(Window.FEATURE_CONTENT_TRANSITIONS)
        dialogs?.setCancelable(false)
        Objects.requireNonNull(dialogs?.window)?.setBackgroundDrawable(
            ColorDrawable(
                Color.TRANSPARENT
            )
        )
        dialogs?.setContentView(R.layout.admin_notification)

        val logout = dialogs?.findViewById(R.id.Logout) as TextView
        val notificationMessage = dialogs?.findViewById(R.id.notificationMessage) as TextView
        notificationMessage.text = message
        logout.setOnClickListener {
            dialogs?.dismiss()
        }
        dialogs?.show()
    }

    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    private fun deleteNotificationObserver() {
        authViewModel.deleteNotificationResponse.observe(viewLifecycleOwner) { response ->
            if (response != null) {
                if (response.success) {
                    if (allClear) {
                        list.clear()
                        uniqueList.clear()
                        callApiForNotificationList()
                        allClear = false
                        adapter.notifyDataSetChanged()
                    }
                }
            }
        }
        authViewModel.showLoading.observe(viewLifecycleOwner) {
            when {
                isPagination -> {
                }

                else -> {
                    if (it) {
                        //showProgressDialog()
                        mBinding.animationView.visibility = View.VISIBLE
                    } else {

                        mBinding.animationView.visibility = View.GONE
                    }
                }
            }
        }

        authViewModel.showMessage.observe(viewLifecycleOwner) {

        }
    }

    override fun onStart() {
        super.onStart()
        org.greenrobot.eventbus.EventBus.getDefault().register(this)
        // initializePlayer()
    }

    override fun onStop() {
        super.onStop()
        org.greenrobot.eventbus.EventBus.getDefault().unregister(this)
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvents(event: OnClickNotification?) {
        Log.e("OnClickNotification", "OnClickNotification")
        if (event?.text == "OnClickNotification") {
            mBinding.clNotificationList.hideTheSoftKeyboard()
            if (isInternetAvailable(requireContext())) {
                list.clear()
                uniqueList.clear()
                mBinding.animationView.visibility = View.VISIBLE
                callApiForNotificationList()
                getRequestCount()
            } else {
                mBinding.animationView.visibility = View.GONE
                showToast(getString(R.string.no_internet_message))
            }

        }
    }

    @Subscribe
    fun onSocketConnect(event: isSocketConnected) {
        if (event.isConnected) {
            Log.e("onSocketConnect", "Connected")
            setSocketListenON()
            //  getRequestCount()

        } else {
            Log.e("onSocketConnect", "not Connected")

        }
    }

    private fun showProgressDialog() {
        dialogProgress = Dialog(requireActivity())
        dialogProgress?.requestWindowFeature(Window.FEATURE_CONTENT_TRANSITIONS)
        dialogProgress?.setCancelable(false)
        Objects.requireNonNull(dialogProgress?.window)?.setBackgroundDrawable(
            ColorDrawable(
                Color.TRANSPARENT
            )
        )
        dialogProgress?.setContentView(R.layout.progressbar)
        dialogProgress?.show()
    }
}