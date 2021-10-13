package com.example.demo.firenotes.utils

import android.app.Activity
import android.app.ProgressDialog
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar

object Extension {

    fun Activity.snackbar(msg: String){
        Snackbar.make(findViewById(android.R.id.content),msg,Snackbar.LENGTH_SHORT).show()
    }
}