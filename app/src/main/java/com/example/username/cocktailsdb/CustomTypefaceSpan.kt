package com.example.username.cocktailsdb

import android.graphics.Paint
import android.graphics.Typeface
import android.text.TextPaint
import android.text.style.MetricAffectingSpan

class CustomTypefaceSpan(private val family: String?, private val typeface: Typeface) : MetricAffectingSpan() {

    override fun updateDrawState(ds: TextPaint) {
        applyCustomTypeface(ds, typeface)
    }

    override fun updateMeasureState(p: TextPaint) {
        applyCustomTypeface(p, typeface)
    }

    private fun applyCustomTypeface(paint: Paint, tf: Typeface) {
        paint.typeface = tf
        // Ajusta otros atributos de estilo según sea necesario (por ejemplo, negrita, cursiva)
        paint.isFakeBoldText = tf.isBold
        paint.textSkewX = if (tf.isItalic) -0.25f else 0f
    }
}

