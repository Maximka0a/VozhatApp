package com.example.vozhatapp.presentation.childprofile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.vozhatapp.data.local.entity.Child

@Composable
fun ProfileHeaderStats(
    child: Child,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.height(210.dp)) {
        // Background gradient for header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                )
        )

        // Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile photo
            ProfilePhoto(child = child)

            // Age and Squad
            ChildBasicInfo(child = child)

            // Parent contact info
            if (!child.parentPhone.isNullOrBlank() || !child.parentEmail.isNullOrBlank()) {
                ParentContactInfo(
                    phone = child.parentPhone,
                    email = child.parentEmail
                )
            }
        }
    }
}

@Composable
fun ProfilePhoto(
    child: Child,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(vertical = 8.dp)
            .size(100.dp)
            .clip(CircleShape)
            .border(3.dp, MaterialTheme.colorScheme.surface, CircleShape)
    ) {
        if (child.photoUrl != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(child.photoUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Фото ${child.fullName}",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${child.name.first()}${child.lastName.first()}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
fun ChildBasicInfo(
    child: Child,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${child.age} лет",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onPrimary
        )
        Text(
            text = " • ",
            color = MaterialTheme.colorScheme.onPrimary
        )
        Text(
            text = child.squadName,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}

@Composable
fun ParentContactInfo(
    phone: String?,
    email: String?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!phone.isNullOrBlank()) {
            Icon(
                imageVector = Icons.Outlined.Phone,
                contentDescription = "Телефон родителя",
                tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = " $phone",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
            )
        }

        if (!phone.isNullOrBlank() && !email.isNullOrBlank()) {
            Text(
                text = " • ",
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
            )
        }

        if (!email.isNullOrBlank()) {
            Icon(
                imageVector = Icons.Outlined.Email,
                contentDescription = "Email родителя",
                tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = " $email",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
            )
        }
    }
}