package com.wedo8020.app.mainui.dialogFragments

import android.app.Dialog
import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.InputFilter
import android.text.Spanned
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.core.widget.NestedScrollView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.apachat.swipereveallayout.core.SwipeLayout
import com.wedo8020.app.R
import com.wedo8020.app.databinding.CommentBottomsheetLayoutBinding
import com.wedo8020.app.databinding.CommentItemLayoutBinding
import com.wedo8020.app.mainui.adapter.CommentsAdapter
import com.wedo8020.app.mainui.adapter.MentionListAdapter
import com.wedo8020.app.mainui.home.postFragment.PostViewModel
import com.wedo8020.app.postOptionListeners
import com.wedo8020.app.request.CommentRequest
import com.wedo8020.app.request.PostRequest
import com.wedo8020.app.request.mentionUserRequest
import com.wedo8020.app.response.Comment
import com.wedo8020.app.response.MentionUserData
import com.wedo8020.app.utils.Utils
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.wedo8020.app.mainui.home.createpost.adapter.HashTagListAdapter
import com.wedo8020.app.request.DeleteCommentRequest
import com.wedo8020.app.request.Hashtag
import com.wedo8020.app.request.HashtagPayload
import com.wedo8020.app.request.PinCommentRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.regex.Pattern


class CommentsBottomSheet(
    val postId: String,
    val postCreator: Boolean,
    val clickListeners: postOptionListeners,
    private var totalComment: String,
    private val commentListener: ClickListeners
) : BottomSheetDialogFragment() {
    private lateinit var binding: CommentBottomsheetLayoutBinding
    private var startIndex = -1
    private var endIndex = -1
    private var offset = 0
    private var limit = 10
    private var handler = Handler(Looper.getMainLooper())
    private val authViewModel by viewModels<PostViewModel>()
    private var mentionLists: ArrayList<MentionUserData> = ArrayList()
    private lateinit var adapter: MentionListAdapter
    private lateinit var hashtagAdapter: HashTagListAdapter
    private lateinit var manager: LinearLayoutManager
    private var mentionUserHash = hashMapOf<String, String>()
    private var stringForApi = ""
    private var mentionUserIdList = ArrayList<String>()
    private lateinit var commentAdapter: CommentsAdapter
    private var swipeLayout: SwipeLayout? = null
    private lateinit var commentManager: LinearLayoutManager
    private var isLoading = false
    private var deleteCommentPosition: Int? = null
    private var commentID: String? = null
    private var commentPosition: Int = -1
    private var commentBinding: CommentItemLayoutBinding? = null
    var commentDescription: String? = null
    private var isBottomSheetOpen: Boolean = false
    private var parentComment = ""
    private var parentCommentPos = -1
    private var hashtagUserHash = hashMapOf<String, String>()
    var hashTagList: ArrayList<Hashtag> = ArrayList()
    private var isKeyboardOpen = false
    private var myCoroutineScope = CoroutineScope(Dispatchers.Main)
    companion object {
        private const val BOTTOM_SHEET_STATE = "bottom_sheet_state"
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(BOTTOM_SHEET_STATE, isBottomSheetOpen)
        super.onSaveInstanceState(outState)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState?.let {
//            setStyle(STYLE_NORMAL, R.style.MyTransparentBottomSheetDialogTheme)
            isBottomSheetOpen = it.getBoolean(BOTTOM_SHEET_STATE, false)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(),R.style.MyTransparentBottomSheetDialogTheme)
        dialog.setOnShowListener { dialogInterface ->
            val bottomSheetDialog = dialogInterface as BottomSheetDialog
            val bottomSheet =
                bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            val behaviour = BottomSheetBehavior.from(bottomSheet!!)

            behaviour.state = BottomSheetBehavior.STATE_EXPANDED
            val screenHeight = resources.displayMetrics.heightPixels
            val peekHeight = getBottomSheetRootLayoutSize()
            behaviour.peekHeight = peekHeight
            behaviour.skipCollapsed = true
        }
        val window = dialog.window
        // Add the FLAG_NOT_TOUCH_MODAL flag to allow interaction with views outside the dialog
        window?.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_DIM_BEHIND
        )
        window?.setDimAmount(0.0f)
        return dialog
    }

    private fun keyboardListenter() {
        val rootView = binding.root

        rootView.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            private var wasKeyboardOpen = false

            override fun onPreDraw(): Boolean {
                val heightDiff = rootView.rootView.height - rootView.height

                val isOpen = heightDiff > 100 // Adjust this threshold as needed

                if (isOpen != wasKeyboardOpen) {
                    wasKeyboardOpen = isOpen

                    if (isOpen) {
                        // Keyboard is open
                        println("Keyboard open")
                    } else {
                        // Keyboard is closed
                        println("Keyboard closed")
                    }
                }

                return true
            }
        })
    }

    private fun getBottomSheetRootLayoutSize(): Int {
        val rootView = view
        rootView?.measure(
            View.MeasureSpec.UNSPECIFIED,
            View.MeasureSpec.UNSPECIFIED
        )
        return rootView?.measuredHeight ?: 0
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (!::binding.isInitialized) {
            binding = CommentBottomsheetLayoutBinding.inflate(inflater, container, false)
        }
//            binding.root.viewTreeObserver?.addOnPreDrawListener(this)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        setStyle(STYLE_NORMAL, R.style.DialogStyle)
//        binding.root?.viewTreeObserver?.addOnGlobalLayoutListener(globalLayoutListener)

//        dialog?.window?.decorView?.viewTreeObserver?.addOnPreDrawListener(preDrawListener)

       /* binding.root.viewIsLoaded {
            val heightInit = binding.root.rootView.height - binding.root.height
            dialog?.window?.decorView?.viewTreeObserver?.addOnPreDrawListener(object :
                ViewTreeObserver.OnPreDrawListener {
                private var wasKeyboardOpen = false

                override fun onPreDraw(): Boolean {
                    val heightDiff = binding.root.rootView.height - binding.root.height

                    val isOpen = heightDiff > heightInit // Adjust this threshold as needed
                    if (isOpen != wasKeyboardOpen) {
                        wasKeyboardOpen = isOpen
                        val commentHeight = binding.searchCard.height
                        val constraintSet = ConstraintSet()
                        constraintSet.clone(binding.mainLayout)

                        // Add a margin from the bottom to 'myView' (change 16 to your desired margin)
//                        constraintSet.clear(binding.searchCard.id, ConstraintSet.BOTTOM)

                        // Apply the constraints
//                        constraintSet.applyTo(binding.mainLayout)
                        if (isOpen) {
                            // Keyboard is open
                            constraintSet.setMargin(
                                binding.searchCard.id,
                                ConstraintSet.BOTTOM,
                                commentHeight
                            )

                            // Apply the constraints
                            constraintSet.applyTo(binding.mainLayout)

                            Log.e("isKeyboard", "open")
                        } else {
                            constraintSet.setMargin(
                                binding.searchCard.id,
                                ConstraintSet.BOTTOM,
                                commentHeight
                            )

                            // Apply the constraints
                            constraintSet.applyTo(binding.mainLayout)
                            // Keyboard is closed
                            Log.e("isKeyboard", "close")
                        }
                    }

                    return true
                }
            })

        }*/
        setUpUi()
        mentionObserver()

        authViewModel.deleteCommentData.observe(viewLifecycleOwner) { data ->
            if (data.success) {
                if (parentComment.isNotEmpty()) {
                    if (parentCommentPos > -1) {
                        val parentView =
                            binding.commentList.layoutManager?.findViewByPosition(parentCommentPos)
                        if (parentView != null) {
                            val binding1: Any? =
                                DataBindingUtil.bind(parentView)
                            if (binding1 is CommentItemLayoutBinding) {
                                val childAdapter = binding1.moreRepliesRecyclerView.adapter
                                if (childAdapter is CommentsAdapter) {
                                    deleteCommentPosition?.let {
                                        childAdapter.removeItem(it)
                                        commentAdapter.list[parentCommentPos].subComments?.removeAt(
                                            it
                                        )
                                    }
                                    commentListener.deleteClick(1)
                                    val totalCommentDeleted: Int

                                    if (commentAdapter.list[parentCommentPos].noOfSubComments > 0) {
                                        commentAdapter.list[parentCommentPos].noOfSubComments =
                                            commentAdapter.list[parentCommentPos].noOfSubComments - 1
                                        totalCommentDeleted =
                                            commentAdapter.list[parentCommentPos].noOfSubComments
                                    } else {
                                        if (!commentAdapter.list[parentCommentPos].subComments.isNullOrEmpty()) {

                                            totalCommentDeleted =
                                                commentAdapter.list[parentCommentPos].subComments!!.size
                                        } else {
                                            totalCommentDeleted = 0
                                        }
                                    }
                                    (if (totalCommentDeleted == 0) {
                                        "No Reply"
                                    } else {
                                        if (totalCommentDeleted == 1) {
                                            "1 Reply"
                                        } else {
                                            "${totalCommentDeleted} Replies"
                                        }
                                    }).also { binding1.moreRepliesTxt.text = it }
                                    try {
                                        val totalComments = totalComment.toInt() - 1
                                        totalComment = totalComments.toString()
                                        setTotalCommentLabel(totalComment)
                                    } catch (_: Exception) {
                                    }
                                }
                            }
                        }
                    }
                } else {
                    deleteCommentPosition?.let {

                        val totalCommentDeleted = if (commentAdapter.list[it].noOfSubComments > 0) {
                            commentAdapter.list[it].noOfSubComments
                        } else {
                            if (!commentAdapter.list[it].subComments.isNullOrEmpty()) {

                                commentAdapter.list[it].subComments!!.size

                            } else {
                                0
                            }
                        }
                        try {
                            val totalComments = totalComment.toInt() - (totalCommentDeleted + 1)
                            totalComment = totalComments.toString()
                            setTotalCommentLabel(totalComment)
                        } catch (_: Exception) {
                        }

                        commentAdapter.removeItem(it)
                        commentListener.deleteClick(totalCommentDeleted + 1)
                    }


                }


            }
        }
        authViewModel.pinCommentData.observe(viewLifecycleOwner) { data ->
            if (data.success) {
                Log.d("pinCommentSuccess", "Pinnec comment")
//              commentAdapter.list.sortedBy {
//                  it.pinTime
//
//              }
                callApi()
                // adapter.notifyDataSetChanged()
            }
        }
    }

    override fun onDestroyView() {
        // Unregister the OnPreDrawListener to avoid memory leaks
//        dialog?.window?.decorView?.viewTreeObserver?.removeOnPreDrawListener(preDrawListener)
//        rootView = null
//        binding.root?.viewTreeObserver?.removeOnGlobalLayoutListener(globalLayoutListener)

        super.onDestroyView()
    }

    private fun setEdittextForKeyboard() {
        binding.apply {
            commentEt.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    // Add an OnPreDrawListener to further adjust the BottomSheet height when the keyboard appears
                    root?.viewTreeObserver?.addOnPreDrawListener(object :
                        ViewTreeObserver.OnPreDrawListener {
                        override fun onPreDraw(): Boolean {
                            root?.viewTreeObserver?.removeOnPreDrawListener(this)
                            // Additional adjustment to the BottomSheet height based on the keyboard height
                            val heightDiff = root?.height?.minus(mainLayout.height) ?: 0
                            if (heightDiff > 0) {
                                val layoutParams = mainLayout.layoutParams
                                layoutParams.height = layoutParams.height + heightDiff
                                Log.e("fgsfgs", "asga")
                            }
                            return true
                        }
                    })
                }
            }

        }
    }


    private fun isTouchInsideEditText(event: MotionEvent): Boolean {
        val view = activity?.currentFocus
        if (view is View) {
            val location = IntArray(2)
            view.getLocationOnScreen(location)
            val x = event.rawX + view.left - location[0]
            val y = event.rawY + view.top - location[1]
            return (x > 0 && x < view.width && y > 0 && y < view.height)
        }
        return false
    }

    private fun hideKeyboard() {
        try {
            if (context != null) {
                val imm =
                    requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.commentEt.windowToken, 0)
            }
        } catch (_: Exception) {
        }

    }

    private fun setUpUi() {


        keyboardListenter()
//        setEdittextForKeyboard()
        binding.apply {

            mainLayout1.setOnClickListener {
                dismiss()
            }
            view.setOnClickListener {
                dismiss()
            }

            container.setOnClickListener {
                hideKeyboard()
            }
//            commentEt.requestFocus()
//            commentEt.filters = arrayOf(NoLeadingSpaceInputFilter())
            mainLayout.setOnClickListener {
                hideKeyboard()
            }
            setRecyclerViewAdapter()
            commentEt.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
//                    Log.e("commentEt","before "+p0.toString())
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
//                    Log.e("commentEt","ontext "+p0.toString())
                    val cursorPosition = commentEt.selectionStart
                    val text = commentEt.text.toString()

                    startIndex = cursorPosition
                    endIndex = cursorPosition

                    // Find the start index of the word
                    while (startIndex > 0 && !text[startIndex - 1].isWhitespace()) {
                        startIndex--
                    }

                    // Find the end index of the word
                    while (endIndex < text.length && !text[endIndex].isWhitespace()) {
                        endIndex++
                    }

                    val selectedWord = text.substring(startIndex, endIndex)


                    if (selectedWord.startsWith("@")) {
                        hashtagMentionListContainer.visibility = View.VISIBLE
                        container.visibility = View.GONE
                        setMentionRecyclerViewAdapter()
                        if (Utils.isInternetAvailables(requireActivity())) {
                            handler.removeCallbacksAndMessages(null)
                            handler.postDelayed({
                                Handler(Looper.getMainLooper()).postDelayed({
                                    mentionUserCallApi(selectedWord.removePrefix("@"))

                                }, 0)

                            }, 200)

                        }
                    } else if (selectedWord.startsWith("#")) {
                        hashtagMentionListContainer.visibility = View.VISIBLE
                        container.visibility = View.GONE
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
                        container.visibility = View.VISIBLE
                        hashtagMentionListContainer.visibility = View.GONE

                    }
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
                    /* if (p0?.isNotEmpty() == true && p0.toString().startsWith(" ")) {
                         // Remove leading space
                         p0.replace(0, 1, "")
                     }
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
                             }
                         }
                     highlightTagsAndMentions(p0)*/
                }
            })
            Log.e("asdfksa", totalComment)
            setTotalCommentLabel(totalComment)

            ivReplyCancel.setOnClickListener {
                commentID = null
                replyContainer.visibility = View.GONE
            }
            postBtn.setOnClickListener {
                if (Utils.isInternetAvailables(requireContext())) {
                    if (commentEt.text.toString().trim().isNotEmpty()) {
                        convertStringFormatToSendApi(commentEt.text.toString())
                        /* val commentRequest = CommentRequest(
                             post = postId,
                             stringForApi.trim(),
                             mentionUserIdList,
                             commentID
                         )*/
                        val mentionUsers = mentionUserIdList
                        val commentRequest = CommentRequest(
                            post = postId,
                            description = stringForApi.trim(),
                            mentionUsers = mentionUsers,
                            parentComment = commentID
                        )
                        authViewModel.addComment(commentRequest)

                        stringForApi = ""
                        commentEt.setText("")

                    } else {
                        Utils.showToast(getString(R.string.please_enter_comment))
                    }
                } else {
                    Utils.showToast(getString(R.string.no_internet_message))
                }

            }

            ivDialogCancel.setOnClickListener {
                dismiss()
            }

        }
        callApi()
        paginationScroll()
    }

    private fun hashTagApiCall(search: String) {
        if (Utils.isInternetAvailables(requireActivity())) {
            authViewModel.hashTagList(
                HashtagPayload(offset = 0, limit = 0, search = search)
            )

        }
    }

    private fun setTotalCommentLabel(totalComments: String) {
        binding.label.text = when (totalComments) {
            "0", "", "null" -> {
                "No Comments"
            }

            "1" -> {
                "1 Comment"
            }

            else -> {
                "$totalComment Comments"
            }
        }
    }

    /* private fun findLastWord(text: String): String {
         val textTrim = text.trim()
         val spaceIndex = textTrim.lastIndexOf(' ')
         return if (spaceIndex != -1) {
             textTrim.substring(spaceIndex + 1)
         } else {
             textTrim
         }
     }*/
    /*    private fun findLastWord(text: String): String {
            val textTrim = text.trim()
            val spaceIndex = textTrim.lastIndexOf(Regex("[\\s\\n]"))

            return if (spaceIndex != -1) {
                textTrim.substring(spaceIndex + 1)
            } else {
                textTrim
            }
        }*/
    private fun findLastWord(text: String): String {
        val words = text.trim().split(Regex("\\s+")) // Split by whitespace characters
        return if (words.isNotEmpty()) {
            words.last()
        } else {
            ""
        }
    }

    private fun setHashTagRecyclerViewAdapter() {
        binding.apply {
            manager = LinearLayoutManager(requireContext())
            hashtagMentionList.layoutManager = manager
            hashtagAdapter = HashTagListAdapter(R.layout.recycler_hashtag_list,
                object : HashTagListAdapter.HashTagClick {
                    override fun onClick(id: String, username: String) {
                        hashtagUserHash["#${username}"] = id
                        commentEt.text =
                            commentEt.text.replace(
                                startIndex,
                                endIndex,
                                "#${username} "
                            )
                        //mBinding?.editText3!!.text.replace(startIndex, endIndex, "@${username} ")
                        commentEt.setSelection(commentEt.text.toString().length)
                        highlightTagsAndMentions(commentEt.text)
                        container.visibility = View.VISIBLE
                        hashtagMentionListContainer.visibility = View.GONE



                    }
                })
            hashtagMentionList.adapter = hashtagAdapter

        }
    }

    private fun setMentionRecyclerViewAdapter() {
        binding.apply {

            manager = LinearLayoutManager(requireContext())

            hashtagMentionList.layoutManager = manager
            adapter =
                MentionListAdapter(R.layout.mention_list_recycleview,
                    object :
                        MentionListAdapter.mentionClick {
                        override fun onClick(id: String, username: String) {
                            mentionUserHash["@${username}"] = id
                            commentEt.text =
                                commentEt.text.replace(startIndex, endIndex, "@${username} ")
                            commentEt.setSelection(commentEt.text.toString().length)
                            binding.hashtagMentionListContainer.visibility = View.GONE
                            binding.container.visibility = View.VISIBLE
                        }
                    })
            hashtagMentionList.adapter = adapter
        }
    }

    private fun setRecyclerViewAdapter() {
        binding.apply {

            /*manager = LinearLayoutManager(requireContext())

            hashtagMentionList.layoutManager = manager
            adapter = MentionListAdapter(R.layout.mention_list_recycleview,
                object : MentionListAdapter.mentionClick {
                    override fun onClick(id: String, username: String) {
                        mentionUserHash["@${username}"] = id
                        commentEt.text =
                            commentEt.text.replace(startIndex, endIndex, "@${username} ")
                        commentEt.setSelection(commentEt.text.toString().length)

                    }
                })
            hashtagMentionList.adapter = adapter*/


            commentManager = LinearLayoutManager(requireContext())
            commentList.layoutManager = commentManager
            commentAdapter = CommentsAdapter(
                R.layout.comment_item_layout,
                object : CommentsAdapter.ClickListener {
                    override fun replyClick(
                        commentId: String,
                        position: Int,
                        binding: CommentItemLayoutBinding
                    ) {
                        commentEt.requestFocus()
                        replyContainer.visibility = View.VISIBLE
                        try {
                            tvReplyTo.text = "${getString(R.string.replying_to)} @${
                                commentAdapter.list.get(position).creator.username
                            }"
                        } catch (_: Exception) {
                            replyContainer.visibility = View.GONE
                        }
                        val inputMethodManager =
                            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
                        inputMethodManager!!.showSoftInput(
                            commentEt,
                            InputMethodManager.SHOW_IMPLICIT
                        )
                        commentID = commentId
                        commentPosition = position
                        if (commentBinding != null && commentBinding != binding) {
                            commentBinding!!.moreRepliesContainer.visibility = View.GONE
                        }
                        commentBinding = binding
                    }

                    override fun showMoreReply(
                        commentId: String,
                        position: Int,
                        binding: CommentItemLayoutBinding
                    ) {
                        callReplyApi(commentId)
                        commentPosition = position
                        if (commentBinding != null) {
                            commentBinding!!.moreRepliesContainer.visibility = View.GONE
                        }
                        commentBinding = binding
                    }

                    override fun likeClick(position: Int, id: String) {
                        hideKeyboard()
                        authViewModel.commentLikeRequest =
                            PostRequest(comment = id)
                        authViewModel.commentLike()
//                    commentAdapter.list[position].isLike = !commentAdapter.list[position].isLike
                    }

                    override fun hasTagClick(text: String) {
                        hideKeyboard()
                        clickListeners.hashTagClick(text)
                        dismiss()
                    }

                    override fun mentionClick(user: String) {
                        hideKeyboard()
                        clickListeners.mentionClick(user)
                        dismiss()
                    }

                    override fun onCommentReport(commentId: String, position: Int, view: TextView) {
                        hideKeyboard()
                        commentListener.commentReport(commentId, position, view)
                    }

                    override fun onDeleteComment(
                        commentId: String,
                        position: Int,
                        comment: String,
                        parentComment: String
                    ) {
                        hideKeyboard()
                        this@CommentsBottomSheet.parentComment = parentComment
                        if (parentComment.isNotEmpty()) {
                            commentAdapter.list.forEachIndexed { index, comment ->
                                Log.e(
                                    "responseDelete",
                                    comment.toString() + " >>>>>> " + parentComment
                                )
                                if (comment._id == parentComment) {
                                    parentCommentPos = index
                                }
                            }
                        }

                        deleteCommentPosition = position
                        commentDescription = comment
                        authViewModel.deleteComment(
                            DeleteCommentRequest(
                                comment = commentId,
                                commentDetails = comment
                            )
                        )
                    }


                    override fun onPin(commentId: String, position: Int, comment: String) {
                        hideKeyboard()
                        authViewModel.pinComment(PinCommentRequest(comment = commentId))
                    }

                    override fun hideKeyboards() {
                        hideKeyboard()
                    }
                },
                swipeLayout,
                postCreator
            )
            commentList.adapter = commentAdapter
        }
    }

    private fun paginationScroll() {
        binding.apply {
            mainLayout.setOnClickListener {
                hideKeyboard()
            }

            nestedSV.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
                // on scroll change we are checking when users scroll as bottom.
                hideKeyboard()
                if (scrollY == v.getChildAt(0).measuredHeight - v.measuredHeight) {
                    // in this method we are incrementing page number,
                    // making progress bar visible and calling get data method.
                    if (!isLoading) {
                        isLoading = true
                        offset += 10
                        loadingPB.visibility = View.VISIBLE
                        callNextPage()

                    }

                }
            })
        }

//        binding.commentList.addOnScrollListener(scrollListener)
    }

    private fun callReplyApi(comment: String) {
        authViewModel.commentRequest = PostRequest(
            search = "",
            offset = 0,
            limit = 100,
            post = postId,
            topComment = null,
            comment = comment
        )
        authViewModel.commentList()
    }

    private fun callApi() {
        if (Utils.isInternetAvailables(requireContext())) {
            authViewModel.commentListData.postValue(null)
            commentAdapter!!.notifyDataSetChanged()
            offset = 0
            limit = 10
            authViewModel.commentRequest =
                PostRequest(
                    search = "",
                    offset = offset,
                    limit = limit,
                    post = postId,
                    topComment = null
                )
            authViewModel.commentList()
        } else {
            authViewModel.commentListData.postValue(null)
            commentAdapter!!.notifyDataSetChanged()
            Utils.showToast(getString(R.string.no_internet_message))
        }

    }

    private fun callNextPage() {
        authViewModel.commentRequest =
            PostRequest(
                search = "",
                offset = offset,
                limit = limit,
                post = postId,
                topComment = null
            )
        authViewModel.commentList()
    }

    private fun mentionObserver() {
        authViewModel.hashTagListResponse.observe(viewLifecycleOwner) { response ->
            if (response != null) {
                if (response.success == true) {
                    //   dialogs?.dismiss()

                    if (response.data != null) {
                        Log.d("myHashTag", response.data.toString())
                        hashTagList = response.data
                        hashtagAdapter.setNewItems(hashTagList)
                    }
                }
            }

        }
        authViewModel.mentionUserData.observe(viewLifecycleOwner) { response ->
            if (response != null) {
                if (response.success) {
//                    dialogs?.dismiss()
                    if (response.data != null) {
                        mentionLists = response.data!!
                        adapter.setNewItems(mentionLists)
                    }
                    Log.e("Mentionuser", mentionLists.toString())
                    //     findNavController().navigateUp()
                }
            }
        }

        authViewModel.addCommentData.observe(viewLifecycleOwner) { response ->
            mentionUserIdList.clear()
            if (response != null) {
                if (response.success) {

                    if (commentID != null) {
                        callReplyApi(commentID!!)
                        commentID = null
                        binding.replyContainer.visibility = View.GONE
                        hideKeyboard()

                    } else {
                        myCoroutineScope.cancel()
                        if (!myCoroutineScope.isActive){
                            myCoroutineScope = CoroutineScope(Dispatchers.Main)
                        }
                        myCoroutineScope.launch {
                            callApi()
                        }
                    }
                }
            }
        }
        authViewModel.commentListData.observe(viewLifecycleOwner) { response ->
            if (response != null) {
                if (response.success) {
                    if (response.data != null) {
                        totalComment = response.data?.totalComments.toString()
                        when (response.data?.totalComments.toString()) {
                            "0", "", "null", null -> {
                                binding.label.text = "No Comments"
                                commentListener.commentAdded("")
                            }

                            "1" -> {
                                binding.label.text = "1 Comment"
                                commentListener.commentAdded("1")
                            }

                            else -> {
                                binding.label.text =
                                    "${response.data?.totalComments.toString()} Comments"
                                commentListener.commentAdded(response.data?.totalComments.toString())
                            }
                        }

                        if (response.data?.comments?.isNotEmpty() == true) {

                            if (response.data!!.comments[0].parentComment.isNullOrEmpty()) {
                                if (commentAdapter.list.size > 0) {
                                    commentAdapter.addNewItems(response.data!!.comments)
                                } else {
                                    commentAdapter.setNewItems(response.data!!.comments)
                                }
                            } else {
                                val comment = commentAdapter.list[commentPosition]
                                comment.subComments = response.data!!.comments
                                val predicate: (Comment) -> Boolean =
                                    { str -> str._id == comment._id }
                                commentAdapter.updateItem(comment, predicate)
                                commentBinding?.apply {
                                    (if (comment.subComments?.size == 0) {
                                        "No Reply"
                                    } else {
                                        if (comment.subComments?.size == 1) {
                                            "${comment.subComments?.size} Reply"
                                        } else {
                                            "${comment.subComments?.size} Replies"
                                        }
                                    }).also { moreRepliesTxt.text = it }
                                    val commentsAdapter = moreRepliesRecyclerView.adapter
                                    if (commentsAdapter is CommentsAdapter) {
                                        val manager = LinearLayoutManager(requireContext())
                                        moreRepliesRecyclerView.layoutManager = manager
                                        moreRepliesRecyclerView.adapter = commentsAdapter
                                        commentsAdapter.setNewItems(response.data!!.comments)
                                        moreRepliesContainer.visibility = View.VISIBLE


                                    }
                                }
                            }
                        }
                    }
                }
                isLoading = false
            } else {
                isLoading = false
                commentAdapter.clearData()
            }
        }
        authViewModel.showMessage.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty() && it.contains("body.").not()) {
                mentionUserIdList.clear()
                binding.loadingPB.visibility = View.GONE
                Utils.showToast(it)
            }
        }
        authViewModel.showLoading.observe(viewLifecycleOwner) {
            if (!it) {
                binding.loadingPB.visibility = View.GONE
            } else {
                binding.loadingPB.visibility = View.VISIBLE
            }
        }
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

    override fun onStart() {
        super.onStart()
        val bottomSheet =
            dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        val behavior = BottomSheetBehavior.from(bottomSheet!!)
//       /* dialog?.window?.setLayout(
//            ViewGroup.LayoutParams.MATCH_PARENT,
//            resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._450sdp)
//        )*/
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)

//        behavior.peekHeight = WindowManager.LayoutParams.MATCH_PARENT
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    inner class NoLeadingSpaceInputFilter : InputFilter {
        override fun filter(
            source: CharSequence?,
            start: Int,
            end: Int,
            dest: Spanned?,
            dstart: Int,
            dend: Int
        ): CharSequence? {
            if (start == 0 && end > 0 && source?.toString()?.startsWith(" ") == true) {
                // Reject leading spaces
                return ""
            }
            return null // Accept the input as is
        }
    }

    interface ClickListeners {
        fun deleteClick(commentsDeleted: Int)
        fun commentAdded(totalComments: String)
        fun commentReport(commentId: String, position: Int, view: TextView)
    }


    private val globalLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
        val r = Rect()
        binding.root?.getWindowVisibleDisplayFrame(r)
        val screenHeight = binding.root?.height ?: 0
        val keypadHeight = screenHeight - r.bottom
        if (keypadHeight > screenHeight * 0.15) {
            // Keyboard is open
            if (!isKeyboardOpen) {
                isKeyboardOpen = true
                Log.e("iseyboard", "OPEN")

                // Add your logic here for keyboard open
            }
        } else {
            // Keyboard is closed
            Log.e("iseyboard", "cLOSE")
            if (isKeyboardOpen) {
                isKeyboardOpen = false
                Log.e("iseyboard", "cLOSE")
                // Add your logic here for keyboard close
            }
        }
    }
}