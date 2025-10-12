package com.wedo8020.app.utils

import android.graphics.Typeface
import android.text.TextPaint
import android.text.style.MetricAffectingSpan

class CustomFont(private val typeface: Typeface) : MetricAffectingSpan() {

    override fun updateMeasureState(p: TextPaint) {
        p.typeface = typeface
    }

    override fun updateDrawState(tp: TextPaint) {
        tp.typeface = typeface
    }
}