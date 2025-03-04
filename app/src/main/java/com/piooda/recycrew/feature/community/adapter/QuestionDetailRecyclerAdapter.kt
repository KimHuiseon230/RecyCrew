package com.piooda.recycrew.feature.community.adapter

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.piooda.data.model.Content
import com.piooda.recycrew.databinding.ItemPostBinding

class QuestionDetailRecyclerAdapter(
    private val onEditClick: (Content) -> Unit, // 수정
    private val onDeleteClick: (Content) -> Unit, // 삭제
) : ListAdapter<Content, QuestionDetailRecyclerAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding, onEditClick, onDeleteClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post = getItem(position)
        holder.bind(post)
    }

    override fun getItemCount(): Int =
        currentList.size


    class ViewHolder(
        private val binding: ItemPostBinding,
        private val onEditClick: (Content) -> Unit,
        private val onDeleteClick: (Content) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(post: Content) {
            binding.tvTitle.text = post.title
            binding.postContent.text = post.content
            Glide.with(binding.root.context)
                .load(post.imagePath)
                .into(binding.imagePath)

            // 메뉴 다이얼로그 클릭 이벤트
            binding.btnOptions.setOnClickListener { showOptionsDialog(post) }
        }

        // 메뉴 다이얼로그 함수 
        private fun showOptionsDialog(content: Content) {
            val context = binding.root.context
            val dialog = AlertDialog.Builder(context)
                .setItems(arrayOf("수정", "삭제")) { _, which ->
                    when (which) {
                        0 -> onEditClick(content)
                        1 -> onDeleteClick(content)
                    }
                }.create()
            dialog.show()
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Content>() {
        override fun areItemsTheSame(oldItem: Content, newItem: Content): Boolean {
            Log.d("DiffCallback", "아이템 비교: ${oldItem.id} == ${newItem.id}")
            return oldItem.id == newItem.id
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: Content, newItem: Content): Boolean {
            val isSame = oldItem.title == newItem.title &&
                    oldItem.favoriteCount == newItem.favoriteCount &&
                    oldItem.favorites == newItem.favorites

            Log.d("DiffCallback", "내용 비교 결과: $isSame")
            return isSame
        }
    }
}
