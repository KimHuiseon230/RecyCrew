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

    // ViewHolder에서 두 개의 객체를 함께 바인딩
    class ViewHolder(
        private val binding: ItemCommunityPostBinding,
        private val onClick: (PostData) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PostData) {
            // PostData 객체의 데이터를 바인딩
            binding.tvTitle.text = item.title
            binding.tvDescription.text = item.content
            binding.tvTime.text = item.time
            binding.tvAuthor.text = item.userName
            binding.tvCommentCount.text = item.viewCount.toString()
            binding.tvLikeCount.text = item.likeCount.toString()
            Glide.with(binding.root.context)
                .load(item.imagePath)
                .into(binding.imagePath)

            // 클릭 시 item과 item2(PostData와 Comment) 둘 다 전달
            binding.root.setOnClickListener { onClick(item) }


        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemCommunityPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding, onClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // adapter에 데이터를 전달할 때, item과 item2를 각각 전달
        val postData = getItem(position)  // PostData
        val commentData = getItem(position)  // commentData

        holder.bind(postData)  // PostData와 Comment를 함께 bind
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