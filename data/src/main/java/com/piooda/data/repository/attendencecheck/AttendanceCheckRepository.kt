package com.piooda.data.repository.attendencecheck

import com.piooda.data.model.Attendance
import com.piooda.UiState
import kotlinx.coroutines.flow.Flow

interface AttendanceCheckRepository {
    fun saveAttendance(email: String, date: String, isChecked: Boolean, rewardGiven: Boolean): Flow<UiState<Unit>>
    fun getAttendances(email: String): Flow<UiState<List<Attendance>>>
}