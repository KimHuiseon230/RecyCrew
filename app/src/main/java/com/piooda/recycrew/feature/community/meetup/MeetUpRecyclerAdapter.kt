package com.piooda.recycrew.feature.community.meetup

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.piooda.recycrew.databinding.ItemMeetupPostBinding

class MeetUpRecyclerAdapter(private val itemList: List<String>) : RecyclerView.Adapter<MeetUpRecyclerAdapter.ViewHolder>() {

    class ViewHolder(private val binding: ItemMeetupPostBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Int) { binding.itemMeetupPost }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMeetupPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = itemList.size
}