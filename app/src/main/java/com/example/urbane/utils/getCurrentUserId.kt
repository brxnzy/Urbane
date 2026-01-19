package com.example.urbane.utils
import com.example.urbane.data.local.SessionManager
import kotlinx.coroutines.flow.firstOrNull


suspend fun getCurrentUserId(sessionManager: SessionManager): String {
    val user = sessionManager.sessionFlow.firstOrNull()
    return user?.userData?.user?.id!!
}
