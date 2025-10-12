package com.wedo8020.app.utils

import android.graphics.Color
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View


class CalloutLink(
    private val clickListener: Hashtag.onHashtagClick,
    private val mentionUserId: String
) : ClickableSpan() {
    override fun updateDrawState(ds: TextPaint) {
        ds.color = Color.parseColor("#2E94D2")
    }

    override fun onClick(widget: View) {
        /*val tv = widget as TextView
        val s = tv.text as Spanned
        val start = s.getSpanStart(this)
        val end = s.getSpanEnd(this)
        val theWord = s.subSequence(start + 1, end).toString()*/
        Log.e("mentionClickcomment",mentionUserId)
        if (mentionUserId!=""){
            clickListener.mentionClick(mentionUserId)
        }
        // you can start another activity here
    }

}