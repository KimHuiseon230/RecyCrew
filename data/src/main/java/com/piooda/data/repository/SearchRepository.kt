package com.piooda.data.repository

import android.content.Context
import com.piooda.data.model.Content
import kotlinx.coroutines.flow.Flow

interface SearchRepository {
    fun saveSearchHistory(query: String, context: Context)
    fun deleteSearchHistory(query: String, context: Context)
    fun getSearchHistory(context: Context): List<String>
    fun searchContentRealtime(query: String): Flow<List<Content>>
}