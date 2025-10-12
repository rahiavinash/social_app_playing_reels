package com.wedo8020.app.mainui.home.postFragment

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
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
import com.wedo8020.app.databinding.HomeListVideoItemBinding
import com.wedo8020.app.databinding.PostInfoLayoutBinding
import com.wedo8020.app.eventBus.Repost
import com.wedo8020.app.eventBus.isDoubleTap
import com.wedo8020.app.eventBus.isGhostMode
import com.wedo8020.app.eventBus.isInnerPageScroll
import com.wedo8020.app.mainui.adapter.PostAdapter
import com.wedo8020.app.mainui.adapter.TagUserAdapter
import com.wedo8020.app.mainui.base.BaseFragment
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
import com.wedo8020.app.response.Data
import com.wedo8020.app.response.Options
import com.wedo8020.app.utils.AppConstant
import com.wedo8020.app.utils.CachedDataSourceFactory
import com.wedo8020.app.utils.DeepLinkType
import com.wedo8020.app.utils.EventObserver
import com.wedo8020.app.utils.Extension.viewIsLoaded
import com.wedo8020.app.utils.LogTypeEnum
import com.wedo8020.app.utils.Prefs
import com.wedo8020.app.utils.RecyclerViewScrollListener
import com.wedo8020.app.utils.Utils
import com.wedo8020.app.utils.Utils.capitalizeFirstChar
import com.wedo8020.app.utils.Utils.formatDate
import com.wedo8020.app.utils.VideoCacheManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.util.Objects
import kotlin.math.max
import kotlin.math.min

private const val ARG_PARAM1 = "post_type"

class PostFragment : BaseFragment(), TagUserAdapter.ClickListener, postOptionListeners {

    private var postType = "drop"
    private var lastVisibleItemPosition = -1
    var dialog: Dialog? = null
    private var cachedDataSourceFactory: CachedDataSourceFactory? = null
    private var player: Player? = null
    private lateinit var itemBinding: HomeListVideoItemBinding
    private var binding: FragmentDropsBinding? = null
    private var postAdapter: PostAdapter? = null
    private val mViewModel by viewModels<PostViewModel>()


    private var offset = 0
    private var limit = 5
    private var isLoading = false

    //    private var isDrop = true
    private var postPosition = -1
    private var imagePagerPosition = -1
    private var postList = ArrayList<Data?>()

    /*private var dropPostList = ArrayList<Data?>()
    private var followingPostList = ArrayList<Data?>()*/
    private lateinit var scrollListener: RecyclerViewScrollListener
    private var layoutManager: LinearLayoutManager? = null
    private var currentPage = -1
    private var isSnapAttached = false

    val handlerSeekBar = Handler(Looper.myLooper()!!)
    val handlerPostViewed = Handler(Looper.myLooper()!!)
    private lateinit var videoCacheManager: VideoCacheManager
    private var isViewCreated = false
    private var targetViewX = 0.0f
    private var targetViewY = 0.0f

//    private var lastFullyVisibleItem = 0

    companion object {

        @JvmStatic
        fun newInstance(param1: String) =
            PostFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                }
            }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            postType = it.getString(ARG_PARAM1) ?: "drop"
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentDropsBinding.inflate(inflater, container, false)

        setUpUi()


        showNavBottom()
        Utils.printLog(tag = "dataType", msg = postType)
        if (!isViewCreated) {
            callApi(postType)
        }

        isViewCreated = true

//        callApi()
        return binding!!.root
    }


    private fun setUpUi() {
        videoCacheManager = VideoCacheManager(requireContext())
        setObserver()
        binding?.let { binding ->
            Log.e("setUpUi", "true")
            val shimmerBuilder = Shimmer.AlphaHighlightBuilder()
            binding.shimmerLayout.shimmerLayout.setShimmer(
                shimmerBuilder.setDuration(4000L).setRepeatMode(ValueAnimator.REVERSE).build()
            )
            binding.swipeRefresh.setOnRefreshListener {
                callApi(postType)
            }
            binding.shimmerLayout.shimmerLayout.startShimmer()
            setAdapter()
            setRecyclerScrollListener()
        }
    }

    private fun setAdapter() {
//        cachedDataSourceFactory = PlayerViewAdapter.addCaching(requireContext())

        val recycledViewPool = RecyclerView.RecycledViewPool()

        postAdapter =
            PostAdapter(
                postList, object : PostAdapter.ClickListener {
                    override fun likeClick(position: Int) {
                        mViewModel.postLikeRequest =
                            PostRequest(post = postAdapter?.list?.get(position)?.postId ?: "")
                        mViewModel.likePost()
                        postAdapter!!.list[position]!!.isLike =
                            !postAdapter!!.list[position]!!.isLike
                    }

                    override fun commentClick(position: Int, postId: String, view: TextView) {

                    }



                    override fun shareClick(position: Int, textView: TextView) {
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
                      /*  mViewModel.postSaveSoundRequest =
                            PostRequest(sound = postAdapter!!.list[position]!!.sound!!._id)
                        mViewModel.postSaveSound()
                        postAdapter!!.list[position]!!.isSoundSaved =
                            !postAdapter!!.list[position]!!.isSoundSaved*/
                    }

                    override fun followUnfollowClick(position: Int) {
                        postAdapter!!.list[position]!!.creator.let { creator ->
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
                                this@PostFragment
                            )
                        }
                        bottomSheetFragment?.show(childFragmentManager, bottomSheetFragment.tag)
                    }

                    override fun tapAndHoldOptionMenu(position: Int, options: Options) {
                        postPosition = position
                        val bottomSheetFragment = OptionBottomSheet(
                            this@PostFragment,
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
                            this@PostFragment,
                            postPosition,
                            postAdapter!!.list[position]!!.images?.get(imagePosition) ?: "",
                            options,
                            Prefs.init().userId == postAdapter!!.list[position]!!.creator._id
                        )
                        bottomSheetFragment.show(childFragmentManager, bottomSheetFragment.tag)
                    }

                    override fun hashTagClick(text: String) {
//                        setLastVisibleItemPos()
                        val bundle = Bundle()
                        bundle.putString("hashtag", text)
                        bundle.putString("type", "hashtag")
                        findNavController().navigate(
                            R.id.action_homeFragment_to_show_profile_post_fragment,
                            bundle
                        )
                        /*val bundle = Bundle()
                        bundle.putString("hashtag", text)
                        findNavController().navigate(
                            R.id.action_homeFragment_to_searchFragment,
                            bundle
                        )*/
                    }

                    override fun mentionClick(text: String) {
                        val bundle = Bundle()
                        bundle.putString("mentionUser", text)
                        findNavController().navigate(
                            R.id.action_homeFragment_to_otherProfileFragment,
                            bundle
                        )
                    }
                },context?:MainApplication.get().applicationContext)


        layoutManager = LinearLayoutManager(requireContext())
        binding?.postRecycler?.apply {
            setRecycledViewPool(recycledViewPool)
            enableVerticalScroll(true)
            layoutManager = this@PostFragment.layoutManager
            adapter = postAdapter
            val snapHelper: SnapHelper = PagerSnapHelper()
            if (!isSnapAttached) {
                isSnapAttached = true
                snapHelper.attachToRecyclerView(this)
            }

            if (lastVisibleItemPosition > -1) {

                scrollToPosition(lastVisibleItemPosition)
                if (postAdapter!!.list[lastVisibleItemPosition]!!.type == "video") {

                    binding?.root?.viewIsLoaded {
                        binding?.postRecycler?.viewIsLoaded {
                            val firstItemView =
                                layoutManager?.findViewByPosition(lastVisibleItemPosition)
                            firstItemView?.viewIsLoaded {
                                releaseExo()
                                findLayoutByPosition(lastVisibleItemPosition)
                            }
                        }
                    }
                    /*binding?.postRecycler?.viewTreeObserver?.addOnGlobalLayoutListener(object :
                        ViewTreeObserver.OnGlobalLayoutListener {
                        override fun onGlobalLayout() {
                            val firstItemView =
                                layoutManager?.findViewByPosition(lastVisibleItemPosition)

                            firstItemView?.viewTreeObserver?.addOnGlobalLayoutListener(object :
                                ViewTreeObserver.OnGlobalLayoutListener {
                                override fun onGlobalLayout() {
                                    releaseExo()
                                    findLayoutByPosition(lastVisibleItemPosition)
                                    firstItemView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                                }
                            })
                            binding?.postRecycler?.viewTreeObserver?.removeOnGlobalLayoutListener(
                                this
                            )
                        }
                    })*/
                }
                visibility = View.VISIBLE
            }
        }
    }

    fun startCurrentPagePlayerIfAny() {
        Utils.printLog(tag = "currentFragment", msg = "sdf")
        PlayerViewAdapter.releaseAllPlayers()

        /*  binding?.postRecycler?.let {
              it.viewTreeObserver?.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                  override fun onGlobalLayout() {*/
        // This method will be called when the RecyclerView has finished laying out its items
        // Perform the action you want to execute after views are initialized here
        Utils.printLog(
            tag = "currentFragment",
            msg = "${layoutManager?.findFirstCompletelyVisibleItemPosition()}"
        )


        currentPage = -1
        /*if (layoutManager?.findFirstCompletelyVisibleItemPosition()==-1){
            startCurrentPagePlayerIfAny()
        }else{*/
        binding?.root?.viewIsLoaded {
            binding?.postRecycler?.viewIsLoaded {
                val firstItemView =
                    layoutManager?.findViewByPosition(
                        layoutManager?.findFirstCompletelyVisibleItemPosition() ?: 0
                    )
                firstItemView?.viewIsLoaded {
//                    releaseExo()
                    findLayoutByPosition(
                        layoutManager?.findFirstCompletelyVisibleItemPosition() ?: 0
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

    private fun findLayoutByPosition(index: Int) {
//        releaseExo()
        if (index == -1) {
            return
        }

        layoutManager?.let {
            //            val position = layoutManager.findLastVisibleItemPosition()
            Utils.printLog(
                LogTypeEnum.DEBUG,
                "onItemIsFirstVisibleItem",
                "currentPage = $currentPage position=> $index "
            )

            if (currentPage != index) {
                currentPage = index
                val view: View? = binding?.postRecycler?.layoutManager?.findViewByPosition(index)
                if (view != null) {
                    mViewModel.postSaveSoundRequest =
                        PostRequest(post = postAdapter?.list?.get(index)?._id ?: "")
                    handlerPostViewed.removeCallbacksAndMessages(null)
                    handlerPostViewed.postDelayed({
                        mViewModel.postViewed()
                    }, 2000)

                    Utils.printLog(
                        LogTypeEnum.DEBUG,
                        "onItemIsFirstVisibleItem",
                        "view is not null"
                    )
                    val binding1: Any? =
                        DataBindingUtil.bind(view)
                    if (binding1 is HomeListVideoItemBinding) {
//                        binding1.playerView.visibility = View.GONE
                        Utils.printLog(
                            LogTypeEnum.DEBUG,
                            "setMediaSourceFromFrag",
                            postAdapter!!.list[index].toString()
                        )
                        PlayerViewAdapter.setMediaSourceFromFrag(requireContext(),
                            postAdapter!!.list[index]!!.video,
                            postAdapter?.list?.get(index)?.postId ?: "",
                            binding1.playerView
                        ) {
                            setItemBinding(
                                binding1, postAdapter?.list?.get(index)?.postId ?: ""
                            )
                            PlayerViewAdapter.playCurrentPlayingVideo()
                        }

                    } else {
                        PlayerViewAdapter.pauseCurrentPlayingVideo()
                    }
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setItemBinding(binding1: HomeListVideoItemBinding?, id: String) {
//        hideKeyboard()
        if (binding1 != null) {
            itemBinding = binding1
        } else {
            return
        }

        itemBinding.let { mBinding ->
            mBinding.playerView.visibility = View.VISIBLE
            player = mBinding.playerView.player

            player?.seekTo(PlayerViewAdapter.getScrollItemPlayingTime(id))

            mBinding.apply {

                playerView.player?.addListener(object : Player.Listener {
                    override fun onPositionDiscontinuity(
                        oldPosition: Player.PositionInfo,
                        newPosition: Player.PositionInfo,
                        reason: Int
                    ) {
                        playBigButton.visibility = View.GONE
//                        thumbnail.visibility = View.GONE
//                        playBigButton.visibility = View.GONE

                        seekBar.progress = mBinding.playerView.player?.currentPosition?.toInt() ?: 0
                    }

                    override fun onPlayerError(error: PlaybackException) {
                        super.onPlayerError(error)
                        playBigButton.visibility = View.GONE

                        Utils.printLog(LogTypeEnum.ERROR, "playerView controller", error.toString())
                    }


                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        if (isPlaying) {
                            playBigButton.visibility = View.GONE
//                            thumbnail.visibility = View.GONE
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
                        } else {
                            playBigButton.visibility = View.VISIBLE
                            handlerSeekBar.removeCallbacksAndMessages(null)
                        }
                    }

                    override fun onPlaybackStateChanged(playbackState: Int) {
                        seekBar.max = playerView.player?.duration?.toInt() ?: 0
                    }

                    override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                        super.onPlayWhenReadyChanged(playWhenReady, reason)

                        if (playWhenReady) {
                            playBigButton.visibility = View.GONE
                            thumbnail.visibility = View.GONE
                        } else {

                            playBigButton.visibility = View.VISIBLE
                        }
                    }

                })
            }


        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Subscribe
    fun onDoubleTap(isDoubleTap: Any) {
        if (isDoubleTap is isDoubleTap) {
            isDoubleTap.apply {
//            addImageViewAtPosition(x,y)
                targetViewX = targetX
                targetViewY = targetY

//                addImageViewAtPosition(x, y, imgView)
                if (!postAdapter!!.list[position]!!.isLike) {
                    mViewModel.postLikeRequest = PostRequest(post = postId)
                    imgView.isEnabled = false
                    Utils.runWithDelay(500) {
                        imgView.isEnabled = true
                        imgView.setImageResource(R.drawable.ic_like)
                        if (textView.text.toString().isNotEmpty()) {
                            textView.text = (textView.text.toString().toInt() + 1).toString()

                        }
                    }


                    mViewModel.likePost()
                    postAdapter!!.doubleTapLike(isDoubleTap.position)
                }
            }
        } else if (isDoubleTap is isInnerPageScroll) {
            binding?.postRecycler?.enableVerticalScroll(isDoubleTap.isScroll)
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

    private fun setLastVisibleItemPos() {
        lastVisibleItemPosition = layoutManager!!.findFirstCompletelyVisibleItemPosition()
        currentPage = -1
    }

    private fun setRecyclerScrollListener() {
        scrollListener = object : RecyclerViewScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val visibleItemCount = layoutManager?.childCount ?: 0
                val totalItemCount = layoutManager?.itemCount ?: 0

                val visibleThreshold = recyclerView.height * 0.1 // 10% visibility threshold
                val visibleThreshold90 = recyclerView.height * 0.9 // 90% visibility threshold

                val firstVisibleItemPosition = layoutManager?.findFirstVisibleItemPosition() ?: 0
                val lastVisibleItemPosition = layoutManager?.findLastVisibleItemPosition() ?: 0
                var position = -1
                val height = -1
                for (i in firstVisibleItemPosition..lastVisibleItemPosition) {
                    val holder = recyclerView.findViewHolderForAdapterPosition(i)
                    if (holder != null) {
                        val view = holder.itemView
                        val visibleHeight =
                            min(view.bottom, recyclerView.bottom) - max(view.top, recyclerView.top)

                        if ((dy > 0 &&
                                    visibleHeight > visibleThreshold &&
                                    visibleHeight > height) || dy < 0 && visibleHeight > visibleThreshold90
                        ) {
                            // Scrolling up and second item visible more than 10% of screen or
                            position = i
                        }
                    }
                }

                if (position > -1) {
                    binding?.root?.viewIsLoaded {
                        binding?.postRecycler?.viewIsLoaded {
                            val firstItemView =
                                layoutManager?.findViewByPosition(position)
                            firstItemView?.viewIsLoaded {
//                    releaseExo()
                                findLayoutByPosition(position)
                            }
                        }
                    }

                }


                if (!isLoading && totalItemCount <= (firstVisibleItemPosition + visibleItemCount + 4)) {
                    // Load more items if needed

                    isLoading = true
                    offset += 5
                    callNextPage()
                } else {


                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    PlayerViewAdapter.clearScrollItemPlayingTimeMap()
                }
            }

            override fun onItemIsFirstVisibleItem(index: Int, lastItemIndex: Int) {

            }
        }
        binding?.postRecycler?.addOnScrollListener(scrollListener)
    }

    fun callApi() {
        val isInternetAvailable = isInternetAvailable(requireActivity())
        if (isInternetAvailable) {
            mViewModel.userDertails()
        }
    }

    private fun callApi(type: String) {

        releaseExo()
        postAdapter!!.notifyDataSetChanged()
        offset = 0
        limit = 5

        mViewModel.postRequest = PostRequest(type = type, offset = offset, limit = limit)
        mViewModel.post()
    }

    private fun callNextPage() {
        mViewModel.postRequest = PostRequest(type = postType, offset = offset, limit = limit)
        mViewModel.post()
    }

    private fun setObserver() {
        mViewModel.postData.observe(viewLifecycleOwner) { response ->
            if (response != null) {
                if (response.success) {
                    response.data?.let { it ->
                        if (binding?.swipeRefresh?.isRefreshing == true){
                            postAdapter?.clear()
                            currentPage = -1
                            lastVisibleItemPosition = -1
                            postPosition = -1
                            releaseExo()
                            setAdapter()
                        }
                        binding?.swipeRefresh?.isRefreshing = false
                        Log.e("postAPI response", it.toString())

//                        mViewModel.preloadVideo(it,cachedDataSourceFactory!!)
                        Log.e("dropsFragment", "drop")
                        if (postList.size > 0) {
                            val urls = ArrayList<String?>()
                            it.forEach {
                                if (it!!.type == "video") {
                                    urls.add(it.video)
                                }
                            }
                            videoCacheManager.preloadVideos(urls)
                            Log.e(
                                "dropsFragment",
                                "add item to drop" + postList.size.toString()
                            )
//                            if (dataType == "drop") {
                            postAdapter!!.addNewItems(it)
//                            }

                        } else {
                            val urls = ArrayList<String?>()
                            it.forEach {
                                if (it!!.type == "video") {
                                    urls.add(it.video)
                                }
                            }
                            videoCacheManager.preloadVideos(urls)
                            postList = it
                            Log.e("dropsFragment", "add to drop" + postList.size.toString())
//                            if (dataType == "drop") {
                            postAdapter?.setNewItems(postList)
//                            }

                        }


                        isLoading = false

                        binding?.apply {
                            // shimmerLayout.shimmerLayout.stopShimmer()
                            postRecycler.visibility = View.VISIBLE
                        }
                    }
                }
            } else {
                isLoading = false
            }
        }
        mViewModel.notInterestedData.observe(viewLifecycleOwner) { response ->
            if (response != null) {
                if (response.success) {
                    postAdapter!!.list.removeAt(postPosition)
                    postAdapter!!.notifyItemRemoved(postPosition)
                    postAdapter!!.notifyDataSetChanged()
                    val animator: RecyclerView.ItemAnimator = binding?.postRecycler?.itemAnimator!!
                    if (animator is DefaultItemAnimator) {
                        animator.supportsChangeAnimations = false
                    }
                }
            }
        }
        mViewModel.postPinLikeData.observe(viewLifecycleOwner) { response ->
            if (response != null) {
                if (response.success) {
                    PlayerViewAdapter.releaseAllPlayers()
                    postAdapter!!.list.removeAt(postPosition)
                    postAdapter!!.notifyItemRemoved(postPosition)
                    val animator: RecyclerView.ItemAnimator = binding?.postRecycler?.itemAnimator!!
                    if (animator is DefaultItemAnimator) {
                        animator.supportsChangeAnimations = false
                    }
                }
            }
        }
        mViewModel.postReportData.observe(viewLifecycleOwner) { response ->
            if (response != null) {
                if (response.success) {
                    postAdapter!!.list.removeAt(postPosition)
                    postAdapter!!.notifyItemRemoved(postPosition)
                    postAdapter!!.notifyDataSetChanged()
                    val animator: RecyclerView.ItemAnimator = binding?.postRecycler?.itemAnimator!!
                    if (animator is DefaultItemAnimator) {
                        animator.supportsChangeAnimations = false
                    }
                }
            }
        }
            mViewModel.restrictResponseData.observe(viewLifecycleOwner, EventObserver { response ->
            if (response != null) {
                if (response.success) {
//                    postAdapter!!.list.removeAt(postPosition)
                    postAdapter!!.list.removeIf { it!!.creator._id == postAdapter!!.list[postPosition]!!.creator._id }
                    postAdapter!!.notifyItemRemoved(postPosition)
                    postAdapter!!.notifyDataSetChanged()
                    val animator: RecyclerView.ItemAnimator = binding?.postRecycler?.itemAnimator!!
                    if (animator is DefaultItemAnimator) {
                        animator.supportsChangeAnimations = false
                    }
                }
            }
        })


        mViewModel.showLoading.observe(viewLifecycleOwner) {
            binding?.apply {
                if (it) {
                    mainLayout.setBackgroundColor(requireActivity().getColor(R.color.unselectedColor))
                    shimmerLayout.shimmerLayout.also { view ->
                        view.visibility = View.VISIBLE
                        view.startShimmer()
                    }
                } else {
                    mainLayout.setBackgroundColor(requireActivity().getColor(R.color.black))
                    shimmerLayout.shimmerLayout.also { view ->
                        view.visibility = View.GONE
                        view.startShimmer()
                    }
                }
            }
        }
    }

    private fun releaseExo() {

        PlayerViewAdapter.releaseAllPlayers()
        mViewModel.postData.postValue(null)
    }

    override fun ghostMode() {
        Prefs.init().isGhostMode = !Prefs.init().isGhostMode
        EventBus.getDefault().post(isGhostMode(Prefs.init().isGhostMode))
        postAdapter!!.notifyDataSetChanged()
    }

    override fun notInterestedClick() {
        mViewModel.notInterestedRequest =
            PostRequest(post = postAdapter?.list?.get(postPosition)?.postId ?: "")
        mViewModel.notInterested()
    }

    override fun userRestrictClick() {
        mViewModel.postRestrict(PostRestrict(post = postAdapter!!.list[postPosition]!!.creator._id))
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
        postAdapter!!.notifyDataSetChanged()
    }

    override fun reportClick(reason: String,isPost: Boolean) {
        mViewModel.postReportRequest = PostRequest(
            post = postAdapter?.list?.get(postPosition)?.postId ?: "",
            reason = reason
        )
        mViewModel.postReport()
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
        PlayerViewAdapter.releaseAllPlayers()
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
        /*val bundle = Bundle()
        bundle.putString("hashtag", text)
        findNavController().navigate(R.id.action_homeFragment_to_searchFragment, bundle)*/
    }

    override fun mentionClick(text: String) {
        val bundle = Bundle()
        bundle.also {
            it.putString("mentionUser", text)
            findNavController().navigate(
                R.id.action_homeFragment_to_otherProfileFragment,
                it
            )
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

    private fun startExo() {
        player?.playWhenReady = true
        PlayerViewAdapter.playCurrentPlayingVideo()
    }

    override fun onResume() {
        super.onResume()
        startExo()
//        findLayoutByPosition(layoutManager?.findFirstCompletelyVisibleItemPosition()?:0)
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onPause() {
        super.onPause()

        videoCacheManager.release()
        player?.playWhenReady = false
        PlayerViewAdapter.pauseCurrentPlayingVideo()
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        releaseExo()
        destroyAllResources()
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

    private fun destroyAllResources() {
        setLastVisibleItemPos()
        binding = null
        layoutManager = null
        postAdapter = null
        isSnapAttached = false
        cachedDataSourceFactory = null
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

        dialog.window?.attributes = layoutParams
        val disableColor = requireContext().getColor(R.color.Grey2)
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

                tvAccountType.text = when (it?.accountType?.lowercase()) {
                    AppConstant.PUBLIC -> AppConstant.PUBLIC.capitalizeFirstChar()
                    AppConstant.BUSINESS -> AppConstant.BUSINESS.capitalizeFirstChar()
                    else -> AppConstant.PRIVATE.capitalizeFirstChar()
                }
                tvDisappearsTime.text = Utils.setDisappearInText(it?.disappearTime ?: "")

                tvDatePosted.text = formatDate(it?.createdAt ?: "")

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

}