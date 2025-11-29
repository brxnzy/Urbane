package com.example.urbane.ui.admin.residences.model

import com.example.urbane.data.model.Residence

data class ResidencesDetailState(
    val isLoading: Boolean = false,
    val success: ResidencesDetailSuccess? = null,
    val errorMessage: String? = null,
    val residence: Residence? = null,
    val userId : String? = null
)


sealed class ResidencesDetailIntent {
//    data object DisableResidence : ResidencesDetailIntent()
//    data object EnableResidence: ResidencesDetailIntent()


}


sealed class ResidencesDetailSuccess {
    object ResidenceEdited : ResidencesDetailSuccess()


}
