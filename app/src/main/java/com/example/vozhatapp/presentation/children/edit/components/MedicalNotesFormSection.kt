package com.example.vozhatapp.presentation.children.edit.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.example.vozhatapp.R

@Composable
fun MedicalNotesFormSection(
    medicalNotes: String,
    onMedicalNotesChange: (String) -> Unit,
    formProgress: Float,
    formOffset: Int
) {
    val focusManager = LocalFocusManager.current

    FormSection(
        title = stringResource(R.string.medical_information),
        icon = Icons.Outlined.HealthAndSafety,
        formProgress = formProgress,
        offset = formOffset
    ) {
        OutlinedTextField(
            value = medicalNotes,
            onValueChange = onMedicalNotesChange,
            label = { Text(stringResource(R.string.medical_notes)) },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.MedicalServices,
                    contentDescription = null,
                    modifier = Modifier.padding(bottom = 40.dp)
                )
            },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            ),
            supportingText = { Text(stringResource(R.string.optional)) }
        )

        // Medical notes tips
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(end = 12.dp)
                )
                Text(
                    text = stringResource(R.string.medical_notes_info),
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}