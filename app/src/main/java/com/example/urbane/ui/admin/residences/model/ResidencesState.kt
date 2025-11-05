package com.example.urbane.ui.admin.residences.model

import android.net.Uri

data class ResidencesState(
    val name : String = "",
    val type : String = "",
    val description: String ="",
    val image: Uri? = null,
    val ownerId: String? = null,
    val isLoading : Boolean = false,
    val success : Boolean = false,
    val errorMessage: String? = null,
)


sealed class ResidencesIntent{
    data class NameChanged(val name:String) : ResidencesIntent()
    data class TypeChanged(val type:String ): ResidencesIntent()
    data class DescriptionChanged(val description: String): ResidencesIntent()
    data object CreateResidence: ResidencesIntent()
}


