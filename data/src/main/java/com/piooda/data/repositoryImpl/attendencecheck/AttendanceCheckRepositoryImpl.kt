package com.piooda.data.repositoryImpl.attendencecheck


import android.util.Log
import com.piooda.data.repository.attendencecheck.AttendanceCheckRepository
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.piooda.data.model.Attendance
import com.piooda.UiState
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow


class AttendanceCheckRepositoryImpl(private val firestore: FirebaseFirestore) :
    AttendanceCheckRepository {

    override fun saveAttendance(
        email: String,
        date: String,
        isChecked: Boolean,
        rewardGiven: Boolean
    ): Flow<UiState<Unit>> = callbackFlow {
        trySend(UiState.Loading)

        val attendance = hashMapOf(
            "date" to date,
            "isChecked" to isChecked,
            "rewardGiven" to rewardGiven
        )

        val attendanceDoc = firestore.collection("users")
            .document(email)
            .collection("attendances")
            .document(date)

        val userDoc = firestore.collection("users")
            .document(email)

        firestore.runTransaction { transaction ->
            val document = transaction.get(attendanceDoc)
            if (document.exists()) {
                throw Exception("이미 출석 처리된 날짜입니다.")
            } else {
                // 출석 정보 저장
                transaction.set(attendanceDoc, attendance)

                // 포인트 업데이트
                transaction.update(userDoc, "point", FieldValue.increment(100))
            }
        }.addOnSuccessListener {
            Log.d("AttendanceRepository", "출석 정보 저장 성공: 날짜 = $date, 포인트 +100")
            trySend(UiState.Success(Unit))
            close()
        }.addOnFailureListener { e ->
            Log.e("AttendanceRepository", "출석 정보 저장 실패: ${e.localizedMessage}", e)
            trySend(UiState.Error(e))
            close()
        }

        awaitClose()
    }



    override fun getAttendances(email: String): Flow<UiState<List<Attendance>>> = callbackFlow {
        trySend(UiState.Loading)
        val listenerRegistration = firestore.collection("users")
            .document(email)
            .collection("attendances")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    trySend(UiState.Error(e))
                    close(e) // 예외 발생 시 Flow 종료
                    return@addSnapshotListener
                }

                val attendances = snapshot?.documents?.map { doc ->
                    Attendance(
                        email = doc.id,
                        date = doc.getString("date") ?: "",
                        isChecked = doc.getBoolean("isChecked") ?: false,
                        rewardGiven = doc.getBoolean("rewardGiven") ?: false
                    )
                } ?: emptyList()

                trySend(UiState.Success(attendances)) // Flow에 데이터 전송
            }
        awaitClose { listenerRegistration.remove() } // Listener 종료
    }
}