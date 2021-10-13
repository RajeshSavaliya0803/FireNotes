package com.example.demo.firenotes.utils

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Window
import com.example.demo.firenotes.R

object Loading {
    private var dialog: Dialog? = null
    fun displayLoadingWithText(
        context: Context?,
        cancelable: Boolean
    ) {
        dialog = Dialog(context!!)
        dialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog!!.setContentView(R.layout.loading_view)
        dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog!!.setCancelable(cancelable)
        try {
            dialog!!.show()
        } catch (e: Exception) {
        }
    }

    fun hideLoading() {
        try {
            if (dialog != null) {
                if (dialog!!.isShowing)
                    dialog!!.dismiss()
            }
        } catch (e: Exception) {
        }
    }
}