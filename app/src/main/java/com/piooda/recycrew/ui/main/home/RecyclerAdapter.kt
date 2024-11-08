package com.piooda.recycrew.ui.main.home

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.piooda.recycrew.databinding.ItemRecyclerViewBinding

class RecyclerAdapter : ListAdapter<ImageData, RecyclerAdapter.ViewHolder>(DiffCallback()) {

    class ViewHolder(private val binding: ItemRecyclerViewBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ImageData) {
            binding.textViewTitle.text = item.title
            Glide.with(binding.root.context)
                .load(item.imageUrl)
                .into(binding.imageView)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRecyclerViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        Log.d("RecyclerAdapter", "Binding item: $item") // 로그 추가

        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<ImageData>() {
        override fun areItemsTheSame(oldItem: ImageData, newItem: ImageData): Boolean {
            return oldItem.imageUrl == newItem.imageUrl // or another unique property
        }

        override fun areContentsTheSame(oldItem: ImageData, newItem: ImageData): Boolean {
            return oldItem == newItem
        }
    }
}
