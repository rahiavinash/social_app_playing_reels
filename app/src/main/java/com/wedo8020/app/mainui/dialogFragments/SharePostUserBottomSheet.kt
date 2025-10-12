package com.wedo8020.app.mainui.dialogFragments



import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.InputFilter
import android.text.Spanned
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wedo8020.app.MainApplication
import com.wedo8020.app.R
import com.wedo8020.app.clickListeners
import com.wedo8020.app.databinding.SharePostUserBottomsheetLayoutBinding
import com.wedo8020.app.mainui.adapter.SharePostUserAdapter
import com.wedo8020.app.mainui.home.postFragment.PostViewModel
import com.wedo8020.app.request.PostRequest
import com.wedo8020.app.utils.Prefs
import com.wedo8020.app.utils.RecyclerViewScrollListener
import com.wedo8020.app.utils.Utils
import com.wedo8020.zelpyy.SocketProvider.SocketIOImpl
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.wedo8020.app.utils.Utils.disableMultiTap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject


class SharePostUserBottomSheet(val postID: String,
                               private val clickListener: ClickSharePost) : BottomSheetDialogFragment(), clickListeners {
    lateinit var binding: SharePostUserBottomsheetLayoutBinding
    lateinit var postUserAdapter: SharePostUserAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private var offset = 0
    private var limit = 100
    private var isLoading = false
    private lateinit var scrollListener: RecyclerViewScrollListener
    private var searchText = ""
    lateinit var socket: SocketIOImpl
    private val mViewModel by viewModels<PostViewModel>()
    private var isAllDone = false
    private var totalShareCount = 0
    private val textWatcherHandler = Handler(Looper.myLooper()!!)

    private val selectedIdList = ArrayList<String>()
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(),R.style.MyTransparentBottomSheetDialogTheme)


        dialog.setOnShowListener { dialogInterface ->
            val bottomSheetDialog = dialogInterface as BottomSheetDialog
            val bottomSheet =
                bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            val behaviour = BottomSheetBehavior.from(bottomSheet!!)
            behaviour.state = BottomSheetBehavior.STATE_EXPANDED
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
            binding = SharePostUserBottomsheetLayoutBinding.inflate(inflater, container, false)
        }

        setUpUI()
        return binding.root
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
    private fun setUpUI() {
        socket = MainApplication.get().socket
        binding.apply {
            postUserAdapter = SharePostUserAdapter(
                R.layout.share_post_user_list_item,
                this@SharePostUserBottomSheet
            )
            layoutManager = LinearLayoutManager(requireContext())
            tagUserRecyclerView.layoutManager = layoutManager
            tagUserRecyclerView.adapter = postUserAdapter
            mainLayout1.setOnClickListener {
                hideKeyboard()
            }
            view.setOnClickListener {
                hideKeyboard()
                dismiss()
            }
            tagUserRecyclerView.setOnTouchListener { view, motionEvent ->
                hideKeyboard()
                false
            }
            sendBtn.setOnClickListener {
                hideKeyboard()  
                if (Utils.isInternetAvailables(requireContext())){
                    if(!selectedIdList.isEmpty()){

                    }
                    selectedIdList.forEachIndexed { index, data ->
                        if (socket.isConnected()) {
                            Utils.printLog(tag = "postUserAdapter_id", msg = data)
                            emitMessage(data)
                            if (selectedIdList.size == index+1){
                                isAllDone = true
                            }
                        }else{
                            isAllDone
                        }
                    }
                /*    postUserAdapter.list.filter { it.isChecked }.forEachIndexed { index, data ->

                    }*/


                }else{
                    Utils.showToast(getString(R.string.no_internet_message))
                }

            }
            shareOther.setOnClickListener {
                it.disableMultiTap()
                hideKeyboard()
                clickListener.sharePostClick()
            }
        }
        setObserver()


        callApi()
        searchUser()
        paginationScroll()
    }

    private fun searchUser() {
        binding.searchEt.also {
            //it.filters = arrayOf(NoSpaceFilter())
            val filter = InputFilter { source, _, _, dest, _, _ ->
                if (source != null && source.contains(" ")) {
                    ""
                } else null
            }
            it.filters = arrayOf(filter)
            it.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    textWatcherHandler.removeCallbacksAndMessages(null)
                    if (p0 == " ") {
                        it.setText(it.text.toString().replace(" ", ""))
                        it.setSelection(it.text.toString().length)

                    }
                    searchText = it.text.toString()
                    textWatcherHandler.postDelayed({
                        callApi()
                    }, 1000)
                }

                override fun afterTextChanged(p0: Editable?) {

                }
            })
        }
    }

    private fun paginationScroll() {
        binding.apply {
            nestedSV.setOnTouchListener { view, motionEvent ->
                hideKeyboard()
                false
            }
            nestedSV.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
                // on scroll change we are checking when users scroll as bottom.
//                hideKeyboard()
                if (scrollY == v.getChildAt(0).measuredHeight - v.measuredHeight) {
                    // in this method we are incrementing page number,
                    // making progress bar visible and calling get data method.
                    if (!isLoading) {
                        isLoading = true
                        offset += 100
                        loadingPB.setVisibility(View.VISIBLE)
                        callNextPage()

                    }

                }
            })
        }
       /* scrollListener = object : RecyclerViewScrollListener() {

            override fun onItemIsFirstVisibleItem(index: Int, lastItemIndex: Int) {
                val visibleItemCount: Int = layoutManager.childCount
                val totalItemCount: Int = layoutManager.itemCount
                if (visibleItemCount + index >= totalItemCount
                    && index >= 0
                ) {
                    if (!isLoading) {
                        isLoading = true
                        offset += 10

                    }
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {


            }
        }
        binding.tagUserRecyclerView.addOnScrollListener(scrollListener)*/
    }

    private fun callApi() {
        if (!Utils.isInternetAvailables(requireContext())){
            binding.noDataFound.visibility = View.VISIBLE
            Utils.showToast(getString(R.string.no_internet_message))
            return
        }
        mViewModel.postShareUserData.postValue(null)
        postUserAdapter?.notifyDataSetChanged()
        offset = 0
        limit = 100
        mViewModel.postShareUserRequest =
            PostRequest(search = searchText, offset = offset, limit = limit)
        mViewModel.postShareUser()
    }

    private fun setObserver() {
        mViewModel.postShareUserData.observe(viewLifecycleOwner) { response ->
            binding.loadingPB.visibility = View.GONE
            if (response != null) {
                if (response.success) {
                    response.data.let {
                        if (postUserAdapter.list.size > 0) {
                            it?.filterNotNull()?.forEach {item ->
                                item.isChecked = selectedIdList.contains(item._id)
                                if (!postUserAdapter.list.any { it._id == item._id }){
                                    postUserAdapter.addNewItem(item)
                                }
                            }
                        } else {
                            it?.filterNotNull()?.forEach {
                                it.isChecked = selectedIdList.contains(it._id)
                            }
                            postUserAdapter.setNewItems(it!!.filterNotNull())
                        }

                    }
                    if (postUserAdapter.list.isEmpty()){
                        binding.noDataFound.visibility = View.VISIBLE
                    }else{
                        binding.noDataFound.visibility = View.GONE
                    }
                }
                isLoading = false
            } else {
                postUserAdapter.clearData()
                isLoading = false
            }
        }
        mViewModel.postShareUserLoading.observe(requireActivity()) {
            if (!it) {
                binding.loadingPB.visibility = View.GONE
            } else{
                binding.loadingPB.visibility = View.VISIBLE
            }
        }


    }

    private fun callNextPage() {
        if (!Utils.isInternetAvailables(requireContext())){
            binding.loadingPB.visibility = View.GONE
            return
        }
        mViewModel.postShareUserRequest =
            PostRequest(search = searchText, offset = offset, limit = limit)
        mViewModel.postShareUser()
    }

    override fun itemClicked(position: Int) {
        hideKeyboard()
        postUserAdapter.list[position].isChecked = !postUserAdapter.list[position].isChecked
        if (postUserAdapter.list[position].isChecked){
            if (selectedIdList.contains(postUserAdapter.list[position]._id).not()){
                selectedIdList.add(postUserAdapter.list[position]._id)
            }
        }else{
            selectedIdList.remove(postUserAdapter.list[position]._id)
        }
        if (selectedIdList.isEmpty()){
            binding.sendBtn.also {
                it.isEnabled = false
                it.backgroundTintList =
                    ColorStateList.valueOf(MainApplication.get().applicationContext.getColor(R.color.pureWhite))
                it.setTextColor(MainApplication.get().getColor(R.color.Grey))
            }
        }else{
            binding.sendBtn.also {
                it.isEnabled = true
                it.backgroundTintList =
                    ColorStateList.valueOf(MainApplication.get().applicationContext.getColor(R.color.appBlue))
                it.setTextColor(MainApplication.get().getColor(R.color.pureWhite))
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!socket.isConnected()) {
            socket.connect()
        } else {
            setSocketListenON()
        }
    }

    private fun emitMessage(userID: String) {
        val obj = Utils.toJsonObject(
            "token" to "Bearer ${Prefs.init().accessToken}",
            "user" to userID,
            "type" to "sharePost",
            "post" to postID
        )
        socket.emitMessage("newMessage", obj) { _, _ ->

        }
    }

    private fun setSocketListenON() {
        socket.listenOff("newMessageSuccess")
        socket.listenOff("error")
        socket.listenOn("error") { args ->
            CoroutineScope(Dispatchers.Main).launch {
                val obj = JSONObject(args[0].toString())
                Log.e("asdga",obj.toString())
                if (isAllDone){
                    dismiss()
                    clickListener.shareUpdate(totalShareCount)
                    Utils.showToast("Sent")
                }

            }

        }
        socket.listenOn("newMessageSuccess") { args ->
            totalShareCount += 1
            CoroutineScope(Dispatchers.Main).launch {
                if (isAllDone){
                    dismiss()
                    clickListener.shareUpdate(totalShareCount)
                    Utils.showToast("Sent")
                }

            }
        }
    }
    private fun hideKeyboard() {
        try {
            if (context != null) {
                val imm =
                    requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.searchEt.windowToken, 0)
            }
        } catch (_: Exception) {
        }

    }
    interface ClickSharePost{
        fun sharePostClick()
        fun shareUpdate(totalShare : Int)
    }
    inner class NoSpaceFilter : InputFilter {

        override fun filter(
            source: CharSequence?,
            start: Int,
            end: Int,
            dest: Spanned?,
            dstart: Int,
            dend: Int
        ): CharSequence? {
            return source?.filterNot { it.isWhitespace() }
        }
    }
}