package com.wedo8020.app.mainui.home.chatAndNotification.chat

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
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
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.AbsListView
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.aspose.cells.Workbook
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.wedo8020.app.MainApplication
import com.wedo8020.app.R
import com.wedo8020.app.databinding.FragmentShowChatBinding
import com.wedo8020.app.enumClasses.ChatType
import com.wedo8020.app.eventBus.isSocketConnected
import com.wedo8020.app.mainui.activities.MainActivity
import com.wedo8020.app.mainui.base.BaseFragment
import com.wedo8020.app.mainui.home.chatAndNotification.adapter.MessageAdapter
import com.wedo8020.app.mainui.home.chatAndNotification.chatViewModel.ChatViewModel
import com.wedo8020.app.mainui.home.chatAndNotification.chatViewModel.EmojiReactionDbViewModelFactory
import com.wedo8020.app.mainui.home.chatAndNotification.chatViewModel.ReactionViewModel
import com.wedo8020.app.mainui.home.chatAndNotification.roomDBReaction.EmoJiReactionDbCreation
import com.wedo8020.app.mainui.home.chatAndNotification.roomDBReaction.EmoJiReactionRepo
import com.wedo8020.app.request.ChatHistoryRequest
import com.wedo8020.app.request.UserDecryptFile
import com.wedo8020.app.response.ReactionData
import com.wedo8020.app.response.UnSendData
import com.wedo8020.app.response.UserMessageData
import com.wedo8020.app.utils.EventObserver
import com.wedo8020.app.utils.ImageUploaded
import com.wedo8020.app.utils.LastMessageUpdate
import com.wedo8020.app.utils.NewEmoJi
import com.wedo8020.app.utils.Prefs
import com.wedo8020.app.utils.UnsentMessageUpdate
import com.wedo8020.app.utils.Utils
import com.wedo8020.app.utils.Utils.convertDateAndTime
import com.wedo8020.app.utils.Utils.disableMultiTap
import com.wedo8020.app.utils.Utils.loadImageAsyncMessage
import com.wedo8020.zelpyy.SocketProvider.SocketIOImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.Objects
import java.util.TimeZone
import java.util.concurrent.TimeUnit


class ShowChatFragment : BaseFragment(), MessageAdapter.OnClickListener {
    private lateinit var mBinding: FragmentShowChatBinding
    private val chatViewModel by viewModels<ChatViewModel>()
    private lateinit var viewModel: ReactionViewModel
    private lateinit var repository: EmoJiReactionRepo
    private lateinit var database: EmoJiReactionDbCreation
    private lateinit var factory: EmojiReactionDbViewModelFactory
    private lateinit var socket: SocketIOImpl
    private var list: ArrayList<UserMessageData> = ArrayList()
    private var reactionList = ReactionData()
    private var unSendData = UnSendData()
    private var reverseList: ArrayList<UserMessageData> = ArrayList()
    private var newMessageList: ArrayList<UserMessageData> = ArrayList()
    private var lastMessage: UserMessageData? = null
    private var userChatId = ""
    private var otherUserName = ""
    private var otherUserProfile = ""
    private var blueTick = false
    private var isMessageOn = false
    private var isPagination = false
    private var isApiSuccess = false
    private var offset = 0
    private var yearMonthDate = ""
    private var messageDays: Long = 0L
    private var amPms = ""
    val bundle = Bundle()
    private var imageCompressed: Uri? = Uri.EMPTY
    var dialog: Dialog? = null
    var dialogs: Dialog? = null
    private var finalDateFormats = ""
    private var messageType = ""
    private var reply = false
    private var replyID: String? = null
    private var galleryPermissionClick = 0
    private var finalEmoji = ""
    private var newMessage = false
    private var isNewMessageSeen = false
    private val childAdapter = MessageAdapter(getLayoutsMap(), this@ShowChatFragment)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Handler(Looper.myLooper()!!).post {
            getChatHistory()
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        if (!::mBinding.isInitialized) {
            mBinding =
                DataBindingUtil.inflate(
                    layoutInflater,
                    R.layout.fragment_show_chat,
                    container,
                    false
                )
        }
        chatHistoryObserver()
        dialog = Dialog(requireActivity())
        dialog?.requestWindowFeature(Window.FEATURE_CONTENT_TRANSITIONS)
        database = EmoJiReactionDbCreation(requireContext())
        repository = EmoJiReactionRepo(database)
        factory = EmojiReactionDbViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[ReactionViewModel::class.java]
        socket = MainApplication.get().socket
        arguments?.let {
            userChatId = arguments?.getString("userChatId").toString()
            otherUserName = it.getString("otherUserName").toString()
            otherUserProfile = it.getString("otherUserProfile").toString()
            blueTick = it.getBoolean("blueTick")
            if (blueTick) {
                mBinding.ivBlueTick.visibility = View.VISIBLE
            } else {
                mBinding.ivBlueTick.visibility = View.GONE
            }
        }

        CoroutineScope(Dispatchers.Main).launch {
            mBinding.ivUserProfileImage.loadImageAsyncMessage(otherUserProfile)
            mBinding.tvOtherUserName.text = otherUserName
        }
        hideNavBottom()
        return mBinding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getRowsCount()

        /** Set the same callback for your mBinding.IvBack.setOnClickListener*/
        val customBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val navController = findNavController()
                val previousDestinationId = navController.previousBackStackEntry?.destination?.id

                if (previousDestinationId == R.id.messageAndNotificationsFragment) {
                    navController.navigateUp()
                } else {
                    findNavController().navigate(R.id.messageAndNotificationsFragment)
                    val ac = activity as MainActivity
                    ac.messagingTabClick()
                }
                if (lastMessage != null) {
                    Handler(Looper.myLooper()!!).post {
                        org.greenrobot.eventbus.EventBus.getDefault()
                            .post(LastMessageUpdate(lastMessage!!))
                    }
                } else {
                    if (!childAdapter.list.isNullOrEmpty()) {
                        Handler(Looper.myLooper()!!).post {
                            org.greenrobot.eventbus.EventBus.getDefault()
                                .post(
                                    UnsentMessageUpdate(
                                        childAdapter.list.last().message,
                                        childAdapter.list.last().createdAt,
                                        childAdapter.list.last().type
                                    )
                                )
                        }
                    }

                }
            }
        }

        /** Set the custom callback for the system back press*/
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            customBackPressedCallback
        )


        mBinding.IvBack.setOnClickListener {
            newMessage = false
            isNewMessageSeen = false
            customBackPressedCallback.handleOnBackPressed()


        }


        mBinding.rvMessageChat.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (!recyclerView.canScrollVertically(1) && dy > 0) {
                    mBinding.ivScrollDown.visibility = View.GONE
                } else if (!recyclerView.canScrollVertically(-1) && dy < 0) {
                    offset += 30
                    isPagination = true
                    if (context != null) {
                        if (isInternetAvailable(requireContext())) {
                            chatViewModel.chatHistory(
                                ChatHistoryRequest(
                                    offset = offset,
                                    limit = 30,
                                    user = userChatId
                                )
                            )
                        } else {
                            Utils.showToast("Please check your internet connection.")
                        }
                    }
                    mBinding.ivScrollDown.visibility = View.VISIBLE
                    mBinding.whiteLotteAnimation.visibility = View.VISIBLE
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                when (newState) {
                    AbsListView.OnScrollListener.SCROLL_STATE_FLING -> {
                    }

                    AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL -> {
                    }

                    AbsListView.OnScrollListener.SCROLL_STATE_IDLE -> {
                    }

                    else -> {
                    }
                }

            }
        })


        mBinding.edMessage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                if (p0?.trim()?.length!! > 0) {
                    mBinding.ivSendButton.visibility = View.VISIBLE
                } else {
                    mBinding.ivSendButton.visibility = View.GONE
                    mBinding.ivImageButton.visibility = View.VISIBLE
                }

            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })




        mBinding.ivUserProfileImage.setOnClickListener {
            bundle.putString("mentionUser", userChatId)
            if (userChatId == Prefs.init().userId) {
                findNavController().navigate(R.id.profileFragment)
            } else {
                findNavController().navigate(R.id.otherProfileFragment, bundle)
            }
        }

        mBinding.edMessage.setOnClickListener {
            Log.e("edMessage", "edMessage is shown")
            Handler(Looper.getMainLooper()).postDelayed(
                {
                    mBinding.rvMessageChat.scrollToPosition(reverseList.size - 1)
                }, 100
            )
        }

        mBinding.ivScrollDown.setOnClickListener {
            mBinding.rvMessageChat.scrollToPosition(reverseList.size - 1)
            mBinding.ivScrollDown.visibility = View.GONE
        }




        mBinding.ivSendButton.setOnClickListener {
            mBinding.ivImageButton.disableMultiTap()
            mBinding.ivSendButton.disableSendBtn()
            if (context != null) {
                if (isInternetAvailable(requireContext())) {
                    if (mBinding.edMessage.text.trim().toString() != "") {
                        messageType = "text"
                        if (replyID.isNullOrEmpty()) {
                            sendMessage(
                                userChatId,
                                mBinding.edMessage.text.trim().toString(),
                                messageType,
                                null
                            )
                            mBinding.ivSendButton.visibility = View.GONE
                            mBinding.edMessage.text.clear()
                        } else {
                            sendMessage(
                                userChatId,
                                mBinding.edMessage.text.trim().toString(),
                                messageType,
                                replyID.toString()
                            )
                        }
                        mBinding.tvReplyMessage.visibility = View.GONE
                        mBinding.close.visibility = View.GONE
                    }
                } else {

                    Toast.makeText(
                        requireContext(),
                        "Please check the internet connection",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        mBinding.close.setOnClickListener {
            mBinding.tvReplyMessage.visibility = View.GONE
            mBinding.close.visibility = View.GONE
            replyID = null
        }
        mBinding.ivImageButton.setOnClickListener {
            if (context != null) {
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

                                val i = Intent(
                                    Intent.ACTION_PICK,
                                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                                )
                                startActivityForResult(i, 1)
                            }
                        }
                }
            }
        }
    }

    @SuppressLint("SuspiciousIndentation")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            lifecycleScope.launch(Dispatchers.IO) {

                imageCompressed = async {
                    data?.data?.let { compressImagesWithGlide(requireContext(), it) }
                }.await()
                if (imageCompressed != null) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        bundle.putString("chatImageUri", imageCompressed.toString())
                        bundle.putString("chatUserID", userChatId)
                        findNavController().navigate(
                            R.id.action_showChatFragment_to_selectedImageAndVideoFragment, bundle
                        )
                    }
                }


            }
//            bundle.putString("chatImageUri", data?.data.toString())
//            bundle.putString("chatUserID", userChatId)
//
//             if (data?.data != null) {
//                 findNavController().navigate(
//                     R.id.action_showChatFragment_to_selectedImageAndVideoFragment,
//                     bundle
//                 )
//             }

        }
    }

    private fun compressImagesWithGlide(context: Context, originalImageUri: Uri): Uri {
        var compressedImageUris: Uri = Uri.EMPTY
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


            val compressedImageFile =
                File.createTempFile("compressed_image", ".jpg", context.cacheDir)


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


    override fun onResume() {
        super.onResume()
        Prefs.init().messageSenderId = userChatId
        if (!socket.isConnected()) {
            socket.connect()
        } else {
            setSocketListenON()

        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun chatHistoryObserver() {
        chatViewModel.chatHistoryResponse.observe(viewLifecycleOwner, EventObserver { it ->
            mBinding.animationView.visibility = View.GONE
            mBinding.whiteLotteAnimation.visibility = View.GONE
            if (it.success) {
                it.data?.let {
                    if (!isNewMessageSeen) {
                        if (!it.history.isNullOrEmpty()) {
                            list = it.history!!
                            val showChatResponse = it.isMessageOn
                            if (showChatResponse) {
                                childAdapter.isMessageOn = true
                                isMessageOn = true
                                mBinding.messageSend.visibility = View.VISIBLE
                                mBinding.clReplyDisable.visibility = View.GONE
                                mBinding.ivImageButton.visibility = View.VISIBLE
                            } else {
                                childAdapter.isMessageOn = false
                                isMessageOn = false
                                mBinding.messageSend.visibility = View.INVISIBLE
                                mBinding.ivImageButton.visibility = View.GONE
                                mBinding.clReplyDisable.visibility = View.VISIBLE
                            }
                            list.reverse()
                            val item =
                                UserMessageData(
                                    "",
                                    "",
                                    "",
                                    false,
                                    "",
                                    null,
                                    null,
                                    null,
                                    false,
                                    "",
                                    "",
                                    "",
                                    "",
                                    ""
                                )
                            var previousDate = ""
                            val iterator = list.listIterator()
                            while (iterator.hasNext()) {
                                val currentItem = iterator.next()
                                //                    val nextItem = iterator.next()
                                convertDateAndTime(currentItem.createdAt)
                                convertDateAndTime2(previousDate)
                                if (Utils.yearMonthDate != yearMonthDate && yearMonthDate != "") {
                                    item.type = "date"
                                    if (Utils.hours <= 24L) {
                                        item.createdAt = "Today"
                                    }/* else if (Utils.hours in 25L..48L) {
                                        item.createdAt = "Yesterday"
                                    } */ else {
                                        item.createdAt = finalDateFormats
                                    }

                                    iterator.previous()
                                    iterator.add(item.copy()) // Adding a copy to avoid ConcurrentModificationException
                                    iterator.next()
                                }

                                previousDate = currentItem.createdAt


                            }
                            /*  list.forEach { list ->
                                  if (reverseList.any { it._id == list._id }) {
                                  } else {
                                      reverseList.add(0, list)
                                  }
                              }*/

                            reverseList =
                                reverseList.distinctBy { it1 -> it1._id } as ArrayList<UserMessageData>
                            reverseList.addAll(0, list)
                            //   reverseList= reverseList.distinctBy { it._id } as ArrayList<UserMessageData>
                            childAdapter.layoutId = getLayoutsMap()
                            childAdapter.list = reverseList
                            childAdapter.list.findLast { it1 -> it1.seenStatus == "seen" }?.showSeen =
                                true
                            mBinding.rvMessageChat.reStoreState()
                            mBinding.rvMessageChat.adapter = childAdapter
                            childAdapter.otherUserId = userChatId
                            childAdapter.notifyDataSetChanged()
                            isApiSuccess = true
                            mBinding.rvMessageChat.scrollToPosition(list.size - 1)

                        } else {
                            val showChatResponse = it.isMessageOn
                            isApiSuccess = true
                            if (showChatResponse) {
                                isMessageOn = true
                                mBinding.messageSend.visibility = View.VISIBLE
                                mBinding.clReplyDisable.visibility = View.GONE
                                mBinding.ivImageButton.visibility = View.VISIBLE
                            } else {
                                isMessageOn = false
                                mBinding.messageSend.visibility = View.INVISIBLE
                                mBinding.ivImageButton.visibility = View.GONE
                                mBinding.clReplyDisable.visibility = View.VISIBLE

                            }
                        }
                    }
                }
            }
        })

        chatViewModel.showLoading.observe(viewLifecycleOwner) {
            if (!isNewMessageSeen) {
                if (isPagination) {
                    if (!it) {
                        mBinding.whiteLotteAnimation.visibility = View.GONE
                    } else {
                        mBinding.whiteLotteAnimation.visibility = View.VISIBLE
                    }

                } else {
                    if (!it) {

                        mBinding.animationView.visibility = View.GONE
                        mBinding.whiteLotteAnimation.visibility = View.GONE
                    } else {
                        dialogs?.dismiss()
                        mBinding.animationView.visibility = View.VISIBLE
                    }
                }
            }


        }

        chatViewModel.showMessage.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty() || it.contains("body.").not()) {
                if (it.contains("User not found")) {
                    isMessageOn = true
                    mBinding.messageSend.visibility = View.VISIBLE
                    mBinding.clReplyDisable.visibility = View.GONE
                    mBinding.ivImageButton.visibility = View.VISIBLE
                }

            }
        }

    }

    @SuppressLint("NotifyDataSetChanged", "SuspiciousIndentation")
    private fun setSocketListenON() {
        //   socket.listenOff("chatSuccess")
        socket.listenOff("newMessageSuccess")
        socket.listenOff("reactMessageSuccess")
        socket.listenOff("unsendMessageSuccess")
        socket.listenOff("error")
        //   socket.listenOn("chatSuccess") {}


        socket.listenOn("error") { args ->
            CoroutineScope(Dispatchers.Main).launch {
                Log.e("emitsMessage", args[0].toString())
                val obj = JSONObject(args[0].toString())
                val message = obj.getString("message")
                Log.e("emitsMessage", message.toString())
                Utils.showToast(message)
            }
        }



        socket.listenOn("newMessageSuccess") { args ->
            CoroutineScope(Dispatchers.Main).launch {
                // mBinding.edMessage.text.clear()
                val obj = JSONObject(args[0].toString())
                mBinding.animationView.visibility = View.GONE
                replyID = ""
                childAdapter.otherUserId = userChatId
                val gson = Gson()
                val listType = object : TypeToken<UserMessageData>() {}.type
                val newList = gson.fromJson<UserMessageData>(
                    obj.getJSONObject("data").getJSONObject("lastMessage").toString(),
                    listType
                )
                lastMessage = newList
                val item =
                    UserMessageData(
                        "",
                        "",
                        "",
                        false,
                        "",
                        null,
                        null,
                        null,
                        false,
                        "",
                        "",
                        "",
                        "",
                        ""
                    )
                if (isApiSuccess) {
                    if (newList.sender == userChatId || newList.receiver == userChatId) {
                        val dateOnlyOFList = if (reverseList.isNotEmpty()) {
                            reverseList.last().createdAt.take(10)
                        } else {
                            "" // Provide a default value if the list is empty
                        }
                        val dateOnlyOFNewMessage = if (newList.createdAt.length >= 10) {
                            newList.createdAt.substring(0, 10)
                        } else {
                            newList.createdAt // Use the entire string if its length is less than 10
                        }
                        if (reverseList.isEmpty()) {
                            if (newMessageList.isNotEmpty()) {
                                newMessageList.forEach {
                                    reverseList.add(it)
                                }
                                newMessageList.clear()
                            }
                            item.type = "date"
                            item.createdAt = "Today"
                            reverseList.add(item)
                        } else
                            if (dateOnlyOFList != dateOnlyOFNewMessage) {
                                if (newMessageList.isNotEmpty()) {
                                    newMessageList.forEach {
                                        reverseList.add(it)
                                    }
                                    newMessageList.clear()
                                }
                                item.type = "date"
                                item.createdAt = "Today"
                                reverseList.add(item)
                            }
                        reverseList.add(newList)
                        childAdapter.layoutId = getLayoutsMap()
                        mBinding.rvMessageChat.adapter = childAdapter
                        childAdapter.list = reverseList
                        mBinding.ivScrollDown.visibility = View.GONE
                        childAdapter.notifyDataSetChanged()
                        mBinding.rvMessageChat.scrollToPosition(
                            childAdapter
                                .list.size - 1
                        )
                        chatViewModel.chatHistory(
                            ChatHistoryRequest(
                                offset = 0,
                                limit = 1,
                                user = userChatId
                            )
                        )
                        isNewMessageSeen = true


                    }
                } else {
                    newMessageList.add(newList)

                }

            }

            /* if (isInternetAvailable(requireActivity())){
             }*/

        }
        socket.listenOn("reactMessageSuccess") { args ->
            CoroutineScope(Dispatchers.Main).launch {
                Log.e("reactMessageSuccess", args[0].toString())
                val obj = JSONObject(args[0].toString())
                val gson = Gson()
                val listType = object : TypeToken<ReactionData>() {}.type
                reactionList = gson.fromJson<ReactionData>(
                    obj.getJSONObject("data").toString(),
                    listType
                )
                reverseList.forEachIndexed { _, userMessageData ->
                    if (reactionList._id == userMessageData._id) {
                        userMessageData.reactions?.clear()
                        reactionList.reactions?.let { userMessageData.reactions?.addAll(it) }
                    }
                }
                val updatedIndex = reverseList.indexOfFirst { it._id == reactionList._id }
                if (updatedIndex != -1) {
                    /** Notify the adapter about the change at the specific index*/
                    childAdapter.layoutId = getLayoutsMap()
                    childAdapter.notifyItemChanged(updatedIndex)
                }


                /*     reverseList.forEachIndexed { _, userMessageData ->
                         if (reactionList._id == userMessageData._id) {
                             userMessageData.reactions?.clear()
                             reactionList.reactions?.let { userMessageData.reactions?.addAll(it) }
                             childAdapter.layoutId = getLayoutsMap()
                             mBinding.rvMessageChat.adapter = childAdapter
                             */
                /** Find the updated index in the list after modification*//*
                        val updatedIndex = reverseList.indexOf(userMessageData)
                        if (updatedIndex != -1) {
                            childAdapter.notifyItemChanged(updatedIndex)
                        }
                        mBinding.rvMessageChat.scrollToPosition(updatedIndex)
                    }
                }*/

                dialog?.dismiss()
            }
        }
        socket.listenOn("unsendMessageSuccess") { args ->
            CoroutineScope(Dispatchers.Main).launch {
                val obj = JSONObject(args[0].toString())
                val gson = Gson()
                Log.e("unsendMessageSuccess", obj.toString())
                val listType = object : TypeToken<UnSendData>() {}.type
                unSendData = gson.fromJson<UnSendData>(
                    obj.getJSONObject("data").toString(),
                    listType
                )
                // Step 1: Update reverseList with the new item or modify the existing item
                // Assuming you have a function to update or add an item to reverseList
                // updateOrAddItemToReverseList(unSendData)
                // Step 2: Find the index of the updated item in the reversed list
                val updatedIndex = reverseList.indexOfFirst { it._id == unSendData.messageId }

                if (updatedIndex != -1) {

                    // Step 3: Notify the adapter about the change at the specific index
                    reverseList[updatedIndex].message = "Unsent"
                    childAdapter.layoutId = getLayoutsMap()
                    childAdapter.notifyItemChanged(updatedIndex)
                }

                dialog?.dismiss()
            }
        }


        /*  socket.listenOn("unsendMessageSuccess") { args ->
              CoroutineScope(Dispatchers.Main).launch {
                  val obj = JSONObject(args[0].toString())
                  val gson = Gson()
                  Log.e("unsendMessageSuccess" , obj.toString())
                  val listType = object : TypeToken<UnSendData>() {}.type
                  unSendData = gson.fromJson<UnSendData>(
                      obj.getJSONObject("data").toString(),
                      listType
                  )
                  var index=0
                  mBinding.whiteLotteAnimation.visibility=View.GONE
                  reverseList.forEachIndexed { index, userMessageData ->
                      if (unSendData.messageId == userMessageData._id) {

                          index=index
                          userMessageData.message = "Unsent"
                      }
                  }
                  childAdapter.layoutId = getLayoutsMap()
                  childAdapter.notifyItemChanged(index)
                 childAdapter.notifyItemRangeChanged(index , list.size)

                  dialog?.dismiss()
              }
          }*/


        socket.listenOn("deleteMessageSuccess") { args ->
            CoroutineScope(Dispatchers.Main).launch {
                Log.e("deleteMessageSuccess", args[0].toString())
                val obj = JSONObject(args[0].toString())
                /*val gson = Gson()
                val listType = object : TypeToken<DeleteMessageResponse>() {}.type*/
                val deleteMessageResponse = obj.getString("messageId")
                reverseList.forEachIndexed { index, userMessageData ->
                    if (deleteMessageResponse == userMessageData._id) {
                        userMessageData.message = "Deleted"
//                      userMessageData.reactions?.get(0)?.react  = reactionList?.reactions?.get(0)?.react.toString()
                        /*  childAdapter.layoutId = getLayoutsMap()
                          mBinding.rvMessageChat.adapter = childAdapter*/
                        childAdapter.notifyItemChanged(index)
                        mBinding.rvMessageChat.scrollToPosition(index)
                    }
                }

                dialog?.dismiss()
            }
        }
    }

    private fun getLayoutsMap(): HashMap<ChatType, Int> {
        val layoutMap = HashMap<ChatType, Int>()
        layoutMap[ChatType.MESSAGE_SENDER] = R.layout.send_message_layout
        layoutMap[ChatType.MESSAGE_RECEIVER] = R.layout.receive_message_layout
        layoutMap[ChatType.IMAGE_SENDER] = R.layout.image_sender_layout
        layoutMap[ChatType.IMAGE_RECEIVER] = R.layout.image_receiver_layout
        layoutMap[ChatType.DATE] = R.layout.date_layout
        return layoutMap
    }

    /*  private fun getChatHistory() {
          val obj = Utils.toJsonObject(
              "token" to "Bearer " + Prefs.init()
                  .accessToken,
              "offset" to offset,
              "limit" to 100,
              "user" to userChatId
          )

          socket.emitMessage("chat", obj) { _, _ ->
          }
      }*/

    private fun getChatHistory() {
        if (context != null) {
            if (isInternetAvailable(requireContext())) {
                chatViewModel.chatHistory(
                    ChatHistoryRequest(
                        offset = 0,
                        limit = 30,
                        user = userChatId
                    )
                )
            } else {
                Utils.showToast("Please check your internet connection.")
            }

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

            /** Get hours/time information*/
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.ENGLISH)
            Utils.time = localFormattedDate?.let {
                df.parse(it)
                    ?.let { it1 -> timeFormat.format(it1) }
            }!!
            /** Get year information*/
            val yearMonth = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
            yearMonthDate = localFormattedDate.let {
                df.parse(it)
                    ?.let { it1 -> yearMonth.format(it1) }
            }!!
            /** Get AM/PM information*/
            val amPmFormat = SimpleDateFormat("a", Locale.ENGLISH)
            amPms = localFormattedDate.let {
                df.parse(it)
                    ?.let { it1 -> amPmFormat.format(it1) }
            }!!
            /** Get year-month-days (01Jan2001) information here*/
            val yearMonths = SimpleDateFormat("EEEE, dd MMM yyyy", Locale.ENGLISH)
            finalDateFormats = localFormattedDate.let {
                df.parse(it)
                    ?.let { it1 -> yearMonths.format(it1) }
            }!!

            /**Calculate time differences*/
            val cal1: Calendar = Calendar.getInstance()
            val cal2: Calendar = Calendar.getInstance()
            cal2.time = df.parse(localFormattedDate)!!
            val msDiff: Long = cal1.timeInMillis - cal2.timeInMillis
            messageDays = TimeUnit.MILLISECONDS.toDays(msDiff)

            /*    Utils.monthsDiff = (Utils.daysDiff / 30.44).toLong()
                Utils.seconds = TimeUnit.MILLISECONDS.toSeconds(msDiff)
                Utils.minutes = TimeUnit.MILLISECONDS.toMinutes(msDiff)
                Utils.hours = TimeUnit.MILLISECONDS.toHours(msDiff)*/
        } catch (e: Exception) {
            e.printStackTrace()
            // Handle the parsing exception
        }
    }


    private fun sendMessage(
        userID: String,
        message: String,
        type: String,
        replyMessage: String? = null
    ) {
        val obj = Utils.toJsonObject(
            "token" to "Bearer ${Prefs.init().accessToken}",
            "message" to message,
            "replyMessage" to replyMessage,
            "user" to userID,
            "type" to type,
        )
        socket.emitMessage("newMessage", obj) { _, _ ->

        }
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    override fun onStart() {
        super.onStart()
        org.greenrobot.eventbus.EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        org.greenrobot.eventbus.EventBus.getDefault().unregister(this)
        Prefs.init().messageSenderId = ""
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onChatEvents(event: ImageUploaded?) {
        sendImage(userChatId, event?.text.toString(), "image")
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEmoJiEvents(event: NewEmoJi?) {
        if (event != null) {
            emoJiReactionToMessage(userChatId, event.messageIDs, event.text)
            /*Log.e("reactionListCount" , reactionListCount.toString())
                        if(reactionListCount<6){
                            viewModel.addEmoJi(
                                ReactionEntity(reaction=event.text, loginTimestamp = getCurrentDateTime())
                            )
                        }else if(reactionListCount==6){
                            viewModel.updateSecondRow(
                                ReactionEntity(reaction=event.text, loginTimestamp = getCurrentDateTime())
                            )
                        }*/

            /*     viewModel.allEmojiReaction.observe(viewLifecycleOwner, Observer{
                         list->
                     Log.d("databaseCount12222", "${list.size} rows of database")
                     if(list.size<6){
                         viewModel.addEmoJi(
                             ReactionEntity(reaction=event.text, loginTimestamp = getCurrentDateTime())
                         )
                     }else if(list.size==6){
                         viewModel.updateSecondRow(
                             ReactionEntity(reaction=event.text, loginTimestamp = getCurrentDateTime())
                         )
                     }
                 })*/


        }
    }

    override fun onLongPressOnSenderMessage(
        view: View,
        position: Int,
        data: UserMessageData,
        time: String
    ) {
        convertDateAndTime(data.createdAt)
        //  hideKeyboardOnClick(view)
        if (isMessageOn) {
            reactionPopUp(message = data.message, type = "senderMessage", Utils.time, data._id)
        }

    }

    override fun onLongPressOnReceiverMessage(position: Int, data: UserMessageData, time: String) {
        convertDateAndTime(data.createdAt)
        if (isMessageOn) {
            reactionPopUp(message = data.message, type = "receiverMessage", Utils.time, data._id)
        }
    }

    override fun onLongPressOnSenderImage(position: Int, data: UserMessageData, time: String) {
        convertDateAndTime(data.createdAt)
        if (isMessageOn) {
            if (data.post != null) {
                reactionPopUp(
                    message = data.post.videoThumbnail,
                    type = "senderImage",
                    Utils.time,
                    data._id
                )
            } else {
                reactionPopUp(message = data.file, type = "senderImage", Utils.time, data._id)
            }
        }
    }

    override fun onLongPressOnReceiverImage(position: Int, data: UserMessageData, time: String) {
        convertDateAndTime(data.createdAt)
        if (isMessageOn) {
            if (data.post != null) {
                reactionPopUp(
                    message = data.post.videoThumbnail,
                    type = "receiverImage",
                    Utils.time,
                    data._id
                )
            } else {
                reactionPopUp(message = data.file, type = "receiverImage", Utils.time, data._id)
            }
        }

    }

    @SuppressLint("SetTextI18n")
    override fun swipeReply(position: Int, data: UserMessageData, type: String) {
        replyID = data._id
        val message = data.message
        mBinding.rvMessageChat.scrollToPosition(reverseList.size - 1)
        when (type) {
            "senderMessage" -> {
                mBinding.tvReplyMessage.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.appBlue
                    )
                )
                mBinding.tvReplyMessage.text = message
            }

            "receiverMessage" -> {
                mBinding.tvReplyMessage.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.lightblack2
                    )
                )
                mBinding.tvReplyMessage.text = message
            }

            "senderImage" -> {
                mBinding.tvReplyMessage.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.appBlue
                    )
                )
                mBinding.tvReplyMessage.text = "replaying to drop"
            }

            "receiverImage" -> {
                mBinding.tvReplyMessage.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.lightblack2
                    )
                )
                mBinding.tvReplyMessage.text = "replaying to drop"

            }
        }

        mBinding.tvReplyMessage.visibility = View.VISIBLE
        mBinding.edMessage.requestFocus()
        val imm =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm!!.showSoftInput(mBinding.edMessage, InputMethodManager.SHOW_IMPLICIT)
        mBinding.close.visibility = View.VISIBLE
    }

    override fun onClickImage(position: Int, data: UserMessageData, type: String) {
        if (type == "sharePost") {
            bundle.putInt("position", 0)
            bundle.putString("dataList", data.post?._id)
            findNavController().navigate(
                R.id.show_profile_post_fragment,
                bundle
            )
        } else {
            bundle.putString("profile", data.file)
            findNavController().navigate(R.id.showProfileFragment, bundle)
        }
    }

    override fun onClickHideKeyboard(position: Int) {
        try {
            val inputMethodManager: InputMethodManager =
                requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            if (inputMethodManager.isAcceptingText) {
                inputMethodManager.hideSoftInputFromWindow(
                    activity?.currentFocus!!.windowToken,
                    0
                )

            }
        } catch (e: Exception) {

        }
    }

    override fun onDoubleTapClick(position: Int, data: UserMessageData) {
        val emojis = "❤️"
        val unicodeInt = emojis.codePointAt(0)
        finalEmoji = getEmojiByUnicode(unicodeInt)
        emoJiReactionToMessage(userChatId, data._id, finalEmoji)
    }

    override fun onDownloadDecryptFile(position: Int, data: UserMessageData) {
        chatViewModel.userDecryptFile(
            UserDecryptFile(
                url = data.file,
                type = data.type
            )
        )

        userDecryptFileObserver()
    }


    @SuppressLint("SetTextI18n")
    private fun reactionPopUp(message: String, type: String, time: String, messageId: String) {
        dialog?.setCancelable(false)
        Objects.requireNonNull(dialog?.window)?.setBackgroundDrawable(
            ColorDrawable(
                Color.TRANSPARENT
            )
        )
        dialog?.setContentView(R.layout.reaction_recycle_view)
        val lp = dialog!!.window!!.attributes
        lp?.gravity = Gravity.BOTTOM
        lp.dimAmount = 0.85f
        lp?.y = 48
        dialog!!.window!!.attributes = lp
        dialog!!.setCanceledOnTouchOutside(true)
        dialog!!.window!!.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)

        val clReaction = dialog?.findViewById(R.id.clMessageSender) as RelativeLayout
        val clMessageReceiver = dialog?.findViewById(R.id.clMessageReceiver) as RelativeLayout
        val clSenderImage = dialog?.findViewById(R.id.clSenderImage) as RelativeLayout
        val clImageReceiver = dialog?.findViewById(R.id.clImageReceiver) as RelativeLayout
        val tvUserSendMessage = dialog?.findViewById(R.id.tvUserSendMessage) as TextView
        val tvUserMessage = dialog?.findViewById(R.id.tvUserMessage) as TextView
        val tvCopy = dialog?.findViewById(R.id.tvCopy) as TextView
        val tvReply = dialog?.findViewById(R.id.tvReply) as TextView
        val tvUnSendMessage = dialog?.findViewById(R.id.tvUnSendMessage) as TextView
        val senderImage = dialog?.findViewById(R.id.senderImage) as ImageView
        val receiveOtherImage = dialog?.findViewById(R.id.receiveOtherImage) as ImageView
        val tvDateReceiver = dialog?.findViewById(R.id.tvDateReceiver) as TextView
        val tvDateImage = dialog?.findViewById(R.id.tvDateImage) as TextView
        val emojiPlus = dialog?.findViewById(R.id.emoji7) as ImageView
        val tvDates = dialog?.findViewById(R.id.tvReplyDates) as TextView
        val tvDate = dialog?.findViewById(R.id.tvDate) as TextView
        viewModel.getAllEmoJiList()
        viewModel.allEmojiReaction.observe(viewLifecycleOwner, Observer { list ->
            if (list.isNotEmpty()) {
                val emojiIds = arrayOf(
                    R.id.emoji1,
                    R.id.emoji2,
                    R.id.emoji3,
                    R.id.emoji4,
                    R.id.emoji5,
                    R.id.emoji6
                )
                list.sortByDescending { it.loginTimestamp }
                for ((index, emojiId) in emojiIds.withIndex()) {
                    val textView = dialog?.findViewById<TextView>(emojiId)
                    if (textView != null && index < list.size) {
                        textView.text = list[index].reaction.toString()
                    }
                }

            }


        })
        tvCopy.visibility = View.VISIBLE
        mBinding.close.visibility = View.GONE
        mBinding.tvReplyMessage.visibility = View.GONE

        when (type) {
            "senderMessage" -> {
                clReaction.visibility = View.VISIBLE
                tvUserSendMessage.text = message
                tvDate.text = time
            }

            "receiverMessage" -> {
                clMessageReceiver.visibility = View.VISIBLE
                tvUserMessage.text = message
                tvDates.text = time
                tvUnSendMessage.text = "Delete"
            }

            "senderImage" -> {
                clSenderImage.visibility = View.VISIBLE
                tvCopy.visibility = View.GONE
                tvDateImage.text = time
                CoroutineScope(Dispatchers.Main).launch {
                    senderImage.loadImageAsyncMessage(message)
                }
            }

            "receiverImage" -> {
                clImageReceiver.visibility = View.VISIBLE
                tvCopy.visibility = View.GONE
                tvDateReceiver.text = time
                CoroutineScope(Dispatchers.Main).launch {
                    receiveOtherImage.loadImageAsyncMessage(message)
                }
                tvUnSendMessage.text = "Delete"
            }
        }


        val emojiIds =
            arrayOf(R.id.emoji1, R.id.emoji2, R.id.emoji3, R.id.emoji4, R.id.emoji5, R.id.emoji6)

        emojiIds.forEach { emojiId ->
            dialog?.findViewById<TextView>(emojiId)?.setOnClickListener {
                val emojis = (it as? TextView)?.text.toString()
                val unicodeInt = emojis.codePointAt(0)
                finalEmoji = getEmojiByUnicode(unicodeInt)
                emoJiReactionToMessage(userChatId, messageId, finalEmoji)
            }
        }



        emojiPlus.setOnClickListener {
            //     emoJi.toggle()
            dialog?.dismiss()
            showEmojiBottomSheet(messageId)
        }

        tvReply.setOnClickListener {
            replyID = messageId
            reply = true
            when (type) {
                "senderMessage" -> {
                    mBinding.tvReplyMessage.setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.appBlue
                        )
                    )
                    mBinding.tvReplyMessage.text = message
                }

                "receiverMessage" -> {
                    mBinding.tvReplyMessage.setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.lightblack2
                        )
                    )
                    mBinding.tvReplyMessage.text = message
                }

                "senderImage" -> {
                    mBinding.tvReplyMessage.setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.appBlue
                        )
                    )
                    mBinding.tvReplyMessage.text = "replaying to drop"
                }

                "receiverImage" -> {
                    mBinding.tvReplyMessage.setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.lightblack2
                        )
                    )
                    mBinding.tvReplyMessage.text = "replaying to drop"

                }
            }
            mBinding.tvReplyMessage.visibility = View.VISIBLE
            mBinding.close.visibility = View.VISIBLE

            mBinding.edMessage.requestFocus()

            Handler(Looper.getMainLooper()).postDelayed({
                val imm =
                    requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
                imm!!.showSoftInput(mBinding.edMessage, InputMethodManager.SHOW_IMPLICIT)
                mBinding.rvMessageChat.scrollToPosition(reverseList.size - 1)
            }, 200)

            dialog?.dismiss()
        }

        tvUnSendMessage.setOnClickListener {
            when (tvUnSendMessage.text) {
                "Delete" -> {
                    deleteMessage(userChatId, messageId)
                }

                else -> {
                    unSendMessage(userChatId, messageId)
                }
            }

        }

        tvCopy.setOnClickListener {
            val clipboardManager =
                requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("text", message)
// Set the ClipData to the clipboard
            clipboardManager.setPrimaryClip(clipData)
            dialog?.dismiss()
        }
        dialog?.show()
    }

    private fun getEmojiByUnicode(unicode: Int): String {
        return String(Character.toChars(unicode))
    }

    private fun sendImage(userID: String, image: String, type: String) {
        val obj = Utils.toJsonObject(
            "token" to "Bearer ${Prefs.init().accessToken}",
            "file" to image,
            "replyMessage" to null,
            "user" to userID,
            "type" to type,
        )
        socket.emitMessage("newMessage", obj) { _, _ ->
        }
    }

    private fun emoJiReactionToMessage(userID: String, message: String, reaction: String) {
        val obj = Utils.toJsonObject(
            "token" to "Bearer ${Prefs.init().accessToken}",
            "message" to message,
            "reaction" to reaction,
            "user" to userID
        )
        socket.emitMessage("reactMessage", obj) { _, _ ->
        }
    }

    private fun unSendMessage(userID: String, message: String) {
        val obj = Utils.toJsonObject(
            "token" to "Bearer ${Prefs.init().accessToken}",
            "message" to message,
            "otherUser" to userID
        )
        socket.emitMessage("unsendMessage", obj) { _, _ ->
        }
    }

    private fun deleteMessage(userID: String, messageId: String) {
        val obj = Utils.toJsonObject(
            "token" to "Bearer ${Prefs.init().accessToken}",
            "message" to messageId,
            "otherUser" to userID
        )
        socket.emitMessage("deleteMessage", obj) { _, _ ->
        }
    }


    private fun showEmojiBottomSheet(messageId: String) {
        val bottomSheetFragment = EmoJiFragment(R.layout.fragment_emo_ji)
        bundle.putString("messageIds", messageId)
        bottomSheetFragment.arguments = bundle
        bottomSheetFragment.height = 70
        bottomSheetFragment.show(childFragmentManager, bottomSheetFragment.tag)
    }

    override fun onDestroy() {
        super.onDestroy()
        newMessage = false
    }


    private fun userDecryptFileObserver() {
        chatViewModel.chatImageUploadResponse.observe(viewLifecycleOwner) { response ->
            if (response.success) {
                Utils.showToast(response.message.toString())
                response.data?.let {
                    /** Assuming you have the Base64 encoded string in jsonString*/
                    val base64String = response.data?.file.toString()

                    /** Decode Base64 string into a byte array*/
                    val decodedBytes =
                        android.util.Base64.decode(base64String, android.util.Base64.DEFAULT)

                    /** Create a new Workbook and load the byte array*/
                    val workbook = Workbook(ByteArrayInputStream(decodedBytes))

                    /** Specify the file path where you want to save the Excel file on the device storage*/
                    val filePath = "/storage/emulated/0/Download/your_excel_file.xlsx"

                    /** Save the Workbook to the specified file path*/
                    workbook.save(filePath)
                    val outputFile = File(filePath)
                    if (outputFile.exists()) {
                        // File exists, you can perform further actions
                        Log.d(
                            "FileStatus",
                            "File saved successfully at: ${outputFile.absolutePath}"
                        )
                    } else {
                        // File doesn't exist, handle the error or retry saving
                        Log.e("FileStatus", "Failed to save the file.")
                    }

                }
            }
        }


        chatViewModel.showLoading.observe(viewLifecycleOwner) {

            if (!it) {

                mBinding.animationView.visibility = View.GONE
            } else {
                dialogs?.dismiss()
                mBinding.animationView.visibility = View.VISIBLE
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun isSocketConnected(event: isSocketConnected) {
        if (event.isConnected) {
            setSocketListenON()
            //    getNotificationCount()
        }
    }


}