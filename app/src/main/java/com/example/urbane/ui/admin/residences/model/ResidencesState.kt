package com.example.urbane.ui.admin.residences.model

import com.example.urbane.data.model.Residence

data class ResidencesState(
    val name: String = "",
    val type: String = "",
    val description: String ="",
    val residences: List<Residence> = emptyList(),
    val availableResidences: List<Residence> = emptyList(),
    val isLoading: Boolean = false,
    val success: Boolean = false,
    val errorMessage: String? = null,
)


sealed class ResidencesIntent{
    data class NameChanged(val name:String) : ResidencesIntent()
    data class TypeChanged(val type:String ): ResidencesIntent()
    data class DescriptionChanged(val description: String): ResidencesIntent()
    data object CreateResidence: ResidencesIntent()
}


