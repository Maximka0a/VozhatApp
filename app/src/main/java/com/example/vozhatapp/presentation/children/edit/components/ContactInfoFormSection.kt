package com.example.vozhatapp.presentation.children.edit.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.vozhatapp.R

@Composable
fun ContactInfoFormSection(
    parentPhone: String,
    parentEmail: String,
    address: String,
    parentPhoneError: String?,
    parentEmailError: String?,
    onParentPhoneChange: (String) -> Unit,
    onParentEmailChange: (String) -> Unit,
    onAddressChange: (String) -> Unit,
    formProgress: Float,
    formOffset: Int
) {
    val focusManager = LocalFocusManager.current

    FormSection(
        title = stringResource(R.string.contact_info),
        icon = Icons.Outlined.ContactPhone,
        formProgress = formProgress,
        offset = formOffset
    ) {
        // Parent phone field
        OutlinedTextField(
            value = parentPhone,
            onValueChange = onParentPhoneChange,
            label = { Text(stringResource(R.string.parent_phone)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Phone,
                    contentDescription = null
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            isError = parentPhoneError != null,
            supportingText = { parentPhoneError?.let { Text(it) } },
            singleLine = true
        )

        // Parent email field
        OutlinedTextField(
            value = parentEmail,
            onValueChange = onParentEmailChange,
            label = { Text(stringResource(R.string.parent_email)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Email,
                    contentDescription = null
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            isError = parentEmailError != null,
            supportingText = { parentEmailError?.let { Text(it) } },
            singleLine = true
        )

        // Address field
        OutlinedTextField(
            value = address,
            onValueChange = onAddressChange,
            label = { Text(stringResource(R.string.address)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Home,
                    contentDescription = null
                )
            },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            ),
            maxLines = 3,
            supportingText = { Text(stringResource(R.string.optional)) }
        )
    }
}