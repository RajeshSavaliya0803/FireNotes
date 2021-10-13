package com.example.demo.firenotes.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.example.demo.firenotes.R
import com.example.demo.firenotes.databinding.ActivityRegisterBinding
import com.example.demo.firenotes.model.Resource
import com.example.demo.firenotes.utils.Extension.snackbar
import com.example.demo.firenotes.utils.Loading
import com.example.demo.firenotes.viewmodel.AuthViewModel

class RegisterActivity : AppCompatActivity() {

    private lateinit var registerBinding: ActivityRegisterBinding
    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerBinding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(registerBinding.root)

        title = getString(R.string.btn_register)

        initViewModel()

        initListeners()

    }

    private fun initViewModel() {
        authViewModel = ViewModelProvider(this).get(AuthViewModel::class.java)
    }

    private fun initListeners() {
        registerBinding.btnRegister.setOnClickListener {

            val email = registerBinding.etEmail.text.toString()
            val password = registerBinding.etPassword.text.toString()
            val confirmPassword = registerBinding.etConfirmPassword.text.toString()

            when {
                email.isEmpty() -> {
                    snackbar(getString(R.string.hint_email))
                }
                password.isEmpty() -> {
                    snackbar(getString(R.string.hint_password))
                }
                confirmPassword != password -> {
                    snackbar(getString(R.string.password_do_not_match))
                }
                else -> {
                    registerAccount(email, password)
                }
            }
        }


    }

    private fun registerAccount(email: String, password: String) {
        authViewModel.registerUser(email, password)?.observe(this,{ resource ->
            when(resource){
                is Resource.Success -> {
                    Loading.hideLoading()
                    val mainActivityIntent = Intent(this, MainActivity::class.java)
                    mainActivityIntent.putExtra("uid", resource.data!!.uid)
                    mainActivityIntent.flags = (Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(mainActivityIntent)
                    finish()
                }
                is Resource.Loading -> {
                    Loading.displayLoadingWithText(this,cancelable = false)
                }
                is Resource.Error -> {
                    Loading.hideLoading()
                    snackbar("${resource.message}")
                }
            }
        })
    }
}