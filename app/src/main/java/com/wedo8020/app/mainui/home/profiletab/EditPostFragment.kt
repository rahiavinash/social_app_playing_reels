package com.wedo8020.app.mainui.home.profiletab

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.PlayerView
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wedo8020.app.MainApplication
import com.wedo8020.app.R
import com.wedo8020.app.base.PlayerViewAdapter
import com.wedo8020.app.databinding.FragmentEditPostBinding
import com.wedo8020.app.mainui.base.BaseFragment
import com.wedo8020.app.mainui.home.profiletab.mentionAdapter.MentionListAdapter
import com.wedo8020.app.mainui.home.profiletab.bottomSheet.DisappearsInBottomSheet
import com.wedo8020.app.mainui.home.profiletab.bottomSheet.PostingFoDialogFragment
import com.wedo8020.app.mainui.home.postFragment.PostViewModel
import com.wedo8020.app.mainui.home.profiletab.bottomSheet.TagCollarBottomSheetFragment
import com.wedo8020.app.request.DeletePost
import com.wedo8020.app.request.mentionUserRequest
import com.wedo8020.app.response.MentionUserData
import com.wedo8020.app.response.UserList
import com.wedo8020.app.utils.MessageEvent
import com.wedo8020.app.utils.Prefs
import com.wedo8020.app.utils.Utils
import com.wedo8020.app.utils.Utils.convertDesc
import com.wedo8020.app.utils.Utils.disableMultiTap
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.wedo8020.app.mainui.home.createpost.adapter.HashTagListAdapter
import com.wedo8020.app.request.EditOptions
import com.wedo8020.app.request.EditPost
import com.wedo8020.app.request.Hashtag
import com.wedo8020.app.request.HashtagPayload
import com.wedo8020.app.response.TagUser
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.Objects
import java.util.regex.Pattern


class EditPostFragment : BaseFragment(), PostingFoDialogFragment.OnClickListener,
    DisappearsInBottomSheet.OnClickListener {
    private lateinit var mBinding: FragmentEditPostBinding
    private var sFTagListData: ArrayList<UserList>? = null
    private val authViewModel by viewModels<PostViewModel>()
    private var tagName = ""
    private var postId = ""
    private var imageUrl = ""
    private var videoUrl = ""
    private var type = ""
    var bundle=Bundle()
    private var comments = false
    private var downloads = false
    private var reposts = false
    private var seeLikes = false
    private var selectPostingFor = ""
    private var setTextPostingFor :String?=null
    private var selectPostDisappearIn = ""
    private var setTextDisappearIn :String?=null
    private var mentionSearch = ""
    var dialogs: Dialog? = null
    private var player: ExoPlayer? = null
    private var tagList: ArrayList<TagUser> = ArrayList()
    private lateinit var manager: RecyclerView.LayoutManager
    var mentionLists: ArrayList<MentionUserData> = ArrayList()
    var handler = Handler(Looper.getMainLooper())
    private var finalTag:ArrayList<String> = ArrayList()
    private var mentionUserHash = hashMapOf<String, String>()
    private var startIndex = -1
    private var endIndex = -1
    private var mentionUserIdList = ArrayList<String>()
    var hashTagList: ArrayList<Hashtag> = ArrayList()
    private var stringForApi = ""
    private  var adapter: MentionListAdapter? = null
    private lateinit var adapterHashtag: HashTagListAdapter
    private var hashtagUserHash = hashMapOf<String, String>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding =
            DataBindingUtil.inflate(layoutInflater, R.layout.fragment_edit_post, container, false)
        hideKeyboardOnOutSideClick(mBinding.clEditPost)
        hideNavBottom()
        if (requireArguments().getString("EditPostId") != null) {
            postId = requireArguments().getString("EditPostId").toString()
        }
        Prefs.init().TagList = ""
        dialogs = Dialog(requireActivity())
        dialogs?.requestWindowFeature(Window.FEATURE_CONTENT_TRANSITIONS)
        mBinding.playerView.player = player
        setObserver(true)
        callApi()
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpUi()
        mentionObserver()
        hideKeyboardOnOutSideClick(mBinding.clEditPost)
        mentionUserCallApi(mentionSearch)
        hashTagObserver()
        mBinding.IvBack.setOnClickListener {
            findNavController().navigateUp()
            Prefs.init().TagList = ""
        }

        mBinding.IvClose.setOnClickListener {
            findNavController().navigateUp()
            Prefs.init().TagList = ""
        }


        mBinding.tvPublic.setOnClickListener {
            mBinding.tvPublic.disableMultiTap()
            showPostingForBottomSheet()
        }

        mBinding.tvDisappearSelect.setOnClickListener {
            mBinding.tvDisappearSelect.disableMultiTap()
            showDisappearInBottomSheet()
        }
        mBinding.tvTagName.setOnClickListener {
            mBinding.tvTagName.disableMultiTap()
            showTagCollarBottomSheet()
        }


        /**For Comment Enable and Disable Handling*/
        mBinding.ivRadioUnselect.setOnClickListener {
            mBinding.let {
                if (it.ivRadioSelect.visibility == View.VISIBLE) {
                    it.ivRadioSelect.visibility = View.GONE
                    comments = false
                } else {
                    it.ivRadioSelect.visibility = View.VISIBLE
                    comments = true
                }
            }
        }
        /**For Downloads Enable and Disable Handling*/
        mBinding.ivRadioUnselect2.setOnClickListener {
            mBinding.let {
                if (it.ivRadioSelect2.visibility == View.VISIBLE) {
                    it.ivRadioSelect2.visibility = View.GONE
                    downloads = false
                } else {
                    it.ivRadioSelect2.visibility = View.VISIBLE
                    downloads = true
                }
            }
        }
    /*    *//**For Reposts Enable and Disable Handling*//*
        mBinding.ivRadioUnselect3.setOnClickListener {
            mBinding.let {
                if (it.ivRadioSelect3.visibility == View.VISIBLE) {
                    it.ivRadioSelect3.visibility = View.GONE
                    reposts = false
                } else {
                    it.ivRadioSelect3.visibility = View.VISIBLE
                    reposts = true
                }
            }
        }*/
        /**For SeeLikes Enable and Disable Handling*/
        mBinding.ivRadioUnselect4.setOnClickListener {
            mBinding.let {
                if (it.ivRadioSelect4.visibility == View.VISIBLE) {
                    it.ivRadioSelect4.visibility = View.GONE
                    seeLikes = false
                } else {
                    it.ivRadioSelect4.visibility = View.VISIBLE
                    seeLikes = true
                }
            }
        }

    }
    private fun hashTagObserver() {
        authViewModel.hashTagListResponse.observe(viewLifecycleOwner) { response ->
            if (response != null) {
                if (response.success == true) {
                    //   dialogs?.dismiss()

                    if (response.data != null) {
                        Log.d("myHashTag", response.data.toString())
                        hashTagList = response.data
                        adapterHashtag.setNewItems(hashTagList)
                    }
                }
            }

        }
    }
    private fun setUpUi() {
        // setEdittextForKeyboard()

        mBinding.apply {
            mBinding.tvDescription.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                    //                    Log.e("commentEt","before "+p0.toString())
                }

                @SuppressLint("SuspiciousIndentation")
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    val cursorPosition = if (mBinding.tvDescription.selectionStart < 0) {
                        0
                    } else {
                        mBinding.tvDescription.selectionStart
                    }
                    val text = mBinding.tvDescription.text.toString()
                    startIndex = cursorPosition
                    endIndex = cursorPosition
                    while (startIndex > 0 && !text[startIndex - 1].isWhitespace()) {
                        startIndex--
                    }
                    while (endIndex < text.length && !text[endIndex].isWhitespace()) {
                        endIndex++
                    }

                    val selectedWord = text.substring(startIndex, endIndex)
                    if (selectedWord.startsWith("@")) {
                        rvMentionList.visibility = View.VISIBLE
                        setRecyclerViewAdapter()
                        if (Utils.isInternetAvailables(requireActivity())) {
                            handler.removeCallbacksAndMessages(null)
                            handler.postDelayed({
                                Handler(Looper.getMainLooper()).postDelayed({
                                    mentionUserCallApi(selectedWord.removePrefix("@"))
                                }, 0)
                            }, 200)

                        }
                    } else if (selectedWord.startsWith("#")) {
                        rvMentionList.visibility = View.VISIBLE
                        setHashTagRecyclerViewAdapter()
                        Log.d("hashSelected", "hash has been written")
                        if (Utils.isInternetAvailables(requireActivity())) {
                            handler.removeCallbacksAndMessages(null)
                            handler.postDelayed({
                                //  setHashTagRecyclerViewAdapter()
                                Handler(Looper.getMainLooper()).postDelayed({
                                    hashTagApiCall(selectedWord.removePrefix("#"))
                                }, 0)

                            }, 200)

                        }
                    } else {
                        rvMentionList.visibility = View.GONE
                    }

                    val length: Int = mBinding.tvDescription.text.length
                    mBinding.tvZero.text = length.toString()



                }

                override fun afterTextChanged(p0: Editable?) {
                    if (!p0.isNullOrEmpty())
                        if (p0.toString().last() == ' ') {
                            //                        Log.e("commentEt","after "+findLastWord(p0.toString()))
                            val lastWord = Utils.findLastWord(p0.toString().trim())
                            if (lastWord.startsWith("@")) {
                                if (!mentionUserHash.containsKey(lastWord)) {
                                    if (mentionLists.size > 0 &&
                                        mentionLists[0].username == lastWord.substring(1)
                                    ) {

                                        mentionUserHash[lastWord] = mentionLists[0]._id
                                    }
                                }
                            } else if (lastWord.startsWith("#")) {
                                if (!hashtagUserHash.containsKey(lastWord)) {
                                    if (hashTagList.size > 0 &&
                                        hashTagList[0].tag == lastWord.substring(1)
                                    ) {

                                        hashtagUserHash[lastWord] = hashTagList[0]._id
                                    }
                                }
                            }
                        }
                    highlightTagsAndMentions(p0)
                }
            })
            /*mBinding!!.postBT.setOnClickListener {
                    if (!sfTagListData.isNullOrEmpty()){
                        sfTagListData!!.forEach {
                            finalTag.add(it._id  )
                        }
                    }
                    convertStringFormatToSendApi(mBinding?.editText3!!.text.toString())

                    Prefs.init().TagList = ""
                }*/

        }

        mBinding.tvPost.setOnClickListener {
            if (!sFTagListData.isNullOrEmpty()){
                sFTagListData!!.forEach {
                    finalTag.add(it._id  )
                }
            }
            convertStringFormatToSendApi(mBinding.tvDescription.text.toString())

            /** EditPost Api Call */
            if (isInternetAvailable(requireContext())){


            authViewModel.editPosts= EditPost(
                disappearTime = selectPostDisappearIn,
                type = type,
                post = postId,
                options = EditOptions(
                    comment = comments,
                    download = downloads,
                    like = seeLikes,
                    repost = reposts
                ),
                tagUsers = finalTag,
                mentionUsers = mentionUserIdList,
                collabUser = null,
                description = stringForApi,
                privacy = selectPostingFor,
                images = imageUrl,
            )
            authViewModel.userEditPost()
            baseObserver()

                Prefs.init().TagList = ""
            }else{
                showToast(getString(R.string.no_internet_message))
            }

        }



        // callApi()
        //   paginationScroll()
    }



    fun mentionUserCallApi(search: String) {
        if (Utils.isInternetAvailables(requireActivity())) {
            //  showProgressDialogs()
            authViewModel.mentionUserRequests =
                mentionUserRequest(offset = 0, limit = 100, search = search)
            authViewModel.mentionUser()
        }
    }
    private fun convertStringFormatToSendApi(text: String) {
        for (i in text.split(" ")) {
            if (mentionUserHash.containsKey(i)) {
                stringForApi += "@[${i.substring(1)}](${mentionUserHash[i]}) "
                if (mentionUserHash[i] != null && !mentionUserIdList.contains(mentionUserHash[i])) {
                    mentionUserIdList.add(mentionUserHash[i]!!)
                }
            } else {
                stringForApi += "$i "
            }
        }
    }
    private fun hashTagApiCall(search: String) {
        if (Utils.isInternetAvailables(requireActivity())) {
            authViewModel.hashTagList(
                HashtagPayload(offset = 0, limit = 0, search = search)
            )

        }
    }
    private fun setHashTagRecyclerViewAdapter() {
        mBinding.apply {
            manager = LinearLayoutManager(requireContext())
            this.rvMentionList.layoutManager = manager
            adapterHashtag = HashTagListAdapter(R.layout.recycler_hashtag_list,
                object : HashTagListAdapter.HashTagClick {
                    override fun onClick(id: String, username: String) {
                        hashtagUserHash["#${username}"] = id
                        mBinding.tvDescription.text =
                            mBinding.tvDescription.text.replace(
                                startIndex,
                                endIndex,
                                "#${username} "
                            )
                        //mBinding?.editText3!!.text.replace(startIndex, endIndex, "@${username} ")
                        mBinding.tvDescription.setSelection(mBinding.tvDescription.text.toString().length)
                        highlightTagsAndMentions(mBinding.tvDescription.text)
                        mBinding.rvMentionList.visibility = View.GONE


                    }
                })
            rvMentionList.adapter = adapterHashtag

        }
    }
    private fun setRecyclerViewAdapter() {
        mBinding.apply {

            manager = LinearLayoutManager(requireContext())

            rvMentionList.layoutManager = manager
            adapter = MentionListAdapter(R.layout.mention_list_recycleview,
                object : MentionListAdapter.mentionClick {
                    override fun onClick(id: String, username: String) {
                        mentionUserHash["@${username}"] = id
                        tvDescription.text =
                            tvDescription.text.replace(startIndex, endIndex, "@${username} ")
                        tvDescription.setSelection(tvDescription.text.toString().length)
                        mBinding.rvMentionList.visibility = View.GONE
                    }
                })
            rvMentionList.adapter = adapter
        }
    }





    private fun showPostingForBottomSheet() {
        val bottomSheetFragment: BottomSheetDialogFragment = PostingFoDialogFragment(this)
        bundle.putString("posting", mBinding.tvPublic.text.toString())
        bottomSheetFragment.arguments = bundle
        bottomSheetFragment.show(childFragmentManager , bottomSheetFragment.tag)
    }

    private fun showDisappearInBottomSheet() {
        val bottomSheetFragment: BottomSheetDialogFragment = DisappearsInBottomSheet(this)
        bundle.putString("disAppears", mBinding.tvDisappearSelect.text.toString() )
        bottomSheetFragment.arguments = bundle
        bottomSheetFragment.show(childFragmentManager, bottomSheetFragment.tag)
    }

    private fun showTagCollarBottomSheet() {
        val bottomSheetFragment: BottomSheetDialogFragment = TagCollarBottomSheetFragment()
        val bundle = Bundle()
        val tagGson = Gson()
        val json = tagGson.toJson(tagList)
        bundle.putString("tagList", json.toString())
        bottomSheetFragment.arguments = bundle
        bottomSheetFragment.show(requireFragmentManager(), bottomSheetFragment.tag)

    }


    fun callApi() {
        if (Utils.isInternetAvailables(requireActivity())) {
            showProgressDialogs()
            authViewModel.deletePostRequest =
                DeletePost(post = postId)
            authViewModel.userPostDetail()
        } else {
            //     dialogs?.dismiss()
            Utils.showToast("Please check your internet connection.")
        }
    }

/*    private fun mentionUserCallApi() {
        if (Utils.isInternetAvailables(requireActivity())) {
            //  showProgressDialogs()
            authViewModel.mentionUserRequests =
                mentionUserRequest(offset = 0, limit = 100, search = mentionSearch)
            authViewModel.mentionUser()
        } else {
            //     dialogs?.dismiss()
            Utils.showToast("Please check your internet connection.")
        }
    }*/

    @SuppressLint("SetTextI18n")
    private fun setObserver(click:Boolean) {
        authViewModel.postDetailData.observe(viewLifecycleOwner) { response ->
            if (response != null) {
                if (response.success) {
                    dialogs?.dismiss()
                    if (response.data != null) {
                        response.data.let { it ->
                            if (click){
                                comments = it!!.options.comment
                                downloads = it.options.download
                                reposts = it.options.repost
                                seeLikes = it.options.like
                                if (it.options.comment) {
                                    mBinding.ivRadioSelect.visibility = View.VISIBLE
                                } else {
                                    mBinding.ivRadioSelect.visibility = View.GONE
                                }
                                if (it.options.download) {
                                    mBinding.ivRadioSelect2.visibility = View.VISIBLE
                                } else {
                                    mBinding.ivRadioSelect2.visibility = View.GONE
                                }
                                /* if (it.options.like) {
                                     mBinding.ivRadioSelect3.visibility = View.VISIBLE
                                 } else {
                                     mBinding.ivRadioSelect3.visibility = View.GONE
                                 }*/
                                if (it.options.like) {
                                    mBinding.ivRadioSelect4.visibility = View.VISIBLE
                                } else {
                                    mBinding.ivRadioSelect4.visibility = View.GONE
                                }

                            }


                            /**To Show the tagged user */
                            tagList = it?.tagUsers?:ArrayList()
                            val totalUser = tagList.size - 1
                            when (it?.tagUsers?.size) {
                                0 -> {
                                    mBinding.tvTagName.text = "No User"
                                }
                                1 -> {
                                    mBinding.tvTagName.text = it.tagUsers[0].name
                                }
                                2 -> {
                                    mBinding.tvTagName.text =
                                        it.tagUsers[0].name + " " + "and" + " " + totalUser.toString() + " " + "other"
                                }
                                else -> {
                                    mBinding.tvTagName.text =
                                        it?.tagUsers?.get(0)?.name + " " + "and" + " " + totalUser.toString() + " " + "others"
                                }
                            }

                            /**To Show the Description user */
                            if (it?.description!=null) {
                                val description = convertDesc(it.description)
                                it.mentionUsers?.forEach {
                                    if (!mentionUserHash.containsKey("@"+it.username)){
                                        mentionUserHash["@"+it.username] = it._id
                                    }
                                }
                                mBinding.tvDescription.movementMethod = LinkMovementMethod.getInstance()
                                mBinding.tvDescription.setText(description)
                                mBinding.tvDescription.setSelection(mBinding.tvDescription.text.length)
                                mBinding.tvZero.text = it.description.length.toString()
                                mBinding.rvMentionList.visibility = View.GONE

                            }

                            /**To Show the privacy user */
                            if (!setTextPostingFor.isNullOrEmpty()) {
                                mBinding.tvPublic.text = setTextPostingFor
                            } else {
                                selectPostingFor= it?.privacy.toString()
                                when (it?.privacy) {
                                    "everyOne" -> {
                                        mBinding.tvPublic.text = "Public"
                                    }
                                    "onlyMe" -> {
                                        mBinding.tvPublic.text = "Only Me"
                                    }
                                    "friends" -> {
                                        mBinding.tvPublic.text = "Friends"
                                    }
                                    "followers" -> {
                                        mBinding.tvPublic.text = "Followers"
                                    }
                                }
                            }

                            /**To Show the DisappearTime user */
                            if (!setTextDisappearIn.isNullOrEmpty()) {
                                mBinding.tvDisappearSelect.text = setTextDisappearIn
                            } else {
                                selectPostDisappearIn= it?.disappearTime.toString()
                                when (it?.disappearTime) {
                                    "1m" -> {
                                        mBinding.tvDisappearSelect.text = "30 Days"
                                    }
                                    "infinity" -> {
                                        mBinding.tvDisappearSelect.text = "Never"
                                    }
                                    "24h" -> {
                                        mBinding.tvDisappearSelect.text = "24 Hours"
                                    }
                                    "48h" -> {
                                        mBinding.tvDisappearSelect.text = "48 Hours"
                                    }
                                    "1w" -> {
                                        mBinding.tvDisappearSelect.text = "7 Days"
                                    }
                                    "3m" -> {
                                        mBinding.tvDisappearSelect.text = "90 Days"
                                    }
                                }
                            }


                            /**To set Post type is (Image or Video)*/
                            type = response.data?.type?:""
                            if (type == "video") {
                                if (!response.data!!.video.isNullOrEmpty()) {
                                    videoUrl = response.data!!.video!!
                                    mBinding.mainLayout.visibility = View.VISIBLE
                                    mBinding.ivPostImage.visibility = View.GONE
                                    player?.release()
                                    setMediaSourceFromFrag(
                                        response.data!!.video!!,
                                        mBinding.playerView
                                    )
                                }
                            } else if (type == "image") {
                                mBinding.mainLayout.visibility = View.GONE


                                mBinding.ivPostImage.visibility = View.VISIBLE
                                if (it?.images?.isNotEmpty() == true) {
                                    imageUrl = it.images?.get(0)?:""
                                    Glide.with(requireContext()).load(it.images?.get(0))
                                        .into(mBinding.ivPostImage)
                                }
                            }

                        }

                    }
                }
            }
        }

        authViewModel.showLoading.observe(viewLifecycleOwner) {
        }
    }

    private fun baseObserver() {
        authViewModel.baseResponse.observe(viewLifecycleOwner) { response ->
            if (response != null) {
                if (response.success) {
                    dialogs?.dismiss()

                    findNavController().navigateUp()
                }
            }
        }

        authViewModel.showLoading.observe(viewLifecycleOwner) {
        }
    }

    private fun mentionObserver() {
        authViewModel.mentionUserData.observe(viewLifecycleOwner) { response ->
            if (response != null) {
                if (response.success) {
                    dialogs?.dismiss()

                    if (response.data != null) {
                        mentionLists = response.data!!
                        adapter?.setNewItems(mentionLists)

                        /*     manager = LinearLayoutManager(requireContext())
                             mBinding.rvMentionList.layoutManager = manager
                             val adapter = MentionAutoCompleteAdapter(mentionLists, this)
                             mBinding.rvMentionList.adapter = adapter*/

                    }
                }
            }
        }

        authViewModel.showLoading.observe(requireActivity()) {
        }
    }

    private fun showProgressDialogs() {
        dialogs?.setCancelable(false)
        Objects.requireNonNull(dialogs?.window)?.setBackgroundDrawable(
            ColorDrawable(
                Color.TRANSPARENT
            )
        )
        dialogs?.setContentView(R.layout.progressbar)
        dialogs?.show()
    }

    @SuppressLint("SetTextI18n")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: MessageEvent?) {
        if (event!!.text == "") {
            val type = object : TypeToken<ArrayList<UserList>>() {}.type
            sFTagListData = Gson().fromJson(Prefs.init().TagList, type)

            for (userList in sFTagListData!!) {
                tagName = userList.name
            }
            val totalUser = sFTagListData!!.size - 1

            when (sFTagListData!!.size) {
                0 -> {
                    mBinding.tvTagName.text = "No User"
                }
                1 -> {
                    mBinding.tvTagName.text = sFTagListData!![0].name
                }
                2 -> {
                    mBinding.tvTagName.text =
                        sFTagListData!![0].name + " " + "and" + " " + totalUser.toString() + " " + "other"
                }
                else -> {
                    mBinding.tvTagName.text =
                        sFTagListData!![0].name + " " + "and" + " " + totalUser.toString() + " " + "others"
                }
            }
            //  setObserver()
        }

    }


    @SuppressLint("UnsafeOptInUsageError")
    fun setMediaSourceFromFrag(url: String, playerView: PlayerView) {

        if (player != null)
            player!!.release()

        player = ExoPlayer.Builder(MainApplication.instance.applicationContext)
            .setLoadControl(PlayerViewAdapter.getLoadControl())
            .setTrackSelector(PlayerViewAdapter.getTrackSelector(MainApplication.instance.applicationContext))
            .build()
        player!!.playWhenReady = false
        player!!.repeatMode = Player.REPEAT_MODE_ALL
        val mediaItem: MediaItem = MediaItem.fromUri(url)
        // Creating media source and adding video to cache
        val mediaSource = ProgressiveMediaSource.Factory(PlayerViewAdapter.cacheDataSourceFactory!!)
            .createMediaSource(mediaItem)

        Log.e("post_url", url)

        player!!.setMediaSource(mediaSource)
        player!!.prepare()
        player!!.play()
        playerView.player = player
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    override fun onDestroy() {
        super.onDestroy()
        Prefs.init().TagList = ""
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    override fun onStart() {
        super.onStart()
        org.greenrobot.eventbus.EventBus.getDefault().register(this)


        //initializePlayer()
    }

    override fun onResume() {
        super.onResume()
        startExo()
    }

    override fun onPause() {
        super.onPause()
        player?.playWhenReady = false
        pauseCurrentPlayingVideo()
    }

    override fun onStop() {
        super.onStop()
        org.greenrobot.eventbus.EventBus.getDefault().unregister(this)

    }


    private fun startExo() {
        player?.playWhenReady = true
        playCurrentPlayingVideo()
    }

    private fun playCurrentPlayingVideo() {
        if (PlayerViewAdapter.player != null) {
            PlayerViewAdapter.player!!.playWhenReady = true
        }

    }

    private fun pauseCurrentPlayingVideo() {
        if (PlayerViewAdapter.player != null) {
            PlayerViewAdapter.player!!.playWhenReady = false
        }
    }

    override fun onClickDisappearClick(disappearData: String,disappearSendData: String) {
        selectPostDisappearIn = disappearSendData
        setTextDisappearIn=disappearData
        setObserver(false)
    }

    override fun onClickTags(data: String, postingForText:String) {
        selectPostingFor = data
        setTextPostingFor = postingForText
        setObserver(false)
    }

    private fun highlightTagsAndMentions(editable: Editable?) {
        val text = editable.toString()

        // Highlight hashtags
        val hashtagPattern = Pattern.compile("\\B#([a-zA-Z0-9_\\p{Punct}]+)\\b")
        val hashtagMatcher = hashtagPattern.matcher(text)
        while (hashtagMatcher.find()) {
            val start = hashtagMatcher.start()
            val end = hashtagMatcher.end()

            editable?.setSpan(
                android.text.style.ForegroundColorSpan(requireContext().getColor(R.color.appBlue)),
                start,
                end,
                0
            )
        }

        // Highlight mentions
        val mentionPattern = Pattern.compile("\\B@([a-zA-Z0-9_\\p{Punct}]+)\\b")
        val mentionMatcher = mentionPattern.matcher(text)

        while (mentionMatcher.find()) {
            val start = mentionMatcher.start()
            val end = mentionMatcher.end()
            val mention = text.substring(start, end)
            if (mentionUserHash.containsKey(mention)) {
                editable?.setSpan(
                    android.text.style.ForegroundColorSpan(requireContext().getColor(R.color.appBlue)),
                    start,
                    end,
                    0
                )
            } else {
                editable?.setSpan(
                    android.text.style.ForegroundColorSpan(requireContext().getColor(R.color.pureWhite)),
                    start,
                    end,
                    0
                )
            }
        }
    }


}