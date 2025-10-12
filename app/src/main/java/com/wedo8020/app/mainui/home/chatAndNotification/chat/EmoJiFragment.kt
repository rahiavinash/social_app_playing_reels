package com.wedo8020.app.mainui.home.chatAndNotification.chat

import android.os.Build
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.EmojiPopup
import com.wedo8020.app.databinding.FragmentEmoJiBinding
import com.wedo8020.app.mainui.base.BaseBottomSheetFragment
import com.wedo8020.app.mainui.home.chatAndNotification.chatViewModel.EmojiReactionDbViewModelFactory
import com.wedo8020.app.mainui.home.chatAndNotification.chatViewModel.ReactionViewModel
import com.wedo8020.app.mainui.home.chatAndNotification.roomDBReaction.EmoJiReactionDbCreation
import com.wedo8020.app.mainui.home.chatAndNotification.roomDBReaction.EmoJiReactionRepo
import com.wedo8020.app.mainui.home.chatAndNotification.roomDBReaction.ReactionEntity
import com.wedo8020.app.utils.NewEmoJi
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class EmoJiFragment(override val layoutId: Int) : BaseBottomSheetFragment<FragmentEmoJiBinding>() {
    private lateinit var viewModel: ReactionViewModel
    private lateinit var repository: EmoJiReactionRepo
    private lateinit var database: EmoJiReactionDbCreation
    private lateinit var factory: EmojiReactionDbViewModelFactory
    var reactionListCount = 0
    override fun onSetUpUi(binding: FragmentEmoJiBinding) {
        database = EmoJiReactionDbCreation(requireContext())
        repository = EmoJiReactionRepo(database)
        factory = EmojiReactionDbViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[ReactionViewModel::class.java]
        val messageIds = arguments?.getString("messageIds")


        val emojiList = EmojiManager.release()
        Log.e("emojiList", emojiList.toString())

        val emoJi =
            EmojiPopup.Builder.fromRootView(binding.rootView).setOnEmojiPopupDismissListener {
            }.build(binding.editText)
        Handler(Looper.getMainLooper()).postDelayed({
            emoJi.toggle()
        }, 100)

        viewModel.getAllEmoJiList()
        viewModel.allEmojiReaction.observe(viewLifecycleOwner, Observer { list ->
            if (list.isNotEmpty()) {
                list.sortByDescending { it.loginTimestamp }
            }
            reactionListCount = list.size
        })

        binding.editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            @RequiresApi(Build.VERSION_CODES.O)
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (p0?.length!! > 0) {
                    org.greenrobot.eventbus.EventBus.getDefault()
                        .post(NewEmoJi(binding.editText.text.toString(), messageIds.toString()))

                    if (reactionListCount < 6) {
                        viewModel.addEmoJi(
                            ReactionEntity(
                                reaction = binding.editText.text.toString(),
                                loginTimestamp = getCurrentDateTime()
                            )
                        )
                    } else if (reactionListCount == 6) {
                        viewModel.updateSecondRow(
                            ReactionEntity(
                                reaction = binding.editText.text.toString(),
                                loginTimestamp = getCurrentDateTime()
                            )
                        )
                    }

                    Handler(Looper.getMainLooper()).postDelayed({
                        dismiss()
                    }, 100)
                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })

    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getCurrentDateTime(): String {
        val currentTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        return currentTime.format(formatter)
    }

}