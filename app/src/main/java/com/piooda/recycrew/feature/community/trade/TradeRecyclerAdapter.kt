package com.piooda.recycrew.feature.community.trade

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.piooda.recycrew.databinding.ItemTradePostBinding

class TradeRecyclerAdapter(private val itemList: List<String>) : RecyclerView.Adapter<TradeRecyclerAdapter.ViewHolder>() {

    class ViewHolder(private val binding: ItemTradePostBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Int) { binding.itemTradePost }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTradePostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = itemList.size
}