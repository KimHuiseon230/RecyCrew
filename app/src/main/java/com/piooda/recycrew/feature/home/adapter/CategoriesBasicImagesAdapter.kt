package com.piooda.recycrew.feature.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.piooda.data.model.DetailedImageData
import com.piooda.recycrew.databinding.ItemCategoriesBasicImageBinding

class CategoriesBasicImagesAdapter(private val onClick: (DetailedImageData) -> Unit) :
    ListAdapter<DetailedImageData, CategoriesBasicImagesAdapter.ViewHolder>(DiffCallback()) {

    class ViewHolder(
        private val binding: ItemCategoriesBasicImageBinding,
        private val onClick: (DetailedImageData) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: DetailedImageData) {
            with(binding) {
                textViewTitle.text = item.title
                Glide.with(binding.root.context)
                    .load(item.imageUrl)
                     .into(binding.imageView)

                root.setOnClickListener { onClick(item) } // item 전체 전달
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemCategoriesBasicImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding, onClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<DetailedImageData>() {
        override fun areItemsTheSame(oldItem: DetailedImageData, newItem: DetailedImageData): Boolean {
            return oldItem.num == newItem.num
        }

        override fun areContentsTheSame(oldItem: DetailedImageData, newItem: DetailedImageData): Boolean {
            return oldItem == newItem
        }
    }
}
