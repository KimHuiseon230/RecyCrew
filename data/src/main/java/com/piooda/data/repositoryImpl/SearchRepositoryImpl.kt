package com.piooda.data.repository

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.piooda.data.model.Content
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class SearchRepositoryImpl(private val db: FirebaseFirestore) : SearchRepository {
    override fun searchContentRealtime(query: String): Flow<List<Content>> = callbackFlow {
        Log.d("SearchRepository", "🔥 Firestore 검색 실행: $query")

        val listener = db.collection("content")
            .whereArrayContains("searchIndex", query.lowercase())  // ✅ 검색어 포함된 게시물 찾기
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("SearchRepository", "❌ Firestore 검색 오류: ${error.message}")
                    close(error)
                    return@addSnapshotListener
                }

                val contents = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Content::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                Log.d("SearchRepository", "✅ Firestore 검색 결과 개수: ${contents.size}")
                trySend(contents).isSuccess
            }

        awaitClose { listener.remove() }
    }

    override fun saveSearchHistory(query: String, context: Context) {
        val sharedPreferences = context.getSharedPreferences("search_history", Context.MODE_PRIVATE)
        val history = sharedPreferences.getStringSet("history", mutableSetOf()) ?: mutableSetOf()

        if (!history.contains(query)) {
            history.add(query)
            sharedPreferences.edit().putStringSet("history", history).apply()
        }
    }

    override fun getSearchHistory(context: Context): List<String> {
        val sharedPreferences = context.getSharedPreferences("search_history", Context.MODE_PRIVATE)
        return sharedPreferences.getStringSet("history", emptySet())?.toList() ?: emptyList()
    }


    override fun deleteSearchHistory(query: String, context: Context) {
        Log.d("SearchRepository", "🗑 검색 기록 삭제: $query")
        val sharedPreferences = context.getSharedPreferences("search_history", Context.MODE_PRIVATE)
        val history = getSearchHistory(context).toMutableList()
        history.remove(query)  // 🔹 검색 기록 삭제
        sharedPreferences.edit().putStringSet("history", history.toSet()).apply()  // 🔹 저장
        Log.d("SearchRepository", "📜 삭제 후 검색 기록: $history")
    }

}
