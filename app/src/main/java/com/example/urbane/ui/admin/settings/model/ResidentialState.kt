package com.example.urbane.ui.admin.settings.model

import com.example.urbane.data.model.Residential


data class ResidentialState(
    val residentials: List<Residential> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showBottomSheet: Boolean = false,
    val isEditMode: Boolean = false,
    val selectedResidential: Residential? = null
)

// Intents
sealed class ResidentialIntent {
    data object LoadResidentials : ResidentialIntent()
    data object ShowCreateSheet : ResidentialIntent()
    data class ShowEditSheet(val residential: Residential) : ResidentialIntent()
    data object DismissBottomSheet : ResidentialIntent()
    data class UpdateResidential(val residential: Residential) : ResidentialIntent()
    data class CreateResidential(
        val name: String,
        val address: String?,
        val phone: String?,
        val logoUrl: String?
    ) : ResidentialIntent()
    data class RemoveResidential(val residentialId: Int) : ResidentialIntent()
    data object DismissError : ResidentialIntent()
}