package com.example.vozhatapp.presentation.children.edit.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.vozhatapp.R

@Composable
fun FormButtonsSection(
    isEditMode: Boolean,
    isFormValid: Boolean,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    formProgress: Float,
    modifier: Modifier = Modifier
) {
    val saveButtonResId = if (isEditMode) R.string.update_child else R.string.save_child

    Column(modifier = modifier) {
        // Save/Update button
        Button(
            onClick = onSave,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
                .graphicsLayer {
                    alpha = formProgress
                    translationY = (1f - formProgress) * 100
                }
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = RoundedCornerShape(16.dp),
            enabled = isFormValid
        ) {
            Icon(
                imageVector = if (isEditMode) Icons.Default.Edit else Icons.Default.Save,
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(
                text = stringResource(saveButtonResId),
                style = MaterialTheme.typography.titleMedium
            )
        }

        // Show deletion option only in edit mode
        if (isEditMode) {
            OutlinedButton(
                onClick = onDelete,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .height(56.dp)
                    .graphicsLayer {
                        alpha = formProgress
                        translationY = (1f - formProgress) * 100
                    },
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = stringResource(R.string.delete_child),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}