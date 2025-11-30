package com.example.urbane.ui.admin.residences.model

import com.example.urbane.data.model.Residence

data class ResidencesDetailState(
    val isLoading: Boolean = false,
    val success: ResidencesDetailSuccess? = null,
    val errorMessage: String? = null,
    val residence: Residence? = null,
    val userId: String? = null,
    val editedName: String = "",
    val editedType: String = "",
    val editedDescription: String = "",
    val originalName: String = "",
    val originalType: String = "",
    val originalDescription: String = ""
) {
    val hasChanges: Boolean
        get() = editedName != originalName ||
                editedType != originalType ||
                editedDescription != originalDescription
}

sealed class ResidencesDetailIntent {
    data class EditResidence(
        val id: Int,
        val name: String,
        val type: String,
        val description: String
    ) : ResidencesDetailIntent()

    data class DeleteResidence(val id: Int) : ResidencesDetailIntent()

    data class VacateResidence(
        val id: Int,
        val residentId: String
    ) : ResidencesDetailIntent()

    data class UpdateName(val name: String) : ResidencesDetailIntent()
    data class UpdateType(val type: String) : ResidencesDetailIntent()
    data class UpdateDescription(val description: String) : ResidencesDetailIntent()
}

sealed class ResidencesDetailSuccess {
    object ResidenceEdited : ResidencesDetailSuccess()
    object ResidenceDeleted : ResidencesDetailSuccess()
    object ResidenceVacated : ResidencesDetailSuccess()
}