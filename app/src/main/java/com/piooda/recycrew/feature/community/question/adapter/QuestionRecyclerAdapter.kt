package com.piooda.recycrew.feature.community.question.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.piooda.data.model.PostData
import com.piooda.recycrew.R
import com.piooda.recycrew.databinding.ItemCommunityPostBinding

class QuestionRecyclerAdapter(
    private val onClick: (PostData) -> Unit,
    private val onLikeClick: (PostData) -> Unit, // onLikeClick 콜백 추가
) : ListAdapter<PostData, QuestionRecyclerAdapter.ViewHolder>(DiffCallback()) {

    // ViewHolder에서 두 개의 객체를 함께 바인딩
    class ViewHolder(
        private val binding: ItemCommunityPostBinding,
        private val onClick: (PostData) -> Unit,
        private val onLikeClick: (PostData) -> Unit, // onLikeClick 콜백 추가

    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PostData) {
            // PostData 객체의 데이터를 바인딩
            binding.tvTitle.text = item.title
            binding.tvDescription.text = item.content
            binding.tvTime.text = item.time
            binding.tvAuthor.text = item.userName
            binding.tvCommentCount.text = item.commentCount.toString()
            binding.tvLikeCount.text = item.likeCount.toString()
            Glide.with(binding.root.context)
                .load(item.imagePath)
                .into(binding.imagePath)

            // 좋아요 상태에 따라 UI 업데이트
            updateLikeButtonUI(item.isLiked)
            binding.icLike.setOnClickListener {
                item.isLiked = !item.isLiked
                // 좋아요 숫자 변경 (최소 0으로 제한)
                item.likeCount = (if (item.isLiked) item.likeCount + 1 else item.likeCount - 1).coerceAtLeast(0)
                // UI 업데이트
                updateLikeButtonUI(item.isLiked)
                // 부모로 변경 사항 전달
                onLikeClick(item)
                Log.e("QuestionRecyclerAdapter","${item}")
            }

            // 클릭 시 item과 item2(PostData와 Comment) 둘 다 전달
            binding.root.setOnClickListener { onClick(item) }

        }

        private fun updateLikeButtonUI(isLiked: Boolean) {
            if (isLiked) {
                binding.icLike.setImageResource(R.drawable.ic_baseline_unfavorite_24)
            } else {
                binding.icLike.setImageResource(R.drawable.ic_baseline_favorite_24)
            }
        }

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemCommunityPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding, onClick, onLikeClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val postData = getItem(position)  // PostData

        holder.bind(postData)
    }

    class DiffCallback : DiffUtil.ItemCallback<PostData>() {
        override fun areItemsTheSame(
            oldItem: PostData,
            newItem: PostData,
        ): Boolean {
            return oldItem.userId == newItem.userId
        }

        override fun areContentsTheSame(
            oldItem: PostData,
            newItem: PostData,
        ): Boolean {
            return oldItem == newItem
        }
    }
}