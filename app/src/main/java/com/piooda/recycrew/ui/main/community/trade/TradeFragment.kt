package com.piooda.recycrew.ui.main.community.trade

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.piooda.recycrew.R
import com.piooda.recycrew.base.BaseFragment
import com.piooda.recycrew.databinding.FragmentHomeBinding
import com.piooda.recycrew.databinding.FragmentQuestionBinding
import com.piooda.recycrew.databinding.FragmentTradeBinding


class TradeFragment : BaseFragment<FragmentTradeBinding>(FragmentTradeBinding::inflate) {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentTradeBinding.inflate(inflater, container, false)
        return binding.root
    }
}