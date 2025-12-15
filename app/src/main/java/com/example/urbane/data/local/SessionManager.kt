package com.example.urbane.data.local

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.urbane.data.model.UserResidentialRole
import com.example.urbane.ui.auth.model.CurrentUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SessionManager(context: Context) {

    private val dataStore = context.authDataStore

    private val USER_ID_KEY = stringPreferencesKey("userId")
    private val EMAIL_KEY = stringPreferencesKey("email")
    private val ROLE_KEY = stringPreferencesKey("role")
    private val TOKEN_KEY = stringPreferencesKey("accessToken")
    private val REFRESH_KEY = stringPreferencesKey("refreshToken")
    private val USER_DATA = stringPreferencesKey("userData")

    @RequiresApi(Build.VERSION_CODES.P)
    suspend fun saveSession(user: CurrentUser) {
        dataStore.edit { prefs ->
            prefs[USER_ID_KEY] = user.userId
            prefs[EMAIL_KEY] = user.email
            prefs[ROLE_KEY] = user.roleId.toString()
            prefs[TOKEN_KEY] = user.accessToken
            prefs[REFRESH_KEY] = user.refreshToken
            prefs[USER_DATA] = user.userData?.let { Json.encodeToString<UserResidentialRole>(it) } ?: ""
        }
    }

    val sessionFlow: Flow<CurrentUser?> = dataStore.data.map { prefs ->
        val userId = prefs[USER_ID_KEY]
        val email = prefs[EMAIL_KEY]
        val roleId = prefs[ROLE_KEY]
        val access = prefs[TOKEN_KEY]
        val refresh = prefs[REFRESH_KEY]
        val userDataJson = prefs[USER_DATA]

        if (userId != null && email != null && roleId != null && access != null && refresh != null) {
            val userData = if (!userDataJson.isNullOrEmpty())
                Json.decodeFromString<UserResidentialRole>(userDataJson)
            else null

            CurrentUser(userId, email, access, refresh, roleId, userData)
        } else null
    }

    val userIdFlow: Flow<String?> = dataStore.data.map { prefs ->
        prefs[USER_ID_KEY]
    }

    val residentialIdFlow: Flow<Int> = dataStore.data.map { prefs ->
        val userDataJson = prefs[USER_DATA]
        if (!userDataJson.isNullOrEmpty()) {
            try {
                val userData = Json.decodeFromString<UserResidentialRole>(userDataJson)
                // Obtener el id del objeto residential
                userData.residential.id ?: 0
            } catch (e: Exception) {
                0
            }
        } else {
            0
        }
    }

    suspend fun clearSession() {
        dataStore.edit { it.clear() }
    }

}