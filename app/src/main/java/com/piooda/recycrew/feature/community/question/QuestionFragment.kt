package com.piooda.recycrew.feature.community.question

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.piooda.data.model.PostData
import com.piooda.recycrew.core_ui.base.BaseFragment
import com.piooda.recycrew.core_ui.base.UIState
import com.piooda.recycrew.core_ui.base.ViewModelFactory
import com.piooda.recycrew.databinding.FragmentQuestionBinding
import com.piooda.recycrew.feature.auth.InputActivity
import com.piooda.recycrew.feature.community.question.adapter.QuestionRecyclerAdapter

class QuestionFragment : BaseFragment<FragmentQuestionBinding>(FragmentQuestionBinding::inflate) {
    private lateinit var recyclerAdapter: QuestionRecyclerAdapter
    private val viewModel by viewModels<QuestionViewModel> { ViewModelFactory }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuestionBinding.inflate(inflater, container, false)

        // RecyclerView 초기화
        setupRecyclerView()

        // ProgressBar 초기 상태 설정
        binding.progressBar.visibility = View.GONE

        // Floating Action Button 클릭 이벤트
        binding.floatingButton.setOnClickListener { navigateToInputActivity() }

        // 데이터 로드 및 UI 업데이트
        observePostData()

        // 데이터 로드 요청
        viewModel.loadData()

        return binding.root
    }

    private fun setupRecyclerView() {
        recyclerAdapter = QuestionRecyclerAdapter { item -> onItemClicked(item) }
        binding.recyclerView.apply {
            adapter = recyclerAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun navigateToInputActivity() {
        val intent = Intent(requireActivity(), InputActivity::class.java)
        startActivity(intent)
    }

    private fun observePostData() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.postData.collect { result ->
                //updateUIState(result)
            }
        }
    }

    private fun updateUIState(result: UIState<List<PostData>>) {
        when (result) {
            is UIState.Loading -> showLoadingState()
            is UIState.Success -> showSuccessState(result.data)
            is UIState.Failure -> showFailureState(result.errorMessage)
        }
    }

    private fun showLoadingState() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun showSuccessState(data: List<PostData>) {
        binding.progressBar.visibility = View.GONE
        recyclerAdapter.submitList(data)
    }

    private fun showFailureState(errorMessage: String?) {
        binding.progressBar.visibility = View.GONE
        Log.e("PostData", "Error fetching data: $errorMessage")
        Toast.makeText(
            requireContext(),
            "Failed to load data: ${errorMessage ?: "Unknown error"}",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun onItemClicked(item: PostData) {
        val action = QuestionFragmentDirections.actionQuestionFragmentToQuestionDetailFragment(item)
        findNavController().navigate(action)
    }
}