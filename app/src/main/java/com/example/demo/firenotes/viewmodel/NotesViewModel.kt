package com.example.demo.firenotes.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.demo.firenotes.model.AddNotesResponse
import com.example.demo.firenotes.model.NotesModel
import com.example.demo.firenotes.model.Resource
import com.example.demo.firenotes.repository.NotesRepository
import com.google.firebase.Timestamp

class NotesViewModel : ViewModel() {

    private var mNotesRepository : NotesRepository ?= null

    init {
        mNotesRepository = NotesRepository()
    }

    fun getNotes(id: String): LiveData<Resource<List<NotesModel>>>? {
        return mNotesRepository?.getNotes(id)
    }

    fun addNote(
        userId: String,
        noteTitle: String,
        noteContent: String,
        currentTimeStamp: Timestamp,
        filePath: Uri?
    ) : LiveData<Resource<AddNotesResponse>>? {
        return mNotesRepository?.addNotes(userId,noteTitle,noteContent,currentTimeStamp,filePath)
    }

    fun deleteNote(userId: String, noteId: String){
        mNotesRepository?.deleteNote(userId,noteId)
    }
}