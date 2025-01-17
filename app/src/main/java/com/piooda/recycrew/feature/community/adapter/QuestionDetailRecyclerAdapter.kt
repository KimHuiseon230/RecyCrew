package com.piooda.recycrew.feature.community.adapter

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.piooda.data.model.Content
import com.piooda.recycrew.databinding.ItemPostBinding

class QuestionDetailRecyclerAdapter(
    private val onEditClick: (Content) -> Unit,   // 수정 클릭 이벤트 처리
    private val onDeleteClick: (Content) -> Unit,  // 삭제 클릭 이벤트 처리
) : ListAdapter<Content, QuestionDetailRecyclerAdapter.ViewHolder>(DiffCallback()) {

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
        private val onEditClick: (Content) -> Unit,
        private val onDeleteClick: (Content) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(post: Content) {
            binding.tvTitle.text = post.title
            binding.postContent.text = post.content
//            Glide.with(binding.root.context)
//                .load(post.imagePath)
//                .into(binding.imagePath)

            // 옵션 버튼 클릭 시 다이얼로그 표시
            binding.btnOptions.setOnClickListener {
                showOptionsDialog(post)
            }
        }

        // 옵션 다이얼로그를 띄우는 메소드
        private fun showOptionsDialog(content: Content) {
            val context = binding.root.context
            val dialog = AlertDialog.Builder(context)
                .setItems(arrayOf("수정", "삭제")) { _, which ->
                    when (which) {
                        0 -> onEditClick(content)  // 수정 클릭 시 onEditClick 호출
                        1 -> onDeleteClick(content)  // 삭제 클릭 시 onDeleteClick 호출
                    }
                }
                .create()

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
