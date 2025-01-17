package com.piooda.recycrew.feature.event.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.piooda.data.model.Attendance
import com.piooda.data.repository.attendencecheck.AttendanceCheckRepository
import com.piooda.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class AttendanceCheckViewModel(private val attendanceCheckRepository: AttendanceCheckRepository) :
    ViewModel() {
    private val _saveAttendanceState = MutableStateFlow<UiState<Unit>>(UiState.Loading)
    val saveAttendanceState get() = _saveAttendanceState.asStateFlow()

    private val _attendanceState = MutableStateFlow<UiState<List<Attendance>>>(UiState.Loading)
    val attendanceState get() = _attendanceState.asStateFlow()

    fun saveAttendance(email: String, date: String, isChecked: Boolean, rewardGiven: Boolean) {
        viewModelScope.launch {
            attendanceCheckRepository.saveAttendance(email, date, isChecked, rewardGiven)
                .flowOn(Dispatchers.IO)
                .collectLatest { state ->
                    _saveAttendanceState.value = state
                }
        }
    }

    fun getAttendances(email: String) {
        viewModelScope.launch {
            attendanceCheckRepository.getAttendances(email).flowOn(Dispatchers.IO)
                .collectLatest { state ->
                    _attendanceState.value = state
                }
        }
    }
}