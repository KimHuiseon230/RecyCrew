package com.piooda.recycrew.ui.main.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.piooda.recycrew.R
import com.piooda.recycrew.base.BaseFragment
import com.piooda.recycrew.databinding.FragmentChatBinding
import com.piooda.recycrew.databinding.FragmentQuestionBinding


class ChatFragment : BaseFragment<FragmentChatBinding>(FragmentChatBinding::inflate) {
    companion object {
        fun newInstance() = ChatFragment()
    }

    private val viewModel: ChatViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }
}