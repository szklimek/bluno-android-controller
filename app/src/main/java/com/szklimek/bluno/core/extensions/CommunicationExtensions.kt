package com.szklimek.bluno.core.extensions

import android.content.Context
import android.view.View
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.szklimek.bluno.core.Log

fun Context.showToastMessage(message: String, lengthInMs: Int = Toast.LENGTH_SHORT) {
    Log.d("Toast message: $message")
    Toast.makeText(this, message, lengthInMs).show()
}

data class SnackBarAction(val label: String, val runnable: Runnable)

fun View.showSnackBarMessage(
    message: String,
    snackBarAction: SnackBarAction? = null,
    lengthInMs: Int = Snackbar.LENGTH_SHORT
) {
    Log.d("SnackBar message: $message")
    Snackbar.make(this, message, lengthInMs).apply {
        if (snackBarAction != null) {
            setAction(snackBarAction.label) {
                Log.d("SnackBarAction triggered: ${snackBarAction.label}")
                snackBarAction.runnable.run()
            }
        }
    }.show()
}
