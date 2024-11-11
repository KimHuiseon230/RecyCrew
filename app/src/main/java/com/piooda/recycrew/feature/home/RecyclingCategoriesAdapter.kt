package com.piooda.recycrew.feature.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.piooda.domain.model.ImageData
import com.piooda.recycrew.databinding.ItemRecyclerViewBinding

class RecyclingCategoriesAdapter(private val onClick: (ImageData) -> Unit) :
    ListAdapter<ImageData, RecyclingCategoriesAdapter.ViewHolder>(
        DiffCallback()
    ) {

    class ViewHolder(
        private val binding: ItemRecyclerViewBinding,
        private val onClick: (ImageData) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ImageData) {
            with(binding) {
                textViewTitle.text = item.title
                Glide.with(binding.root.context)
                    .load(item.imageUrl)
                    .into(binding.imageView)

                root.setOnClickListener { onClick(item) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemRecyclerViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding, onClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
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
