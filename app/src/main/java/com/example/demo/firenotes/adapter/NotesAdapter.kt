package com.example.demo.firenotes.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.demo.firenotes.databinding.ItemNoteBinding
import com.example.demo.firenotes.model.NotesModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class NotesAdapter(private val mNotesList: ArrayList<NotesModel>) : RecyclerView.Adapter<NotesAdapter.MyViewHolder>() {

    class MyViewHolder(private val itemNoteBinding: ItemNoteBinding) : RecyclerView.ViewHolder(itemNoteBinding.root) {
        fun bind(model: NotesModel) {
            if(model.noteImage!!.isNotEmpty()){
                itemNoteBinding.notesImage.load(model.noteImage){
                    listener(
                        onSuccess = { _, _ -> itemNoteBinding.imageProgress.visibility = View.GONE},
                        onError = { _, t ->  }
                    )
                }
            }else{
                itemNoteBinding.notesImage.visibility =View.GONE
                itemNoteBinding.imageProgress.visibility = View.GONE
            }

            val sfd = SimpleDateFormat("dd-MM-yyyy HH:mm",Locale.getDefault())
            val date = sfd.format(model.date)
            itemNoteBinding.notesTitle.text = model.note
            itemNoteBinding.notesContent.text = model.content
            itemNoteBinding.notesDate.text = date
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(ItemNoteBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(mNotesList[position])
    }

    fun setData(newList: ArrayList<NotesModel>){
        val diffCallback = DiffCallback(mNotesList,newList)
        val diff = DiffUtil.calculateDiff(diffCallback)
        mNotesList.clear()
        mNotesList.addAll(newList)
        diff.dispatchUpdatesTo(this)
    }

    override fun getItemCount(): Int {
        return mNotesList.size
    }
}

private class DiffCallback(val oldItem: List<NotesModel>,val newItem: List<NotesModel>) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldItem.size

    override fun getNewListSize(): Int = newItem.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldItem[oldItemPosition].note === newItem[newItemPosition].note
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val (_, value, name) = oldItem[oldItemPosition]
        val (_, value1, name1) = newItem[newItemPosition]

        return name == name1 && value == value1
    }
}