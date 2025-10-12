package com.wedo8020.app.mainui.home.profiletab

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
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.Window
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
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
import com.wedo8020.app.databinding.FragmentShowProfilePostBinding
import com.wedo8020.app.databinding.HomeListItemBinding
import com.wedo8020.app.databinding.HomeListVideoItemBinding
import com.wedo8020.app.databinding.PostInfoLayoutBinding
import com.wedo8020.app.eventBus.Repost
import com.wedo8020.app.eventBus.isDoubleTap
import com.wedo8020.app.eventBus.isGhostMode
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
import com.wedo8020.app.mainui.home.adapter.ActivePostAdapter
import com.wedo8020.app.mainui.home.postFragment.PostViewModel
import com.wedo8020.app.postOptionListeners
import com.wedo8020.app.request.DeletePost
import com.wedo8020.app.request.HashtagPayload
import com.wedo8020.app.request.PostRequest
import com.wedo8020.app.request.PostRestrict
import com.wedo8020.app.request.ProfilePostRequest
import com.wedo8020.app.request.UserRequest
import com.wedo8020.app.request.UserRestrict
import com.wedo8020.app.response.Data
import com.wedo8020.app.response.Options
import com.wedo8020.app.utils.ActivePostUpdate
import com.wedo8020.app.utils.AppConstant
import com.wedo8020.app.utils.CachedDataSourceFactory
import com.wedo8020.app.utils.DeepLinkType
import com.wedo8020.app.utils.EventObserver
import com.wedo8020.app.utils.Extension.viewIsLoaded
import com.wedo8020.app.utils.Extension.waitForFirstVisibleItem
import com.wedo8020.app.utils.LogTypeEnum
import com.wedo8020.app.utils.Prefs
import com.wedo8020.app.utils.RecyclerViewScrollListener
import com.wedo8020.app.utils.SaveDropUpdate
import com.wedo8020.app.utils.Utils
import com.wedo8020.app.utils.Utils.capitalizeFirstChar
import com.wedo8020.app.utils.Utils.fromJsonToGson
import com.wedo8020.app.utils.Utils.fromJsonToList
import com.wedo8020.app.utils.Utils.runWithDelay
import com.wedo8020.app.utils.VideoCacheManager
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

private const val POSITION = "position"
private const val DATA_LIST = "dataList"
private const val TYPE = "type"
private const val HASHTAG = "hashtag"


class ShowProfilePostFragment : BaseFragment(), TagUserAdapter.ClickListener, postOptionListeners,
    CommentsBottomSheet.ClickListeners {
    private var binding: FragmentShowProfilePostBinding? = null
    private lateinit var commentReportText: TextView
    private lateinit var commentCountTextView: TextView
    private var commentID = ""
    var dialog: Dialog? = null
    private var hashtagId = ""
    private var id: String? = null
    private var type: String = ""
    private var postAdapter: PostAdapter? = null
    private var cachedDataSourceFactory: CachedDataSourceFactory? = null
    private var player: Player? = null
    private lateinit var itemBinding: HomeListVideoItemBinding
    private var offset = 0
    private var lastVisibleItemPosition = -1
    private var currentPage = -1
    val handlerSeekBar = Handler(Looper.myLooper()!!)
    private var isLoading = false
    private lateinit var scrollListener: RecyclerViewScrollListener
    private var layoutManager: LinearLayoutManager? = null
    private var isSnapAttached = false
    private val mViewModel by viewModels<PostViewModel>()
    private var postList = ArrayList<Data?>()
    private var postPosition = -1
    private var imagePagerPosition = -1
    private var targetViewX = 0.0f
    private var targetViewY = 0.0f
    private var deepLinkId: String? = null
    private var ownProfile: Boolean = false
    private var dataRequestMap: HashMap<String, String> = HashMap()
    private val handlerPostViewed by lazy {
        Handler(Looper.myLooper()!!)
    }
    private var originalSoftInputMode = 0
    private var currentPostItemClickedPosition = -1
    private var myCoroutineScope = CoroutineScope(Dispatchers.Main)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

            hashtagId = it.getString(HASHTAG) ?: ""
            type = it.getString(TYPE) ?: "single"
            if (it.containsKey("ownProfile")) {
                ownProfile = it.getBoolean("ownProfile")
            }

            Log.e("afg", type)
            if (type != "hashtag") {
                if (type == "single") {
                    deepLinkId = it.getString(DATA_LIST)
                    /*val data = it.getString(DATA_LIST)?.fromJsonToGson<Data>()*/
                    Utils.printLog(tag = "single", msg = deepLinkId.toString())
//                    postList.add(data)
                } else {
                    lastVisibleItemPosition = it.getInt(POSITION)
                    val list = it.getString(DATA_LIST)?.fromJsonToList<Data>()
                    list?.forEach {
                        postList.add(it)
                    }
                    offset = postList.size - 20
                }
            }
        }
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
        PlayerViewAdapter.releaseAllPlayers()
        binding = FragmentShowProfilePostBinding.inflate(inflater, container, false)
        hideNavBottom()

        return binding!!.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpUI()
    }

    private fun setUpUI() {
        setObserver()
        setAdapter()
        setRecyclerScrollListener()
        binding?.let {
            Log.e("safhdkasbfjka", type)
            when (type) {
                "hashtag" -> {
//                    runWithDelay(5000){
                    mViewModel.getHashtagPost(HashtagPayload(offset, 20, "", hashtagId))
//                    }
                }

                "single" -> {
//                    runWithDelay(3000){
                    if (PlayerViewAdapter.player == null) {
                        Log.e("safhdkasbfjka", "deletePostRequest")
                        mViewModel.deletePostRequest =
                            DeletePost(
                                post = deepLinkId ?: ""
                            )

                        mViewModel.userPostDetail()
                    } else {
                        Log.e("safhdkasbfjka", "not null")
                    }

//                    }
                }

                else -> {
                    it.dropText.visibility = View.VISIBLE
                    it.tvUsername.text = postList[0]?.creator?.name
                }
            }
            it.ivBack.setOnClickListener {
                findNavController().navigateUp()

            }

        }
    }

    private fun setAdapter() {
        cachedDataSourceFactory = PlayerViewAdapter.addCaching(requireContext())

        val recycledViewPool = RecyclerView.RecycledViewPool()

        postAdapter =
            PostAdapter(
                postList, object : PostAdapter.ClickListener {
                    override fun likeClick(position: Int) {
                        currentPostItemClickedPosition = position
                        mViewModel.postLikeRequest =
                            PostRequest(
                                post = postAdapter?.list?.get(position)?.postId
                                    ?: postAdapter?.list?.get(position)?._id ?: ""
                            )
                        mViewModel.likePost()
                        postAdapter!!.list[position]!!.isLike =
                            !postAdapter!!.list[position]!!.isLike
                    }

                    override fun commentClick(position: Int, postId: String, view: TextView) {
                        commentCountTextView = view
                        val bottomSheetFragment =
                            CommentsBottomSheet(
                                postId,
                                (postAdapter?.list?.get(position)?.creator?._id
                                    ?: "") == Prefs.init().userId, this@ShowProfilePostFragment,
                                postAdapter?.list?.get(position)?.noOfComments.toString(),
                                this@ShowProfilePostFragment
                            )
                        bottomSheetFragment.show(childFragmentManager, bottomSheetFragment.tag)
                    }

                    override fun shareClick(position: Int, textView: TextView) {
                        val bottomSheetFragment =
                            SharePostUserBottomSheet(postAdapter?.list?.get(position)?.postId
                                ?: postAdapter?.list?.get(position)?._id ?: "",
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
                                            textView.text = totalShare.toString()
                                        }
                                    }
                                })

                        bottomSheetFragment.show(childFragmentManager, bottomSheetFragment.tag)
                    }

                    override fun saveClick(position: Int) {
                        currentPostItemClickedPosition = position
                        mViewModel.postSaveRequest =
                            PostRequest(
                                post = postAdapter?.list?.get(position)?.postId
                                    ?: postAdapter?.list?.get(position)?._id ?: ""
                            )
                        mViewModel.postSave()
                        postAdapter!!.list[position]!!.isSaved =
                            !postAdapter!!.list[position]!!.isSaved



                        org.greenrobot.eventbus.EventBus.getDefault()
                            .post(
                                ActivePostUpdate(
                                    postAdapter?.list?.get(position)?.postId
                                        ?: postAdapter?.list?.get(position)?._id ?: "" ,
                                    "save"
                                )
                            )
                    }

                    override fun saveSoundClick(position: Int) {
                        /* mViewModel.postSaveSoundRequest =
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
                                            UserRequest(creator._id ?: "", AppConstant.CANCEL)
                                        mViewModel.postFollowRequestStatus()
                                    }
                                    postAdapter!!.list[position]!!.creator.followStatus = 2
                                    postAdapter!!.list.forEachIndexed { index, data ->
                                        val dataID = data?.postId ?: data?._id ?: ""
                                        val currentPostId = postAdapter?.list?.get(position)?.postId
                                            ?: postAdapter?.list?.get(position)?._id ?: ""
                                        if (dataID != currentPostId &&
                                            data?.creator?._id == creator._id
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
                                        val dataID = data?.postId ?: data?._id ?: ""
                                        val currentPostId = postAdapter?.list?.get(position)?.postId
                                            ?: postAdapter?.list?.get(position)?._id ?: ""
                                        if (dataID != currentPostId &&
                                            data?.creator?._id == creator._id
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
                                this@ShowProfilePostFragment
                            )
                        }
                        bottomSheetFragment?.show(childFragmentManager, bottomSheetFragment.tag)
                    }

                    override fun tapAndHoldOptionMenu(position: Int, options: Options) {
                        postPosition = position

                        Log.e(
                            "skjda",
                            Prefs.init().userId + "  " + postAdapter!!.list[position]!!.creator._id
                        )
                        val bottomSheetFragment = OptionBottomSheet(
                            this@ShowProfilePostFragment,
                            postPosition,
                            postAdapter!!.list[position]!!.video ?: "",
                            options,
                            Prefs.init().userId == postAdapter!!.list[position]!!.creator._id, true
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
                            this@ShowProfilePostFragment,
                            postPosition,
                            postAdapter!!.list[position]!!.images?.get(imagePosition) ?: "",
                            options,
                            Prefs.init().userId == postAdapter!!.list[position]!!.creator._id, true
                        )
                        bottomSheetFragment.show(childFragmentManager, bottomSheetFragment.tag)
                    }

                    override fun hashTagClick(text: String) {
//                        setLastVisibleItemPos()
                        val bundle = Bundle()
                        bundle.putString("hashtag", text)
                        bundle.putString("type", "hashtag")
                        findNavController().navigate(
                            R.id.action_show_profile_post_fragment_to_show_profile_post_fragment,
                            bundle
                        )
                        /* val bundle = Bundle()
                         bundle.putString("hashtag", text)
                         findNavController().navigate(
                             R.id.action_other_post_fragment_to_searchFragment,
                             bundle
                         )*/
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
                                    R.id.action_other_post_fragment_to_otherProfileFragment,
                                    it
                                )
                            }
                        }
                    }
                }, context ?: MainApplication.get().applicationContext
            )


        layoutManager = LinearLayoutManager(requireContext())
        binding?.postRecycler?.apply {
            setRecycledViewPool(recycledViewPool)
            enableVerticalScroll(true)
            layoutManager = this@ShowProfilePostFragment.layoutManager
            adapter = postAdapter
            val snapHelper: SnapHelper = PagerSnapHelper()
            if (!isSnapAttached) {
                isSnapAttached = true
                snapHelper.attachToRecyclerView(this)
            }

            if (lastVisibleItemPosition > -1) {
                scrollToPosition(lastVisibleItemPosition)
//                if (postAdapter!!.list[lastVisibleItemPosition]!!.type == "video") {
                binding?.postRecycler?.viewIsLoaded {
                    val firstItemView =
                        layoutManager?.findViewByPosition(lastVisibleItemPosition)
                    firstItemView?.viewIsLoaded {
                        findLayoutByPosition(lastVisibleItemPosition, "setupadapter")
                    }
//                    }
                }
            }
        }
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
                    findLayoutByPosition(position, "scroll")
                }


                if (!isLoading && (postAdapter?.list?.size!! - 1) == firstVisibleItemPosition) {
                    // Load more items if needed
                    Utils.printLog(tag = "sadfff", msg = "SAdfsa")
                    if (type != "single" && postAdapter!!.list.size > 4) {
                        binding?.paginationAnimation?.visibility = View.VISIBLE
                    }
                    isLoading = true
                    offset += 20
                    callNextPage()
                } else {
                    Utils.printLog(
                        tag = "sadfff",
                        msg = "loading ${isLoading} ${postAdapter?.list?.size} $firstVisibleItemPosition $visibleItemCount"
                    )
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

    /*    private fun findLayoutByPosition(index: Int) {
            Utils.printLog(
                LogTypeEnum.DEBUG,
                "onItemIsFirstVisibleItem",
                "currentPage = $currentPage position=> $index "
            )
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

                        mViewModel.postSaveSoundRequest = PostRequest(
                            post =
                            postAdapter?.list?.get(index)?.postId
                                ?: postAdapter?.list?.get(index)?._id ?: ""
                        )
                        mViewModel.postViewed()
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
                                "onItemIsFirstVisibleItem",
                                postAdapter!!.list[index].toString()
                            )

                            PlayerViewAdapter.setMediaSourceFromFrag(
                                requireContext(),
                                postAdapter!!.list[index]!!.video,
                                postAdapter?.list?.get(index)?.postId
                                    ?: postAdapter?.list?.get(index)?._id ?: "0",
                                binding1.playerView
                            ) {
                                Utils.printLog(
                                    LogTypeEnum.DEBUG,
                                    "onItemIsFirstVisibleItem",
                                    "set item"
                                )
                                setItemBinding(
                                    binding1,
                                    postAdapter?.list?.get(index)?.postId ?: postAdapter?.list?.get(
                                        index
                                    )?._id ?: "0"
                                )
                                PlayerViewAdapter.playCurrentPlayingVideo()
                            }

                        } else {
                            Utils.printLog(
                                LogTypeEnum.DEBUG,
                                "onItemIsFirstVisibleItem", "not " +
                                        postAdapter!!.list[index].toString()
                            )
                            PlayerViewAdapter.pauseCurrentPlayingVideo()
                        }
                    } else {
                        Utils.printLog(
                            LogTypeEnum.DEBUG,
                            "onItemIsFirstVisibleItem",
                            "view is  null"
                        )
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
                if (mBinding.playerView.player == null) {
                    Utils.printLog(
                        LogTypeEnum.DEBUG,
                        "onItemIsFirstVisibleItem",
                        "player is  null"
                    )
                } else {
                    Utils.printLog(
                        LogTypeEnum.DEBUG,
                        "onItemIsFirstVisibleItem",
                        "player is not null"
                    )
                }
                player = mBinding.playerView.player


                player?.seekTo(PlayerViewAdapter.getScrollItemPlayingTime(id))

                mBinding.apply {


                    playerView.player?.addListener(object : Player.Listener {
                        override fun onPositionDiscontinuity(
                            oldPosition: Player.PositionInfo,
                            newPosition: Player.PositionInfo,
                            reason: Int
                        ) {
                            Utils.printLog(
                                LogTypeEnum.DEBUG,
                                "onItemIsFirstVisibleItem",
                                "onPositionDiscontinuity"
                            )
                            playBigButton.visibility = View.GONE
    //                        playBigButton.visibility = View.GONE

                            seekBar.progress = mBinding.playerView.player!!.currentPosition.toInt()
                        }

                        override fun onPlayerError(error: PlaybackException) {
                            super.onPlayerError(error)
                            playBigButton.visibility = View.GONE

                            Utils.printLog(LogTypeEnum.ERROR, "playerView controller", error.toString())
                        }


                        override fun onIsPlayingChanged(isPlaying: Boolean) {
                            Utils.printLog(
                                LogTypeEnum.DEBUG,
                                "onItemIsFirstVisibleItem",
                                isPlaying.toString()
                            )
                            if (isPlaying) {
                                playBigButton.visibility = View.GONE
                                handlerSeekBar.postDelayed(object : Runnable {
                                    override fun run() {
                                        seekBar.progress = playerView.player!!.currentPosition.toInt()
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
                            Utils.printLog(
                                LogTypeEnum.DEBUG,
                                "onItemIsFirstVisibleItem",
                                playbackState.toString()
                            )

                            seekBar.max = playerView.player!!.duration.toInt()
                        }

                        override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                            super.onPlayWhenReadyChanged(playWhenReady, reason)
                            Utils.printLog(
                                LogTypeEnum.DEBUG,
                                "onItemIsFirstVisibleItem", "onPlayWhenReadyChanged" +
                                        playWhenReady.toString()
                            )
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
        }*/

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

    private fun findLayoutByPosition(index: Int, str: String = "") {
        if (index == -1) {
            return
        }
        if (!myCoroutineScope.isActive) {
            myCoroutineScope = CoroutineScope(Dispatchers.Main)
        }
        myCoroutineScope.launch {
            layoutManager?.let {
                if (currentPage != index) {
                    currentPage = index
                    val view: View? =
                        binding?.postRecycler?.layoutManager?.findViewByPosition(index)
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
                            itemBinding.thumbnail.visibility = View.VISIBLE
                        }
                        if (binding1 is HomeListVideoItemBinding) {
                            Utils.printLog(
                                LogTypeEnum.DEBUG,
                                "setMediaSourceFromFrag",
                                postAdapter!!.list[index].toString()
                            )
                            binding1.playerView.viewIsLoaded {
                                if (context != null) {
                                    PlayerViewAdapter.setMediaSourceFromFrag(
                                        requireContext(),
                                        postAdapter!!.list[index]!!.video,
                                        postAdapter?.list?.get(index)?.postId ?: "",
                                        binding1.playerView
                                    ) {
                                        setItemBinding(
                                            binding1, postAdapter?.list?.get(index)?.postId ?: ""
                                        )
                                        PlayerViewAdapter.playCurrentPlayingVideo()
                                        Log.e("sakldfhsa", str)
                                    }
                                }
                            }
                        } else {
                            PlayerViewAdapter.pauseCurrentPlayingVideo()
                        }
                    }
                }
            }
        }

    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setItemBinding(binding1: HomeListVideoItemBinding?, id: String) {
        if (binding1 != null) {
            itemBinding = binding1
        } else {
            return
        }

        itemBinding.let { mBinding ->
            mBinding.playerView.visibility = View.VISIBLE
            mBinding.playerView.player?.seekTo(PlayerViewAdapter.getScrollItemPlayingTime(id))
            mBinding.apply {
                playerView.player?.addListener(object : Player.Listener {
                    override fun onPositionDiscontinuity(
                        oldPosition: Player.PositionInfo,
                        newPosition: Player.PositionInfo,
                        reason: Int
                    ) {
                        playBigButton.visibility = View.GONE
                        Log.e("asdff", mBinding.playerView.player?.currentPosition.toString())
                        seekBar.progress = mBinding.playerView.player?.currentPosition?.toInt() ?: 0
                    }

                    override fun onPlayerError(error: PlaybackException) {
                        super.onPlayerError(error)
                        playBigButton.visibility = View.GONE
                        Utils.printLog(LogTypeEnum.ERROR, "playerView controller", error.toString())
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

    @SuppressLint("UseCompatLoadingForDrawables")
    @Subscribe
    fun onDoubleTap(isDoubleTap: Any) {
        if (isDoubleTap is isDoubleTap) {
            isDoubleTap.apply {
//            addImageViewAtPosition(x,y)
                targetViewX = targetX
                targetViewY = targetY
                val likePostData = postAdapter?.list?.get(position)
                if (likePostData?.isLike == false) {
                    postAdapter!!.doubleTapLike(isDoubleTap.position)
                    mViewModel.postLikeRequest = PostRequest(post = postId)
                    imgView.isEnabled = false
                    runWithDelay(500) {
                        imgView.isEnabled = true
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
                        overallTxt.text = likePostData.creator.overallLikes.toString()
                    }
                    mViewModel.likePost()
                }
//                addImageViewAtPosition(x, y, imgView)
                /*if (!postAdapter!!.list[position]!!.isLike) {
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
                    postAdapter?.doubleTapLike(isDoubleTap.position)
                }*/
            }
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
        }
    }

    private fun callNextPage() {
        if (activity != null) {
            if (isInternetAvailable(requireActivity())) {
                /* mViewModel.profilePostRequest =
                     ProfilePostRequest(type = "post", offset = offset, limit = 5)*/
                if (type != "hashtag") {
                    setRequestData(offset, 20)
                    mViewModel.profilePosts(dataRequestMap)
                } else {
                    Log.e("sadfafg", "getHashtagPost")

                    mViewModel.getHashtagPost(HashtagPayload(offset, 20, "", hashtagId))
                }

            } else {
//            dialogs?.dismiss()
                Utils.showToast("Please check your internet connection.")
            }
        }

    }

    private fun setRequestData(offset: Int, limit: Int) {
        dataRequestMap.clear()
//        Log.e("sadfafg", type + postList.get(0)?.creator?._id)
        dataRequestMap["type"] = type
        dataRequestMap["offset"] = offset.toString()
        dataRequestMap["limit"] = limit.toString()
        if (postList?.isNotEmpty() == true)
            if (postList?.get(0)?.creator?._id != Prefs.init().userId) {
                if (!ownProfile) {
                    dataRequestMap["user"] = postList?.get(0)?.creator?._id ?: ""

                }
            }
    }

    override fun ghostMode() {
        Prefs.init().isGhostMode = !Prefs.init().isGhostMode
        EventBus.getDefault().post(isGhostMode(Prefs.init().isGhostMode))
//        postAdapter!!.notifyDataSetChanged()
    }

    override fun notInterestedClick() {
        mViewModel.notInterestedRequest =
            PostRequest(
                post = postAdapter?.list?.get(postPosition)?.postId ?: postAdapter?.list?.get(
                    postPosition
                )?._id ?: ""
            )
        mViewModel.notInterested()
    }

    override fun userRestrictClick() {
        mViewModel.userRestrict(UserRestrict(id = postAdapter!!.list[postPosition]!!.creator._id))
    }

    override fun userRepostClick() {
//        EventBus.getDefault().post(Repost(postAdapter!!.list[postPosition]!!.postId))
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
                    DeletePost(
                        post = postAdapter?.list?.get(postPosition)?.postId
                            ?: postAdapter?.list?.get(postPosition)?._id ?: "0"
                    )
                userArchivePost()
            }
        }
    }

    override fun editPost() {

        val bundle = Bundle()
        bundle.putString(
            "EditPostId",
            postAdapter?.list?.get(postPosition)?.postId
                ?: postAdapter?.list?.get(postPosition)?._id ?: ""
        )
        PlayerViewAdapter.releaseAllPlayers("editPostother")
        findNavController().navigate(R.id.editPostFragment, bundle)
    }

    override fun hashTagClick(text: String) {
        val bundle = Bundle()
        bundle.putString("hashtag", text)
        bundle.putString("type", "hashtag")
        findNavController().navigate(
            R.id.action_show_profile_post_fragment_to_show_profile_post_fragment,
            bundle
        )
//  .      findNavController().navigate(R.id.action_other_post_fragment_to_searchFragment, bundle)
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
                    R.id.action_other_post_fragment_to_otherProfileFragment,
                    it
                )
            }
        }

    }

    override fun postInfoClick(position: Int) {
        postInfoDialog(binding!!.toolbar, postAdapter?.list?.get(position))
    }

    override fun followClick(position: Int) {
        postAdapter!!.list[postPosition]!!.tagUsers?.get(position).let { creator ->
            mViewModel.followUnfollowUserRequest = UserRequest(creator?._id ?: "")
//            mViewModel.followUnfollowUser()
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
                            val dataID = data?.postId ?: data?._id ?: ""
                            val currentPostId =
                                postAdapter?.list?.get(position)?.postId ?: postAdapter?.list?.get(
                                    position
                                )?._id ?: ""
                            if (dataID != currentPostId &&
                                data?.creator?._id == creator._id
                            ) {
                                data.creator.followStatus = 2
                                postAdapter!!.notifyItemChanged(index)
                            } else {
                                CoroutineScope(Dispatchers.IO).launch {
                                    if (data?.tagUsers?.any { it._id == creator._id } == true) {
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
                            val dataID = data?.postId ?: data?._id ?: ""
                            val currentPostId =
                                postAdapter?.list?.get(position)?.postId ?: postAdapter?.list?.get(
                                    position
                                )?._id ?: ""

                            if (dataID != currentPostId &&
                                data?.creator?._id == creator._id
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
                                    if (data?.tagUsers?.any { it._id == creator._id } == true) {
                                        data.tagUsers.filter { it._id == creator._id }[0].followStatus =
                                            3

                                    } else {
                                        //                                CoroutineScope(Dispatchers.IO).launch{
                                        if (data?.tagUsers?.any { it._id == creator._id } == true) {
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

                tvDatePosted.text = Utils.formatDate(it?.createdAt ?: "")

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
                    DeletePost(
                        post = postAdapter?.list?.get(postPosition)?.postId
                            ?: postAdapter?.list?.get(postPosition)?._id ?: ""
                    )
                mViewModel.userDeletedPost()
            } else {
//                dialogs?.dismiss()
            }
            dialog?.dismiss()
        }
        dialog?.show()
    }

    private fun setObserver() {
        mViewModel.userProfilePost.observe(viewLifecycleOwner, EventObserver { it1 ->
            if (it1 != null) {
                if (it1.success) {
                    Log.e("UserDetails", it1.data!!.size.toString())
                    it1.data?.let {

                        isLoading = false
                        binding?.paginationAnimation?.visibility = View.GONE
                        val dataList = ArrayList<Data?>()
                        if (!it1.data.isNullOrEmpty()) {
                            it1?.data?.forEach {
                                dataList.add(it)
                            }
                            postAdapter?.addNewItems(dataList)
//                            adapter.notifyItemInserted(11)
                        }
                        if (postAdapter?.list?.isNullOrEmpty() == true) {
                            binding?.noDataFound?.visibility = View.VISIBLE
                            binding?.postRecycler?.visibility = View.GONE
                        } else {
                            binding?.noDataFound?.visibility = View.GONE
                            binding?.postRecycler?.visibility = View.VISIBLE
                        }
                    }
                }
            }
            binding?.shimmerLayout?.shimmerLayout?.visibility = View.GONE
        })
        mViewModel.postPinLikeData.observe(viewLifecycleOwner) { response ->
            if (response != null) {
                if (response.success) {
                        org.greenrobot.eventbus.EventBus.getDefault()
                            .post(postAdapter?.list?.get(postPosition)?._id?.let {
                                ActivePostUpdate(
                                    it ,  ""
                                )
                            })

                 /*   org.greenrobot.eventbus.EventBus.getDefault()
                        .post(ActivePostUpdate("save"))*/
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
                    if (postAdapter?.list?.isNullOrEmpty() == true) {
//                        binding?.postNotFound?.visibility = View.VISIBLE
                        offset = 0
                        callNextPage()
                    }
                }
            }
        }
        mViewModel.postDetailData.observe(viewLifecycleOwner) { response ->
            if (response != null) {
                if (response.success) {
                    response.data?.let {


                        binding?.tvUsername?.text = it.creator.name
                        postList.clear()
                        postList.add(it)

                        postAdapter?.setNewItems(postList)

                        lastVisibleItemPosition = 0

                        binding?.root?.viewIsLoaded {
                            binding?.postRecycler?.viewIsLoaded {
                                binding?.postRecycler?.waitForFirstVisibleItem(Handler(Looper.myLooper()!!)) {
                                    val firstItemView =
                                        layoutManager?.findViewByPosition(lastVisibleItemPosition)

                                    firstItemView?.viewIsLoaded {
                                        findLayoutByPosition(
                                            lastVisibleItemPosition,
                                            "postDetailData"
                                        )
                                    }
                                }
                            }
                        }

                        isLoading = false
                        binding?.paginationAnimation?.visibility = View.GONE
                        binding?.apply {
                            // shimmerLayout.shimmerLayout.stopShimmer()
                            postRecycler.visibility = View.VISIBLE
                        }
                    }
                }
            } else {
                isLoading = false
                binding?.paginationAnimation?.visibility = View.GONE
            }
            binding?.shimmerLayout?.shimmerLayout?.visibility = View.GONE

        }
        mViewModel.postData.observe(viewLifecycleOwner) { response ->
            if (response != null) {
                if (response.success) {
                    response.data?.let { it ->
                        Log.e("postAPI response", it.toString())

                        Log.e("dropsFragment", "drop")
                        if (postAdapter!!.list.size > 0) {
                            /*val urls = ArrayList<String?>()
                            it.forEach {
                                if (it!!.type == "video") {
                                    urls.add(it.video)
                                }
                            }*/
//
                            postAdapter?.addNewItems(it)
//

                        } else {
                            /* val urls = ArrayList<String?>()
                             it.forEach {
                                 if (it!!.type == "video") {
                                     urls.add(it.video)
                                 }
                             }*/
//                            videoCacheManager.preloadVideos(urls)
                            postList = it
                            Log.e("dropsFragment", "add to drop" + postList.size.toString())
//                            if (dataType == "drop") {
                            postAdapter?.setNewItems(postList)
//                            }


                            if (postAdapter?.list?.isNullOrEmpty() == true) {
                                binding?.postNotFound?.visibility = View.VISIBLE
                                binding?.postRecycler?.visibility = View.GONE
                            } else {
                                binding?.postNotFound?.visibility = View.GONE
                                binding?.postRecycler?.visibility = View.VISIBLE
                            }

                            lastVisibleItemPosition = 0

//                            if (postAdapter!!.list[lastVisibleItemPosition]!!.type == "video") {
                            Log.e("uyfg", "null")
                            if (binding == null) {
                                Log.e("uyfg", "null")

                            }
                            binding?.root?.viewIsLoaded {
                                binding?.postRecycler?.viewIsLoaded {
                                    binding?.postRecycler?.waitForFirstVisibleItem(Handler(Looper.myLooper()!!)) {
                                        val firstItemView =
                                            layoutManager?.findViewByPosition(
                                                lastVisibleItemPosition
                                            )

                                        firstItemView?.viewIsLoaded {
                                            findLayoutByPosition(
                                                lastVisibleItemPosition,
                                                "postData"
                                            )
                                        }
                                    }
                                }
                            }
//
                        }

                        binding?.paginationAnimation?.visibility = View.GONE
                        isLoading = false

                        binding?.apply {
                            // shimmerLayout.shimmerLayout.stopShimmer()
                            postRecycler.visibility = View.VISIBLE
                        }
                    }
                }
            } else {
                binding?.paginationAnimation?.visibility = View.GONE
                isLoading = false
            }
            binding?.shimmerLayout?.shimmerLayout?.visibility = View.GONE

        }
        mViewModel.postReportData.observe(viewLifecycleOwner) { response ->
            if (response != null) {
                if (response.success) {
                    if (commentID != "") {
                        commentID = ""
                        try {
                            if (commentReportText.text == "Report") {
                                Log.e("Sdaf", "report")
                                commentReportText.text = "Un-\nReport"
                            } else {

                                commentReportText.text = "Report"
                            }

                        } catch (e: Exception) {
                            Log.e("Sdaf", e.toString())
                        }
                    }
                }
            }
            binding?.shimmerLayout?.shimmerLayout?.visibility = View.GONE

        }
        mViewModel.showMessage.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty() && it.contains("body.").not()) {
                if (it.lowercase() == "required valid object id") {
                    Utils.showToast("Post not found")
                } else {
                    Utils.showToast(it)
                }

                if (type == "single") {
                    PlayerViewAdapter.releaseAllPlayers()
                    binding?.postRecycler?.visibility = View.GONE
                    binding?.postNotFound?.visibility = View.VISIBLE
                } else {

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
                            saveText.visibility = View.INVISIBLE
                            postAdapter?.list?.get(currentPostItemClickedPosition)?.let {
                                it.isLike = false
                                it.isSaved = false
                            }
                        }
                    } else if (binding1 is HomeListItemBinding) {
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
                    }
                }
            }
            binding?.shimmerLayout?.shimmerLayout?.visibility = View.GONE
        }
        mViewModel.showLoading.observe(viewLifecycleOwner) {
        }
    }

    private fun releaseExo(str: String) {
        PlayerViewAdapter.releaseAllPlayers(str)
        mViewModel.postData.postValue(null)
    }

    private fun startExo() {
        player?.playWhenReady = true
        PlayerViewAdapter.playCurrentPlayingVideo()
    }

    override fun onResume() {
        super.onResume()
//        startExo()
        if (PlayerViewAdapter.player != null) {
//            Utils.printLog(tag = "onStart", msg = "playCurrentPlayingVideo $postType")
//            PlayerViewAdapter.playCurrentPlayingVideo()
//            if (mainActivity.currentDrop == postType) {
                binding?.postRecycler?.viewIsLoaded {
                    Utils.printLog(
                        tag = "onStart",
                        msg = "viewIsLoaded ${layoutManager?.findFirstCompletelyVisibleItemPosition()}"
                    )
                    currentPage = -1
                    Log.e(
                        "asjgh",
                        layoutManager?.findFirstCompletelyVisibleItemPosition().toString()
                    )
                    val firstItemView =
                        layoutManager?.findViewByPosition(
                            layoutManager?.findFirstCompletelyVisibleItemPosition() ?: 0
                        )
                    firstItemView?.viewIsLoaded {
                        Log.e(
                            "asjgh",
                            "firstview" + layoutManager?.findFirstCompletelyVisibleItemPosition()
                                .toString()
                        )
                        findLayoutByPosition(
                            layoutManager?.findFirstCompletelyVisibleItemPosition() ?: 0
                        )
                    }


//                }
            }
        } else {
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
                Log.e("asjgh", layoutManager?.findFirstCompletelyVisibleItemPosition().toString())
                val firstItemView =
                    layoutManager?.findViewByPosition(
                        layoutManager?.findFirstCompletelyVisibleItemPosition() ?: 0
                    )
                firstItemView?.viewIsLoaded {
                    Log.e(
                        "asjgh",
                        "firstview" + layoutManager?.findFirstCompletelyVisibleItemPosition()
                            .toString()
                    )
                    findLayoutByPosition(
                        layoutManager?.findFirstCompletelyVisibleItemPosition() ?: 0
                    )
                }


            }

        }
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onPause() {
        super.onPause()

//        videoCacheManager.release()
        handlerPostViewed.removeCallbacksAndMessages(null)

        Log.e("sakldfhsa", "onpause")
        player?.playWhenReady = false
        myCoroutineScope.cancel()
//        PlayerViewAdapter.pauseCurrentPlayingVideo()
        PlayerViewAdapter.setPlayerCurrentTime()

        releaseExo("onPause")
    }

    override fun onStop() {
        super.onStop()
        if (getActivity() != null) {
            getActivity()?.getWindow()
                ?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
        EventBus.getDefault().unregister(this)
    }

    /*  override fun onDestroyView() {
          super.onDestroyView()
          releaseExo()
          destroyAllResources()
      }*/
    override fun onDestroyView() {
        Log.e("onItemIsFirstVisibleItem", "onDestroyViewother")
//        releaseExo("onDestroyView")
        destroyAllResources()
        super.onDestroyView()

    }


    private fun destroyAllResources() {
        setLastVisibleItemPos()
        binding = null
        layoutManager = null
        postAdapter = null
        isSnapAttached = false
        cachedDataSourceFactory = null
    }

    private fun setLastVisibleItemPos() {
        lastVisibleItemPosition = layoutManager?.findFirstCompletelyVisibleItemPosition() ?: 0
        currentPage = -1
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
        Log.e("Adfsa", commentCountTextView.text.toString())
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
        commentID = commentId
        commentReportText = view
        if (view.text == "Report") {
            val bottomSheetFragment = ReportBottomSheet(this, false)
            bottomSheetFragment.show(childFragmentManager, bottomSheetFragment.tag)
        } else {
            mViewModel.commentReport(PostRequest(comment = commentID, reason = ""))
        }
    }
}