package com.piooda.data.repositoryImpl

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.piooda.data.model.Content
import com.piooda.data.repository.SearchRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class SearchRepositoryImpl(private val db: FirebaseFirestore) : SearchRepository {

    override fun searchContentRealtime(query: String) = callbackFlow {
        Log.d("SearchRepository", "🔥 Firestore 검색 실행: $query")

        val listener = db.collection("content")
            .whereArrayContains("searchIndex", query.lowercase()) //  정확한 배열 검색 적용
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("SearchRepository", "❌ Firestore 검색 오류: ${error.message}")
                    close(error)
                    return@addSnapshotListener
                }

                val contents = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Content::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                Log.d("SearchRepository", "" +
                        "Firestore 검색 결과 개수: ${contents.size}")
                trySend(contents).isSuccess
            }

        awaitClose { listener.remove() }
    }


    @SuppressLint("MutatingSharedPrefs")
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

    // 유저 검색 (닉네임 또는 userId 포함)
    override fun searchUsers(query: String): Flow<List<Content>> = flow {
        val users = db.collection("users")
            .orderBy("nickname", Query.Direction.ASCENDING)
            .startAt(query)
            .endAt(query + "\uf8ff")
            .get()
            .await()
            .documents
            .mapNotNull { it.toObject(Content::class.java) }

        emit(users)
    }

    override fun searchAll(query: String): Flow<List<Content>> = callbackFlow {
        val searchResults = mutableListOf<Content>() // 🔹 `List<Any>` → `List<Content>`로 변경

        val postListener = db.collection("posts")
            .whereGreaterThanOrEqualTo("searchIndex", query)
            .whereLessThanOrEqualTo("searchIndex", query + "\uf8ff")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    close(e)
                    return@addSnapshotListener
                }

                val posts = snapshot?.documents?.mapNotNull { it.toObject(Content::class.java) }
                    ?: emptyList()
                searchResults.addAll(posts) // 🔥 게시글(`Content`)만 추가
                trySend(searchResults) // 🔥 이제 Flow<List<Content>>를 반환
            }

        awaitClose {
            postListener.remove()
        }
    }


}
