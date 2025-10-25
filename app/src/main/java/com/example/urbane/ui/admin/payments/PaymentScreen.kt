package com.example.urbane.ui.admin.payments

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.res.stringResource
import com.example.urbane.R


@Composable
fun PaymentsScreen(modifier: Modifier) {
    Column(modifier=modifier) {

    Text(stringResource(R.string.pagos))
    }
}