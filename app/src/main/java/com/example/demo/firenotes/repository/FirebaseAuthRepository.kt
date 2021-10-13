package com.example.demo.firenotes.repository

import androidx.lifecycle.MutableLiveData
import com.example.demo.firenotes.model.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class FirebaseAuthRepository {

    private var auth: FirebaseAuth? = null
    private var authenticatedLiveData: MutableLiveData<FirebaseUser>? = null
    private var loginResponse: MutableLiveData<Resource<FirebaseUser>>?= null
    private var registerResponse : MutableLiveData<Resource<FirebaseUser>>? = null


    init {
        auth = FirebaseAuth.getInstance()
        authenticatedLiveData = MutableLiveData()

    }

    fun registerUserWithFirebase(email: String, password: String): MutableLiveData<Resource<FirebaseUser>>?{
        registerResponse = MutableLiveData()
        registerResponse?.postValue(Resource.Loading())
        auth?.createUserWithEmailAndPassword(email, password)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                registerResponse?.postValue(Resource.Success(data = auth?.currentUser!!))
            } else {
                registerResponse?.postValue(Resource.Error(message = task.exception?.message))
            }
        }
        return registerResponse
    }

    fun loginWithFirebase(email: String, password: String): MutableLiveData<Resource<FirebaseUser>>? {
        loginResponse = MutableLiveData()
        //initialize with Loading
        loginResponse?.postValue(Resource.Loading())
        auth?.signInWithEmailAndPassword(email, password)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                //Success
                loginResponse?.postValue(Resource.Success(data = auth?.currentUser!!))
            } else {
                loginResponse?.postValue(Resource.Error(message = task.exception?.message))
            }
        }

        return loginResponse
    }

    fun loggedInUser() : MutableLiveData<FirebaseUser>{
        val currentUser = auth?.currentUser
        authenticatedLiveData?.postValue(currentUser)
        return  authenticatedLiveData!!
    }

    fun logout() {
        auth?.signOut()
        authenticatedLiveData?.postValue(auth?.currentUser)
    }

}