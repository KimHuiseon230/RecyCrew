package com.piooda.recycrew.ui.main.community.questions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.piooda.recycrew.base.BaseFragment
import com.piooda.recycrew.databinding.FragmentQuestionBinding
import com.piooda.recycrew.ui.main.community.CommunityViewModel

class QuestionFragment : BaseFragment<FragmentQuestionBinding>(FragmentQuestionBinding::inflate) {
    private lateinit var recyclerAdapter: QuestionRecyclerAdapter

    private val viewModel: CommunityViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentQuestionBinding.inflate(inflater, container, false)
        // 샘플 데이터
        val sampleData =
            listOf("Item 1", "Item 2", "Item 3", "Item 4", "Item 5", "Item 6", "Item 7", "Item 8"
                ,"Item 9","Item 10")
        recyclerAdapter = QuestionRecyclerAdapter(sampleData)
        binding.recyclerView.adapter = recyclerAdapter
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        return binding.root
    }
}

