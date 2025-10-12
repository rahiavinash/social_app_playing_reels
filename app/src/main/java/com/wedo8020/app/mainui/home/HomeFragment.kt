package com.wedo8020.app.mainui.home


import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.wedo8020.app.MainApplication
import com.wedo8020.app.R
import com.wedo8020.app.databinding.FragmentHomeBinding
import com.wedo8020.app.eventBus.HomePagerScroll
import com.wedo8020.app.eventBus.StartStopAR
import com.wedo8020.app.eventBus.isGhostMode
import com.wedo8020.app.eventBus.isSocketConnected
import com.wedo8020.app.mainui.activities.MainActivity
import com.wedo8020.app.mainui.adapter.ViewPagerAdapter
import com.wedo8020.app.mainui.base.BaseFragment
import com.wedo8020.app.mainui.home.createpost.CreatePostViewModel
import com.wedo8020.app.mainui.home.createpost.CreatePostViewModelFactory
import com.wedo8020.app.mainui.home.postFragment.PostScreen
import com.wedo8020.app.roomdb.CreatePostDbCreation
import com.wedo8020.app.roomdb.CreatePostDbRepo
import com.wedo8020.app.utils.NewMessage
import com.wedo8020.app.utils.Prefs
import com.wedo8020.app.utils.SocketEmitConstant
import com.wedo8020.app.utils.SocketListenerConstant
import com.wedo8020.app.utils.Utils
import com.wedo8020.app.utils.Utils.disableMultiTap
import com.wedo8020.zelpyy.SocketProvider.SocketIOImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.json.JSONObject


class HomeFragment : BaseFragment() {
    var binding: FragmentHomeBinding? = null
    val fragments = ArrayList<Fragment>()
    private lateinit var database: CreatePostDbCreation
    private lateinit var repository: CreatePostDbRepo
    private lateinit var factory: CreatePostViewModelFactory
    private lateinit var viewModel: CreatePostViewModel
    private var mediaSuccess: Boolean? = null
    lateinit var socket: SocketIOImpl
    private var arCountHandler = Handler(Looper.myLooper()!!)
    private var isArCountUpdate = false
    private lateinit var adapter: ViewPagerAdapter
    private val durationMillis: Long = 31 * 1000
    private var originalSoftInputMode = 0
    private lateinit var mainActivity: MainActivity
    private var arFirstLoaded = false

    //    var postTypePagerAdapter: PostTypePagerAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /* Handler(Looper.myLooper()!!).postDelayed({

             //Todo handle this for deeplink videos not playing because release
             Utils.printLog(tag = "currentFragment", msg = binding?.postTypePageView?.currentItem.toString())
         },2000)*/
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainActivity = requireActivity() as MainActivity
        if (mainActivity.currentDrop == "drop") {
            binding?.postTypePageView?.currentItem = 0
        } else {
            binding?.postTypePageView?.currentItem = 1
        }

        val callback = requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            val navController = findNavController()
            val previousDestinationId = navController.previousBackStackEntry?.destination?.id

            if (previousDestinationId == R.id.createPostFragment) {


            } else {

                requireActivity().moveTaskToBack(true)
            }
        }


        mediaSuccess = arguments?.getBoolean("responseSuccess")
        if (mediaSuccess == true) {

//            if(mediaType=="video"){
            showToasts("Dropped.See it in profile.")
            CoroutineScope(Dispatchers.IO).launch {
                Utils.printLog(
                    tag = "getCreatePostData",
                    msg = database.getCreatePostData().fetchAllPost().toString()
                )
                database.getCreatePostData().fetchAllPost().forEach {
                    Utils.printLog(tag = "getCreatePostData", msg = it.toString())
                    viewModel.compressAndUploadVideoOrImages(it)

                }
            }

//                videoString?.let { compressVideoFromGallery(it.toUri()) }
//            }
//            else if(mediaType=="image"){
//                apiCallWithImageMedia()
//            }
        }

        val frag = fragments[binding?.postTypePageView?.currentItem ?: 0] as PostScreen
        frag.startCurrentPagePlayerIfAny()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (getActivity() != null) {
            originalSoftInputMode = requireActivity().getWindow().getAttributes().softInputMode
        }
        if (getActivity() != null) {
            getActivity()?.getWindow()
                ?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        }
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        database = CreatePostDbCreation(requireContext())
        repository = CreatePostDbRepo(database)
        factory = CreatePostViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[CreatePostViewModel::class.java]


        arguments?.let {

            val successBool = it.getBoolean("responseSuccess")

            mediaSuccess = successBool

            Log.d("post111", "u posted media $successBool")

        }
        Log.d("post111", mediaSuccess.toString())

        setUpUI()

        // Inflate the layout for this fragment
        return binding!!.root
    }


    private fun setUpUI() {
        socket = MainApplication.get().socket
        if (Prefs.init().isGhostMode) {
            binding?.headerLayout?.alpha = 0.0f
        } else {
            binding?.headerLayout?.alpha = 1.0f
        }
        binding?.apply {


            fragments.add(PostScreen.newInstance("drop"))
            fragments.add(PostScreen.newInstance("following"))
            val titles = listOf("", "")
            adapter = ViewPagerAdapter(childFragmentManager, 2, fragments, titles)

            postTypePageView.adapter = adapter
            postTypePageView.offscreenPageLimit = 1


//            postTypePageView.currentItem=0
            search.setOnClickListener {
                it.disableMultiTap()
                findNavController().navigate(R.id.action_homeFragment_to_searchFragment)
            }
            val mainActivity = activity
            if (mainActivity != null && mainActivity is MainActivity) {
                arTreeCount.text = if (mainActivity.impactData?.eTrees != null) {
                    mainActivity.impactData?.eTrees?.toString() ?: "0"
                } else {
                    "0"
                }
            } else {
                arTreeCount.text = "0"
            }

        }

        pageSelectedListener()
    }

    private fun showToasts(message: String) {
        val inflater = layoutInflater
        val layout =
            inflater.inflate(R.layout.toast_layout, requireView().findViewById(R.id.toast_layout))

        val text = layout.findViewById<TextView>(R.id.toast_text)
        text.text = message

        val toast = Toast(requireContext())
        toast.duration = Toast.LENGTH_SHORT
        toast.view = layout
        toast.setGravity(Gravity.TOP, 0, 100)
        toast.show()
    }

    private fun pageSelectedListener() {

        binding?.apply {

            dropBtn.setOnClickListener {
                dropsSelected()

                postTypePageView.currentItem = 0
            }
            followingBtn.setOnClickListener {

                followingSelected()
                postTypePageView.currentItem = 1
            }


            postTypePageView.addOnPageChangeListener(object : OnPageChangeListener {
                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {
                    /*if (position == mCurrentSelectedScreen) {
                        // We are moving to next screen on right side
                        if (positionOffset > 0.5) {
                            // Closer to next screen than to current
                            if (position + 1 != mNextSelectedScreen) {
                                mNextSelectedScreen = position + 1;
                            }
                        } else {
                            // Closer to current screen than to next
                            if (position != mNextSelectedScreen) {
                                val frag = fragments[position - 1]
                                if (frag is PostFragment) {
                                    frag.startCurrentPagePlayerIfAny()
                                }

                                mNextSelectedScreen = position;
                            }
                        }
                    } else {
                        // We are moving to next screen left side
                        if (positionOffset > 0.5) {
                            // Closer to current screen than to next
                            if (position + 1 != mNextSelectedScreen) {
                                val frag = fragments[position + 1]
                                if (frag is PostFragment) {

                                    frag.startCurrentPagePlayerIfAny()
                                }

                                mNextSelectedScreen = position + 1;
                            }
                        } else {
                            // Closer to next screen than to current
                            if (position != mNextSelectedScreen) {
                                mNextSelectedScreen = position;
                            }
                        }
                    }*/
                }

                override fun onPageSelected(position: Int) {
                    EventBus.getDefault().post(StartStopAR(true))
                    when (position) {
                        0 -> {
                            dropsSelected()
                        }

                        1 -> {
                            followingSelected()
                        }
                    }
                    val frag = fragments[position] as PostScreen
                    frag.startCurrentPagePlayerIfAny()
                    when (position) {
                        0 -> {

                            Utils.printLog(tag = "currentFragment", msg = "drop")
                        }

                        1 -> {
                            Utils.printLog(tag = "currentFragment", msg = "following")
                        }
                    }
                }

                override fun onPageScrollStateChanged(state: Int) {

                }
            })
        }
    }

    // countdown timer for stoppin AR count
    val countDownTimer = object : CountDownTimer(durationMillis, 1000) {
        override fun onTick(millisUntilFinished: Long) {
        }

        override fun onFinish() {
            startStopArCount("stop")
        }
    }

    private fun dropsSelected() {
        mainActivity.currentDrop = "drop"
        binding?.apply {
            dropsTabImg.imageTintList =
                ColorStateList.valueOf(requireActivity().getColor(R.color.pureWhite))
            dropTabText.setTextColor(requireContext().getColor(R.color.pureWhite))
            followingTabImg.imageTintList =
                ColorStateList.valueOf(requireActivity().getColor(R.color.tabUnselected))
            followingTabText.setTextColor(requireContext().getColor(R.color.tabUnselected))
        }
    }

    private fun followingSelected() {
        mainActivity.currentDrop = "following"
        binding?.apply {
            followingTabImg.imageTintList =
                ColorStateList.valueOf(requireActivity().getColor(R.color.pureWhite))
            followingTabText.setTextColor(requireContext().getColor(R.color.pureWhite))
            dropsTabImg.imageTintList =
                ColorStateList.valueOf(requireActivity().getColor(R.color.tabUnselected))
            dropTabText.setTextColor(requireContext().getColor(R.color.tabUnselected))
        }
    }


    override fun onResume() {
        super.onResume()
        showNavBottom()
        if (!socket.isConnected()) {
            socket.connect()
            Log.e("socket_connect", "notConnected")
        } else {
            Log.e("socket_connect", "Connected")
            setSocketListenON()
            startStopArCount("start")
            getNotificationCount()
        }
        if (!arFirstLoaded) {
            binding?.apply {
                arCount.text = "0.0"
                arCountHandler.removeCallbacksAndMessages(null)
                arCountHandler.postDelayed(object : Runnable {
                    @SuppressLint("SuspiciousIndentation")
                    override fun run() {
                        if (arCount.text.toString().toFloat() < 5000.0f) {
                            arCount.text = (arCount.text.toString()
                                .toFloat() + 0.5).toString()
                            arCountHandler.postDelayed(this, 500)
                        }
                    }
                }, 500)
                arFirstLoaded = true
            }

        }
    }


    private fun getNotificationCount() {
        socket.emitMessage(
            "totalNotification", "Bearer " + Prefs.init()
                .accessToken
        ) { _, _ ->
        }
    }


    private fun setSocketListenON() {
        socket.listenOff(SocketListenerConstant.ERROR)
        socket.listenOff(SocketListenerConstant.TRACK_AR_SUCCESS)
        socket.listenOff(SocketListenerConstant.AR_UPDATED)

        socket.listenOn(SocketListenerConstant.AR_UPDATED) { args ->
            val obj = JSONObject(args[0].toString())

            Log.e("kjfg", args[0].toString())
            startStopArCount("start")
        }

        socket.listenOn(SocketListenerConstant.TRACK_AR_SUCCESS) { args ->
            CoroutineScope(Dispatchers.Main).launch {
                arCountHandler.removeCallbacksAndMessages(null)
                val obj = JSONObject(args[0].toString())
                val str = obj.getString("dailyARCount")
                Log.e("kjfg", str)
                binding?.let { binding ->
                    binding.arCount.text = str
                    if (str.toInt() < 5000) {
                        if (isArCountUpdate) {
                            arCountHandler.postDelayed(object : Runnable {
                                @SuppressLint("SuspiciousIndentation")
                                override fun run() {
                                    if (binding.arCount.text.toString().toFloat() < 5000.0f) {
                                        binding.arCount.text = (binding.arCount.text.toString()
                                            .toFloat() + 0.5).toString()
                                        arCountHandler.postDelayed(this, 500)
                                    }
                                }
                            }, 500)
                        }
                    }
                }
            }
        }

        socket.listenOn(SocketListenerConstant.ERROR) { args ->
            CoroutineScope(Dispatchers.Main).launch {
                Log.e("emitsMessage", args[0].toString())
            }
        }
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

    }


    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    fun startStopArCount(arg: String) {
        if (socket.isConnected()) {
            isArCountUpdate = arg == "start"
            val obj = Utils.toJsonObject("status" to arg)
            socket.emitMessage(SocketEmitConstant.TRACK_AR, obj) { _, _ ->
            }
        }
    }

    override fun onPause() {
        super.onPause()
        startStopArCount("stop")
        if (socket.isConnected()) {
            socket.disconnect()
        }
        EventBus.getDefault().unregister(this)
    }

    override fun onStop() {
        super.onStop()
        if (getActivity() != null) {
            getActivity()?.getWindow()
                ?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Subscribe
    fun isSocketConnected(isSocketConnected: Any) {
        if (isSocketConnected is isSocketConnected) {
            if (isSocketConnected.isConnected) {
                Log.e("socket_connect", "emit login socket")
                setSocketListenON()
                startStopArCount("start")
                getNotificationCount()
            }
        } else if (isSocketConnected is isGhostMode) {
            if (Prefs.init().isGhostMode) {
                binding?.headerLayout?.alpha = 0.0f
            } else {
                binding?.headerLayout?.alpha = 1.0f
            }
        } else if (isSocketConnected is HomePagerScroll) {
            if (isSocketConnected.isScroll) {
                binding?.postTypePageView?.setSwipeable(true)
            } else {
                binding?.postTypePageView?.setSwipeable(false)
            }
        } else if (isSocketConnected is StartStopAR) {
            if (isSocketConnected.isStart) {
                startStopArCount("start")
                countDownTimer.cancel()
                countDownTimer.start()
            }
        }
    }
}
