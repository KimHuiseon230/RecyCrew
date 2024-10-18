package com.piooda.recycrew.ui.main.event.attendanceCheck

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.piooda.recycrew.R
import com.piooda.recycrew.base.BaseFragment
import com.piooda.recycrew.databinding.FragmentAttendanceCheckBinding
import com.piooda.recycrew.databinding.FragmentHomeBinding
import com.piooda.recycrew.databinding.FragmentTradeBinding

class AttendanceCheckFragment : BaseFragment<FragmentAttendanceCheckBinding>(FragmentAttendanceCheckBinding::inflate){
    companion object {
        fun newInstance() = AttendanceCheckFragment()
    }

    private val viewModel: AttendanceCheckViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentAttendanceCheckBinding.inflate(inflater, container, false)
        return binding.root
    }

}