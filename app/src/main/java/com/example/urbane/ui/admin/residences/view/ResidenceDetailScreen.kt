package com.example.urbane.ui.admin.residences.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.urbane.ui.admin.residences.viewmodel.ResidencesDetailViewModel
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column

import androidx.compose.foundation.layout.padding

import androidx.compose.material3.CircularProgressIndicator

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults

import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import coil.compose.AsyncImage
import com.example.urbane.R
import com.example.urbane.data.local.SessionManager
import com.example.urbane.data.model.User
import com.example.urbane.ui.admin.residences.viewmodel.ResidencesViewModel
import com.example.urbane.ui.admin.users.model.DetailSuccess
import com.example.urbane.ui.admin.users.model.UsersDetailIntent
import com.example.urbane.ui.admin.users.view.components.DisabledDialog
import com.example.urbane.ui.admin.users.view.components.EnableDialog
import com.example.urbane.ui.admin.users.view.components.EnableResidentDialog
import com.example.urbane.ui.admin.users.view.components.InfoSection
import com.example.urbane.ui.admin.users.view.components.UserInfoItem
import com.example.urbane.ui.admin.users.viewmodel.UsersViewModel
import com.example.urbane.utils.getRoleLabelRes
import com.example.urbane.utils.getTipoPropiedadLabelRes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResidencesDetailScreen(
    residenceId: Int,
    viewmodel: ResidencesDetailViewModel,
    goBack: () -> Unit
) {
    val state by viewmodel.state.collectAsState()

    LaunchedEffect(residenceId) {
        viewmodel.loadResidence(residenceId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de Residencia") },
                navigationIcon = {
                    IconButton(onClick = goBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                        )
            )
        }
    ) { padding ->

        when {
            state.isLoading -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            state.errorMessage != null -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Error al cargar los datos")
                }
            }

            state.residence != null -> {
                val r = state.residence

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    DetailItem("Nombre", r?.name ?: "")
                    DetailItem("Tipo", r?.type ?: "")
                    DetailItem("Descripción", r?.description ?: "")
                    DetailItem("Estado", if (r?.available == true) "Disponible" else "Ocupada")
                    DetailItem("ID del residencial", r?.residentialId.toString())
                    DetailItem("Propietario (ownerId)", r?.ownerId ?: "Sin propietario")
                    DetailItem("Residente (residentId)", r?.residentId ?: "Sin residente")
                }
            }
        }
    }
}

@Composable
 fun DetailItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
    }
}
