package com.example.urbane.ui.admin.residences.model

import com.example.urbane.data.model.Residence
import com.example.urbane.data.model.User

// Estado corregido
data class ResidencesState(
    val name: String = "",
    val type: String = "",
    val description: String = "",
    val residences: List<Residence> = emptyList(),
    val availableOwners: List<User> = emptyList(),
    val selectedOwnerId: String? = "",
    val ownerName: String = "",
    val selectedResidence: Int? = null,
    val ownerEmail: String = "",
    val ownerIdCard: String = "",
    val ownerPassword: String = "",
    val ownerId: String? = null,
    val isLoading: Boolean = false,
    val isLoadingOwners: Boolean = false,
    val success: SuccessType? = null,
    val errorMessage: String? = null
)

sealed class SuccessType {
    object ResidenceCreated : SuccessType()
    object PropietarioAssigned : SuccessType()
    object ResidenceUpdated : SuccessType()
}

// Intents corregidos
sealed class ResidencesIntent {
    data class NameChanged(val name: String) : ResidencesIntent()
    data class TypeChanged(val type: String) : ResidencesIntent()
    data class DescriptionChanged(val description: String) : ResidencesIntent()
    data class OwnerNameChanged(val ownerName: String) : ResidencesIntent()
    data class OwnerEmailChanged(val ownerEmail: String) : ResidencesIntent()
    data class OwnerIdCardChanged(val ownerIdCard: String) : ResidencesIntent()
    data class OwnerPasswordChanged(val ownerPassword: String) : ResidencesIntent()
    object CreateResidence : ResidencesIntent()
    object CreateOwner : ResidencesIntent()
    data class AssignPropietario(val propietario: User) : ResidencesIntent()
    data class SelectOwner(val ownerId: String) : ResidencesIntent() // ✅ Añadido
    object ClearSelectedOwner : ResidencesIntent() // ✅ Añadido
    object LoadOwners : ResidencesIntent() // ✅ Añadido
}


