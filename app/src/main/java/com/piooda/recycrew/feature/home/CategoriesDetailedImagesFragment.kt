package com.piooda.recycrew.feature.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.piooda.recycrew.core_ui.base.BaseFragment
import com.piooda.recycrew.core_ui.base.UIState
import com.piooda.recycrew.core_ui.base.ViewModelFactory
import com.piooda.recycrew.databinding.FragmentCategoriesDetailedImagesBinding

class CategoriesDetailedImagesFragment :
    BaseFragment<FragmentCategoriesDetailedImagesBinding>(FragmentCategoriesDetailedImagesBinding::inflate) {

    private val viewModel by viewModels<CategoriesDetailedImagesViewModel> { ViewModelFactory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // NavArgs를 통해 전달된 데이터
        val args: CategoriesDetailedImagesFragmentArgs by navArgs()
        val detailedImageData = args.detailedImageData
        Log.d("CategoriesDetailedImagesFragment", "detailedImageData: $detailedImageData")

        // 뷰모델에서 데이터 로딩 시작
        viewModel.loadDetailedImageData()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoriesDetailedImagesBinding.inflate(inflater, container, false)
        val progressBar = binding.progressBar // ProgressBar에 대한 바인딩

        // 뷰모델에서 데이터 로딩 상태에 따라 UI 업데이트
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.detailedImageData.collect { state ->
                when (state) {
                    is UIState.Loading -> {
                        // 데이터 로딩 중에는 ProgressBar를 보이게 설정
                        progressBar.visibility = View.VISIBLE
                        binding.imagePlaceholder.visibility = View.GONE
                    }
                    is UIState.Success -> {
                        // 데이터 로드 성공 후
                        progressBar.visibility = View.GONE
                        binding.imagePlaceholder.visibility = View.VISIBLE

                        // 데이터를 UI에 반영
                        val item = state.data.firstOrNull()
                        binding.titleTextView.text = item?.title ?: ""
                        Glide.with(requireContext())
                            .load(item?.imageUrl)
                            .into(binding.imagePlaceholder)
                        binding.categoryTextView.text = item?.categoryLabel ?: ""
                        binding.subcategoryLabel.text = item?.subcategory ?: ""
                        binding.excludedItemsTextView.text = item?.excludedItems ?: ""
                    }
                    is UIState.Failure -> {
                        // 에러 발생 시 ProgressBar 숨기기 및 에러 메시지 표시
                        progressBar.visibility = View.GONE
                        Toast.makeText(requireContext(), state.errorMessage, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        return binding.root
    }
}
