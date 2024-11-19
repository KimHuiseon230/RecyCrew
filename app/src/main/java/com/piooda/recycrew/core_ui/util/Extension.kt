package com.piooda.recycrew.core_ui.util

import android.content.res.Resources.getSystem
import android.util.Log
import android.util.TypedValue
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions


inline fun ImageView.load(
    data: Any?,
    glide: RequestManager = Glide.with(this),
    builder: RequestBuilder<*>.() -> Unit = {}
) {
    val requestBuilder = glide
        .load(data)
        .apply { builder() }

    requestBuilder.into(this)
}


fun Int.dp(): Int {
    val metrics = getSystem().displayMetrics
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), metrics)
        .toInt()
}


fun crossfade(enable: Boolean): DrawableTransitionOptions {
    return if (enable) {
        DrawableTransitionOptions.withCrossFade()
    } else {
        DrawableTransitionOptions().dontTransition()
    }
}

fun Fragment.logDebug(tag: String, messageResId: Int) {
    Log.d(tag, getString(messageResId))
}

fun Fragment.logError(tag: String, messageResId: Int, exception: Throwable) {
    Log.e(tag, getString(messageResId, exception.message), exception)
}

fun Fragment.showToastShort(messageResId: Int, vararg formatArgs: Any) {
    Toast.makeText(requireContext(), getString(messageResId, *formatArgs), Toast.LENGTH_SHORT).show()
}