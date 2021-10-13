package com.example.demo.firenotes.repository

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import com.example.demo.firenotes.model.AddNotesResponse
import com.example.demo.firenotes.model.NotesModel
import com.example.demo.firenotes.model.Resource
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class NotesRepository {

    private var db: FirebaseFirestore ?= null
    private var storage : FirebaseStorage ?= null
    private var addNotesResponse : MutableLiveData<Resource<AddNotesResponse>> ?= null
    private var getNotesResponse : MutableLiveData<Resource<List<NotesModel>>> ?= null


    init {
        db = Firebase.firestore
        storage = Firebase.storage
        getNotesResponse = MutableLiveData()
    }

    fun getNotes(id: String) : MutableLiveData<Resource<List<NotesModel>>>? {
        getNotesResponse?.postValue(Resource.Loading())
        val listOfModel = ArrayList<NotesModel>()
        try {
            db?.collection("notes")?.document(id)?.collection("myNotes")?.orderBy("date",Query.Direction.DESCENDING)?.addSnapshotListener { value, error ->
                val listOfDocs = value!!.documents
                if(listOfModel.size>0){
                    listOfModel.clear()
                }
                for (i in 0 until value.documents.size){
                    val timestamp: Timestamp = listOfDocs[i].data?.get("date") as Timestamp
                    val date = timestamp.toDate()
                    val notesModel = NotesModel(listOfDocs[i].id,listOfDocs[i].data?.get("notes").toString(),listOfDocs[i].data?.get("content").toString(),listOfDocs[i].data?.get("imageUri").toString(),
                        date
                    )
                    listOfModel.add(notesModel)
                }
                getNotesResponse?.postValue(Resource.Success(data = listOfModel))
            }
        }
        catch (e: Exception){
            getNotesResponse?.postValue(Resource.Error(message = e.localizedMessage, data = emptyList()))
        }
        return  getNotesResponse
    }

    fun addNotes(id: String,note: String, content: String, timestamp: Timestamp,filePath: Uri?) : MutableLiveData<Resource<AddNotesResponse>>?{

        addNotesResponse = MutableLiveData()

        addNotesResponse?.postValue(Resource.Loading())

        val storageReference = storage?.reference

        val imagesRef: StorageReference? = storageReference?.child("images" + UUID.randomUUID().toString())

        if(filePath != null){
            val uploadTask = imagesRef?.putFile(filePath)

            uploadTask?.continueWithTask { task ->
                if(!task.isSuccessful){
                    task.exception?.let {
                        throw it
                    }
                }
                imagesRef.downloadUrl
            }?.addOnCompleteListener { task->
                if(task.isSuccessful){
                    val downloadUri = task.result
                    addNoteToFirebase(id,note,content,timestamp,downloadUri.toString(),addNotesResponse)
                }
            }
        }
        else{
            addNoteToFirebase(id,note,content,timestamp,"",addNotesResponse)
        }

        return addNotesResponse
    }

    private fun addNoteToFirebase(
        id: String,
        note: String,
        content: String,
        timestamp: Timestamp,
        filePath: String,
        mAddNoteStatus: MutableLiveData<Resource<AddNotesResponse>>?
    ){
        val notesMap = HashMap<String,Any>()
        notesMap["notes"] = note
        notesMap["content"] = content
        notesMap["date"] = timestamp
        notesMap["imageUri"] = filePath

        val docRef = db?.collection("notes")?.document(id)?.collection("myNotes")?.document()
        docRef?.set(notesMap)?.addOnCompleteListener {
            if (it.isSuccessful) {
                addNotesResponse?.postValue(Resource.Success(data = AddNotesResponse.SUCCESS))
            } else {
                mAddNoteStatus?.postValue(Resource.Error(message = it.exception?.message,data = AddNotesResponse.ERROR))
            }
        }
    }

    fun deleteNote(userId: String, noteId: String){
        db?.collection("notes")?.document(userId)?.collection("myNotes")?.document(noteId)?.delete()?.addOnCompleteListener { task ->
            if(task.isSuccessful){

            }
        }

    }
}