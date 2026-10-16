package com.featherroad.quest.core.ext

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

fun formatGrouped(value: Int): String {
    val raw = value.toString()
    val builder = StringBuilder()
    var counter = 0
    for (index in raw.length - 1 downTo 0) {
        builder.append(raw[index])
        counter++
        if (counter % 3 == 0 && index > 0) {
            builder.append(',')
        }
    }
    return builder.reverse().toString()
}

fun View.applyBottomBarInset() {
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        try {
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(bottom = bars.bottom)
        } catch (e: Exception) {
        }
        insets
    }
    ViewCompat.requestApplyInsets(this)
}

fun View.safePost(action: () -> Unit) {
    post {
        try {
            if (!isAttachedToWindow) return@post
            action()
        } catch (e: Exception) {
        }
    }
}

fun View.safePostDelayed(delayMs: Long, action: () -> Unit) {
    postDelayed({
        try {
            if (!isAttachedToWindow) return@postDelayed
            action()
        } catch (e: Exception) {
        }
    }, delayMs)
}

fun View.showOrHide(visible: Boolean) {
    visibility = if (visible) View.VISIBLE else View.GONE
}
