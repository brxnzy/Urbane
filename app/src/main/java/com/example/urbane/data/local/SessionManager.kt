package com.example.urbane.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.urbane.ui.auth.model.CurrentUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SessionManager(context: Context) {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth")
    private val dataStore = context.dataStore

    private val USER_ID_KEY = stringPreferencesKey("userId")
    private val EMAIL_KEY = stringPreferencesKey("email")
    private val ROLE_KEY = stringPreferencesKey("role")
    private val TOKEN_KEY = stringPreferencesKey("accessToken")
    private val REFRESH_KEY = stringPreferencesKey("refreshToken")

    suspend fun saveSession(user: CurrentUser) {
        dataStore.edit { prefs ->
            prefs[USER_ID_KEY] = user.userId
            prefs[EMAIL_KEY] = user.email
            prefs[ROLE_KEY] = user.role
            prefs[TOKEN_KEY] = user.accessToken
            prefs[REFRESH_KEY] = user.refreshToken
        }
    }

    val sessionFlow: Flow<CurrentUser?> = dataStore.data.map { prefs ->
        val userId = prefs[USER_ID_KEY]
        val email = prefs[EMAIL_KEY]
        val role = prefs[ROLE_KEY]
        val access = prefs[TOKEN_KEY]
        val refresh = prefs[REFRESH_KEY]

        if (userId != null && email != null && role != null && access != null && refresh != null) {
            CurrentUser(userId, email, access, refresh, role)
        } else null
    }

    suspend fun clearSession() {
        dataStore.edit { it.clear() }
    }
}
