package com.example.vozhatapp.presentation.children.edit.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.vozhatapp.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BasicInfoFormSection(
    name: String,
    lastName: String,
    age: Int,
    squadName: String,
    nameError: String?,
    lastNameError: String?,
    ageError: String?,
    squadNameError: String?,
    squadNames: List<String>,
    isSquadMenuExpanded: Boolean,
    onNameChange: (String) -> Unit,
    onLastNameChange: (String) -> Unit,
    onAgeChange: (String) -> Unit,
    onSquadNameChange: (String) -> Unit,
    onSquadMenuExpandedChange: (Boolean) -> Unit,
    onAddNewSquad: () -> Unit,
    focusRequesters: ChildFormFocusRequesters,
    formProgress: Float,
    formOffset: Int
) {
    FormSection(
        title = stringResource(R.string.basic_info),
        icon = Icons.Outlined.Person,
        formProgress = formProgress,
        offset = formOffset
    ) {
        // Name field
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text(stringResource(R.string.name)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .focusRequester(focusRequesters.nameFocusRequester),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.AccountCircle,
                    contentDescription = null
                )
            },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusRequesters.lastNameFocusRequester.requestFocus() }
            ),
            isError = nameError != null,
            supportingText = { nameError?.let { Text(it) } },
            singleLine = true
        )

        // Last name field
        OutlinedTextField(
            value = lastName,
            onValueChange = onLastNameChange,
            label = { Text(stringResource(R.string.last_name)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .focusRequester(focusRequesters.lastNameFocusRequester),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = null
                )
            },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusRequesters.ageFocusRequester.requestFocus() }
            ),
            isError = lastNameError != null,
            supportingText = { lastNameError?.let { Text(it) } },
            singleLine = true
        )

        // Age field
        OutlinedTextField(
            value = if (age > 0) age.toString() else "",
            onValueChange = onAgeChange,
            label = { Text(stringResource(R.string.age)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .focusRequester(focusRequesters.ageFocusRequester),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Cake,
                    contentDescription = null
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusRequesters.squadFocusRequester.requestFocus() }
            ),
            isError = ageError != null,
            supportingText = { ageError?.let { Text(it) } },
            singleLine = true
        )

        // Squad dropdown
        ExposedDropdownMenuBox(
            expanded = isSquadMenuExpanded,
            onExpandedChange = onSquadMenuExpandedChange,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequesters.squadFocusRequester)
        ) {
            OutlinedTextField(
                value = squadName,
                onValueChange = onSquadNameChange,
                label = { Text(stringResource(R.string.squad)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Groups,
                        contentDescription = null
                    )
                },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(
                        expanded = isSquadMenuExpanded
                    )
                },
                isError = squadNameError != null,
                supportingText = { squadNameError?.let { Text(it) } },
                singleLine = true,
                readOnly = true,
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
            )

            ExposedDropdownMenu(
                expanded = isSquadMenuExpanded,
                onDismissRequest = { onSquadMenuExpandedChange(false) }
            ) {
                squadNames.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onSquadNameChange(option)
                            onSquadMenuExpandedChange(false)
                        }
                    )
                }

                // Add new squad option
                HorizontalDivider()
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.add_new_squad)) },
                    leadingIcon = {
                        Icon(Icons.Default.Add, contentDescription = null)
                    },
                    onClick = onAddNewSquad
                )
            }
        }
    }
}