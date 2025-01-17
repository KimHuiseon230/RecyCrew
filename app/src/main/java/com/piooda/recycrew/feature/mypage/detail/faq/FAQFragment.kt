package com.piooda.recycrew.feature.mypage.detail.faq

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.piooda.recycrew.R
import com.piooda.UiState
import com.piooda.recycrew.core.BaseFragment
import com.piooda.recycrew.core.ViewModelFactory
import com.piooda.recycrew.core.util.logDebug
import com.piooda.recycrew.core.util.logError
import com.piooda.recycrew.databinding.FragmentFaqBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FAQFragment: BaseFragment<FragmentFaqBinding>(FragmentFaqBinding::inflate) {
    private lateinit var faqRecyclerView: RecyclerView
    private lateinit var adapter: FAQListAdapter

    private val viewModel by viewModels<FAQViewModel> {
        ViewModelFactory(requireContext())
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFaqBinding.inflate(layoutInflater, container, false)
        faqRecyclerView = binding.recyclerviewFaq
        //faqRecyclerView.layoutManager = LinearLayoutManager(requireContext()) // 중복?

        // 초기화 코드 추가
        adapter = FAQListAdapter()
        faqRecyclerView.adapter = adapter
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getFAQs()
        collectGetFAQ()
    }


    private fun collectGetFAQ() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getFAQState.collectLatest { state ->
                when (state) {
                    is UiState.Loading -> {
                        logDebug("FAQFragment", R.string.loading_faq_data)
                    }

                    is UiState.Success -> {
                        adapter.submitList(state.resultData)
                    }

                    is UiState.Error -> {
                        logError("FAQFragment", R.string.error_loading_data, state.exception)
                    }

                    is UiState.Empty -> {
                        logDebug("FAQFragment", R.string.no_data_available)
                    }
                }
            }
        }
    }
}
