package com.piooda.recycrew.ui.main.community

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.piooda.recycrew.R
import com.piooda.recycrew.base.BaseFragment
import com.piooda.recycrew.databinding.FragmentCommunityBinding

class CommunityFragment :
    BaseFragment<FragmentCommunityBinding>(FragmentCommunityBinding::inflate) {
    companion object {
        fun newInstance() = CommunityFragment()
    }

    private val viewModel: CommunityViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentCommunityBinding.inflate(inflater, container, false)

        binding.tradeBtn.setOnClickListener {
            findNavController().navigate(R.id.action_communityFragment_to_tradeFragment)
        }

        binding.questionBtn.setOnClickListener {
            findNavController().navigate(R.id.action_communityFragment_to_questionFragment)
        }

        binding.meetUpBtn.setOnClickListener {
            findNavController().navigate(R.id.action_communityFragment_to_meetUpFragment)
        }
        return binding.root
    }

}