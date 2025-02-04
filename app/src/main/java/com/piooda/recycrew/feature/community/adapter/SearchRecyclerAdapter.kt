package com.piooda.recycrew.feature.community.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.piooda.data.model.Content
import com.piooda.recycrew.databinding.ItemSearchBinding

class SearchRecyclerAdapter(
    private var contentList: List<Content>,
    private var userList: List<String>,
    private val onItemClick: (Content) -> Unit,  // 게시글 클릭 이벤트
    private val onUserClick: (String) -> Unit   // 유저 클릭 이벤트 (닉네임 전달)
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_USER = 0
        private const val VIEW_TYPE_CONTENT = 1
    }

    fun updateList(newContentList: List<Content>, newUserList: List<String>) {
        contentList = newContentList
        userList = newUserList
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return if (position < userList.size) VIEW_TYPE_USER else VIEW_TYPE_CONTENT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ItemSearchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return if (viewType == VIEW_TYPE_USER) {
            UserViewHolder(binding, onUserClick)
        } else {
            ContentViewHolder(binding, onItemClick)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is UserViewHolder) {
            holder.bind(userList[position])
        } else if (holder is ContentViewHolder) {
            holder.bind(contentList[position - userList.size])
        }
    }

    override fun getItemCount(): Int = contentList.size + userList.size

    class UserViewHolder(
        private val binding: ItemSearchBinding,
        private val onUserClick: (String) -> Unit  //  유저 클릭 이벤트 추가
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(username: String) {
            binding.textView.text = username
            binding.root.setOnClickListener {
                Log.d("SearchRecyclerAdapter", "유저 클릭: $username")
                onUserClick(username)  // 클릭 이벤트 실행
            }
        }
    }

    class ContentViewHolder(
        private val binding: ItemSearchBinding,
        private val onItemClick: (Content) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(content: Content) {
            binding.textView.text = content.title
            binding.textView2.text = content.content

            binding.root.setOnClickListener {
                if (content.id.isNullOrEmpty()) {
                    Log.e("SearchRecyclerAdapter", "❌ content.id가 null이거나 비어 있음!")
                } else {
                    onItemClick(content)  // 게시글 클릭 이벤트 실행
                }
            }
        }
    }
}
