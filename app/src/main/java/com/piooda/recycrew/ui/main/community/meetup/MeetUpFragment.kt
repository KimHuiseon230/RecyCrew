package com.piooda.recycrew.ui.main.community.meetup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.piooda.recycrew.base.BaseFragment
import com.piooda.recycrew.databinding.FragmentMeetUpBinding

class MeetUpFragment : BaseFragment<FragmentMeetUpBinding>(FragmentMeetUpBinding::inflate) {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentMeetUpBinding.inflate(inflater, container, false)
        // 샘플 데이터
        val sampleData =
            listOf("Item 1", "Item 2", "Item 3", "Item 4", "Item 5", "Item 6", "Item 7", "Item 8"
                ,"Item 9","Item 10")
        // RecyclerView와 어댑터 설정
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = MeetUpRecyclerAdapter(sampleData)
        return binding.root
    }
}