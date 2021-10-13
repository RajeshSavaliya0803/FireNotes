package com.example.demo.firenotes.activity

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import androidx.lifecycle.ViewModelProvider
import com.example.demo.firenotes.R
import com.example.demo.firenotes.databinding.ActivityAddEditNoteBinding
import com.example.demo.firenotes.model.Resource
import com.example.demo.firenotes.utils.Extension.snackbar
import com.example.demo.firenotes.utils.Loading
import com.example.demo.firenotes.viewmodel.NotesViewModel
import com.google.firebase.Timestamp
import com.markodevcic.peko.Peko
import com.markodevcic.peko.PermissionResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import kotlin.coroutines.CoroutineContext

class AddNote : AppCompatActivity(), CoroutineScope {

    private lateinit var addEditNoteBinding: ActivityAddEditNoteBinding
    private lateinit var userId: String
    private lateinit var mNotesViewModel: NotesViewModel
    private val PICK_IMAGE_REQUEST = 71
    private var filePath: Uri? = null
    private var builder: AlertDialog.Builder?= null

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addEditNoteBinding = ActivityAddEditNoteBinding.inflate(layoutInflater)
        setContentView(addEditNoteBinding.root)
        builder = AlertDialog.Builder(this)

        title = getString(R.string.add_note)

        userId = intent.getStringExtra("uid")!!

        initViewModel()

        initListener()

    }

    private fun initViewModel() {
        mNotesViewModel = ViewModelProvider(this).get(NotesViewModel::class.java)
    }

    private fun initListener() {

        addEditNoteBinding.btnSave.setOnClickListener {

            val noteTitle = addEditNoteBinding.etTitle.text.toString()
            val noteContent = addEditNoteBinding.etContent.text.toString()
            val currentTimeStamp = Timestamp.now()

            when{
                noteTitle.isEmpty() -> {
                    snackbar(getString(R.string.hint_title))
                }
                noteContent.isEmpty() -> {
                    snackbar(getString(R.string.hint_content))
                }
                else -> saveNoteToFirebase(noteTitle,noteContent,currentTimeStamp)
            }
        }

        addEditNoteBinding.addImage.setOnClickListener {
            requestReadExtPermission()
        }
    }


    private fun requestReadExtPermission() {
        launch {
            when(Peko.requestPermissionsAsync(this@AddNote, Manifest.permission.READ_EXTERNAL_STORAGE)){
                is PermissionResult.Granted -> {
                    launchGallery()
                }
                is PermissionResult.Denied.JustDenied -> {
                    snackbar(getString(R.string.permission_required))
                }
                is PermissionResult.Denied.NeedsRationale -> {
                    snackbar(getString(R.string.permission_required))
                }
                is PermissionResult.Denied.DeniedPermanently -> {
                    showSettingDialog()
                }
                PermissionResult.Cancelled -> TODO()
            }
        }
    }

    private suspend fun showSettingDialog() {
        withContext(Dispatchers.Main){
            if(builder != null){
                builder!!.setTitle(getString(R.string.setting_diag_title))
                builder!!.setMessage(getString(R.string.setting_diag_msg))
                builder!!.setCancelable(false)
                builder!!.setPositiveButton(
                    getString(R.string.settings)
                ) { dialog, _ ->
                    dialog.dismiss()
                    val intent = Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", packageName, null)
                    )
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
                builder!!.setNegativeButton(getString(R.string.cancel)
                ) { dialog, _ -> dialog.dismiss()
                }
                builder!!.show()
            }
        }

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            if(data == null || data.data == null){
                return
            }
            filePath = data.data
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, filePath)
                addEditNoteBinding.imageView.setImageBitmap(bitmap)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun saveNoteToFirebase(
        noteTitle: String,
        noteContent: String,
        currentTimeStamp: Timestamp
    ) {
        mNotesViewModel.addNote(userId,noteTitle,noteContent,currentTimeStamp,filePath)?.observe(this,{ resource ->
            when(resource){
                is Resource.Loading -> {
                    Loading.displayLoadingWithText(this,false)
                }
                is Resource.Success -> {
                    Loading.hideLoading()
                    onBackPressed()
                }
                is Resource.Error -> {
                    snackbar("${resource.message}")
                }
            }
        })
    }

    private fun launchGallery(){
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
    }
}