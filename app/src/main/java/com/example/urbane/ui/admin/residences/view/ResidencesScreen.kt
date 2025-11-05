package com.example.urbane.ui.admin.residences.view

import android.annotation.SuppressLint
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.example.urbane.R
import com.example.urbane.navigation.Routes


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ResidencesScreen(navController: NavController, modifier: Modifier= Modifier) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Routes.ADMIN_RESIDENCES_ADD) },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, tint = Color.White, contentDescription = "Agregar usuario")
            }
        },
    ){
    Column (modifier=modifier){

    Text(stringResource(R.string.residencias))
    }
}

    }
