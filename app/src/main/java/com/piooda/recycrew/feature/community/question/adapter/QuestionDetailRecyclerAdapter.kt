package com.piooda.recycrew.feature.community.question.adapter

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.piooda.data.model.PostData
import com.piooda.recycrew.databinding.ItemPostBinding

class QuestionDetailRecyclerAdapter(
    private val onEditClick: (PostData) -> Unit,   // 수정 클릭 이벤트 처리
    private val onDeleteClick: (PostData) -> Unit,  // 삭제 클릭 이벤트 처리
) : ListAdapter<PostData, QuestionDetailRecyclerAdapter.ViewHolder>(PostDiffCallback()) {

    // 아이템 뷰 홀더를 생성하는 함수
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding, onEditClick, onDeleteClick)
    }

    // 뷰 홀더에 데이터를 바인딩하는 함수
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post = getItem(position)
        holder.bind(post)
    }

    // 데이터 아이템 개수를 반환하는 함수
    override fun getItemCount(): Int =
        currentList.size

    // 뷰 홀더 클래스
    class ViewHolder(
        private val binding: ItemPostBinding,
        private val onEditClick: (PostData) -> Unit,
        private val onDeleteClick: (PostData) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(post: PostData) {
            binding.tvTitle.text = post.title
            binding.postContent.text = post.content
            Glide.with(binding.root.context)
                .load(post.imagePath)
                .into(binding.imagePath)

            // 옵션 버튼 클릭 시 다이얼로그 표시
            binding.btnOptions.setOnClickListener {
                showOptionsDialog(post)
            }
        }

        // 옵션 다이얼로그를 띄우는 메소드
        private fun showOptionsDialog(postData: PostData) {
            val context = binding.root.context
            val dialog = AlertDialog.Builder(context)
                .setItems(arrayOf("수정", "삭제")) { _, which ->
                    when (which) {
                        0 -> onEditClick(postData)  // 수정 클릭 시 onEditClick 호출
                        1 -> onDeleteClick(postData)  // 삭제 클릭 시 onDeleteClick 호출
                    }
                }
                .create()

            dialog.show()
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
