package com.example.vozhatapp.presentation.children.edit.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester

class ChildFormFocusRequesters(
    val nameFocusRequester: FocusRequester,
    val lastNameFocusRequester: FocusRequester,
    val ageFocusRequester: FocusRequester,
    val squadFocusRequester: FocusRequester
)

@Composable
fun rememberChildFormFocusRequesters(): ChildFormFocusRequesters {
    val nameFocusRequester = remember { FocusRequester() }
    val lastNameFocusRequester = remember { FocusRequester() }
    val ageFocusRequester = remember { FocusRequester() }
    val squadFocusRequester = remember { FocusRequester() }

    return remember {
        ChildFormFocusRequesters(
            nameFocusRequester = nameFocusRequester,
            lastNameFocusRequester = lastNameFocusRequester,
            ageFocusRequester = ageFocusRequester,
            squadFocusRequester = squadFocusRequester
        )
    }
}