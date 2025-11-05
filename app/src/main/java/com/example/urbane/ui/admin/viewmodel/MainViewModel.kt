package com.example.urbane.ui.admin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.urbane.data.local.SessionManager
import com.example.urbane.data.model.UserResidentialRole
import com.example.urbane.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel(val sessionManager: SessionManager): ViewModel() {
    private val _userData = MutableStateFlow<UserResidentialRole?>(null)
    val userData: StateFlow<UserResidentialRole?> = _userData




}