package com.piooda.recycrew.feature.community.question

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.piooda.data.model.PostData
import com.piooda.recycrew.core_ui.base.BaseFragment
import com.piooda.recycrew.core_ui.base.InputActivity
import com.piooda.recycrew.core_ui.base.ViewModelFactory
import com.piooda.recycrew.databinding.FragmentQuestionBinding
import com.piooda.recycrew.feature.community.question.adapter.QuestionRecyclerAdapter
import kotlinx.coroutines.launch

class QuestionFragment : BaseFragment<FragmentQuestionBinding>(FragmentQuestionBinding::inflate) {
    private lateinit var recyclerAdapter: QuestionRecyclerAdapter
    private val viewModel by viewModels<QuestionViewModel> {
        ViewModelFactory(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentQuestionBinding.inflate(inflater, container, false)

        recyclerAdapter = QuestionRecyclerAdapter { item -> onItemClicked(item) }
        // RecyclerView 초기화
        binding.recyclerView.adapter = recyclerAdapter
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // ProgressBar를 위한 변수
        val progressBar = binding.progressBar
        progressBar.visibility = View.GONE

        binding.floatingButton.setOnClickListener {
            // InputActivity로 이동
            val intent = Intent(requireActivity(), InputActivity::class.java)
            startActivity(intent)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                viewModel.postData.collect { result ->
                    // 정상적으로 데이터를 처리
                    progressBar.visibility = View.GONE
                    recyclerAdapter.submitList(result)
                }
            } catch (e: Exception) {
                // 예외 처리
                Log.e("PostData", "Error fetching data: ${e.message}")
                progressBar.visibility = View.GONE
            }
        }
        viewModel.loadData()
        return binding.root
    }

    private fun onItemClicked(item: PostData) {
        val action = QuestionFragmentDirections.actionQuestionFragmentToQuestionDetailFragment(item)
        findNavController().navigate(action)
    }
}