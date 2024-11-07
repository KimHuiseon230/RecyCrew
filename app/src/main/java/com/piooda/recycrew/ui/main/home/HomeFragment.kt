package com.piooda.recycrew.ui.main.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.piooda.recycrew.base.BaseFragment
import com.piooda.recycrew.databinding.FragmentHomeBinding

class HomeFragment : BaseFragment<FragmentHomeBinding>(FragmentHomeBinding::inflate) {
    companion object {
        fun newInstance() = HomeFragment()
    }

    private lateinit var recyclerAdapter: RecyclerAdapter

    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        // RecyclerView 초기화
        recyclerAdapter = RecyclerAdapter()
        binding.recyclerView.adapter = recyclerAdapter
        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)

        // ViewModel에서 데이터 관찰
        viewModel.imageList.observe(viewLifecycleOwner) { imageList ->
            recyclerAdapter.submitList(imageList) // 데이터 업데이트
        }
        viewModel.fetchImagesFromFirebase()

        return binding.root
    }
}