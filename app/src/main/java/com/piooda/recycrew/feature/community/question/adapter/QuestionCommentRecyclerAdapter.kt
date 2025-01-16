package com.piooda.recycrew.feature.community.question.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.piooda.data.model.Content
import com.piooda.recycrew.databinding.ItemCommentUserBinding

class QuestionCommentRecyclerAdapter :
    ListAdapter<Content.Comment, QuestionCommentRecyclerAdapter.CommentViewHolder>(CommentDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = ItemCommentUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CommentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = getItem(position)
        holder.bind(comment)
    }

    inner class CommentViewHolder(private val binding: ItemCommentUserBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(comment: Content.Comment) {
            binding.apply {
                userName.text = comment.author
                commentText.text = comment.content
            }
        }
    }

    class CommentDiffCallback : DiffUtil.ItemCallback<Content.Comment>() {
        override fun areItemsTheSame(oldItem: Content.Comment, newItem: Content.Comment): Boolean {
            return oldItem.content == newItem.content
        }

        override fun areContentsTheSame(oldItem: Content.Comment, newItem: Content.Comment): Boolean {
            return oldItem == newItem
        }
    }
}
