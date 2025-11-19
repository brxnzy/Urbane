package com.example.urbane.ui.admin.users.view.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.urbane.R

@Composable
fun EnableDialog(goBack: () -> Unit, onDismiss: () -> Unit){
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
        },
        title = { Text(stringResource(R.string.usuario_habilitado)) },
        text = {
            Text(
                "Usuario habilitado correctamente"
            )
        },
        confirmButton = {
            TextButton(onClick = {
                onDismiss
                goBack()

            }) {
                Text(stringResource(R.string.aceptar))
            }
        }
    )
}


@Composable
fun EditDialog(goBack: () -> Unit, onDismiss: () -> Unit){
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
        },
        title = { Text(stringResource(R.string.usuario_editado)) },
        text = {
            Text(
                stringResource(R.string.usuario_editado_correctamente)
            )
        },
        confirmButton = {
            TextButton(onClick = {
                onDismiss
                goBack()

            }) {
                Text(stringResource(R.string.aceptar))
            }
        }
    )
}


@Composable
fun DisabledDialog(goBack: () -> Unit, onDismiss: () -> Unit){
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
        },
        title = { Text(stringResource(R.string.usuario_deshabilitado)) },
        text = {
            Text(
                stringResource(R.string.usuario_deshabilitado_correctamente)
            )
        },
        confirmButton = {
            TextButton(onClick = {
                onDismiss
                goBack()

            }) {
                Text(stringResource(R.string.aceptar))
            }
        }
    )
}