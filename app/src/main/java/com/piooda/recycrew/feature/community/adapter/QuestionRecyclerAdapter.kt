package com.piooda.recycrew.feature.community.adapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.piooda.data.model.Content
import com.piooda.recycrew.R
import com.piooda.recycrew.databinding.ItemCommunityPostBinding

class QuestionRecyclerAdapter(
    private val onClick: (Content) -> Unit,
    private val onLikeClick: (Content) -> Unit
) : ListAdapter<Content, QuestionRecyclerAdapter.ViewHolder>(DiffCallback())  {

    class ViewHolder(
    private val binding: ItemCommunityPostBinding,
    private val onClick: (Content) -> Unit,
    private val onLikeClick: (Content) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Content) {
            binding.tvTitle.text = item.title
            binding.tvDescription.text = item.content
            binding.tvCommentCount.text = item.commentCount.toString()
            binding.tvLikeCount.text = item.favoriteCount.toString()

            // ✅ Firestore 데이터 반영 후 UI 갱신
            val isLiked = item.favorites.containsKey(FirebaseAuth.getInstance().currentUser?.uid)
            updateLikeButtonUI(isLiked)

            binding.icLike.setOnClickListener {
                if (!isButtonClickedRecently()) {
                    onLikeClick(item)
                }
            }

            binding.root.setOnClickListener { onClick(item) }
        }

        private var lastClickTime = 0L
        private fun isButtonClickedRecently(): Boolean {
            val currentTime = System.currentTimeMillis()
            return if (currentTime - lastClickTime < 1000) {
                true
            } else {
                lastClickTime = currentTime
                false
            }
        }

        private fun updateLikeButtonUI(isLiked: Boolean) {
            binding.icLike.setImageResource(
                if (isLiked) R.drawable.ic_baseline_unfavorite_24
                else R.drawable.ic_baseline_favorite_24
            )
        }

    }
    class DiffCallback : DiffUtil.ItemCallback<Content>() {
        override fun areItemsTheSame(oldItem: Content, newItem: Content): Boolean {
            val result = oldItem.id == newItem.id
            Log.d("DiffCallback", "areItemsTheSame 호출: $result")
            return result
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: Content, newItem: Content): Boolean {
            val result = oldItem.favoriteCount == newItem.favoriteCount &&
                    oldItem.favorites.size == newItem.favorites.size &&
                    oldItem.favorites == newItem.favorites
            Log.d("DiffCallback", "areContentsTheSame 호출: $result")
            return result
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCommunityPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding, onClick, onLikeClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
