package com.piooda.recycrew.feature.community.question.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.piooda.data.model.PostData
import com.piooda.recycrew.databinding.ItemPostBinding

class QuestionDetailRecyclerAdapter() :
    ListAdapter<PostData, QuestionDetailRecyclerAdapter.PostViewHolder>(PostDiffCallback()) {

    // 아이템 뷰 홀더를 생성하는 함수
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
    }

    // 뷰 홀더에 데이터를 바인딩하는 함수
    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = getItem(position)  // ListAdapter에서는 getItem()을 사용하여 데이터에 접근합니다.
        holder.bind(post)
    }

    // 데이터 아이템 개수를 반환하는 함수
    override fun getItemCount(): Int =
        currentList.size  // currentList는 ListAdapter에서 자동으로 제공되는 리스트입니다.

    // 뷰 홀더 클래스
    inner class PostViewHolder(private val binding: ItemPostBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(post: PostData) {
            binding.tvTitle.text = post.title
            binding.postContent.text = post.content
            Glide.with(binding.root.context)
                .load(post.imagePath)
                .into(binding.imagePath)
            // 필요한 경우 클릭 이벤트 처리

            binding.btnOptions.setOnClickListener {
            }
        }
    }

    class PostDiffCallback : DiffUtil.ItemCallback<PostData>() {
        override fun areItemsTheSame(oldItem: PostData, newItem: PostData): Boolean {
            return oldItem.postId == newItem.postId
        }

        override fun areContentsTheSame(oldItem: PostData, newItem: PostData): Boolean {
            return oldItem == newItem
        }
    }
}
