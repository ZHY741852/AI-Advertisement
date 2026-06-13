package com.aiadvertisement.core.util

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes

object ToastUtil {

    private var toast: Toast? = null

    fun show(context: Context, message: String, duration: Int = Toast.LENGTH_SHORT) {
        toast?.cancel()
        toast = Toast.makeText(context, message, duration).also { it.show() }
    }

    fun show(context: Context, @StringRes resId: Int, duration: Int = Toast.LENGTH_SHORT) {
        show(context, context.getString(resId), duration)
    }
}
