package com.piooda.recycrew.feature.community.question.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.piooda.data.model.PostData
import com.piooda.recycrew.databinding.ItemCommunityPostBinding

//첫리사이클러뷰 목록가져오기
class QuestionRecyclerAdapter(
    private val onClick: (PostData) -> Unit,
) : ListAdapter<PostData, QuestionRecyclerAdapter.ViewHolder>(DiffCallback()) {

    class ViewHolder(
        private val binding: ItemCommunityPostBinding,
        private val onClick: (PostData) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PostData) {
            binding.tvTitle.text = item.title
            binding.tvDescription.text = item.content
            binding.tvTime.text = item.time
            binding.tvAuthor.text = item.userName
            binding.tvCommentCount.text = item.viewCount.toString()
            binding.tvLikeCount.text = item.likeCount.toString()
            Glide.with(binding.root.context)
                .load(item.imagePath)
                .into(binding.imgUserProfile)
            binding.root.setOnClickListener { onClick(item) } // 아이템 클릭 이벤트 처리
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemCommunityPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding, onClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<PostData>() {
        override fun areItemsTheSame(oldItem: PostData, newItem: PostData): Boolean {
            return oldItem.userId == newItem.userId
        }

        override fun areContentsTheSame(oldItem: PostData, newItem: PostData): Boolean {
            return oldItem == newItem
        }
    }
}

