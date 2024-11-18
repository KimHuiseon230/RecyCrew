package com.piooda.data.model


data class Attendance(
    val date: String = "",       // 출석 날짜
    val email: String = "",     // 사용자 ID
    val isChecked: Boolean = false, // 출석 체크 여부
    val rewardGiven: Boolean = false // 보상 지급 여부
)
