package com.example.urbane.ui.admin.fines.model

import com.example.urbane.data.model.Fine
import com.example.urbane.data.model.User

data class FinesState(
    val isLoading: Boolean = false,
    val residents: List<User> = emptyList(),
    val title: String = "",
    val description: String = "",
    val amount: String = "",
    val selectedResidentId: String? = null,
    val fines: List<Fine> = emptyList(),
    val errorMessage: String? = null,
    val success: FinesSuccessType? = null
)

sealed class FinesSuccessType {
    object FineCreated : FinesSuccessType()
}

sealed class FinesIntent {

    data class TitleChanged(val value: String) : FinesIntent()
    data class DescriptionChanged(val value: String) : FinesIntent()
    data class AmountChanged(val value: String) : FinesIntent()
    data class ResidentSelected(val residentId: String) : FinesIntent()

    object CreateFine : FinesIntent()
    object ClearSuccess : FinesIntent()
}
