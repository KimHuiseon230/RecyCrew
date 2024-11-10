package com.piooda.recycrew.feature.event.attendanceCheck

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.piooda.recycrew.core_ui.base.BaseFragment
import com.piooda.recycrew.databinding.FragmentAttendanceCheckBinding

class AttendanceCheckFragment : BaseFragment<FragmentAttendanceCheckBinding>(FragmentAttendanceCheckBinding::inflate){
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentAttendanceCheckBinding.inflate(inflater, container, false)
        return binding.root
    }
}