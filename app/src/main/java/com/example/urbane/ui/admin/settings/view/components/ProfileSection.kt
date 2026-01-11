package com.example.urbane.ui.admin.settings.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.urbane.ui.auth.model.CurrentUser

@Composable
fun ProfileSection(user: CurrentUser?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* TODO */ }
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        if (user?.userData?.user?.photoUrl != null) {
            AsyncImage(
                model = user.userData.user.photoUrl,
                contentDescription = "Foto de perfil",
                modifier = Modifier
                    .size(70.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = null,
                modifier = Modifier.size(76.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.width(16.dp).background(MaterialTheme.colorScheme.surface))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = user?.userData?.user?.name ?: "",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = user?.userData?.user?.email ?: "",
                fontSize = 14.sp,
            )
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color.Gray
        )
    }

    Divider(color = Color(0xFFE0E0E0))
}


