package com.wedo8020.app.mainui.home.exchangescreen

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.wedo8020.app.R
import com.wedo8020.app.databinding.FragmentTransferSrcBinding
import com.wedo8020.app.mainui.activities.MainActivity
import com.wedo8020.app.mainui.base.BaseFragment
import com.wedo8020.app.mainui.home.exchangescreen.adapter.UserListAdapter
import com.wedo8020.app.request.UserListRequest
import com.wedo8020.app.response.TransferSrcDetails
import com.wedo8020.app.response.User
import com.wedo8020.app.utils.RecyclerViewScrollListener
import com.wedo8020.app.utils.Utils
import java.util.Objects


class TransferSrcFragment : BaseFragment() {
    private lateinit var mBinding: FragmentTransferSrcBinding
    lateinit var adapter: UserListAdapter
    private lateinit var manager: RecyclerView.LayoutManager
    private lateinit var viewModel: PurchasesViewModel
    private var userListData: ArrayList<User> = arrayListOf()
    private var src: String? = null
    private var dialog: Dialog? = null
    var nameValue: String? = null
    var srcValue: String? = null
    private var isEditTextFocused = false
    var userNameValue: String? = null
    var profileUrl: String? = null
    var user: String? = null
    private var isLoading = false
    private var offset = 0
    private var search = ""
    private var isClicked = true


    var amountValue: Double? = null
    //var calculatedValue : Double?=null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        mBinding =
            DataBindingUtil.inflate(
                layoutInflater,
                R.layout.fragment_transfer_src,
                container,
                false
            )
        hideNavBottom()
        dialog = Dialog(requireActivity())
        dialog?.requestWindowFeature(Window.FEATURE_CONTENT_TRANSITIONS)
        return mBinding.root
    }

    @SuppressLint("ClickableViewAccessibility", "UseCompatLoadingForDrawables")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = requireActivity() as MainActivity



        Log.d("hello", "src value is $src")


        viewModel = ViewModelProvider(this)[PurchasesViewModel::class.java]



        mBinding.amountAr.isEnabled = false
        mBinding.publicKey.isEnabled = false
        mBinding.transferNowYourselfButton.isEnabled = false
        viewModel.impactDetails()
        mBinding.transferNowButton.isEnabled = false
        setRecyclerAdapter()
        hitApi()

        viewModel.userListResponse.observe(viewLifecycleOwner) { response ->

            Log.d("userList", "I am observer userList")
            if (response != null) {
                if (response.success == true) {
                    isClicked = true
                    isLoading = false
                    mBinding.animationPaginationLoader.visibility = View.GONE
                    if (response.data != null) {
                        Log.d("myHashTag", response.data.toString())
                        isLoading = false

                        userListData = response.data
                        adapter.addNewItems(userListData)
//                        Log.d("myHashTagPagination", response.data.size.toString())
//                        mBinding.noDataRecyclerView.visibility = View.GONE


                            if (!mBinding.userId.text.isNullOrEmpty() && adapter.list.size == 0) {
                                Log.d("myHashTagSearch", "Searched user does not exist")
                                adapter.clearData()
                                mBinding.noDataRecyclerView.visibility = View.VISIBLE
                            }
                            else if(adapter.list.size == 0) {
                                adapter.clearData()
                                mBinding.noDataRecyclerView.visibility = View.VISIBLE
                            }


                            else{
                                //adapter.clearData()
                                //adapter.addNewItems(userListData)
                                mBinding.noDataRecyclerView.visibility = View.GONE
                            }


                    } else {
                        adapter.clearData()
                        Log.d("myHashTagPagination", "user list is null")
                        mBinding.noDataRecyclerView.visibility = View.VISIBLE
                    }

                }
            }
        }





        mBinding.userId.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                Log.d("userIdFocus", "drop down has the focus")
                mBinding.userList.visibility = View.VISIBLE
            } else {
                mBinding.userList.visibility = View.GONE
            }
        }
        viewModel.mutableBaseResponse.observe(viewLifecycleOwner) { impactDetails ->

            if (impactDetails.success == true) {

                src = impactDetails.data?.WD.toString()

            }
        }
        mBinding.userId.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                mBinding.noDataRecyclerView.visibility = View.GONE
                mBinding.userId.setText("")
                mBinding.userId.clearFocus()
                mBinding.userId.hideTheSoftKeyboard()
                true // Consume the event
            } else {
                false // Return false to let the system handle other actions
            }
        }



        mBinding.userId.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                Log.d("textWatcher", "Before text changed")
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {


                if (p0?.isNotEmpty() == true) {


                    if (isClicked) {
                        Log.d("myHashTagPagination", "is clicked is true")
                        //mBinding.userList.visibility = View.GONE
                        isClicked = false
                    } else {
                        Log.d("textChanged111", "I am not clicked $isClicked")
                        Log.d("myHashTagPagination", " is clicked is false and po is not empty")
                        search = p0.toString()
                        adapter.clearData()
                        offset = 0
                        viewModel.getUserDetails(
                            UserListRequest(
                                offset = offset,
                                limit = 10,
                                search = search
                            )
                        )

                    }


                } else {

                    search = p0.toString()
                    offset = 0
                    adapter.clearData()
                    viewModel.getUserDetails(
                        UserListRequest(
                            offset = offset,
                            limit = 10,
                            search = search
                        )
                    )
                    Log.d("myHashTagPagination", " is clicked is false and po is empty")
                }


                val isUserIdEmpty = p0.isNullOrBlank()
                val amountEmpty = mBinding.amount.text.isNullOrBlank()
                if (!amountEmpty && !isUserIdEmpty) {
                    //isSelected = false
                    mBinding.transferNowButton.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.white
                        )
                    )
                    mBinding.transferNowButton.setBackgroundDrawable(requireContext().getDrawable(R.drawable.blue_button_background))
                    mBinding.transferNowButton.isEnabled = true
                } else {
                    //isSelected = false
                    mBinding.transferNowButton.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.Grey
                        )
                    )
                    mBinding.transferNowButton.setBackgroundDrawable(requireContext().getDrawable(R.drawable.white_button_background))
                    mBinding.transferNowButton.isEnabled = false
                }


            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })
        paginationScroll()

        mBinding.transferNowButton.isClickable = false

        mBinding.crossButton.setOnClickListener {
            findNavController().navigateUp()
        }


        mBinding.amount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }


            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {

                if (p0.toString().length == 1 && p0?.toString() == "0") {
                    mBinding.amount.text.clear()
                }

                amountValue = p0.toString().toDoubleOrNull() ?: 0.0


                srcValue = p0.toString()
                val isUserIdEmpty = mBinding.userId.text.isNullOrBlank()
                val isAmountEmpty = p0.isNullOrBlank()

                if (!isUserIdEmpty && !isAmountEmpty) {
                    // isSelected = false
                    mBinding.transferNowButton.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.white
                        )
                    )
                    mBinding.transferNowButton.setBackgroundDrawable(requireContext().getDrawable(R.drawable.blue_button_background))
                    mBinding.transferNowButton.isEnabled = true

                } else {
                    // isSelected = false
                    mBinding.transferNowButton.setBackgroundDrawable(requireContext().getDrawable(R.drawable.white_button_background))
                    mBinding.transferNowButton.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.Grey
                        )
                    )
                    mBinding.transferNowButton.isEnabled = false
                }


            }

        })


        mBinding.dropDownClick.setOnClickListener {
            if (mBinding.userList.visibility == View.VISIBLE) {
                mBinding.userList.visibility = View.GONE
                mBinding.noDataRecyclerView.visibility = View.GONE
                mBinding.userId.setText("")
                mBinding.userId.clearFocus()
                mBinding.userId.hideTheSoftKeyboard()

            } else {
                search = ""
                mBinding.userId.clearFocus()
                mBinding.userList.visibility = View.VISIBLE
                mBinding.noDataRecyclerView.visibility = View.GONE
                viewModel.getUserDetails(
                    UserListRequest(
                        offset = offset,
                        limit = 10,
                        search = search
                    )
                )
            }
        }

        viewModel.srcSendResponse.observe(viewLifecycleOwner, Observer { response ->
            if (response.success) {
                Log.d("successCallApi", "Api hit")
                findNavController().navigate(R.id.walletFragment)
            }
        })
        mBinding.transferNowButton.setOnClickListener {
            val calculatedValue = amountValue?.times(0.00001)
            Log.d("differenceBetween", "  $calculatedValue : ${src?.toDouble()}")

            if (calculatedValue != null) {
                if (calculatedValue > (src?.toDouble() ?: 0.0)) {

                    sorryDialog()
                } else {
                    successDialog(
                        src = srcValue.toString(),
                        name = nameValue.toString(),
                        userName = userNameValue.toString(),
                        profile = profileUrl
                    )
                }
            }

        }
    }


    private fun paginationScroll() {
        mBinding.userList.addOnScrollListener(object : RecyclerViewScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                //                hideSearchKeyboard()
                if (!recyclerView.canScrollVertically(1) && dy > 0 && !isLoading) {
                    mBinding.animationPaginationLoader.visibility = View.VISIBLE
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


    private fun hitApi() {
        if (isInternetAvailable(requireContext())) {
            // mBinding.animationPaginationLoader.visibility = View.VISIBLE
            offset = 0
            search = ""
            viewModel.getUserDetails(UserListRequest(offset = offset, limit = 10, search = search))
        } else {
            showToast(getString(R.string.no_internet_message))
        }
    }

    private fun loadMoreData() {
        if (isInternetAvailable(requireContext())) {

            viewModel.getUserDetails(UserListRequest(offset = offset, limit = 10, search = search))
        } else {
            showToast(getString(R.string.no_internet_message))
        }
    }


    private fun setRecyclerAdapter() {
        mBinding.apply {
            manager = LinearLayoutManager(requireContext())
            this.userList.layoutManager = manager
            adapter = UserListAdapter(R.layout.user_list,
                object : UserListAdapter.UserListClick {
                    override fun onClick(id: String, username: String, profile: String) {
                        Log.d("userList", "I am adapter userList clicked")
                        Log.d("itemSelected", "item is clicked : $id")
                        mBinding.userList.visibility = View.GONE
                        mBinding.userId.setText("$id")
                        mBinding.userId.setSelection(mBinding.userId.text.length)
                        mBinding.userId.clearFocus()
                        mBinding.userId.hideTheSoftKeyboard()
                        nameValue = id
                        userNameValue = username
                        profileUrl = profile
                        isClicked = true


                    }
                })
            mBinding.userList.adapter = adapter

        }
    }

    private fun successDialog(
        src: String,
        name: String,
        userName: String,
        profile: String? = null
    ) {
        dialog?.setCancelable(false)
        Objects.requireNonNull(dialog?.window)?.setBackgroundDrawable(
            ColorDrawable(
                Color.TRANSPARENT
            )
        )
        dialog?.setContentView(R.layout.success_src_dialog)

        val transfer = dialog?.findViewById(R.id.transfer_button) as TextView
        val ivClose = dialog?.findViewById(R.id.ivClose) as ImageView
        val image = dialog?.findViewById(R.id.image) as ImageView
        val nameValue = dialog?.findViewById(R.id.name) as TextView
        val userNameValue = dialog?.findViewById(R.id.user_name) as TextView
        val srcValue = dialog?.findViewById(R.id.src_amount) as TextView

        nameValue.text = name
        Glide.with(requireContext()).load(profile).error(R.drawable.profile_dummy).into(image)


        userNameValue.text = userName
        val spannableStringBuilder = SpannableStringBuilder()
        spannableStringBuilder.append("You are giving ")

        val srcStart = spannableStringBuilder.length
        spannableStringBuilder.append("$src")
        spannableStringBuilder.setSpan(
            ForegroundColorSpan(Color.WHITE),
            srcStart,
            spannableStringBuilder.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannableStringBuilder.append(" AR")
        val arStart = spannableStringBuilder.length - 2
        spannableStringBuilder.setSpan(
            ForegroundColorSpan(Color.parseColor("#2E94D2")),
            arStart,
            spannableStringBuilder.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        spannableStringBuilder.append("\n")
        spannableStringBuilder.append("to")
        srcValue.text = spannableStringBuilder

        ivClose.setOnClickListener {
            dialog?.dismiss()
        }
        transfer.setOnClickListener {
            if (isInternetAvailable(requireContext())) {

                viewModel.sendSrc(TransferSrcDetails(username = userName, ar = src.toInt()))


            } else {

                Utils.showToast("Please check your internet connection.")
            }
            dialog?.dismiss()
        }
        dialog?.show()
    }

    private fun sorryDialog() {
        dialog?.setCancelable(false)
        Objects.requireNonNull(dialog?.window)?.setBackgroundDrawable(
            ColorDrawable(
                Color.TRANSPARENT
            )
        )
        dialog?.setContentView(R.layout.sorry_src_dialog)

        val transfer = dialog?.findViewById(R.id.transfer_button) as TextView
        val ivClose = dialog?.findViewById(R.id.ivClose) as ImageView

        ivClose.setOnClickListener {
            dialog?.dismiss()
        }

        transfer.setOnClickListener {

            dialog?.dismiss()
        }




        dialog?.show()
    }


}