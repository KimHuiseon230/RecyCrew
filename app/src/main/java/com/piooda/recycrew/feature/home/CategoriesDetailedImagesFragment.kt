package com.piooda.recycrew.feature.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.piooda.recycrew.R
import com.piooda.recycrew.core_ui.base.BaseFragment
import com.piooda.recycrew.core_ui.base.ViewModelFactory
import com.piooda.recycrew.databinding.FragmentCategoriesDetailedImagesBinding
import kotlinx.coroutines.launch

class CategoriesDetailedImagesFragment :
    BaseFragment<FragmentCategoriesDetailedImagesBinding>(FragmentCategoriesDetailedImagesBinding::inflate) {

    private val viewModel by viewModels<CategoriesDetailedImagesViewModel> { ViewModelFactory(requireContext()) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val args: CategoriesDetailedImagesFragmentArgs by navArgs()
        val detailedImageData = args.detailedImageData
        Log.d("CategoriesDetailedImagesFragment", "detailedImageData: $detailedImageData")

        viewModel.loadDetailedImageData(detailedImageData)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentCategoriesDetailedImagesBinding.inflate(inflater, container, false)

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.detailedImageData.collect { data ->
                if (isAdded) {
                    if (data.isNotEmpty()) {
                        val item = data.first()
                        binding.titleTextView.text = item.title ?: ""
                        Glide.with(requireContext())
                            .load(item.imageUrl)
                            .into(binding.imagePlaceholder)
                        binding.categoryTextView.text = item.categoryLabel ?: ""
                        binding.subcategoryLabel.text = item.subcategory ?: ""
                        binding.excludedItemsTextView.text = item.excludedItems ?: ""
                    } else {
                        binding.titleTextView.text = ""
                        binding.imagePlaceholder.setImageResource(R.drawable.ic_launcher_background)
                        binding.categoryTextView.text = ""
                        binding.subcategoryLabel.text = ""
                        binding.excludedItemsTextView.text = ""
                    }
                }
            }
        }

        return binding.root
    }
}