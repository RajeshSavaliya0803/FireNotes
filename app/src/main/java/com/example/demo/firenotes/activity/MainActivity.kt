package com.example.demo.firenotes.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.demo.firenotes.R
import com.example.demo.firenotes.adapter.NotesAdapter
import com.example.demo.firenotes.databinding.ActivityMainBinding
import com.example.demo.firenotes.model.NotesModel
import com.example.demo.firenotes.utils.SwipeGesture
import com.example.demo.firenotes.viewmodel.AuthViewModel
import com.example.demo.firenotes.viewmodel.NotesViewModel
import androidx.appcompat.app.AlertDialog
import com.example.demo.firenotes.model.Resource
import com.example.demo.firenotes.utils.Extension.snackbar


class MainActivity : AppCompatActivity(){

    private lateinit var mainBinding : ActivityMainBinding
    private lateinit var mUserId: String
    private lateinit var authViewModel: AuthViewModel
    private lateinit var notesViewModel: NotesViewModel
    private lateinit var mNotesAdapter: NotesAdapter
    private var mNotesList: ArrayList<NotesModel> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)

        mUserId = intent.getStringExtra("uid").toString()

        mainBinding.rvNotes.layoutManager = LinearLayoutManager(this,RecyclerView.VERTICAL,false)

        mNotesAdapter = NotesAdapter(mNotesList)
        mainBinding.rvNotes.adapter = mNotesAdapter

        initViewModels()

        initObservers()

        setupGestureSwipe()

    }

    private fun initViewModels() {
        authViewModel = ViewModelProvider(this).get(AuthViewModel::class.java)
        notesViewModel = ViewModelProvider(this).get(NotesViewModel::class.java)
    }



    private fun initObservers() {
        authViewModel.getLoggedInUser().observe(this,{
            if(it == null){
                startActivity(Intent(this,LoginActivity::class.java))
                finish()
            }
        })
        notesViewModel.getNotes(mUserId)?.observe(this,{ resource ->
            when(resource){
                is Resource.Loading -> {}
                is Resource.Success -> {
                    mNotesList = resource.data as ArrayList<NotesModel>
                    mainBinding.progressBar.visibility = View.GONE
                    if(mNotesList.isNotEmpty()){
                        mainBinding.noNotes.visibility = View.GONE
                        mainBinding.rvNotes.visibility = View.VISIBLE
                        mNotesAdapter.setData(mNotesList)

                    }else{
                        mainBinding.rvNotes.visibility = View.GONE
                        mainBinding.noNotes.visibility = View.VISIBLE
                    }
                }
                is Resource.Error -> snackbar("${resource.message}")
            }
        })
    }



    private fun setupGestureSwipe() {
        val swipe = object : SwipeGesture(this){

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                when(direction){
                    ItemTouchHelper.LEFT -> {
                        val builder: AlertDialog.Builder = AlertDialog.Builder(this@MainActivity)
                        builder.setTitle(getString(R.string.delete_diag_title))
                        builder.setMessage(getString(R.string.delete_diag_msg))
                        builder.setCancelable(false)
                        builder.setPositiveButton(
                            getString(R.string.ok)
                        ) { dialog, _ ->
                            notesViewModel.deleteNote(mUserId,mNotesList[viewHolder.adapterPosition].noteId)
                            mNotesAdapter.notifyItemChanged(viewHolder.adapterPosition)
                            dialog.dismiss()
                        }
                        builder.setNegativeButton(getString(R.string.cancel)
                        ) { dialog, arg1 -> dialog.dismiss()
                            mNotesAdapter.notifyItemChanged(viewHolder.adapterPosition)}
                        builder.show()
                    }
                }
            }


        }

        val touchHelper = ItemTouchHelper(swipe)
        touchHelper.attachToRecyclerView(mainBinding.rvNotes)
    }

    // Add menu items to appbar
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.app_bar_menu,menu)
        return true
    }

    //Handle menu items click on appbar
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.add_note -> {
                val createNoteIntent = Intent(this,AddNote::class.java)
                createNoteIntent.putExtra("uid",mUserId)
                startActivity(createNoteIntent)
            }
            R.id.log_out -> logOut()
            else -> super.onOptionsItemSelected(item)
        }
        return false
    }

    private fun logOut() {
        authViewModel.logout()
    }
}