package com.wedo8020.app.mainui.home.createpost

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.wedo8020.app.R
import com.wedo8020.app.base.PlayerViewAdapter
import com.wedo8020.app.databinding.FragmentCreatePostBinding
import com.wedo8020.app.mainui.base.BaseFragment
import com.wedo8020.app.mainui.home.createpost.adapter.HashTagListAdapter
import com.wedo8020.app.mainui.home.postFragment.PostViewModel
import com.wedo8020.app.mainui.home.profiletab.mentionAdapter.MentionListAdapter
import com.wedo8020.app.mainui.home.profiletab.bottomSheet.TagCollarBottomSheetFragment
import com.wedo8020.app.request.CreatePost
import com.wedo8020.app.request.DeletePost
import com.wedo8020.app.request.Hashtag
import com.wedo8020.app.request.HashtagPayload
import com.wedo8020.app.request.Options
import com.wedo8020.app.request.mentionUserRequest
import com.wedo8020.app.response.MentionUserData
import com.wedo8020.app.response.UserList
import com.wedo8020.app.roomdb.CreatePostDbCreation
import com.wedo8020.app.roomdb.CreatePostDbRepo
import com.wedo8020.app.roomdb.CreatePostEntity
import com.wedo8020.app.utils.MessageEvent
import com.wedo8020.app.utils.NetworkUtils
import com.wedo8020.app.utils.Prefs
import com.wedo8020.app.utils.Utils
import com.wedo8020.app.utils.Utils.disableMultiTap
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.wedo8020.app.mainui.activities.MainActivity
import com.wedo8020.app.mainui.home.viewPagerUploadImage.ImageSlider
import com.wedo8020.app.request.UserListRequest
import com.wedo8020.app.response.TagUser
import com.wedo8020.app.utils.RecyclerViewScrollListener
import com.wedo8020.app.utils.Utils.getCountryNameFromCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File
import java.util.Objects
import java.util.regex.Pattern


@Suppress("DEPRECATION")
class CreatePostFragment : BaseFragment(),PostingForDialogBottomSheet.OnClickListener, DisappearsTimeBottomSheet.OnClickListener {
    var mBinding: FragmentCreatePostBinding? = null
    var imageView: ViewPager? = null
    lateinit var imagePager: ImageSlider
    var position = 0
    var type : Int? =null
    private var sfTagListData: ArrayList<UserList>? = null
    private val authViewModel by viewModels<PostViewModel>()
    private var tagName = ""
    private var postId = ""
    private lateinit var player : ExoPlayer
    private var tagList: ArrayList<TagUser> = ArrayList()
    private var retrieveType1 = ""
    private var retrieveVideo = ""
    private var postingFor : String ="everyOne"
    private var disappearIn : String = "infinity"
    private var retrieveBase64Video : Uri? = Uri.EMPTY
    private var compressedVideoString : String ? = null
    private var count = 0
    private var receivedUri : ArrayList<Uri>? = arrayListOf()
    private var compressedImages : ArrayList<Uri>? = arrayListOf()
    private var tagList1 : ArrayList<String>? = arrayListOf()
    private var dialogs:Dialog?=null
    var mentionLists: ArrayList<MentionUserData> = ArrayList()
    var hashTagList: ArrayList<Hashtag> = ArrayList()
    var handler = Handler(Looper.getMainLooper())
    private var finalTag:ArrayList<String> = ArrayList()
    private var mentionUserHash = hashMapOf<String, String>()
    private var hashtagUserHash = hashMapOf<String, String>()
    private var startIndex = -1
    private var endIndex = -1
    private var mentionUserIdList = ArrayList<String>()
    lateinit var adapter: MentionListAdapter
    private lateinit var hashtagAdapter: HashTagListAdapter
    private lateinit var manager: RecyclerView.LayoutManager
    private var commentAllowed = true
    private var downloadAllowed = true
    private var repostAllowed = false
    private var seeLikesAllowed = true
    private var isCreatingPost = false
    private var listMentionUsers : ArrayList<String> = arrayListOf()
    private lateinit var database : CreatePostDbCreation
    private var isLoading = false
    private var offset = 0
    private var hashtagOffset = 0
    private var searchValue = ""





    private lateinit var repository : CreatePostDbRepo
    private lateinit var factory : CreatePostViewModelFactory
    private lateinit var viewModel : CreatePostViewModel
    var dialog : Dialog?=null
    private var seekTimeOutput : String ? = null
    private var responseWithoutMediaSuccess = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        mBinding=
            DataBindingUtil.inflate(layoutInflater,R.layout.fragment_create_post, container, false)

        checkIfFragmentAttached {


            dialogs = Dialog(requireContext())
            dialogs?.requestWindowFeature(Window.FEATURE_CONTENT_TRANSITIONS)

            dialog = Dialog(requireContext())
            dialog?.requestWindowFeature(Window.FEATURE_CONTENT_TRANSITIONS)
        }
        hideKeyboardOnOutSideClick(mBinding!!.clEditPost)
        hideNavBottom()

        Prefs.init().TagList = ""
        mBinding?.ivRadioSelect?.visibility = View.VISIBLE
        mBinding?.ivRadioSelect2?.visibility = View.VISIBLE
        mBinding?.ivRadioSelect4?.visibility = View.VISIBLE

        mBinding?.ivRadioUnselect?.setOnClickListener {

            mBinding.let {

                if (it?.ivRadioSelect?.visibility == View.VISIBLE) {
                    commentAllowed = false
                    it.ivRadioSelect.visibility = View.GONE
                } else {
                    commentAllowed = true
                    it?.ivRadioSelect?.visibility = View.VISIBLE
                }

            }
            Log.d("radioButtonCommentAllowed", "$commentAllowed")
        }

        mBinding?.ivRadioUnselect2?.setOnClickListener {

            mBinding.let {
                if (it?.ivRadioSelect2?.visibility == View.VISIBLE) {
                    downloadAllowed = false
                    it.ivRadioSelect2.visibility = View.GONE
                } else {
                    downloadAllowed = true
                    it?.ivRadioSelect2?.visibility = View.VISIBLE
                }
            }
        }

        mBinding?.ivRadioUnselect4?.setOnClickListener {

            mBinding.let {
                if (it?.ivRadioSelect4?.visibility == View.VISIBLE) {
                    seeLikesAllowed = false

                    it.ivRadioSelect4.visibility = View.GONE
                } else {
                    seeLikesAllowed = true
                    it?.ivRadioSelect4?.visibility = View.VISIBLE
                }
            }
        }





        return mBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = requireActivity() as MainActivity
        checkIfFragmentAttached {


            database = CreatePostDbCreation(requireContext())
        }

            repository = CreatePostDbRepo(database)
            factory = CreatePostViewModelFactory(repository)
            viewModel = ViewModelProvider(this, factory)[CreatePostViewModel::class.java]

        //outputDirectory = getOutputDirectory()
        mBinding?.overlayLayout?.visibility = View.GONE
        arguments?.let {
            val receivedUris: ArrayList<Uri>? =
                it.getParcelableArrayList("images")
            Log.d("imagesList", receivedUris.toString())
            receivedUri = receivedUris
            val retrieveType: String? = it.getString("mediaType")
            if (retrieveType != null) {
                retrieveType1 = retrieveType
                Log.d("mediaType", retrieveType1)
            }
            val retrieveBase64Video1: Uri? = it.getParcelable("video")
            retrieveBase64Video = retrieveBase64Video1
            seekTimeOutput = it.getString("seekTime")

        }
        setUpUi()
        paginationScroll()
        mentionObserver()
        hashTagObserver()
        Log.d("VideoSeekTimeIs", "Video seek time is $seekTimeOutput")

//        if(retrieveBase64Video !=null){
//            //val videoUri = Uri.parse(retrieveBase64Video)
//            mBinding?.video?.setVideoURI(retrieveBase64Video)
//            mBinding?.video?.start()
//        }
//        if (retrieveType1 == "image") {
//            checkIfFragmentAttached {
//                val adapter = ImageSlider(requireContext(), receivedUri)
//                mBinding?.imagePager?.adapter = adapter
//                adapter.updateDataSet(receivedUri)
//                mBinding?.video?.visibility = View.GONE
//                imageView?.visibility = View.VISIBLE
//            }
//
//
//        } else if (retrieveType1 == "video") {
//            mBinding?.video?.visibility = View.VISIBLE
//            imageView?.visibility = View.GONE
//            receivedUri?.clear()
//            //data.add(MediaData("mp4", retrieveBase64Video.toString()))
//
//        }
        initializePlayer()
        callApi()

//        imageView = mBinding?.imagePager
//        mBinding?.playerView?.player = player


        mBinding?.IvBack?.setOnClickListener {
            findNavController().navigateUp()
            Prefs.init().TagList = ""
        }


        mBinding?.IvClose?.setOnClickListener {
            if (isCreatingPost) {
                isCreatingPost = false
                dialogs!!.dismiss()
                mBinding?.postBT?.isEnabled = true
                mBinding!!.IvBack.isEnabled = true
            }
            findNavController().navigateUp()
            Prefs.init().TagList = ""
        }





        viewModel.mutableBaseResponse.observe(viewLifecycleOwner) {

                responseWithoutMedia ->

            run {

                if (responseWithoutMedia.success) {

                    dialogs?.dismiss()

                    responseWithoutMediaSuccess = true

                    responseWithoutMedia.data?.let { it.post?.let { it1 ->
                        Log.d("successData",
                            it1
                        )
                    } }
                    Log.e("kjgss",responseWithoutMedia.toString())
                    val stringList = ArrayList<String>()

                    for(x in 0 until (receivedUri?.size ?: 0)){
                        receivedUri?.get(x)?.let { stringList.add(it.toString()) }
                    }

                    Log.d("compressedImageswwwwwwww", "All images are compressed : $stringList")

                    val createPostData = CreatePostEntity(id = responseWithoutMedia.data?.post?:"",disappearTime = disappearIn,type = retrieveType1,

                        download = downloadAllowed, comment = commentAllowed, repost = repostAllowed, like = seeLikesAllowed,
                        description = mBinding?.editText3?.text.toString(), privacy = postingFor, tagUsers = tagList1,
                        images = stringList, video = retrieveBase64Video.toString(), seekTime = seekTimeOutput)
                    CoroutineScope(Dispatchers.IO).launch {
                        viewModel.createPostInDB(createPostData)
                    }
                    seekTimeOutput?.let { bundle.putString("seekInputTime", it) }
                    bundle.putString("postId", responseWithoutMedia.data!!.post)
                    bundle.putParcelableArrayList("mediaImage", receivedUri)
                    bundle.putString("mediaVideo", retrieveBase64Video.toString())
                    bundle.putString("mediaType", retrieveType1)
                    bundle.putBoolean("responseSuccess", responseWithoutMediaSuccess)
//                    findNavController().navigate(R.id.homeFragment, bundle)
                    activity.let {
                        it.homeTabClick(bundle)
                    }
                    mBinding?.postBT?.isEnabled = true
                    mBinding!!.IvBack.isEnabled = false

                } else if (responseWithoutMedia.success == false) {
                    dialogs?.dismiss()
                    responseWithoutMedia.error
                    Log.d("postCreated", "i am here ${responseWithoutMedia.error}")
                    mBinding?.postBT?.isEnabled = true
                    mBinding?.IvBack?.isEnabled = false
                }
            }
        }



        viewModel.hashTagListResponse.observe(viewLifecycleOwner) {
            hashTagListResponse ->
            if(hashTagListResponse.success!!){
                Log.d("hashTagCreated", "hash tag is here $hashTagListResponse")
            }
            else {
                Log.d("hashTagCreated", "hash tag response is not " +
                        " here $hashTagListResponse")
            }
        }

//        viewModel.mediaResponse.observe(viewLifecycleOwner, Observer {
//                responseWithMedia ->
//            if(responseWithMedia.success) {
//
//                Log.d("mediaSuccess", "Post is created with media")
//                dialogs?.dismiss()
//                CoroutineScope(Dispatchers.IO).launch {
//                    viewModel.deleteAll()
//                }
//
//            }
//            else {
//                dialogs?.dismiss()
//                responseWithMedia.error
//                Log.d("postCreated", "i am here ${responseWithMedia.error}")
//                mBinding?.postBT?.isEnabled = true
//                mBinding!!.IvBack.isEnabled = false
//
//            }
//        })




        mBinding?.postBT?.setOnClickListener {




            if (NetworkUtils.isInternetAvailable.value == true) {


                showProgressDialog()
                val ediTTextData =
                    convertStringFormatToSendApi(mBinding?.editText3?.text.toString())
                Log.d("PostUploadData", "text in edit text is $ediTTextData")
                isCreatingPost = true
                mBinding?.postBT?.isEnabled = false
                mBinding!!.IvBack.isEnabled = false


                if (!sfTagListData.isNullOrEmpty()) {
                    sfTagListData!!.forEach {
                        finalTag.add(it._id)
                        tagList1?.add(it._id)
                    }
                }





                    Log.d("PostUploadData", "Post disappears : $disappearIn retrieve type is : $retrieveType1" +
                            "  country is ${getCountryNameFromCode()}")

                    viewModel.createPost(
                        CreatePost(
                            disappearTime = disappearIn,
                            type = retrieveType1,
                            privacy = postingFor,
                            options = Options(
                                download = downloadAllowed, comment = commentAllowed,
                                repost = repostAllowed, like = seeLikesAllowed
                            ),
                            description = ediTTextData, country = getCountryNameFromCode(), tagUsers = tagList1,
                            mentionUsers = mentionUserIdList
                        )
                    )




            }
            else {
                    Toast.makeText(requireContext(), "Please check your internet connection", Toast.LENGTH_SHORT).show()
                }

        }

        NetworkUtils.initNetworkAvailabilityCheck(requireContext())

        NetworkUtils.isInternetAvailable.observe(viewLifecycleOwner) { isAvailable ->
            if (!isAvailable) {
                dialogs?.dismiss()
                mBinding?.postBT?.isEnabled = true
                mBinding!!.IvBack.isEnabled = true
                Toast.makeText(
                    requireContext(),
                    "Please check your internet connection",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }



        mBinding?.tvPublic?.setOnClickListener {
            showPostingForBottomSheet()
        }

        mBinding?.tvDisappearSelect?.setOnClickListener {
           showDisappearTimeBottomSheet()

        }
        mBinding?.tvTagName?.setOnClickListener {

            mBinding?.tvTagName?.disableMultiTap()
            showTagCollabBottomSheet()
        }


//        mBinding?.video?.setOnPreparedListener {
//            it.start()
//        }
        mBinding?.IvClose?.setOnClickListener {
            discardPost()
        }


    }

    private fun mentionObserver() {
        authViewModel.mentionUserData.observe(viewLifecycleOwner) { response ->
            if (response != null) {
                if (response.success) {
                    isLoading = false
                    dialogs?.dismiss()
                    mBinding?.animationPaginationLoader?.visibility = View.GONE
                    if (response.data != null) {

                        var mentionListUser = response.data as ArrayList<MentionUserData>
                        Log.d("mentionUserList", "Mention users are $mentionListUser")
                        mentionLists = response.data?: arrayListOf()
                        adapter.addNewItems(mentionListUser)
                    }
                }
                else {
                    isLoading = false
                    mBinding?.animationPaginationLoader?.visibility = View.GONE
                }
            }
        }

        authViewModel.showLoading.observe(viewLifecycleOwner) {
        }
    }

    private fun hashTagObserver() {
        viewModel.hashTagListResponse.observe(viewLifecycleOwner) { response ->
            if (response != null) {
                if (response.success == true) {
                    dialogs?.dismiss()

                    if (response.data != null) {
                        Log.d("myHashTag", response.data.toString())
                        hashTagList = response.data
                        hashtagAdapter.setNewItems(hashTagList)
                    }
                }
            }

        }
    }


    private fun updateCountTextView() {
        mBinding?.tvZero?.text = count.toString()
    }
    fun getFileFromUri(context: Context,uri: Uri): File? {
        val contentResolver = context.contentResolver
        var filePath: String? = null

        // Get the file path from the Uri
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            it.moveToFirst()
            val columnIndex = it.getColumnIndex(MediaStore.Images.Media.DATA)
            filePath = it.getString(columnIndex)
        }

        // Create a File object using the file path
        return if (filePath != null) {
            File(filePath)
        } else {
            null
        }
    }




    private fun showPostingForBottomSheet() {
        val bottomSheetFragment: BottomSheetDialogFragment = PostingForDialogBottomSheet(this)
        bundle.putString("postText", mBinding!!.tvPublic.text.toString())
        bottomSheetFragment.arguments = bundle
        bottomSheetFragment.show(childFragmentManager , bottomSheetFragment.tag)



    }


    private fun callApi() {
        if (Utils.isInternetAvailables(requireContext())) {
            authViewModel.deletePostRequest =
                DeletePost(post = postId)
            authViewModel.userPostDetail()
        } else {
            Utils.showToast("Please check your internet connection.")
        }
    }


    private fun setUpUi() {

        // setEdittextForKeyboard()

        mBinding?.apply {

            setHashTagRecyclerViewAdapter()
            setRecyclerViewAdapter()
            mBinding?.editText3?.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    //                    Log.e("commentEt","before "+p0.toString())
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    val cursorPosition = if (mBinding?.editText3!!.selectionStart<0){
                        0
                    }else{
                        mBinding?.editText3!!.selectionStart
                    }
                    val text = mBinding?.editText3!!.text.toString()

                    startIndex = cursorPosition
                    endIndex = cursorPosition

                    while (startIndex > 0 && !text[startIndex - 1].isWhitespace()) {
                        startIndex--
                    }

                    while (endIndex < text.length && !text[endIndex].isWhitespace()) {
                        endIndex++
                    }

                    val selectedWord = text.substring(startIndex, endIndex)
                    Log.d("selectedWord", selectedWord)


                    if (selectedWord.startsWith("@")) {
                        rvMentionList.visibility = View.VISIBLE
                        searchValue = selectedWord.removePrefix("@")
                        setRecyclerViewAdapter()
                        if (Utils.isInternetAvailables(requireContext())) {
                            handler.removeCallbacksAndMessages(null)
                            handler.postDelayed({
                                Handler(Looper.getMainLooper()).postDelayed({
                                    hitMentionApi(selectedWord.removePrefix("@"))

                                }, 0)

                            }, 200)

                        }
                    }else if (selectedWord.startsWith("#")) {
                        rvMentionList.visibility = View.VISIBLE
                        //setHashTagRecyclerViewAdapter()
                        Log.d("hashSelected", "hash has been written")
                        if (Utils.isInternetAvailables(requireActivity())) {

                            handler.removeCallbacksAndMessages(null)
                            handler.postDelayed({
                                setHashTagRecyclerViewAdapter()
                                Handler(Looper.getMainLooper()).postDelayed({
                                    hashTagApiCall(selectedWord.removePrefix("#"))
                                }, 0)

                            }, 200)

                        }
                    }

                    else {
                        rvMentionList.visibility = View.GONE
                    }
                    val length: Int? = mBinding?.editText3?.text?.length
                    mBinding?.tvZero?.text = length.toString()
                }

                override fun afterTextChanged(p0: Editable?) {

                    if (!p0.isNullOrEmpty())
                        if (p0.toString().last() == ' ') {
//                        Log.e("commentEt","after "+findLastWord(p0.toString()))
                            val lastWord = findLastWord(p0.toString().trim())
                            if (lastWord.startsWith("@")) {
                                if (!mentionUserHash.containsKey(lastWord)) {
                                    if (mentionLists.size > 0 &&
                                        mentionLists[0].username == lastWord.substring(1)
                                    ) {
                                        mentionUserHash[lastWord] = mentionLists[0]._id
                                    }
                                }
                            }else if (lastWord.startsWith("#")){
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


        }
        callApi()
        //   paginationScroll()
    }

    private fun hashTagApiCall(search: String){
        checkIfFragmentAttached {


            if (Utils.isInternetAvailables(requireContext())) {
                hashtagOffset = 0
                viewModel.hashTagList(
                    HashtagPayload(search = search)
                )

            }
            else {
                showToast(getString(R.string.no_internet_message))
            }
        }
    }



    private fun paginationScroll() {
        mBinding?.rvMentionList?.addOnScrollListener(object : RecyclerViewScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                Log.d("mentionListScroll", "Mention list scroll is not working")
                //                hideSearchKeyboard()
                if (!recyclerView.canScrollVertically(1) && dy > 0 && !isLoading) {
                    Log.d("mentionListScroll", "Mention list scroll is not getting inside")
                    mBinding?.animationPaginationLoader?.visibility = View.VISIBLE
                    isLoading = true
                    offset += 10
                    // search=""
                    loadMoreData()
                }
            }

            override fun onItemIsFirstVisibleItem(index: Int, lastItemIndex: Int) {

            }
        })
    }


    private fun hitMentionApi(search : String) {
        checkIfFragmentAttached {
            if (Utils.isInternetAvailables(requireContext())) {
                offset = 0
                authViewModel.mentionUserRequests =
                    mentionUserRequest(offset = offset, limit = 10, search = searchValue)
                authViewModel.mentionUser()
            } else {
                showToast(getString(R.string.no_internet_message))
            }
        }
    }

    private fun loadMoreData() {
        if (isInternetAvailable(requireContext())) {

            authViewModel.mentionUserRequests =
                mentionUserRequest(offset = offset, limit = 10, search = searchValue)
            authViewModel.mentionUser()
        } else {
            showToast(getString(R.string.no_internet_message))
        }
    }


    private fun handleSelection(selectedWord: String) {
        val text = mBinding?.editText3?.text.toString()

        val startIndex = text.indexOf(selectedWord)
        val endIndex = startIndex + selectedWord.length
        val startsWithSymbol = text.startsWith(selectedWord)

        if (startsWithSymbol) {

            val newText = text.removeRange(startIndex, endIndex)
            mBinding?.editText3?.setText(newText)
            mBinding?.editText3?.setSelection(newText.length) // Set cursor position to the end
        }
    }


    private fun setRecyclerViewAdapter() {
        mBinding.apply {
            checkIfFragmentAttached {
                manager = LinearLayoutManager(requireContext())
            }


            this!!.rvMentionList.layoutManager = manager
            adapter = MentionListAdapter(R.layout.mention_list_recycleview,
                object : MentionListAdapter.mentionClick {
                    override fun onClick(id: String, username: String) {
                        mentionUserHash["@${username}"] = id
                        mBinding?.editText3!!.text =
                            mBinding?.editText3!!.text.replace(startIndex, endIndex, "@${username} ")
                        mBinding?.editText3!!.setSelection(mBinding?.editText3!!.text.toString().length)
                        highlightTagsAndMentions(mBinding?.editText3!!.text)
                        mBinding!!.rvMentionList.visibility = View.GONE
                    }
                })
            rvMentionList.adapter = adapter

        }
    }
    private fun setHashTagRecyclerViewAdapter() {
        mBinding.apply {
            checkIfFragmentAttached {
                manager = LinearLayoutManager(requireContext())
            }

            this!!.rvMentionList.layoutManager = manager
            hashtagAdapter = HashTagListAdapter(R.layout.recycler_hashtag_list,
                object : HashTagListAdapter.HashTagClick {
                    override fun onClick(id: String, username: String) {
                        hashtagUserHash["#${username}"] = id
                        mBinding?.editText3!!.text =
                            mBinding?.editText3!!.text.replace(startIndex, endIndex, "#${username} ")
                            //mBinding?.editText3!!.text.replace(startIndex, endIndex, "@${username} ")
                        mBinding?.editText3!!.setSelection(mBinding?.editText3!!.text.toString().length)
                        highlightTagsAndMentions(mBinding?.editText3!!.text)
                        mBinding!!.rvMentionList.visibility = View.GONE



                    }
                })
            rvMentionList.adapter = hashtagAdapter

        }
    }



    private fun findLastWord(text: String): String {
        val words = text.trim().split(Regex("\\s+")) // Split by whitespace characters
        return if (words.isNotEmpty()) {
            words.last()
        } else {
            ""
        }
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
   /* private fun highlightTagsAndMentions(editable: Editable?) {
        val text = editable.toString()

        checkIfFragmentAttached {
            val hashtagPattern = Pattern.compile("\\B#([a-zA-Z0-9_\\p{Punct}]+)\\b")
            val hashtagMatcher = hashtagPattern.matcher(text)

            while (hashtagMatcher.find()) {
                val start = hashtagMatcher.start()
                val end = hashtagMatcher.end()
                val hashtag = text.substring(start, end)
                if (hashtagUserHash.containsKey(hashtag)) {
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
    }*/

//    fun mentionUserCallApi(search: String) {
//        checkIfFragmentAttached {
//            if (Utils.isInternetAvailables(requireContext())) {
//                //  showProgressDialogs()
//                authViewModel.mentionUserRequests =
//                    mentionUserRequest(offset = 0, limit = 100, search = search)
//                authViewModel.mentionUser()
//            }
//        }
//    }



    private fun convertStringFormatToSendApi(text: String): String {
        var processedString = ""
        Log.d("PostUploadData", "text function in edit text is $text")

        for (word in text.split(" ")) {
            if (mentionUserHash.containsKey(word)) {
                val username = word.substring(1)
                val userId = mentionUserHash[word]
                processedString += "@[$username]($userId) "
                if (userId != null) {
                    listMentionUsers.add(userId)
                }
                // Add userId to mentionUserIdList if not present
                if (userId != null && !mentionUserIdList.contains(userId)) {
                    mentionUserIdList.add(userId)
                }
            } else {
                // Append the original word to the processed string
                processedString += "$word "
            }
        }
        Log.d("PostUploadData", "Mention users are $listMentionUsers")

        return processedString.trim()  // Trim to remove trailing whitespace
    }

    private fun discardPost() {
        mBinding?.overlayLayout?.visibility = View.VISIBLE
        dialog?.setCancelable(false)
        dialog?.setCanceledOnTouchOutside(true)
        Objects.requireNonNull(dialog?.window)?.setBackgroundDrawable(
            ColorDrawable(
                Color.TRANSPARENT
            )
        )
        dialog?.setContentView(R.layout.discard_post)

        val logout = dialog?.findViewById(R.id.Logout) as TextView

        logout.setOnClickListener {
            checkIfFragmentAttached {
                if (isInternetAvailable(requireContext())) {
                    bundle.putBoolean("navigating", true)
                    findNavController().navigate(
                        R.id.action_createPostFragment_to_uploadFragment,
                        bundle
                    )
                } else {

                    Utils.showToast("Please check your internet connection.")
                }
                dialog?.dismiss()
            }
        }
        dialog?.show()
    }




    @SuppressLint("SetTextI18n")
    private fun setPinObserver() {
        authViewModel.postDetailData.observe(viewLifecycleOwner) { response ->
            if (response != null) {
                Log.d("response1111", " response is not null")
                if (response.success) {
                    Log.d("response", " response is success")
                    //        dialogs?.dismiss()
                    if (response.data != null) {
                        response.data.let {
                            if (it!!.options.comment) {
                                mBinding?.ivRadioSelect?.visibility = View.VISIBLE
                            } else {
                                mBinding?.ivRadioSelect?.visibility = View.GONE
                            }
                            if (it.options.download) {
                                mBinding?.ivRadioSelect2?.visibility = View.VISIBLE
                            } else {
                                mBinding?.ivRadioSelect2?.visibility = View.GONE
                            }

                            if (it.options.repost) {
                                mBinding?.ivRadioSelect4?.visibility = View.VISIBLE
                            } else {
                                mBinding?.ivRadioSelect4?.visibility = View.GONE
                            }

                            /**To Show the tagged user */
                            tagList = it?.tagUsers?:ArrayList()
                            val totalUser = tagList.size - 1
                            when (it.tagUsers?.size) {
                                0 -> {
                                    mBinding?.tvTagName?.text = "No User"
                                }
                                1 -> {
                                    mBinding?.tvTagName?.text = it.tagUsers[0].name
                                }
                                2 -> {
                                    mBinding?.tvTagName?.text =
                                        sfTagListData!![0].name + " " + "and" + " " + totalUser.toString() + " " + "other"
                                }
                                else -> {
                                    mBinding?.tvTagName?.text =
                                        sfTagListData!![0].name + " " + "and" + " " + totalUser.toString() + " " + "others"
                                }
                            }


                            /**To Show the privacy user */
                            mBinding?.tvPublic?.text = it.privacy


                            /**To Show the DisappearTime user */
                            mBinding?.tvDisappearSelect?.text = it.disappearTime


                            /**To set Post type is (Image or Video)*/
//                            if (response.data!!.type == "video") {
//                                mBinding?.playerView?.player=player
//                                val mediaItem = MediaItem.fromUri(Uri.parse(retrieveVideo))
//                                player.setMediaItem(mediaItem)
//                                player.prepare()
//                                player.play()
//
//                                mBinding?.playerView?.visibility=View.VISIBLE
//                                mBinding?.ivPostImage?.visibility=View.GONE
//                            } else if (response.data!!.type == "image") {
//                                mBinding?.playerView?.visibility=View.GONE
//                                mBinding?.ivPostImage?.visibility=View.VISIBLE
//                                if (it.images?.isNotEmpty() == true) {
//                                    checkIfFragmentAttached {
//                                        Glide.with(requireContext()).load(it.images?.get(0))
//                                            .into(mBinding!!.ivPostImage)
//                                    }
//                                }
//                            }

                        }

                    }
                }
            } else {
                Log.d("response", "You are not getting the required response")

            }
        }

        authViewModel.showLoading.observe(viewLifecycleOwner) {
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    override fun onDestroy() {
        super.onDestroy()
        releasePlayer()
        Prefs.init().TagList = ""
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    override fun onStart() {
        super.onStart()
        org.greenrobot.eventbus.EventBus.getDefault().register(this)
        initializePlayer()
    }


    override fun onStop() {
        super.onStop()
        org.greenrobot.eventbus.EventBus.getDefault().unregister(this)
        releasePlayer()
    }
    private fun releasePlayer() {
        player.release()
        //player = null
    }
    @SuppressLint("SetTextI18n")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: MessageEvent?) {
        if (event!!.text == "") {
            Log.e("events", event.text)

            val type = object : TypeToken<ArrayList<UserList>>() {}.type
            sfTagListData = Gson().fromJson(Prefs.init().TagList, type)
            Log.e("events", sfTagListData!!.size.toString())

            for (userList in sfTagListData!!) {
                tagName = userList.name
            }
            val totalUser = sfTagListData!!.size - 1

            when (sfTagListData!!.size) {
                0 -> {
                    mBinding?.tvTagName?.text ="No User"
                }
                1 -> {
                    mBinding?.tvTagName?.text = sfTagListData!![0].name
                }
                2 -> {
                    mBinding?.tvTagName?.text =
                        sfTagListData!![0].name + " " + "and" + " " + totalUser.toString() + " " + "other"
                }
                else -> {
                    mBinding?.tvTagName?.text =
                        sfTagListData!![0].name + " " + "and" + " " + totalUser.toString() + " " + "others"
                }
            }
            //  setPinObserver()
        }

    }

    @SuppressLint("UnsafeOptInUsageError")
    fun initializePlayer(){
        checkIfFragmentAttached {
            player = ExoPlayer.Builder(requireContext())
                .setLoadControl(PlayerViewAdapter.getLoadControl())
                .setTrackSelector(PlayerViewAdapter.getTrackSelector(requireContext()))
                .build()
        }

    }



    private fun showDisappearTimeBottomSheet() {
        val bottomSheetFragment: BottomSheetDialogFragment = DisappearsTimeBottomSheet(this)
        bundle.putString("disappearTime", mBinding?.tvDisappearSelect?.text.toString() )
        bottomSheetFragment.arguments = bundle
        bottomSheetFragment.show(childFragmentManager, bottomSheetFragment.tag)
    }





    private fun showTagCollabBottomSheet() {
        val bottomSheetFragment: BottomSheetDialogFragment = TagCollarBottomSheetFragment()
        val bundle = Bundle()
        val tagGson = Gson()
        val json = tagGson.toJson(tagList)
        bundle.putString("tagList", json.toString())
        bottomSheetFragment.arguments = bundle
        bottomSheetFragment.show(requireFragmentManager(), bottomSheetFragment.tag)
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

    companion object{
        val bundle = Bundle()
    }

    override fun onClickTags(data: String, postingForText: String) {
       Log.d("postingForDialog", " $data  $postingForText")
        mBinding?.tvPublic?.text= postingForText
        Log.d("disppearData", "Posting  data : $data $postingForText")
        postingFor = data
    }

    override fun onClickDisappearClick(disappearData: String,disappearSendData: String) {
        mBinding?.tvDisappearSelect?.text  = disappearData
        Log.d("disppearData", "Disappear data : $disappearSendData")
        disappearIn =disappearSendData

    }
}




