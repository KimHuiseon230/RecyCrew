package com.piooda.data.repositoryImpl

import androidx.recyclerview.widget.DiffUtil
import com.piooda.data.model.Content

class ContentDiffCallback : DiffUtil.ItemCallback<Content>() {
    override fun areItemsTheSame(oldItem: Content, newItem: Content) = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: Content, newItem: Content) = oldItem == newItem
}