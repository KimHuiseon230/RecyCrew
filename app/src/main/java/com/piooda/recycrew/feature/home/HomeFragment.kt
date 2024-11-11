package com.piooda.recycrew.feature.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.piooda.domain.model.ImageData
import com.piooda.recycrew.core_ui.base.BaseFragment
import com.piooda.recycrew.core_ui.base.ViewModelFactory
import com.piooda.recycrew.databinding.FragmentHomeBinding
import kotlinx.coroutines.launch

class HomeFragment : BaseFragment<FragmentHomeBinding>(FragmentHomeBinding::inflate) {
    private lateinit var recyclingCategoriesAdapter: RecyclingCategoriesAdapter
    private val viewModel by viewModels<RecyclingCategoriesImageViewModel> {
        ViewModelFactory
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        // RecyclerView 초기화
        recyclingCategoriesAdapter = RecyclingCategoriesAdapter { item -> onItemClicked(item) }
        binding.recyclerView.adapter = recyclingCategoriesAdapter

        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.imageData.collect { result ->
                recyclingCategoriesAdapter.submitList(result)
            }
        }

        viewModel.loadImageData()

        return binding.root
    }
    private fun onItemClicked(item: ImageData) {
        val action = HomeFragmentDirections.actionHomeFragmentToRecyclingCategoriesFragment(item.imageUrl)
        findNavController().navigate(action)
    }
}