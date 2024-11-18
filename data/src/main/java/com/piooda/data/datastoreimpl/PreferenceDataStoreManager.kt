package com.piooda.data.datastoreimpl

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import com.piooda.data.datastore.ICRUDPreferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore


private val Context.jetpackDataStore by preferencesDataStore(
    name = "my_app_preference_datastore"
)

class PreferenceDataStoreManager(context: Context) : ICRUDPreferencesDataStore {
    private val dataSource = context.jetpackDataStore

    override suspend fun <T> readPreference(key: Preferences.Key<T>, defaultValue: T): Flow<T> =
        dataSource.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                val result = preferences[key] ?: defaultValue
                result
            }

    override suspend fun <T> createPreference(key: Preferences.Key<T>, value: T) {
        dataSource.edit { preferences ->
            preferences[key] = value
        }
    }

    override suspend fun <T> deletePreference(key: Preferences.Key<T>) {
        dataSource.edit { preferences ->
            preferences.remove(key)
        }
    }

    override suspend fun <T> clearAllPreference() {
        dataSource.edit { preferences ->
            preferences.clear()
        }
    }
}
