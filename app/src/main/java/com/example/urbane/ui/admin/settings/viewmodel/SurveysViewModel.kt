package com.example.urbane.ui.admin.settings.viewmodel

import androidx.lifecycle.ViewModel
import com.example.urbane.ui.admin.settings.model.SurveysState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SurveysViewModel() : ViewModel() {
    private val _state = MutableStateFlow(SurveysState())
    val state: StateFlow<SurveysState> = _state.asStateFlow()


}