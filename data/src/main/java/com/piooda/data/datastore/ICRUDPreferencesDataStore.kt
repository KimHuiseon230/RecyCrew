package com.piooda.data.datastore

import androidx.datastore.preferences.core.Preferences.*
import kotlinx.coroutines.flow.Flow

interface ICRUDPreferencesDataStore{
    suspend fun <T> readPreference(key: Key<T>, defaultValue: T): Flow<T>
    suspend fun <T> createPreference(key: Key<T>, value:T)
    suspend fun <T> deletePreference(key: Key<T>)
    suspend fun <T> clearAllPreference()
}