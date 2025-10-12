package com.wedo8020.app.mainui.home.chatAndNotification.chat

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.wedo8020.app.MainApplication
import com.wedo8020.app.R
import com.wedo8020.app.databinding.FragmentMessageUserListBinding
import com.wedo8020.app.eventBus.isSocketConnected
import com.wedo8020.app.mainui.base.BaseFragment
import com.wedo8020.app.mainui.home.chatAndNotification.adapter.MessageListAdapter
import com.wedo8020.app.mainui.home.chatAndNotification.chatViewModel.ChatViewModel
import com.wedo8020.app.mainui.home.postFragment.PostViewModel
import com.wedo8020.app.request.ChatList
import com.wedo8020.app.request.UserBlockedRequest
import com.wedo8020.app.response.MessageListData
import com.wedo8020.app.utils.EventObserver
import com.wedo8020.app.utils.LastMessageUpdate
import com.wedo8020.app.utils.Prefs
import com.wedo8020.app.utils.UnsentMessageUpdate
import com.wedo8020.app.utils.Utils
import com.wedo8020.app.utils.Utils.loadImageAsync
import com.wedo8020.app.utils.Utils.loadImageAsyncMessage
import com.wedo8020.zelpyy.SocketProvider.SocketIOImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import java.util.Objects

class
MessageUserListFragment : BaseFragment(), MessageListAdapter.OnClickListener {
    private lateinit var mBinding: FragmentMessageUserListBinding
    private lateinit var socket: SocketIOImpl
    private lateinit var adapter: MessageListAdapter
    private val authViewModel by viewModels<PostViewModel>()
    private val chatViewModel by viewModels<ChatViewModel>()
    private var list: ArrayList<MessageListData> = ArrayList()
    private var uniqueList: ArrayList<MessageListData> = ArrayList()
    var handlers = Handler(Looper.getMainLooper())
    var dialog: Dialog? = null
    var dialogs: Dialog? = null
    var bundle = Bundle()
    var offset = 0
    var search = ""
    private var newMessage = false
    private var isClear = false
    private var refreshItems = false
    private var pagination = false
    private var updatedIndex = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Handler(Looper.myLooper()!!).post {
            getChatHistory("")
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        mBinding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.fragment_message_user_list,
            container,
            false
        )
        dialog = Dialog(requireActivity())
        dialog?.requestWindowFeature(Window.FEATURE_CONTENT_TRANSITIONS)
        socket = MainApplication.get().socket
        showNavBottom()
        chatListObserver()
        hideKeyboardOnOutSideClick(mBinding.clMessageList)
        adapter = MessageListAdapter(uniqueList, this@MessageUserListFragment)
        mBinding.rvMessageList.adapter = adapter
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /** Refresh the Data */
        /*    mBinding.refresh.setOnRefreshListener {
                if (isInternetAvailable(requireContext())) {
                    mBinding.rvMessageList.visibility = View.GONE
                    list.clear()
                    uniqueList.clear()
                    refreshItems = true
                    offset = 0
                    mBinding.animationView.visibility = View.GONE
                    mBinding.TvNoData.visibility = View.GONE
                    getChatHistory(search)
                *//*    Handler(Looper.myLooper()!!).postDelayed({
                    getChatHistory(search)
                },500)*//*
            } else {
                showToast(getString(R.string.no_internet_message))
            }
            mBinding.refresh.isRefreshing = false
        }*/

        mBinding.rvMessageList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (!recyclerView.canScrollVertically(1)) {
                    if (dy > 0) { // Check if scrolling down
                        offset += 20
                        pagination = true
                        if (isInternetAvailable(requireContext())) {
                            chatViewModel.chatList(
                                ChatList(
                                    offset = offset,
                                    search = search,
                                    limit = 20
                                )
                            )

                        } else {
                            Utils.showToast("Please check your internet connection.")
                        }
                    }
                }
            }

        })

        mBinding.searchEt.filters = Utils.ignoreFirstWhiteSpace(mBinding.searchEt, 60)
        mBinding.searchEt.apply {
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                override fun afterTextChanged(p0: Editable?) {
                    offset = 0
                    if (search != p0.toString()) {
                        list.clear()
                        uniqueList.clear()
                        mBinding.rvMessageList.visibility = View.GONE
                        search = p0.toString()
                        if (Utils.isInternetAvailables(requireActivity())) {
                            handlers.removeCallbacksAndMessages(null)
                            handlers.postDelayed({
                                Handler(Looper.getMainLooper()).postDelayed({

                                    getChatHistory(search)
                                }, 0)
                            }, 400)
                        } else {
                            Utils.showToast("Please check your internet connection.")
                        }
                    }
                }

            })
        }
    }


    private fun getChatHistory(search: String) {
        chatViewModel.chatList(
            ChatList(
                offset = 0,
                search = search,
                limit = 20

            )
        )
    }

    private fun RecyclerView.reStoreState() {
        val recyclerViewState = this.layoutManager?.onSaveInstanceState()
        this.layoutManager?.onRestoreInstanceState(recyclerViewState)
    }


    override fun onClickChat(position: Int, data: MessageListData) {
        /*        mBinding.searchEt.text.clear()
                search = ""*/
        updatedIndex = position
        adapter.list?.get(position)?.noOfUnseenMessages = 0
        bundle.putString("userChatId", data._id)
        bundle.putString("otherUserName", data.otherUser?.username)
        bundle.putString("otherUserProfile", data.otherUser?.profile)
        data.otherUser?.blueTickEnable?.let { bundle.putBoolean("blueTick", it) }
        findNavController().navigate(
            R.id.showChatFragment,
            bundle
        )
    }

    override fun onCLickBlock(position: Int, data: MessageListData) {
        data.otherUser?.profile?.let { userBlocked(data._id, it, data.otherUser.name) }

    }

    override fun onClickDelete(position: Int, data: MessageListData) {
        data.otherUser?.profile?.let { userDelete(data._id, it, data.otherUser.name) }

    }

    override fun onClickClear(position: Int, data: MessageListData) {
        mBinding.animationView.visibility = View.VISIBLE
        val obj = Utils.toJsonObject(
            "token" to "Bearer " + Prefs.init()
                .accessToken,
            "otherUser" to data._id,
        )
        if (isInternetAvailable(requireContext())) {
            socket.emitMessage("clearChat", obj) { _, _ ->
            }
        } else {
            Utils.showToast("Please check your internet connection.")
        }


    }

    override fun onClickPin(position: Int, data: MessageListData) {
        mBinding.animationView.visibility = View.VISIBLE
        val obj = Utils.toJsonObject(
            "token" to "Bearer " + Prefs.init()
                .accessToken,
            "otherUser" to data._id,
        )
        if (isInternetAvailable(requireContext())) {
            socket.emitMessage("pinChat", obj) { _, _ ->
            }

        } else {
            Utils.showToast("Please check your internet connection.")
        }

    }

    @SuppressLint("SetTextI18n")
    private fun userBlocked(id: String, image: String, name: String) {
        dialog?.setCancelable(false)
        Objects.requireNonNull(dialog?.window)?.setBackgroundDrawable(
            ColorDrawable(
                Color.TRANSPARENT
            )
        )
        dialog?.setContentView(R.layout.discard_post)
        dialog?.setCanceledOnTouchOutside(true)
        val logout = dialog?.findViewById(R.id.Logout) as TextView
        val mainText = dialog?.findViewById(R.id.SureLoutout) as TextView
        val ivAlert = dialog?.findViewById(R.id.ivAlert) as ImageView
        val ivUserProfileImage = dialog?.findViewById(R.id.ivUserProfileImage) as ImageView
        // val ivClose = dialog?.findViewById(R.id.ivClose) as ImageView
        //ivClose.visibility=View.GONE
        ivAlert.visibility = View.GONE
        ivUserProfileImage.visibility = View.VISIBLE
        mainText.text = "Are you sure you want to Block \n \"$name\"?"
        logout.text = "Block"
        CoroutineScope(Dispatchers.Main).launch {
            ivUserProfileImage.loadImageAsyncMessage(image)
        }
        /*  ivClose.setOnClickListener {
              dialog?.dismiss()
          }*/
        logout.setOnClickListener {
            if (isInternetAvailable(requireActivity())) {
                authViewModel.userBlocked(UserBlockedRequest(id))
                blockObserver()
            } else {

                Utils.showToast("Please check your internet connection.")
            }
            dialog?.dismiss()
        }
        dialog?.show()
    }

    @SuppressLint("SetTextI18n")
    private fun userDelete(id: String, image: String, name: String) {
        dialog?.setCancelable(false)
        Objects.requireNonNull(dialog?.window)?.setBackgroundDrawable(
            ColorDrawable(
                Color.TRANSPARENT
            )
        )
        dialog?.setContentView(R.layout.discard_post)
        val logout = dialog?.findViewById(R.id.Logout) as TextView
        val mainText = dialog?.findViewById(R.id.SureLoutout) as TextView
        val ivAlert = dialog?.findViewById(R.id.ivAlert) as ImageView
        val ivUserProfileImage = dialog?.findViewById(R.id.ivUserProfileImage) as ImageView

        ivAlert.visibility = View.GONE
        dialog?.setCanceledOnTouchOutside(true)
        ivUserProfileImage.visibility = View.VISIBLE
        mainText.text = "Are you sure you want to delete chat ?"
        logout.text = "Delete"
        CoroutineScope(Dispatchers.Main).launch {
            ivUserProfileImage.loadImageAsync(image)
        }

        logout.setOnClickListener {
            if (isInternetAvailable(requireActivity())) {
                mBinding.animationView.visibility = View.VISIBLE
                val obj = Utils.toJsonObject(
                    "token" to "Bearer " + Prefs.init()
                        .accessToken,
                    "otherUser" to id,
                )
                socket.emitMessage("deleteChat", obj) { _, _ ->
                }
                blockObserver()
            } else {

                Utils.showToast("Please check your internet connection.")
            }
            dialog?.dismiss()
        }
        dialog?.show()
    }

    private fun blockObserver() {
        authViewModel.baseResponse.observe(requireActivity()) { response ->
            dialog?.dismiss()
            if (response != null) {
                if (response.success) {
                    dialog?.dismiss()
                    list.clear()
                    uniqueList.clear()
                    getChatHistory("")
                }
            }
        }
        authViewModel.showMessage.observe(requireActivity()) {
            dialog?.dismiss()
            //  showToast(response)
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

    }


    /*    @SuppressLint("SetTextI18n")
        @Subscribe(threadMode = ThreadMode.MAIN)
        fun onMessageEvents(event: NotificationListener?) {
            if(event?.text=="totalNotificationSuccess"){

            }
        }*/

    /*    @Subscribe()
        fun showInAppNotification(events: InAppNotification) {
            *//* list.clear()
         uniqueList.clear()
         newMessage=true
         val dataObject = events.jsonObject
         val notificationDetail = dataObject.getJSONObject("notificationDetail")
         val type = notificationDetail.getString("type")
         if(type==NotificationType.MESSAGE){
             getChatHistory("")
         }*//*

    }*/


    @SuppressLint("NotifyDataSetChanged")
    private fun chatListObserver() {
        chatViewModel.chatListResponse.observe(viewLifecycleOwner, EventObserver { response ->
            dialog?.dismiss()
            dialogs?.dismiss()
            mBinding.animationView.visibility = View.GONE
            mBinding.rvMessageList.visibility = View.VISIBLE
            if (response.success) {
                if (!response.data.isNullOrEmpty()) {
                    dialog?.dismiss()
                    mBinding.TvNoData.visibility = View.GONE
                    mBinding.rvMessageList.visibility = View.VISIBLE
                    response.data?.let {
                       // list.clear()
                        list.addAll(it)
                    }
                    if (!list.isNullOrEmpty()) {
                        if (updatedIndex != -1 && updatedIndex < list.size) {
                            if (list[updatedIndex].lastMessage != null) {
                                isClear = false
                            }
                        }
                    }
//                        list = list.distinctBy { it._id } as ArrayList<MessageListData>
                    /*  list.sortByDescending { it.lastMessage.createdAt }
                      list.sortByDescending { it.pinTime }*/
                  /*  if (adapter.list.isNullOrEmpty()) {
                        adapter.setNewItem(list)
                    } else {
                        adapter.addNewItem(list)
                    }*/
                    list.forEach { list ->
                        when {
                            uniqueList.any { it._id == list._id } -> {
                            }

                            else -> {
                                uniqueList.add(list)
                            }
                        }
                    }


                    mBinding.rvMessageList.reStoreState()
                    adapter = MessageListAdapter(uniqueList, this@MessageUserListFragment)
                    mBinding.rvMessageList.adapter = adapter
                    adapter.notifyDataSetChanged()

                    if (adapter.list?.size == 0) {
                        mBinding.TvNoData.visibility = View.VISIBLE
                        mBinding.TvNoData.setText(R.string.no_result_found)
                    } else {
                        mBinding.TvNoData.visibility = View.GONE
                    }
                } else {
                    /* mBinding.rvMessageList.reStoreState()
                     adapter = MessageListAdapter(uniqueList, this@MessageUserListFragment)
                     mBinding.rvMessageList.adapter = adapter*/
//                    adapter.list?.clear()
//                        adapter.notifyDataSetChanged()
                    if (adapter.list?.size == 0) {
                        mBinding.TvNoData.visibility = View.VISIBLE
                        mBinding.TvNoData.setText(R.string.no_result_found)
                    } else {
                        mBinding.TvNoData.visibility = View.GONE
                    }
                }
            }
            mBinding.rvMessageList.visibility = View.VISIBLE
            //  mBinding.refresh.isRefreshing = false


        })


        chatViewModel.showLoading.observe(viewLifecycleOwner) {
            when {
                refreshItems -> {

                }

                pagination -> {
                    if (!it) {
                        mBinding.paginationLoader.visibility = View.GONE
                    } else {
                        mBinding.paginationLoader.visibility = View.VISIBLE
                    }
                }

                else -> {
                    if (newMessage) {
                        newMessage = false
                    } else {
                        if (!it) {
                            //  showProgressDialog()
                            mBinding.animationView.visibility = View.GONE
                        } else {
                            dialogs?.dismiss()
                            mBinding.animationView.visibility = View.VISIBLE
                        }
                    }
                }
            }


        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
        if (!socket.isConnected()) {
            socket.connect()
        } else {
            setSocketListenON()
            /*  list.clear()
              uniqueList.clear()
              getChatHistory("")
            */
        }
        if (isClear) {
            adapter.list?.clear()
            getChatHistory("")
        } else {
            mBinding.rvMessageList.reStoreState()
            /*   if (!::adapter.isInitialized){

                   adapter.notifyDataSetChanged()
               }else{
                   mBinding.rvMessageList.adapter = adapter
                   adapter.notifyDataSetChanged()
               }*/
        }


    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setSocketListenON() {
        socket.listenOff("chatHistorySuccess")
        socket.listenOff("deleteChatSuccess")
        socket.listenOff("clearChatSuccess")
        socket.listenOff("newMessageSuccess")
        socket.listenOff("pinChatSuccess")
        socket.listenOff("error")
        socket.listenOn("error") {
            CoroutineScope(Dispatchers.Main).launch {
            }
        }
        socket.listenOn("newMessageSuccess") { args ->
            CoroutineScope(Dispatchers.Main).launch {
                val obj = JSONObject(args[0].toString())
                mBinding.animationView.visibility = View.GONE
                val gson = Gson()
                val listType = object : TypeToken<MessageListData>() {}.type
                val newList = gson.fromJson<MessageListData>(
                    obj.getJSONObject("data").toString(),
                    listType
                )

                /** Check if an item with the same _id already exists in the list*/
                val existingItemIndex = uniqueList.indexOfFirst { it._id == newList._id }
                if (existingItemIndex != -1) {
                    adapter.list?.get(existingItemIndex)?.noOfUnseenMessages =
                        newList.noOfUnseenMessages
                    if (newList.lastMessage != null) {
                        if (adapter.list?.get(existingItemIndex)?.lastMessage == null) {
                            adapter.list?.get(existingItemIndex)?.lastMessage = newList.lastMessage
                        } else {
                            if (newList.lastMessage != null) {
                                uniqueList[existingItemIndex].lastMessage?.type =
                                    newList.lastMessage?.type.toString()

                                if (!newList.lastMessage?.message.isNullOrEmpty()) {
                                    uniqueList[existingItemIndex].lastMessage?.message =
                                        newList.lastMessage?.message.toString()
                                }
                                uniqueList[existingItemIndex].lastMessage?.createdAt =
                                    newList.lastMessage?.createdAt.toString()


                                uniqueList[existingItemIndex].lastMessage?.isMessageHide =
                                    if (newList.lastMessage?.isMessageHide == true) {
                                        true
                                    } else {
                                        false
                                    }
                            } else {
                                adapter.list?.get(existingItemIndex)?.lastMessage =
                                    newList.lastMessage

                            }
                        }
                        adapter.notifyItemChanged(existingItemIndex)
                    }
                } else {
                    /** Item with _id not found, add the newList to the list*/
                    if (isInternetAvailable(requireContext())) {
                        list.clear()
                        uniqueList.clear()
                        getChatHistory("")
                    } else {
                        Utils.showToast(getString(R.string.no_internet_message))
                    }
                }

            }

        }

        socket.listenOn("deleteChatSuccess") {
            CoroutineScope(Dispatchers.Main).launch {
                if (isInternetAvailable(requireContext())) {
                    list.clear()
                    uniqueList.clear()
                    getChatHistory("")
                } else {
                    Utils.showToast("Please check your internet connection.")
                }
            }
        }
        socket.listenOn("clearChatSuccess") {
            CoroutineScope(Dispatchers.Main).launch {
                if (isInternetAvailable(requireContext())) {
                    isClear = true
                    list.clear()
                    uniqueList.clear()
                    getChatHistory("")
                } else {
                    Utils.showToast("Please check your internet connection.")
                }
            }
        }
        socket.listenOn("pinChatSuccess") {
            CoroutineScope(Dispatchers.Main).launch {
                if (isInternetAvailable(requireContext())) {
                    list.clear()
                    uniqueList.clear()
                    getChatHistory("")
                } else {
                    Utils.showToast("Please check your internet connection.")
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onLastMessageUpdate(event: LastMessageUpdate?) {
        adapter.list?.get(updatedIndex)?.lastMessage?.message = event?.data?.message.toString()
        adapter.list?.get(updatedIndex)?.lastMessage?.type = event?.data?.type.toString()
        adapter.list?.get(updatedIndex)?.lastMessage?.createdAt = event?.data?.createdAt.toString()
        adapter.notifyItemChanged(updatedIndex)


    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUnsentMessageUpdate(event: UnsentMessageUpdate?) {
        adapter.list?.get(updatedIndex)?.lastMessage?.message = event?.message.toString()
        adapter.list?.get(updatedIndex)?.lastMessage?.type = event?.type.toString()
        adapter.list?.get(updatedIndex)?.lastMessage?.createdAt = event?.time.toString()
        adapter.notifyItemChanged(updatedIndex)

    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun isSocketConnected(event: isSocketConnected) {
        if (event.isConnected) {
            setSocketListenON()
            //    getNotificationCount()
        }
    }
}