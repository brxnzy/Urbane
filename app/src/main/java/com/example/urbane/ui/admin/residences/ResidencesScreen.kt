package com.example.urbane.ui.admin.residences

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.res.stringResource
import com.example.urbane.R


@Composable
fun ResidencesScreen(modifier: Modifier) {
    Column (modifier=modifier){

    Text(stringResource(R.string.residencias))
    }
}