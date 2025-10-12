package com.wedo8020.app.mainui.home.postFragment

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.facebook.shimmer.Shimmer
import com.wedo8020.app.MainApplication
import com.wedo8020.app.R
import com.wedo8020.app.base.PlayerViewAdapter
import com.wedo8020.app.databinding.FragmentDropsBinding
import com.wedo8020.app.databinding.HomeListItemBinding
import com.wedo8020.app.databinding.HomeListVideoItemBinding
import com.wedo8020.app.databinding.PostInfoLayoutBinding
import com.wedo8020.app.eventBus.HomeTabClick
import com.wedo8020.app.eventBus.Repost
import com.wedo8020.app.eventBus.StartStopAR
import com.wedo8020.app.eventBus.isDoubleTap
import com.wedo8020.app.eventBus.isGhostMode
import com.wedo8020.app.eventBus.isInnerPageScroll
import com.wedo8020.app.mainui.activities.MainActivity
import com.wedo8020.app.mainui.adapter.PostAdapter
import com.wedo8020.app.mainui.adapter.TagUserAdapter
import com.wedo8020.app.mainui.base.BaseFragment
import com.wedo8020.app.mainui.dialogFragments.CommentsBottomSheet
import com.wedo8020.app.mainui.dialogFragments.OptionBottomSheet
import com.wedo8020.app.mainui.dialogFragments.ReportBottomSheet
import com.wedo8020.app.mainui.dialogFragments.SharePostUserBottomSheet
import com.wedo8020.app.mainui.dialogFragments.TagUserBottomSheet
import com.wedo8020.app.mainui.home.HomeFragment
import com.wedo8020.app.postOptionListeners
import com.wedo8020.app.request.DeletePost
import com.wedo8020.app.request.PostRequest
import com.wedo8020.app.request.PostRestrict
import com.wedo8020.app.request.UserRequest
import com.wedo8020.app.request.UserRestrict
import com.wedo8020.app.response.Data
import com.wedo8020.app.response.Options
import com.wedo8020.app.utils.AppConstant
import com.wedo8020.app.utils.CachedDataSourceFactory
import com.wedo8020.app.utils.DeepLinkType
import com.wedo8020.app.utils.EventObserver
import com.wedo8020.app.utils.Extension.viewIsLoaded
import com.wedo8020.app.utils.Extension.waitForFirstVisibleItem
import com.wedo8020.app.utils.LogTypeEnum
import com.wedo8020.app.utils.Prefs
import com.wedo8020.app.utils.RecyclerViewScrollListener
import com.wedo8020.app.utils.Utils
import com.wedo8020.app.utils.Utils.capitalizeFirstChar
import com.wedo8020.app.utils.Utils.runWithDelay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.util.Objects
import kotlin.math.max
import kotlin.math.min

private const val ARG_PARAM1 = "post_type"

class PostScreen : BaseFragment(), TagUserAdapter.ClickListener, postOptionListeners,
    CommentsBottomSheet.ClickListeners {
    private lateinit var commentReportText: TextView
    private lateinit var commentCountTextView: TextView
    private var postType: String = ""
    private var isCreatedOnce = false
    private var binding: FragmentDropsBinding? = null
    private var cachedDataSourceFactory: CachedDataSourceFactory? = null
    private var postAdapter: PostAdapter? = null
    private val mViewModel by lazy { viewModels<PostViewModel>().value }
    private var offset = 0
    private var limit = 16
    private var commentID = ""
    private var isGhostMode = Prefs.init().isGhostMode
    private var isLHPost = Prefs.init().isLHPost
    private var postPosition = -1
    private var imagePagerPosition = -1
    private var lastVisibleItemPosition = -1
    private var isSnapAttached = false
    private var isLoading = false
    private var currentPage = -1

    //    private var postList = ArrayList<Data?>()
    private lateinit var scrollListener: RecyclerViewScrollListener
    private var layoutManager: LinearLayoutManager? = null
    private var targetViewX = 0.0f
    private var targetViewY = 0.0f
    private var currentRecyclerPositionBeforeScroll = -1
    private var otherPositionWhileScroll = -1
    private lateinit var mainActivity: MainActivity

    private var isInternetAvailableWhileScroll = true
    val handlerSeekBar by lazy {
        Handler(Looper.myLooper()!!)
    }
    private val handlerPostViewed by lazy {
        Handler(Looper.myLooper()!!)
    }
    private lateinit var itemBinding: HomeListVideoItemBinding
    var dialog: Dialog? = null
    private var isViewLoaded = false

    private var currentPostItemClickedPosition = -1
    private var isRefresh = false
    private var myCoroutineScope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            postType = it.getString(ARG_PARAM1) ?: "drop"
        }
        if (activity != null) {
            mainActivity = requireActivity() as MainActivity
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDropsBinding.inflate(inflater, container, false)
        showNavBottom()
        Utils.printLog(tag = "dataType", msg = postType)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        if (!isViewLoaded){


        setUpUi()
        if (getPostList().isEmpty()) {
            callApi(postType)
        } else {
            offset = getPostList().size
        }

    }

    override fun onStart() {
        super.onStart()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }

    }

    override fun onResume() {
        super.onResume()


        val snapHelper: SnapHelper = PagerSnapHelper()
        if (!isSnapAttached) {
            isSnapAttached = true
            snapHelper.attachToRecyclerView(binding?.postRecycler)
        }
        Log.e("asgjasfg",mainActivity.currentDrop)
        if (PlayerViewAdapter.player != null && isCreatedOnce) {
            Utils.printLog(tag = "onStart", msg = "playCurrentPlayingVideo $postType")
//            PlayerViewAdapter.playCurrentPlayingVideo()
            if (mainActivity.currentDrop == postType) {
                binding?.postRecycler?.viewIsLoaded {
                    Utils.printLog(
                        tag = "onStart",
                        msg = "viewIsLoaded ${layoutManager?.findFirstCompletelyVisibleItemPosition()}"
                    )
                    currentPage = -1

                    val firstItemView =
                        layoutManager?.findViewByPosition(
                            layoutManager?.findFirstCompletelyVisibleItemPosition() ?: 0
                        )
                    firstItemView?.viewIsLoaded {

                        findLayoutByPosition(
                            layoutManager?.findFirstCompletelyVisibleItemPosition() ?: 0,
                            "onResume:player not null"
                        )
                    }


                }
            }
        } else {
            isCreatedOnce = true
            Utils.printLog(
                tag = "onStart",
                msg = layoutManager?.findFirstCompletelyVisibleItemPosition().toString()
            )
            binding?.postRecycler?.viewIsLoaded {
                Utils.printLog(
                    tag = "onStart",
                    msg = "viewIsLoaded ${layoutManager?.findFirstCompletelyVisibleItemPosition()}"
                )
                currentPage = -1

                val pos = if (mainActivity.currentDrop == "drop") {
                    mainActivity.currentDropPosition
                } else {
                    mainActivity.currentFollowingPosition
                }
//                Log.e("asjgh", pos.toString())
                if (mainActivity.currentDrop == "drop") {
                    Log.e("asjgh", "drop" + mainActivity.currentDropPosition.toString())
                    binding?.postRecycler?.scrollToPosition(mainActivity.currentDropPosition)
                } else {
                    Log.e("asjgh", "following" + mainActivity.currentFollowingPosition.toString())
                    binding?.postRecycler?.scrollToPosition(mainActivity.currentFollowingPosition)
                }




                if (mainActivity.currentDrop == postType) {
                    binding?.postRecycler?.waitForFirstVisibleItem(Handler(Looper.myLooper()!!)) {
                        val firstItemView =
                            layoutManager?.findViewByPosition(
                                pos
                            )
                        firstItemView?.viewIsLoaded {
                            findLayoutByPosition(
                                pos, "onResume player null"
                            )
                        }
                    }
                }

            }

        }

    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    override fun onPause() {
        super.onPause()
        myCoroutineScope.cancel()

        if (postType == "drop") {
//            mainActivity.dropList = postAdapter?.list ?: ArrayList()
            mainActivity.currentDropPosition =
                layoutManager?.findFirstCompletelyVisibleItemPosition() ?: 0
        } else {
//            mainActivity.followingList = postAdapter?.list ?: ArrayList()
            mainActivity.currentFollowingPosition =
                layoutManager?.findFirstCompletelyVisibleItemPosition() ?: 0
        }
        handlerPostViewed.removeCallbacksAndMessages(null)
        Log.e("safdasf", "onPause")
        PlayerViewAdapter.pauseCurrentPlayingVideo()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        releaseExo()
        Log.e("asjgh", "onDestroyView")

        isSnapAttached = false
    }

    override fun onDestroy() {
        super.onDestroy()
//        releaseExo()

        if (postType == "drop") {
            mainActivity.dropList = postAdapter?.list ?: ArrayList()
//            mainActivity.currentDropPosition = layoutManager?.findFirstCompletelyVisibleItemPosition()?:0
        } else {
            mainActivity.followingList = postAdapter?.list ?: ArrayList()
//            mainActivity.currentFollowingPosition = layoutManager?.findFirstCompletelyVisibleItemPosition()?:0
        }
        Log.e("asjgh", "onDestroy")
        binding = null

    }

    private fun setRecyclerPosition(index: Int) {
        if (postType == "drop") {
//            mainActivity.dropList = postAdapter?.list ?: ArrayList()
            mainActivity.currentDropPosition = index
        } else {
//            mainActivity.followingList = postAdapter?.list ?: ArrayList()
            mainActivity.currentFollowingPosition = index
        }
    }

    private fun setUpUi() {
//        if (postType == "drop")
        setObserver()
        setUpInternetConnectivity()
        binding?.let { binding ->
            Log.e("setUpUi", "true")
            val shimmerBuilder = Shimmer.AlphaHighlightBuilder()
            binding.shimmerLayout.shimmerLayout.setShimmer(
                shimmerBuilder.setDuration(4000L).setRepeatMode(ValueAnimator.REVERSE).build()
            )
            binding.swipeRefresh.setOnRefreshListener {
                if (isInternetAvailable(requireContext())) {
                    isRefresh = true
                    binding.noDataFound.visibility = View.GONE
                    callApi(postType)
                } else {
                    binding.swipeRefresh.isRefreshing = false
                    binding.noDataFound.visibility = View.VISIBLE
                    Utils.showToast(getString(R.string.no_internet_message))
                }
            }
            binding.shimmerLayout.shimmerLayout.startShimmer()
            setAdapter()
            setRecyclerScrollListener()
        }
    }

    private fun setAdapter() {
        cachedDataSourceFactory = PlayerViewAdapter.addCaching(requireContext())

        val recycledViewPool = RecyclerView.RecycledViewPool()

        postAdapter =
            PostAdapter(
                getPostList(), object : PostAdapter.ClickListener {
                    override fun likeClick(position: Int) {
                        currentPostItemClickedPosition = position
                        mViewModel.postLikeRequest =
                            PostRequest(post = postAdapter?.list?.get(position)?.postId ?: "")
                        mViewModel.likePost()
                        postAdapter!!.list[position]!!.isLike =
                            !postAdapter!!.list[position]!!.isLike
                    }

                    override fun commentClick(position: Int, postId: String, view: TextView) {
                        currentPostItemClickedPosition = position
                        commentCountTextView = view
                        val bottomSheetFragment = CommentsBottomSheet(
                            postId,
                            (postAdapter?.list?.get(position)?.creator?._id
                                ?: "") == Prefs.init().userId,
                            this@PostScreen,
                            postAdapter?.list?.get(position)?.noOfComments.toString(),
                            this@PostScreen
                        )

                        bottomSheetFragment.show(childFragmentManager, bottomSheetFragment.tag)
                    }

                    override fun shareClick(position: Int, textView: TextView) {
                        currentPostItemClickedPosition = position
                        val bottomSheetFragment =
                            SharePostUserBottomSheet(postAdapter!!.list[position]!!._id,
                                object : SharePostUserBottomSheet.ClickSharePost {
                                    override fun sharePostClick() {
                                        postAdapter?.list?.get(position).let {
                                            val desc =
                                                "${it?.creator?.name} on Wedo80: ${it?.description ?: ""}"
                                            val imgUrl = if (it?.type == "video") {
                                                it.videoThumbnail
                                            } else {
                                                it?.images?.get(0)
                                            }
                                            val str = generateDeepLink(
                                                DeepLinkType.POST,
                                                postAdapter?.list?.get(position)?._id ?: "",
                                                desc,
                                                imgUrl ?: ""
                                            )
                                            val intent = Intent()
                                            intent.action = Intent.ACTION_SEND
                                            intent.putExtra(
                                                Intent.EXTRA_TEXT, str
                                            )
                                            intent.type = "text/plain"
                                            startActivity(Intent.createChooser(intent, "Share To:"))
                                        }
                                    }

                                    override fun shareUpdate(totalShare: Int) {
                                        if (textView.text.toString().isNotEmpty()) {
                                            textView.text =
                                                textView.text.toString().toInt().plus(totalShare)
                                                    .toString()
                                        } else {
                                            if (totalShare > 0) {
                                                textView.text = totalShare.toString()
                                            }

                                        }
                                    }
                                })



                        bottomSheetFragment.show(childFragmentManager, bottomSheetFragment.tag)
                    }

                    override fun saveClick(position: Int) {
                        mViewModel.postSaveRequest =
                            PostRequest(post = postAdapter?.list?.get(position)?.postId ?: "")
                        mViewModel.postSave()
                        postAdapter!!.list[position]!!.isSaved =
                            !postAdapter!!.list[position]!!.isSaved
                    }

                    override fun saveSoundClick(position: Int) {
                        /*mViewModel.postSaveSoundRequest =
                            PostRequest(sound = postAdapter!!.list[position]!!.sound!!._id)
                        mViewModel.postSaveSound()
                        postAdapter!!.list[position]!!.isSoundSaved =
                            !postAdapter!!.list[position]!!.isSoundSaved*/
                    }

                    override fun followUnfollowClick(position: Int) {
                        currentPostItemClickedPosition = position
                        postAdapter?.list?.get(position)?.creator?.let { creator ->
                            mViewModel.followUnfollowUserRequest = UserRequest(creator._id)
//                            mViewModel.followUnfollowUser()
                            when (creator.followStatus) {
                                1, 3 -> {
                                    if (creator.followStatus == 1) {
                                        mViewModel.followUnfollowUser()
                                    } else {
                                        mViewModel.followUnfollowUserRequest =
                                            UserRequest(creator._id, AppConstant.CANCEL)
                                        mViewModel.postFollowRequestStatus()
                                    }
                                    postAdapter!!.list[position]!!.creator.followStatus = 2
                                    postAdapter!!.list.forEachIndexed { index, data ->
                                        if (data!!.postId != postAdapter!!.list[position]!!.postId &&
                                            data.creator._id == creator._id
                                        ) {
                                            data.creator.followStatus = 2
                                            postAdapter!!.notifyItemChanged(index)
                                        }
                                    }
                                }

                                2 -> {
                                    mViewModel.followUnfollowUser()
                                    if (creator.accountType.equals("private", true)) {
                                        postAdapter!!.list[position]!!.creator.followStatus = 3
                                    } else {
                                        postAdapter!!.list[position]!!.creator.followStatus = 1
                                    }
                                    postAdapter!!.list.forEachIndexed { index, data ->
                                        if (data!!.postId != postAdapter!!.list[position]!!.postId &&
                                            data.creator._id == creator._id
                                        ) {
                                            if (creator.accountType.equals("private", true)) {
                                                data.creator.followStatus = 3
                                            } else {
                                                data.creator.followStatus = 1
                                            }
                                            postAdapter!!.notifyItemChanged(index)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    override fun tagClick(position: Int) {
                        postPosition = position
                        val bottomSheetFragment = postAdapter!!.list[position]!!.tagUsers?.let {
                            TagUserBottomSheet(
                                it,
                                this@PostScreen
                            )
                        }
                        bottomSheetFragment?.show(childFragmentManager, bottomSheetFragment.tag)
                    }

                    override fun tapAndHoldOptionMenu(position: Int, options: Options) {
                        postPosition = position
                        val bottomSheetFragment = OptionBottomSheet(
                            this@PostScreen,
                            postPosition,
                            postAdapter!!.list[position]!!.video ?: "",
                            options,
                            Prefs.init().userId == postAdapter!!.list[position]!!.creator._id
                        )
                        bottomSheetFragment.show(childFragmentManager, bottomSheetFragment.tag)
                    }

                    override fun tapAndHoldOptionMenu(
                        position: Int,
                        imagePosition: Int,
                        options: Options
                    ) {
                        postPosition = position
                        imagePagerPosition = imagePosition
                        val bottomSheetFragment = OptionBottomSheet(
                            this@PostScreen,
                            postPosition,
                            postAdapter!!.list[position]!!.images?.get(imagePosition) ?: "",
                            options,
                            Prefs.init().userId == postAdapter!!.list[position]!!.creator._id
                        )
                        bottomSheetFragment.show(childFragmentManager, bottomSheetFragment.tag)
                    }

                    override fun hashTagClick(text: String) {
//                        setLastVisibleItemPos()
                        Log.e("hashTagClick", text)
                        val bundle = Bundle()
                        bundle.putString("hashtag", text)
                        bundle.putString("type", "hashtag")
                        findNavController().navigate(
                            R.id.action_homeFragment_to_show_profile_post_fragment,
                            bundle
                        )
                    }

                    override fun mentionClick(text: String) {
                        val bundle = Bundle()
                        bundle.putString("mentionUser", text)
                        if (text == Prefs.init().userId) {
                            if (activity != null) {
                                (requireActivity() as MainActivity).profileTabClick()
                            }
                        } else {
                            findNavController().navigate(
                                R.id.action_homeFragment_to_otherProfileFragment,
                                bundle
                            )
                        }

                    }
                }, context ?: MainApplication.get().applicationContext
            )


        layoutManager = LinearLayoutManager(requireContext())
        binding?.postRecycler?.apply {
            setRecycledViewPool(recycledViewPool)
            enableVerticalScroll(true)
            layoutManager = this@PostScreen.layoutManager
            adapter = postAdapter
            val snapHelper: SnapHelper = PagerSnapHelper()
            if (!isSnapAttached) {
                isSnapAttached = true
                snapHelper.attachToRecyclerView(this)
            }
            if (getPostList().isNotEmpty()) {
                this.visibility = View.VISIBLE
//                runWithDelay(4000){
                postAdapter?.setNewItems(getPostList())

//                }
            } else {
            }
//            postAdapter?.notifyDataSetChanged()
        }
    }

    private fun getPostList(): ArrayList<Data?> {
        return if (postType == "drop") {
            mainActivity.dropList
        } else {
            mainActivity.followingList
        }
    }

    private fun setRecyclerScrollListener() {

        binding?.postRecycler?.viewIsLoaded {
            currentRecyclerPositionBeforeScroll =
                layoutManager?.findFirstCompletelyVisibleItemPosition() ?: 0
        }
        scrollListener = object : RecyclerViewScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val totalItemCount = layoutManager?.itemCount ?: 0
                val visibleThresholdDown =
                    recyclerView.height * 0.9 // 90% visibility threshold fpr down scroll
                val firstVisibleItemPosition = layoutManager?.findFirstVisibleItemPosition() ?: 0
                val lastVisibleItemPosition = layoutManager?.findLastVisibleItemPosition() ?: 0
                var position = -1
                var showThumbnail = false

                otherPositionWhileScroll =
                    if (currentRecyclerPositionBeforeScroll == firstVisibleItemPosition) {
                        lastVisibleItemPosition
                    } else {
                        firstVisibleItemPosition
                    }

                if (currentRecyclerPositionBeforeScroll > -1) {
                    val holder = recyclerView.findViewHolderForAdapterPosition(
                        currentRecyclerPositionBeforeScroll
                    )
                    if (holder != null) {
                        val view = holder.itemView
                        val visibleHeight =
                            min(view.bottom, recyclerView.bottom) - max(view.top, recyclerView.top)

                        if (visibleHeight > visibleThresholdDown && visibleHeight > -1) {
                            position = currentRecyclerPositionBeforeScroll
                            showThumbnail = true
                        } else {
                            showThumbnail = false
                            position = otherPositionWhileScroll
                        }
                    }
                }

                if (position > -1) {
                    binding?.postRecycler?.viewIsLoaded {
                        val firstItemView =
                            layoutManager?.findViewByPosition(position)
                        firstItemView?.viewIsLoaded {
                            findLayoutByPosition(
                                position,
                                "setRecyclerScrollListener",
                                showThumbnail
                            )
                        }
                    }
                }
                Log.e("pagination", "$totalItemCount $firstVisibleItemPosition")
                if (!isLoading && (postAdapter?.list?.size!! - 1) == firstVisibleItemPosition) {
                    // Load more items if needed
                    if (isInternetAvailable(requireContext())) {
                        Log.e("pagination", "scrolled $offset")
                        isInternetAvailableWhileScroll = true
                        binding?.paginationAnimation?.visibility = View.VISIBLE
                        isLoading = true
                        offset += 16
                        Log.e("pagination", "scrolled $offset")
                        callNextPage()
                    } else {
                        if (isInternetAvailableWhileScroll) {
                            Utils.showToast(getString(R.string.no_internet_message))
                            isInternetAvailableWhileScroll = false
                        }

                    }
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    currentRecyclerPositionBeforeScroll =
                        layoutManager?.findFirstCompletelyVisibleItemPosition() ?: 0
                    if (currentPage != currentRecyclerPositionBeforeScroll && currentRecyclerPositionBeforeScroll > -1) {
                        binding?.postRecycler?.viewIsLoaded {
                            val firstItemView =
                                layoutManager?.findViewByPosition(
                                    currentRecyclerPositionBeforeScroll
                                )
                            firstItemView?.viewIsLoaded {
                                findLayoutByPosition(
                                    currentRecyclerPositionBeforeScroll,
                                    "onScrollStateChanged"
                                )
                            }
                        }
                    }
                    PlayerViewAdapter.clearScrollItemPlayingTimeMap()
                }
            }

            override fun onItemIsFirstVisibleItem(index: Int, lastItemIndex: Int) {

            }
        }
        binding?.postRecycler?.addOnScrollListener(scrollListener)
    }

    private fun setUpInternetConnectivity() {
        try {
            val connectivityManager =
                requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            val networkRequest = NetworkRequest.Builder().build()
            val activity = activity as MainActivity
            connectivityManager.registerNetworkCallback(
                networkRequest,
                object : ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: Network) {
                        if (!activity.registerNetworkConnected) {
                            CoroutineScope(Dispatchers.Main).launch {
                                binding?.apply {
                                    isRefresh = true
                                    swipeRefresh.isRefreshing = true
                                    if (postAdapter != null) {
                                        if (postAdapter!!.itemCount > 0) {
                                            postRecycler.scrollToPosition(0)
                                        }
                                    }
                                }
                                callApi(postType)
                                activity.registerNetworkConnected = true
                                this.cancel()
                            }
                        }
                    }

                    override fun onLost(network: Network) {
                        activity.registerNetworkConnected = false
                    }
                })
        } catch (_: Exception) {

        }
    }

    override fun ghostMode() {
        Prefs.init().isGhostMode = !Prefs.init().isGhostMode
        EventBus.getDefault().post(isGhostMode(Prefs.init().isGhostMode))
//        postAdapter!!.notifyDataSetChanged()
    }

    override fun notInterestedClick() {
        mViewModel.notInterestedRequest =
            PostRequest(post = postAdapter?.list?.get(postPosition)?.postId ?: "")
        mViewModel.notInterested()
    }

    override fun userRestrictClick() {
        mViewModel.userRestrict(
            UserRestrict(
                postAdapter?.list?.get(postPosition)?.creator?._id ?: ""
            )
        )
    }

    override fun userRepostClick() {
        EventBus.getDefault().post(Repost(postAdapter?.list?.get(postPosition)?.postId ?: ""))

    }

    override fun userReportClick() {
        val bottomSheetFragment = ReportBottomSheet(this)

        bottomSheetFragment.show(childFragmentManager, bottomSheetFragment.tag)
    }

    override fun swapLayout() {
        Prefs.init().isLHPost = !Prefs.init().isLHPost
        val view: View? = binding?.postRecycler?.layoutManager?.findViewByPosition(postPosition)
        if (view != null) {
            val binding1: Any? =
                DataBindingUtil.bind(view)
            if (binding1 is HomeListVideoItemBinding) {
                postAdapter?.reverseVideoItem(binding1)
                //reverse profile info container
                postAdapter?.reverseLinearLayout(binding1.profileInfoContainer)
                postAdapter?.reverseLinearLayout(binding1.profileNameInfoContainer)
            } else if (binding1 is HomeListItemBinding) {
                postAdapter?.reverseImageItem(binding1)
                //reverse profile info container
                postAdapter?.reverseLinearLayout(binding1.profileInfoContainer)
                postAdapter?.reverseLinearLayout(binding1.profileNameInfoContainer)
            }
            postAdapter?.itemCount?.let {
                for (i in 0 until it) {
                    if (i != postPosition) {
                        postAdapter?.notifyItemChanged(i)
                    }
                }
            }
        }
    }

    override fun reportClick(reason: String, isPost: Boolean) {
        if (isPost) {
            mViewModel.postReportRequest = PostRequest(
                post = postAdapter?.list?.get(postPosition)?.postId ?: "",
                reason = reason
            )
            mViewModel.postReport()
        } else {
            mViewModel.commentReport(PostRequest(comment = commentID, reason = reason))
        }

    }

    override fun downloadPost(position: Int) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {

            postAdapter!!.list[position].also {
                lifecycleScope.launch {
                    val downloadSuccess = if (it!!.type == "video") {
                        Utils.downloadFileFromUrl(
                            requireContext(),
                            it.video!!,
                            "${System.currentTimeMillis()}.mp4",
                            "video"
                        )
                    } else {
                        Utils.downloadFileFromUrl(
                            requireContext(),
                            it.images?.get(imagePagerPosition) ?: "",
                            "${System.currentTimeMillis()}.png",
                            "image"
                        )
                    }
                    if (downloadSuccess) {
                        Utils.showToast("File downloaded successfully")
                        // File downloaded successfully
                    } else {
                        Utils.showToast("File downloaded failed")
                        // File download failed
                    }
                }

            }

        } else {
            postAdapter!!.list[position].also {
                if (it!!.type == "video") {
                    Utils.downloadFile(
                        it.video!!,
                        "${System.currentTimeMillis()}.mp4"
                    )
                } else {
                    Utils.downloadFile(
                        it.images?.get(imagePagerPosition) ?: "",
                        "${System.currentTimeMillis()}.png"
                    )
                }
            }
        }
    }

    override fun deletePost() {
        deletePostDialog()
    }

    override fun archivePost() {
        if (isInternetAvailable(requireActivity())) {
            mViewModel.apply {
                deletePostRequest =
                    DeletePost(post = postAdapter?.list?.get(postPosition)?.postId ?: "")
                userArchivePost()
            }
        }
    }

    override fun editPost() {
        val bundle = Bundle()
        bundle.putString("EditPostId", postAdapter!!.list[postPosition]!!.postId)
        PlayerViewAdapter.releaseAllPlayers("editPost")
        findNavController().navigate(R.id.editPostFragment, bundle)
    }

    override fun hashTagClick(text: String) {
        val bundle = Bundle()
        bundle.putString("hashtag", text)
        bundle.putString("type", "hashtag")
        findNavController().navigate(
            R.id.action_homeFragment_to_show_profile_post_fragment,
            bundle
        )
    }

    override fun mentionClick(text: String) {
        if (text == Prefs.init().userId) {
            if (activity != null) {
                (requireActivity() as MainActivity).profileTabClick()
            }
        } else {
            val bundle = Bundle()
            bundle.also {
                it.putString("mentionUser", text)
                findNavController().navigate(
                    R.id.action_homeFragment_to_otherProfileFragment,
                    it
                )
            }
        }

    }

    override fun postInfoClick(position: Int) {
        val parentFrag = requireParentFragment() as HomeFragment

        postInfoDialog(parentFrag.binding!!.headerLayout, postAdapter?.list?.get(position))
    }

    override fun followClick(position: Int) {
        postAdapter!!.list[postPosition]!!.tagUsers?.get(position).let { creator ->
            mViewModel.followUnfollowUserRequest = UserRequest(creator?._id ?: "")

            if (creator != null) {
                when (creator.followStatus) {
                    1, 3 -> {
                        if (creator.followStatus == 1) {
                            mViewModel.followUnfollowUser()
                        } else {
                            mViewModel.followUnfollowUserRequest =
                                UserRequest(creator._id ?: "", AppConstant.CANCEL)
                            mViewModel.postFollowRequestStatus()
                        }
                        creator.followStatus = 2
                        postAdapter!!.list.forEachIndexed { index, data ->
                            if (data!!.postId != postAdapter!!.list[postPosition]!!.postId &&
                                data.creator._id == creator._id
                            ) {
                                data.creator.followStatus = 2
                                postAdapter!!.notifyItemChanged(index)
                            } else {
                                CoroutineScope(Dispatchers.IO).launch {
                                    if (data.tagUsers?.any { it._id == creator._id } == true) {
                                        data.tagUsers.filter { it._id == creator._id }[0].followStatus =
                                            2
                                    }
                                }
                            }
                        }
                    }

                    2 -> {
                        mViewModel.followUnfollowUser()
                        if (creator.accountType.equals("private", true)) {
                            creator.followStatus = 3
                        } else {
                            creator.followStatus = 1
                        }
                        postAdapter!!.list.forEachIndexed { index, data ->
                            if (data!!.postId != postAdapter!!.list[postPosition]!!.postId &&
                                data.creator._id == creator._id
                            ) {
                                if (creator.accountType.equals("private", true)) {
                                    data.creator.followStatus = 3

                                    if (data.tagUsers?.any { it._id == creator._id } == true) {
                                        data.tagUsers.filter { it._id == creator._id }[0].followStatus =
                                            3
                                    }

                                } else {
                                    data.creator.followStatus = 1
                                    //                                CoroutineScope(Dispatchers.IO).launch{
                                    if (data.tagUsers?.any { it._id == creator._id } == true) {
                                        data.tagUsers.filter { it._id == creator._id }[0].followStatus =
                                            1
                                    }
                                    //                                }
                                }
                                postAdapter!!.notifyItemChanged(index)
                            } else {
                                if (creator.accountType.equals("private", true)) {
                                    if (data.tagUsers?.any { it._id == creator._id } == true) {
                                        data.tagUsers.filter { it._id == creator._id }[0].followStatus =
                                            3

                                    } else {
                                        //                                CoroutineScope(Dispatchers.IO).launch{
                                        if (data.tagUsers?.any { it._id == creator._id } == true) {
                                            data.tagUsers.filter { it._id == creator._id }[0].followStatus =
                                                1
                                        }
                                        //                                }
                                    }
                                }
                            }
                        }
                    }

                }
            }
        }
    }


    private fun callApi(type: String) {
        checkIfFragmentAttached {
            if (isInternetAvailable(requireContext())) {
                releaseExo()
//                postAdapter?.notifyDataSetChanged()
                offset = 0
                limit = 16
                mViewModel.postRequest = PostRequest(type = type, offset = offset, limit = limit)
                mViewModel.post()
            } else {
                Utils.showToast(getString(R.string.no_internet_message))
            }
        }


    }

    private fun callNextPage() {
        mViewModel.postRequest = PostRequest(type = postType, offset = offset, limit = limit)
        mViewModel.post()
    }

    private fun callPostViewApi(index: Int) {
        if (postAdapter?.list?.isNotEmpty() == true) {
            mViewModel.postSaveSoundRequest =
                PostRequest(post = postAdapter?.list?.get(index)?._id ?: "")
            handlerPostViewed.removeCallbacksAndMessages(null)
            handlerPostViewed.postDelayed({
                mViewModel.postViewed()
            }, 2000)
        }
    }

    private fun setFollowUnfollow(position: Int) {
        postAdapter?.list?.get(position)?.creator?.let { creator ->
            mViewModel.followUnfollowUserRequest = UserRequest(creator._id)
//                            mViewModel.followUnfollowUser()
            when (creator.followStatus) {
                1, 3 -> {
                    if (creator.followStatus == 1) {
                        mViewModel.followUnfollowUser()
                    } else {
                        mViewModel.followUnfollowUserRequest =
                            UserRequest(creator._id, AppConstant.CANCEL)
                        mViewModel.postFollowRequestStatus()
                    }
                    postAdapter!!.list[position]!!.creator.followStatus = 2
                    postAdapter!!.list.forEachIndexed { index, data ->
                        if (data!!.postId != postAdapter!!.list[position]!!.postId &&
                            data.creator._id == creator._id
                        ) {
                            data.creator.followStatus = 2
                            postAdapter!!.notifyItemChanged(index)
                        }
                    }
                }

                2 -> {
                    mViewModel.followUnfollowUser()
                    if (creator.accountType.equals("private", true)) {
                        postAdapter!!.list[position]!!.creator.followStatus = 3
                    } else {
                        postAdapter!!.list[position]!!.creator.followStatus = 1
                    }
                    postAdapter!!.list.forEachIndexed { index, data ->
                        if (data!!.postId != postAdapter!!.list[position]!!.postId &&
                            data.creator._id == creator._id
                        ) {
                            if (creator.accountType.equals("private", true)) {
                                data.creator.followStatus = 3
                            } else {
                                data.creator.followStatus = 1
                            }
                            postAdapter!!.notifyItemChanged(index)
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("SuspiciousIndentation")
    private fun setObserver() {
        mViewModel.postData.observe(viewLifecycleOwner) { response ->
            if (response != null) {
                if (response.success) {
                    response.data?.let { it ->
                        if (isRefresh == true) {
                            postAdapter?.clear()
                            getPostList().clear()
                            currentPage = -1
                            lastVisibleItemPosition = -1
                            postPosition = -1
//                            releaseExo()
                            Log.e("askhgasd", "$postType $isRefresh")

                        }

                        binding?.swipeRefresh?.isRefreshing = false
                        Log.e("askhgasd", "$postType $isRefresh")

                        if (postAdapter?.list?.isNotEmpty() == true) {
                            val urls = ArrayList<String?>()
                            it.forEach {
                                if (it!!.type == "video") {
                                    urls.add(it.video)
                                }
                            }
                            mViewModel.preloadVideo(urls)
//                            postList.addAll(it)
                            postAdapter?.addNewItems(it)

                            Log.e(
                                "askhgasd",
                                "offset => ${offset}  response ${it.size}  add " + postAdapter!!.list.size.toString()
                            )
                            isRefresh = false
                        } else {
                            val urls = ArrayList<String?>()
                            it.forEach {
                                if (it!!.type == "video") {
                                    urls.add(it.video)
                                }
                            }
                            mViewModel.preloadVideo(urls)
                            if (postType == "drop") {
                                mainActivity.dropList = it
                            } else {
                                mainActivity.followingList = it
                            }
//                            postList = it

                            postAdapter?.setNewItems(getPostList())
                            Log.e("askhgasd", "$postType " + postAdapter!!.list.size.toString())

                            if (isRefresh == true && mainActivity.currentDrop == postType) {
                                isRefresh = false
                                binding?.postRecycler?.viewIsLoaded {
                                    val pos = layoutManager?.findFirstVisibleItemPosition() ?: 0
                                    val firstItemView =
                                        layoutManager?.findViewByPosition(pos)
                                    Log.e("askhgasd", "$postType $pos")

                                    firstItemView?.viewIsLoaded {
                                        if (postAdapter?.list?.isNotEmpty() == true) {
                                            findLayoutByPosition(0, "postData")
                                        }
                                    }
                                }
                            }
                        }


                        isLoading = false
                        binding?.paginationAnimation?.visibility = View.GONE
                        binding?.apply {
                            if (postAdapter?.list?.isEmpty() == true) {
                                noDataFound.visibility = View.VISIBLE
                                postRecycler.visibility = View.GONE
                            } else {
                                noDataFound.visibility = View.GONE
                                postRecycler.visibility = View.VISIBLE
                            }
                        }
                    }
                }
            } else {
                isLoading = false
                binding?.paginationAnimation?.visibility = View.GONE
            }
        }
        mViewModel.postLikeData.observe(viewLifecycleOwner) {

        }
        mViewModel.postSaveData.observe(viewLifecycleOwner) {

        }
        mViewModel.notInterestedData.observe(viewLifecycleOwner) { response ->
            if (response != null) {
                if (response.success) {
                    postAdapter?.list?.removeAt(postPosition)
                    postAdapter?.notifyItemRemoved(postPosition)
                    postAdapter?.notifyItemRangeChanged(postPosition, postAdapter!!.list.size)
//                    runWithDelay(1000){

                    binding?.postRecycler?.viewIsLoaded {
                        binding?.postRecycler?.waitForFirstVisibleItem(Handler(Looper.myLooper()!!)) {
                            val pos = layoutManager?.findFirstVisibleItemPosition() ?: 0
                            val firstItemView =
                                layoutManager?.findViewByPosition(pos)
                            currentPage = -1
                            firstItemView?.viewIsLoaded {
                                if (postAdapter?.list?.isNotEmpty() == true) {
                                    findLayoutByPosition(pos, "notInterestedData")
                                }
                            }
                        }

//                        }
                    }


                    /*  val animator: RecyclerView.ItemAnimator = binding?.postRecycler?.itemAnimator!!
                      if (animator is DefaultItemAnimator) {
                          animator.supportsChangeAnimations = false
                      }*/
                }
            }
        }
        mViewModel.postPinLikeData.observe(viewLifecycleOwner) { response ->
            if (response != null) {
                if (response.success) {
                    PlayerViewAdapter.releaseAllPlayers("postPinLikeData")
                    postAdapter?.list?.removeAt(postPosition)
                    postAdapter?.notifyItemRemoved(postPosition)
                    postAdapter?.notifyItemRangeChanged(postPosition, postAdapter!!.list.size)
                    binding?.postRecycler?.viewIsLoaded {
                        binding?.postRecycler?.waitForFirstVisibleItem(Handler(Looper.myLooper()!!)) {
                            val pos = layoutManager?.findFirstVisibleItemPosition() ?: 0
                            val firstItemView =
                                layoutManager?.findViewByPosition(pos)
                            currentPage = -1
                            firstItemView?.viewIsLoaded {
                                if (postAdapter?.list?.isNotEmpty() == true) {
                                    findLayoutByPosition(pos, "postPinLikeData")
                                }
                            }
                        }

//                        }
                    }

                }
            }
        }
        mViewModel.postReportData.observe(viewLifecycleOwner) { response ->
            if (response != null) {
                if (response.success) {
                    if (commentID != "") {
                        commentID = ""
                        try {
                            if (commentReportText.text == "Report") {
                                commentReportText.text = "Un-\nReport"
                            } else {
                                commentReportText.text = "Report"
                            }
                        } catch (_: Exception) {
                        }

                    } else {
                        postAdapter?.list?.removeAt(postPosition)
                        postAdapter?.notifyItemRemoved(postPosition)
                        postAdapter?.notifyItemRangeChanged(postPosition, postAdapter!!.list.size)
                        binding?.postRecycler?.viewIsLoaded {
                            binding?.postRecycler?.waitForFirstVisibleItem(Handler(Looper.myLooper()!!)) {
                                val pos = layoutManager?.findFirstVisibleItemPosition() ?: 0
                                val firstItemView =
                                    layoutManager?.findViewByPosition(pos)
                                currentPage = -1
                                firstItemView?.viewIsLoaded {
                                    if (postAdapter?.list?.isNotEmpty() == true) {
                                        findLayoutByPosition(pos, "postReportData")
                                    }
                                }
                            }

//                        }
                        }
//                        postAdapter!!.notifyDataSetChanged()

                    }

                }
            }
        }
        mViewModel.userRestrictData.observe(viewLifecycleOwner, EventObserver { response ->
            if (response.success) {
//                    postAdapter!!.list.removeAt(postPosition)
                postAdapter?.list?.removeIf { it!!.creator._id == postAdapter!!.list[postPosition]!!.creator._id }
                currentPage = -1
//                    postAdapter?.notifyItemRemoved(postPosition)
                postAdapter?.notifyItemRangeChanged(postPosition, postAdapter!!.list.size)
                binding?.postRecycler?.viewIsLoaded {
                    binding?.postRecycler?.waitForFirstVisibleItem(Handler(Looper.myLooper()!!)) {
                        val pos = layoutManager?.findFirstVisibleItemPosition() ?: 0
                        val firstItemView =
                            layoutManager?.findViewByPosition(pos)
                        currentPage = -1
                        firstItemView?.viewIsLoaded {
                            if (postAdapter?.list?.isNotEmpty() == true) {
                                findLayoutByPosition(pos, "userRestrictData")
                            }
                        }
                    }

//                        }
                }
            }
        })
        mViewModel.showMessage.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty() && it.contains("body.").not()) {
                Utils.showToast(it)
            }
            val view: View? = binding?.postRecycler?.layoutManager?.findViewByPosition(
                currentPostItemClickedPosition
            )
            if (view != null) {
                Utils.printLog(
                    LogTypeEnum.DEBUG,
                    "onItemIsFirstVisibleItem",
                    "view is not null"
                )
                val binding1: Any? =
                    DataBindingUtil.bind(view)
                if (binding1 is HomeListVideoItemBinding) {
                    binding1.apply {
                        runWithDelay(500) {
                            likeBtn.setImageResource(R.drawable.ic_unlike)
                            totalPostLike.text = ""
                            if (overallLike.text.isNotEmpty() && overallLike.text.toString() != "0") {
                                try {
                                    overallLike.text =
                                        (overallLike.text.toString().toInt() - 1).toString()
                                } catch (_: Exception) {
                                    overallContainer.visibility = View.INVISIBLE
                                }
                            }
                        }
                        saveBtn.setImageResource(R.drawable.ic_unsavepost)
                        saveText.text = ""
                        postAdapter?.list?.get(currentPostItemClickedPosition)?.let {
                            it.isLike = false
                            it.isSaved = false
                        }
                    }
                } else if (binding1 is HomeListItemBinding) {
                    binding1.apply {
                        likeBtn.setImageResource(R.drawable.ic_unlike)
                        runWithDelay(500) {
                            likeBtn.setImageResource(R.drawable.ic_unlike)
                            totalPostLike.text = ""
                            if (overallLike.text.isNotEmpty() && overallLike.text.toString() != "0") {
                                try {
                                    overallLike.text =
                                        (overallLike.text.toString().toInt() - 1).toString()
                                } catch (_: Exception) {
                                    overallContainer.visibility = View.INVISIBLE
                                }
                            }
                        }
                        saveBtn.setImageResource(R.drawable.ic_unsavepost)
                        saveText.text = ""
                        postAdapter?.list?.get(currentPostItemClickedPosition)?.let {
                            it.isLike = false
                            it.isSaved = false
                        }
                    }
                }
            }
        }

        mViewModel.followUnfollowUserData.observe(viewLifecycleOwner) {
            if (it.success) {

                val view: View? = binding?.postRecycler?.layoutManager?.findViewByPosition(
                    currentPostItemClickedPosition
                )
                if (view != null) {
                    Utils.printLog(
                        LogTypeEnum.DEBUG,
                        "onItemIsFirstVisibleItem",
                        "view is not null"
                    )
                    val binding1: Any? =
                        DataBindingUtil.bind(view)
                    if (binding1 is HomeListVideoItemBinding) {
                        if (it.message?.lowercase() == "unfollowed" || it.message?.lowercase() == "request cancelled") {
                            binding1.followStatus.text = "Follow"
                        } else if (it.message?.lowercase() == "followed") {
                            binding1.followStatus.text = "Following"
                        } else {
                            binding1.followStatus.text = "Requested"
                        }
                    } else if (binding1 is HomeListItemBinding) {
                        if (it.message?.lowercase() == "unfollowed" || it.message?.lowercase() == "request cancelled") {
                            binding1.followStatus.text = "Follow"
                        } else if (it.message?.lowercase() == "followed") {
                            binding1.followStatus.text = "Following"
                        } else {
                            binding1.followStatus.text = "Requested"
                        }
                    }
                }
//                setFollowUnfollow(currentPostItemClickedPosition)
            }
        }
        mViewModel.showLoading.observe(viewLifecycleOwner) {
            binding?.apply {
                if (it) {
//                    mainLayout.setBackgroundColor(requireActivity().getColor(R.color.unselectedColor))
                    shimmerLayout.shimmerLayout.also { view ->
                        view.visibility = View.VISIBLE
                        view.startShimmer()
                    }
                } else {
                    binding?.swipeRefresh?.isRefreshing = false
//                    mainLayout.setBackgroundColor(requireActivity().getColor(R.color.black))
                    shimmerLayout.shimmerLayout.also { view ->
                        view.visibility = View.GONE
                        view.startShimmer()
                    }
                }
            }
        }
    }

    private fun releaseExo() {
        PlayerViewAdapter.releaseAllPlayers("releaseExo")
//        mViewModel.postData.postValue(null)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Subscribe
    fun onDoubleTap(isDoubleTap: Any) {
        if (isDoubleTap is isDoubleTap) {
            isDoubleTap.apply {
                targetViewX = targetX
                targetViewY = targetY

                val likePostData = postAdapter?.list?.get(position)
                val id = likePostData?._id ?: likePostData?.postId ?: ""
                if (isDoubleTap.postId == id) {
                    if (likePostData?.isLike == false) {

                        postAdapter?.doubleTapLike(isDoubleTap.position)
                        mViewModel.postLikeRequest = PostRequest(post = postId)
                        runWithDelay(500) {

                            imgView.setImageResource(R.drawable.ic_like)
                            likePostData.noOfLikes?.let {
                                if (it < 1) {
                                    textView.text = ""
                                } else {
                                    textView.text = likePostData.noOfLikes?.toString() ?: ""
                                }
                            }
                            if (likePostData.noOfLikes == null) {
                                textView.text = ""
                            }

                            Log.e(
                                "asasdffgdf",
                                "${postAdapter?.list?.get(isDoubleTap.position)?.creator?.username}  ${likePostData.creator.overallLikes}"
                            )

                            overallTxt.text = likePostData.creator.overallLikes.toString() ?: ""
                        }
                        mViewModel.likePost()
                    }
                }

            }
        } else if (isDoubleTap is isInnerPageScroll) {
            binding?.postRecycler?.enableVerticalScroll(isDoubleTap.isScroll)
        } else if (isDoubleTap is isGhostMode) {
            val view: View? = binding?.postRecycler?.layoutManager?.findViewByPosition(postPosition)
            if (view != null) {
                val binding1: Any? =
                    DataBindingUtil.bind(view)
                if (binding1 is HomeListVideoItemBinding) {
                    postAdapter?.videoGhostMode(binding1)
                } else if (binding1 is HomeListItemBinding) {
                    postAdapter?.ghostMode(binding1)
                }
                postAdapter?.itemCount?.let {
                    for (i in 0 until it) {
                        if (i != postPosition) {
                            postAdapter?.notifyItemChanged(i)
                        }
                    }
                }
            }
        } else if (isDoubleTap is HomeTabClick) {
            if (mainActivity.currentDrop == postType) {
                if (layoutManager?.findFirstVisibleItemPosition() == 0 && context != null) {
                    if (Utils.isInternetAvailables(requireContext())) {
                        CoroutineScope(Dispatchers.Main).launch {
                            binding?.apply {
                                isRefresh = true
                                swipeRefresh.isRefreshing = true
                                if (postAdapter != null) {
                                    if (postAdapter!!.itemCount > 0) {
                                        postRecycler.scrollToPosition(0)
                                    }
                                }
                            }
                            callApi(postType)
                            this.cancel()
                        }
                    } else {
                        Utils.showToast(getString(R.string.no_internet_message))
                    }
                } else {
                    if (!postAdapter?.list.isNullOrEmpty()) {
                        if (postAdapter!!.list.size > 6 && layoutManager!!.findFirstVisibleItemPosition() > 5) {
                            binding?.postRecycler?.scrollToPosition(5)
                            binding?.postRecycler?.smoothScrollToPosition(0)
                        } else {
                            binding?.postRecycler?.smoothScrollToPosition(0)
                        }

                    }

                }
            }
        }
    }


    private fun animateViewToTargetLocation(
        targetViewX: Float,
        targetViewY: Float,
        view: View,
        targetView: View
    ) {

        ObjectAnimator.ofFloat(
            view,
            "translationX",
            targetViewX - view.x
        ).apply {
            duration = 500 // Duration in milliseconds
            start()
        }

        ObjectAnimator.ofFloat(
            view,
            "translationY",
            targetViewY - view.y
        ).apply {
            duration = 500 // Duration in milliseconds
            start()
        }
        val finalWidth = targetView.width
        val finalHeight = targetView.height

// Calculate the delta change in width and height
        val widthChange = (view.width - finalWidth) / 2
        val heightChange = (view.height - finalHeight) / 2
        val scaleXAnimator = ObjectAnimator.ofFloat(
            view,
            "scaleX",
            view.scaleX,
            finalWidth.toFloat() / view.width.toFloat()
        ).apply {
            duration = 500 // Duration in milliseconds
            startDelay = 0 // Delay before starting the animation (if needed)
            interpolator = AccelerateDecelerateInterpolator() // Animation interpolator
        }

        val scaleYAnimator = ObjectAnimator.ofFloat(
            view,
            "scaleY",
            view.scaleY,
            finalHeight.toFloat() / view.height.toFloat()
        ).apply {
            duration = 500 // Duration in milliseconds
            startDelay = 0 // Delay before starting the animation (if needed)
            interpolator = AccelerateDecelerateInterpolator() // Animation interpolator
        }
        AnimatorSet().apply {
            play(scaleXAnimator).with(scaleYAnimator)
            start()
        }
        Utils.runWithDelay(500) { binding?.likeLayout?.removeView(view) }
    }

    private fun addImageViewAtPosition(x: Float, y: Float, view: View) {
        val imageView = ImageView(requireContext())
        imageView.setImageResource(R.drawable.double_tap_like) // Set your image resource here
        val params = RelativeLayout.LayoutParams(
            300,
            300
        )
        params.leftMargin = (x - 300 / 2).toInt()
        params.topMargin = (y - 300 / 2).toInt()
        imageView.layoutParams = params
        imageView.elevation = 5f
        binding?.likeLayout?.addView(imageView)
        val isLeft: Boolean
        val screenWidth = resources.displayMetrics.widthPixels
        isLeft = x <= screenWidth / 2
        imageView.rotation = -40f
        imageView.animate().rotation(0f)
        val scaleAnimation = ScaleAnimation(
            0f, 1f, 0f, 1f,
            Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f
        )
        scaleAnimation.duration = 300 // Animation duration in milliseconds (e.g., 500 milliseconds)

        imageView.startAnimation(scaleAnimation)
        Utils.runWithDelay(300) {
            animateViewToTargetLocation(targetViewX, targetViewY, imageView, view)
        }

        /*val distanceToCenter: Float = abs(x - (screenWidth / 2))
        val animation = if (isLeft) {
            TranslateAnimation(0f, distanceToCenter, 0f, -3000f)
        } else {
            TranslateAnimation(0f, -distanceToCenter, 0f, -3000f)
        }
        animation.duration = 400
        Utils.runWithDelay(300) { imageView.startAnimation(animation) }
        Utils.runWithDelay(700) { binding?.likeLayout?.removeView(imageView) }*/
    }

    fun startCurrentPagePlayerIfAny() {
        Utils.printLog(tag = "currentFragment", msg = "sdf")
        if (binding?.swipeRefresh?.isRefreshing == true) {
            binding?.swipeRefresh?.isRefreshing = false
        }

        if (isGhostMode != Prefs.init().isGhostMode || isLHPost != Prefs.init().isLHPost) {
            postAdapter?.notifyDataSetChanged()
            isGhostMode = Prefs.init().isGhostMode
            isLHPost = Prefs.init().isLHPost
        }

        PlayerViewAdapter.releaseAllPlayers("startCurrentPagePlayerIfAny")

        Utils.printLog(
            tag = "currentFragment",
            msg = "${layoutManager?.findFirstCompletelyVisibleItemPosition()}"
        )

        currentPage = -1

        binding?.root?.viewIsLoaded {
            binding?.postRecycler?.viewIsLoaded {
                val firstItemView =
                    layoutManager?.findViewByPosition(
                        layoutManager?.findFirstCompletelyVisibleItemPosition() ?: 0
                    )
                firstItemView?.viewIsLoaded {
                    findLayoutByPosition(
                        layoutManager?.findFirstCompletelyVisibleItemPosition() ?: 0,
                        "startCurrentPagePlayerIfAny"
                    )
                }
            }
        }
//                    }
        // Remove the listener to avoid multiple calls
        /*it.viewTreeObserver.removeOnGlobalLayoutListener(this)
    }
})
}*/

    }

    private fun findLayoutByPosition(index: Int, str: String, showThumbail: Boolean = true) {
        if (!myCoroutineScope.isActive) {
            myCoroutineScope = CoroutineScope(Dispatchers.Main)
        }
        myCoroutineScope.launch {
            findLayoutByPosition1(index, str, showThumbail)
        }
    }

    private fun findLayoutByPosition1(index: Int, str: String, showThumbail: Boolean = true) {
        if (index == -1 || postAdapter?.list?.isEmpty() == true) {
            return
        }

        Log.e("sdgsf", str)

        layoutManager?.let {
            if (currentPage != index) {
//                setRecyclerPosition(index)
                EventBus.getDefault().post(StartStopAR(true))
                currentPage = index
                Utils.printLog(
                    LogTypeEnum.DEBUG,
                    "onItemIsFirstVisibleItem",
                    "matched $index"
                )
                val view: View? = binding?.postRecycler?.layoutManager?.findViewByPosition(index)
                if (view != null) {
                    callPostViewApi(index)
                    Utils.printLog(
                        LogTypeEnum.DEBUG,
                        "onItemIsFirstVisibleItem",
                        "view is not null"
                    )
                    val binding1: Any? =
                        DataBindingUtil.bind(view)
                    if (::itemBinding.isInitialized) {
                        itemBinding.playBigButton.visibility = View.GONE
                        if (showThumbail)
                            itemBinding.thumbnail.visibility = View.VISIBLE
                    }
                    if (binding1 is HomeListVideoItemBinding) {

                        binding1.playerView.viewIsLoaded {
                            Utils.printLog(
                                LogTypeEnum.DEBUG,
                                "onItemIsFirstVisibleItem",
                                "HomeListVideoItemBinding"
                            )
                            if (context != null) {
                                PlayerViewAdapter.setMediaSourceFromFrag(
                                    requireContext(),
                                    postAdapter?.list?.get(index)?.video,
                                    postAdapter?.list?.get(index)?.postId ?: "",
                                    binding1.playerView
                                ) {
                                    setItemBinding(
                                        binding1, postAdapter?.list?.get(index)?.postId ?: ""
                                    )
                                    PlayerViewAdapter.playCurrentPlayingVideo()
                                }
                            }
                        }
                    } else if (binding1 is HomeListItemBinding) {
                        Utils.printLog(
                            LogTypeEnum.DEBUG,
                            "onItemIsFirstVisibleItem",
                            "HomeListItemBinding"
                        )
                        PlayerViewAdapter.releaseAllPlayers()
                    } else {
                        Utils.printLog(
                            LogTypeEnum.DEBUG,
                            "onItemIsFirstVisibleItem",
                            "not known"
                        )
                    }
                } else {
                    Utils.printLog(
                        LogTypeEnum.DEBUG,
                        "onItemIsFirstVisibleItem",
                        "view null"
                    )
                }
            }
        }


    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setItemBinding(binding1: HomeListVideoItemBinding?, id: String) {
        Utils.printLog(
            LogTypeEnum.DEBUG,
            "onItemIsFirstVisibleItem",
            "setItemBinding"
        )
        if (binding1 != null) {
            itemBinding = binding1
        } else {
            return
        }
        Utils.printLog(
            LogTypeEnum.DEBUG,
            "onItemIsFirstVisibleItem",
            "binding1 not null"
        )
        itemBinding.let { mBinding ->
//            mBinding.playerView.visibility = View.VISIBLE
            mBinding.playerView.player?.seekTo(PlayerViewAdapter.getScrollItemPlayingTime(id))
            mBinding.apply {
                playerView.player?.addListener(object : Player.Listener {
                    override fun onPositionDiscontinuity(
                        oldPosition: Player.PositionInfo,
                        newPosition: Player.PositionInfo,
                        reason: Int
                    ) {
                        playBigButton.visibility = View.GONE
                        seekBar.progress = mBinding.playerView.player?.currentPosition?.toInt() ?: 0
                    }

                    override fun onPlayerError(error: PlaybackException) {
                        super.onPlayerError(error)
                        playBigButton.visibility = View.GONE
                    }


                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        if (isPlaying) {
                            playerView.keepScreenOn = true
                            playBigButton.visibility = View.GONE
                            handlerSeekBar.postDelayed(object : Runnable {
                                override fun run() {
                                    seekBar.progress =
                                        playerView.player?.currentPosition?.toInt() ?: 0
                                    handlerSeekBar.postDelayed(
                                        this,
                                        10
                                    ) // Update SeekBar every second (1000 milliseconds)
                                }
                            }, 10)
                            thumbnail.visibility = View.GONE

                        } else {
                            playerView.keepScreenOn = false
                            handlerSeekBar.removeCallbacksAndMessages(null)
                        }
                    }

                    override fun onPlaybackStateChanged(playbackState: Int) {

                        seekBar.max = playerView.player?.duration?.toInt() ?: 0
                    }

                    override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                        super.onPlayWhenReadyChanged(playWhenReady, reason)
                        if (reason == Player.STATE_BUFFERING) {
//                            progressBar.visibility = View.VISIBLE
                        } else {
//                            progressBar.visibility = View.GONE
                        }
                        if (playWhenReady) {
                            // Video is playing and ready, hide your view


                        } /*else if (!playWhenReady && playerView.player?.playbackState == Player.STATE_READY){
                            // Video is paused, stopped, or buffering, show your view
                            playBigButton.visibility = View.VISIBLE
                            handlerSeekBar.removeCallbacksAndMessages(null)
                        }else{
                            playBigButton.visibility = View.GONE
                        }*/
                        /* if (playWhenReady) {
                             playBigButton.visibility = View.GONE
                         } else {
                             playBigButton.visibility = View.VISIBLE
                         }*/
                    }
                })
            }
        }
    }

    private fun postInfoDialog(view: View, data: Data?) {
        val postInfoDialogBinding = PostInfoLayoutBinding.inflate(layoutInflater)

        // Create a Dialog
        val dialog = Dialog(requireContext())
        dialog.setContentView(postInfoDialogBinding.root)

        val anchorRect = Rect()
        view.getGlobalVisibleRect(anchorRect)

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        // Set dialog position below the anchor view
        val layoutParams = dialog.window?.attributes
        layoutParams?.apply {
            width = WindowManager.LayoutParams.MATCH_PARENT
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = anchorRect.bottom // Position below the anchor view
        }
        Log.e("asdfasg", data.toString())

        dialog.window?.attributes = layoutParams
        val disableColor = requireContext().getColor(R.color.Grey3)
        postInfoDialogBinding.apply {
            data.let {
                if (it?.options?.comment == false) {
                    tvComment.setTextColor(disableColor)
                }
                if (it?.options?.like == false) {
                    tvSeeLikes.setTextColor(disableColor)
                }
                if (it?.options?.download == false) {
                    tvDownload.setTextColor(disableColor)
                }

                Log.e("asdfasg", it?.privacy?.lowercase().toString())
                tvAccountType.text = when (it?.privacy?.lowercase()) {
                    AppConstant.FRIENDS -> AppConstant.FRIENDS.capitalizeFirstChar()
                    AppConstant.EVERYONE -> AppConstant.PUBLIC.capitalizeFirstChar()
                    AppConstant.ONLY_ME -> "Only Me"
                    AppConstant.FOLLOWERS -> AppConstant.FOLLOWERS.capitalizeFirstChar()
                    else -> AppConstant.PRIVATE.capitalizeFirstChar()
                }
                tvDisappearsTime.text = Utils.setDisappearInText(it?.disappearTime ?: "")

                if (it?.createdAt.isNullOrEmpty()) {
                    View.GONE.let {
                        tvDatePosted.visibility = it
                        tvDatePostedLabel.visibility = it
                    }
                } else {
                    tvDatePosted.text = Utils.formatDate(it?.createdAt ?: "")
                }
                ivCancelBtn.setOnClickListener {
                    dialog.dismiss()
                }
            }
        }

        // Optional: Customize dialog properties
        dialog.setCancelable(true) // Set if the dialog is cancellable
        // Other dialog properties setup if needed...

        // Show the dialog
        dialog.show()
    }

    private fun deletePostDialog() {
        dialog = Dialog(requireActivity())
        dialog?.requestWindowFeature(Window.FEATURE_CONTENT_TRANSITIONS)
        dialog?.setCancelable(false)
        Objects.requireNonNull(dialog?.window)?.setBackgroundDrawable(
            ColorDrawable(
                Color.TRANSPARENT
            )
        )
        dialog?.setContentView(R.layout.logout_are_you_sure)

        val logout = dialog?.findViewById(R.id.Logout) as TextView
        val sureLogout = dialog?.findViewById(R.id.SureLoutout) as TextView
        val ivClose = dialog?.findViewById(R.id.ivClose) as ImageView
        logout.text = requireContext().getString(R.string.delete)
        sureLogout.text = requireContext().getString(R.string.delete_permanently)
        ivClose.setOnClickListener {
            dialog?.dismiss()
        }
        logout.setOnClickListener {

            if (isInternetAvailable(requireActivity())) {
                // showProgressDialogs()
                mViewModel.deletePostRequest =
                    DeletePost(post = postAdapter?.list?.get(postPosition)?.postId ?: "")
                mViewModel.userDeletedPost()
            } else {
//                dialogs?.dismiss()
            }
            dialog?.dismiss()
        }
        dialog?.show()
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String) =
            PostScreen().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                }
            }

    }

    override fun deleteClick(commentsDeleted: Int) {
        layoutManager?.findFirstCompletelyVisibleItemPosition()?.let {
            val view: View? = binding?.postRecycler?.layoutManager?.findViewByPosition(it)
            if (view != null) {
                val binding1: Any? =
                    DataBindingUtil.bind(view)
                if (binding1 is HomeListVideoItemBinding) {
                    try {
                        val comments = commentCountTextView.text.toString().trim().toInt()
                        val remainingComments = comments - commentsDeleted
                        if (remainingComments < 1) {
                            commentCountTextView.text = ""
                            postAdapter?.list?.get(currentPostItemClickedPosition)?.noOfComments = 0

                        } else {
                            commentCountTextView.text = remainingComments.toString()
                            postAdapter?.list?.get(currentPostItemClickedPosition)?.noOfComments =
                                remainingComments
                        }
                    } catch (_: Exception) {
                    }

                } else if (binding1 is HomeListItemBinding) {
                    try {
                        val comments = commentCountTextView.text.toString().trim().toInt()
                        val remainingComments = comments - commentsDeleted
                        if (remainingComments < 1) {
                            commentCountTextView.text = ""
                            if (currentPostItemClickedPosition > -1)
                                postAdapter?.list?.get(currentPostItemClickedPosition)?.noOfComments =
                                    0

                        } else {
                            commentCountTextView.text = remainingComments.toString()
                            if (currentPostItemClickedPosition > -1)
                                postAdapter?.list?.get(currentPostItemClickedPosition)?.noOfComments =
                                    remainingComments

                        }
                    } catch (_: Exception) {
                    }
                }
            }
        }
    }

    override fun commentAdded(totalComments: String) {

        layoutManager?.findFirstCompletelyVisibleItemPosition()?.let {
            val view: View? = binding?.postRecycler?.layoutManager?.findViewByPosition(it)
            if (view != null) {
                val binding1: Any? =
                    DataBindingUtil.bind(view)
                if (binding1 is HomeListVideoItemBinding) {
                    try {
                        commentCountTextView.text = totalComments
                        if (totalComments.isNotEmpty()) {
                            if (currentPostItemClickedPosition > -1)
                                postAdapter?.list?.get(currentPostItemClickedPosition)?.noOfComments =
                                    totalComments.toInt()
                        } else {
                            if (currentPostItemClickedPosition > -1)
                                postAdapter?.list?.get(currentPostItemClickedPosition)?.noOfComments =
                                    0
                        }
                    } catch (e: Exception) {
                        if (currentPostItemClickedPosition > -1)
                            postAdapter?.list?.get(currentPostItemClickedPosition)?.noOfComments = 0

                    }

                } else if (binding1 is HomeListItemBinding) {
                    try {
                        commentCountTextView.text = totalComments
                        if (totalComments.isNotEmpty()) {
                            if (currentPostItemClickedPosition > -1)
                                postAdapter?.list?.get(currentPostItemClickedPosition)?.noOfComments =
                                    totalComments.toInt()
                        } else {
                            if (currentPostItemClickedPosition > -1)
                                postAdapter?.list?.get(currentPostItemClickedPosition)?.noOfComments =
                                    0

                        }
                    } catch (e: Exception) {
                        if (currentPostItemClickedPosition > -1)
                            postAdapter?.list?.get(currentPostItemClickedPosition)?.noOfComments = 0
                    }
                }
            }
        }
    }

    override fun commentReport(commentId: String, position: Int, view: TextView) {
        commentReportText = view
        commentID = commentId
        if (view.text.toString() == "Report") {
            val bottomSheetFragment = ReportBottomSheet(this, false)

            bottomSheetFragment.show(childFragmentManager, bottomSheetFragment.tag)
        } else {
            mViewModel.commentReport(PostRequest(comment = commentID, reason = ""))
        }

    }

}