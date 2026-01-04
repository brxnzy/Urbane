package com.example.urbane.ui.admin.settings.model

import com.example.urbane.data.model.AuditLog

data class AuditLogsState(
    val isLoading: Boolean = false,
    val logs: List<AuditLog> = emptyList(),
    val errorMessage: String? = null
)
