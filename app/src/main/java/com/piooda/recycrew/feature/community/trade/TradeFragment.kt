package com.piooda.recycrew.feature.community.trade

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.piooda.recycrew.core_ui.base.BaseFragment
import com.piooda.recycrew.databinding.FragmentTradeBinding

class TradeFragment : BaseFragment<FragmentTradeBinding>(FragmentTradeBinding::inflate) {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentTradeBinding.inflate(inflater, container, false)
        val sampleData =
            listOf("Item 1", "Item 2", "Item 3", "Item 4", "Item 5", "Item 6", "Item 7", "Item 8"
            ,"Item 9","Item 10")
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = TradeRecyclerAdapter(sampleData)
        return binding.root
    }
}