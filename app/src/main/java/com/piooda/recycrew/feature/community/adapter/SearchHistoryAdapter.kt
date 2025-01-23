package com.piooda.recycrew.feature.community.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.piooda.recycrew.databinding.ItemSearchHistoryBinding

class SearchHistoryAdapter(
    private val onItemClick: (String) -> Unit,
    private val onDeleteClick: (String) -> Unit
) : ListAdapter<String, SearchHistoryAdapter.ViewHolder>(DiffCallback()) {

    class ViewHolder(private val binding: ItemSearchHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(query: String, onItemClick: (String) -> Unit, onDeleteClick: (String) -> Unit) {
            binding.chipSearchHistory.text = query

            // 🔍 검색어 클릭 시 자동 입력 및 검색 실행
            binding.chipSearchHistory.setOnClickListener {
                Log.d("SearchHistoryAdapter", "🔍 검색 실행: $query")
                onItemClick(query)
            }

            // ❌ 삭제 버튼 클릭 시 검색 기록 삭제
            binding.chipSearchHistory.setOnCloseIconClickListener {
                Log.d("SearchHistoryAdapter", "🗑 검색 기록 삭제 클릭됨: $query")
                onDeleteClick(query)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSearchHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), onItemClick, onDeleteClick)
    }

    class DiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean = oldItem == newItem
        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean = oldItem == newItem
    }
}
