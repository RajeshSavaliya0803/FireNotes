package com.example.demo.firenotes.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.demo.firenotes.model.Resource
import com.example.demo.firenotes.repository.FirebaseAuthRepository
import com.google.firebase.auth.FirebaseUser

class AuthViewModel : ViewModel() {

    private var authRepo : FirebaseAuthRepository ?= null
    var authUser : MutableLiveData<FirebaseUser> ? = null


    init {
        authRepo = FirebaseAuthRepository()
        authUser = authRepo?.loggedInUser()
    }


    //Register user
    fun registerUser(email: String, password: String) : LiveData<Resource<FirebaseUser>>? {
        return authRepo?.registerUserWithFirebase(email,password)
    }

    fun loginUser(email: String,password: String): LiveData<Resource<FirebaseUser>>?{
        return authRepo?.loginWithFirebase(email,password)
    }

    fun getLoggedInUser(): LiveData<FirebaseUser?>{
        return authUser!!
    }

    fun logout(){
        authRepo?.logout()
    }

}