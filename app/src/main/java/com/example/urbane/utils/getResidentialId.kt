package com.example.urbane.utils

import android.annotation.SuppressLint
import androidx.compose.ui.platform.LocalContext
import com.example.urbane.data.local.SessionManager
import kotlinx.coroutines.flow.firstOrNull

suspend fun getResidentialId(sessionManager: SessionManager): Int? {
    val user = sessionManager.sessionFlow.firstOrNull()
    return user?.userData?.residential?.id
}

