package com.piooda.recycrew.feature.event.ui

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.applandeo.materialcalendarview.EventDay

import com.google.firebase.auth.FirebaseAuth
import com.piooda.data.model.Attendance
import com.piooda.recycrew.R
import com.piooda.UiState
import com.piooda.recycrew.core.BaseFragment
import com.piooda.recycrew.core.ViewModelFactory
import com.piooda.recycrew.core.util.logDebug
import com.piooda.recycrew.core.util.logError
import com.piooda.recycrew.core.util.showToastShort
import com.piooda.recycrew.databinding.FragmentCalendarBinding
import com.piooda.recycrew.feature.event.viewmodel.AttendanceCheckViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AttendanceCheckFragment :
    BaseFragment<FragmentCalendarBinding>(FragmentCalendarBinding::inflate) {
    private val viewModel by viewModels<AttendanceCheckViewModel> {
        ViewModelFactory(requireContext())
    }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(requireContext(),
                getString(R.string.access_notification_permission), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(),
                getString(R.string.denied_notification_permission), Toast.LENGTH_SHORT).show()
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentCalendarBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        collectGetAttendanceState()
        collectSaveAttendanceState()
        moveToMyPage()
        requestNotificationPermissionIfNeeded() // 알림 권한 요청

        // FirebaseAuth 인스턴스 가져오기
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser != null) {
            val email = currentUser.email ?: return
            val todayDate = getTodayDate()

            setButtonClickListener(todayDate, email)

            viewModel.getAttendances(email) // 특정 사용자 데이터 요청
        } else {
            Toast.makeText(requireContext(), getString(R.string.need_login), Toast.LENGTH_SHORT).show()
        }
    }

    // 알림 권한 요청 함수
    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // 오늘 날짜를 가져오는 함수
    private fun getTodayDate(): String {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }

    // 출석 저장 버튼 리스너
    private fun setButtonClickListener(todayDate: String, email: String) {
        binding.attendanceCheckBtn.setOnClickListener {
            viewModel.saveAttendance(
                email,
                todayDate,
                isChecked = true,
                rewardGiven = true
            ) // 출석 저장
        }
    }


    private fun collectSaveAttendanceState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.saveAttendanceState.collectLatest { state ->
                when (state) {
                    is UiState.Loading -> {
                        // 로그와 로딩 중 메시지
                        logDebug("SaveAttendance", R.string.saving_attendance)
                    }

                    is UiState.Success -> {
                        logDebug("SaveAttendance", R.string.success_attendance)
                    }

                    is UiState.Error -> {
                        // 저장 실패 시 로그와 에러 메시지
                        logError("SaveAttendance", R.string.failure_attendance, state.exception)
                        showToastShort(R.string.failure_attendance, state.exception.message ?: "")
                    }

                    is UiState.Empty -> {
                        // 기본 상태: UI 갱신 필요 없음
                        logDebug("SaveAttendance", R.string.nothing_to_attendance)
                    }
                }
            }
        }
    }

    // ViewModel에서 데이터를 가져와 달력에 반영
    private fun collectGetAttendanceState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.attendanceState.collectLatest { state ->
                when (state) {
                    is UiState.Loading -> {
                        logDebug("AttendanceCheck", R.string.loading_attendance_data)
                    }

                    is UiState.Success -> {
                        logDebug("AttendanceCheck", R.string.success_get_attendance_data)
                        updateCalendar(state.resultData)
                    }

                    is UiState.Error -> {
                        logDebug("AttendanceCheck", R.string.failure_get_attendance_data)
                        showToastShort(R.string.failure_get_attendance_data, state.exception.message ?: "")
                    }

                    is UiState.Empty -> {
                        logDebug("AttendanceCheck", R.string.empty_attendance_data)
                    }
                }
            }
        }
    }


    // 달력에 출석 데이터 표시
    private fun updateCalendar(attendances: List<Attendance>) {
        val eventDays = mutableListOf<EventDay>()
        attendances.forEach {
            val calendar = Calendar.getInstance().apply {
                time = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it.date)!!
            }
            val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.checked)
            if (drawable != null) {
                eventDays.add(EventDay(calendar, drawable))
            }
        }
        binding.calendarView.setEvents(eventDays)
    }

    private fun moveToMyPage() {
        binding.myPageBtn.setOnClickListener {
            findNavController().navigate(R.id.action_attendanceCheckFragment_to_myPageFragment)
        }
    }
}
