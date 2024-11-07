package com.piooda.recycrew.ui.main.community.questions

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.piooda.recycrew.databinding.ItemCommunityPostBinding

class QuestionRecyclerAdapter (private val itemList: List<String>) : RecyclerView.Adapter<QuestionRecyclerAdapter.ViewHolder>() {

    class ViewHolder(private val binding: ItemCommunityPostBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Int) { binding.itemQuestionPost }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCommunityPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.bind(position)
    }

    override fun getItemCount(): Int = itemList.size
}