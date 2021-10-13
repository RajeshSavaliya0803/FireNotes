package com.example.demo.firenotes.model

import java.util.*


data class NotesModel(
    val noteId: String,
    val note : String,
    val content: String,
    val noteImage: String?,
    val date: Date)

enum class  AddNotesResponse {
    SUCCESS,
    ERROR
}