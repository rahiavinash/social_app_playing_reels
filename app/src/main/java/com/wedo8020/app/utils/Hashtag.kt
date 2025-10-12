package com.wedo8020.app.utils

import android.graphics.Color
import android.text.Spanned
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView


class Hashtag(private val clickListener : onHashtagClick,private val hashtagId: String) : ClickableSpan() {
    private var textPaint: TextPaint? = null


    override fun updateDrawState(ds: TextPaint) {
        textPaint = ds
        ds.color =  Color.parseColor("#2E94D2")
//        ds.setARGB(255, 30, 144, 255)
    }

    override fun onClick(widget: View) {
        val tv = widget as TextView
        val s = tv.text as Spanned
        val start = s.getSpanStart(this)
        val end = s.getSpanEnd(this)
        val theWord = s.subSequence(start , end).toString()
        // you can start another activity here
        Utils.printLog(tag = "hashtag id", msg = hashtagId)
        clickListener?.hashTagClick(hashtagId)

//        clicklistner!!.onClickMentions(mentions.filter {theWord==it.username}[0].user)


//        Toast.makeText(context, String.format("Tag : %s", theWord), Toast.LENGTH_SHORT).show()
    }


    interface onHashtagClick {
        fun hashTagClick(text: String)
        fun mentionClick(text:String)
    }

}
