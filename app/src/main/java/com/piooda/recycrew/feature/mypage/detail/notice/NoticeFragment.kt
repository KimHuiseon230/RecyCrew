package com.piooda.recycrew.feature.mypage.detail.notice


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.piooda.recycrew.R
import com.piooda.recycrew.common.UiState
import com.piooda.recycrew.core_ui.base.BaseFragment
import com.piooda.recycrew.core_ui.base.ViewModelFactory
import com.piooda.recycrew.core_ui.util.logDebug
import com.piooda.recycrew.core_ui.util.logError
import com.piooda.recycrew.databinding.FragmentNoticeBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class NoticeFragment: BaseFragment<FragmentNoticeBinding>(FragmentNoticeBinding::inflate) {
    private lateinit var noticeRecyclerView: RecyclerView
    private lateinit var adapter: NoticeListAdapter

    private val viewModel by viewModels<NoticeViewModel> {
        ViewModelFactory(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNoticeBinding.inflate(layoutInflater, container, false)
        noticeRecyclerView = binding.recyclerviewNotice

        adapter = NoticeListAdapter()
        noticeRecyclerView.adapter = adapter
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getNotices()
        collectGetNotice()
    }


    private fun collectGetNotice() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getNoticeState.collectLatest { state ->
                when (state) {
                    is UiState.Loading -> {
                        logDebug("NoticeFragment", R.string.loading_notice_data)
                    }

                    is UiState.Success -> {
                        adapter.submitList(state.resultData)
                    }

                    is UiState.Error -> {
                        logError("NoticeFragment", R.string.error_loading_notice_data, state.exception)
                    }

                    is UiState.Empty -> {
                        logDebug("NoticeFragment", R.string.no_notice_data_available)
                    }
                }
            }
        }
    }
}

