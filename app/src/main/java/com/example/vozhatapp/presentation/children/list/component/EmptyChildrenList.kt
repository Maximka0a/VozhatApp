package com.example.vozhatapp.presentation.children.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.vozhatapp.R
import kotlinx.coroutines.delay

@Composable
fun EmptyChildrenList(isSearch: Boolean) {
    val density = LocalDensity.current
    var startAnimation by remember { mutableStateOf(false) }
    val translateAnimation = remember { Animatable(initialValue = -100f) }
    val alphaAnimation = remember { Animatable(initialValue = 0f) }

    LaunchedEffect(Unit) {
        startAnimation = true
        translateAnimation.animateTo(
            targetValue = 0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        alphaAnimation.animateTo(
            targetValue = 1f,
            animationSpec = tween(500)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .graphicsLayer {
                    translationY = translateAnimation.value
                    alpha = alphaAnimation.value
                }
        ) {
            Icon(
                imageVector = if (isSearch) Icons.Default.Search else Icons.Default.People,
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp)
                    .padding(bottom = 16.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            )

            Text(
                text = if (isSearch)
                    stringResource(R.string.no_children_search_results)
                else
                    stringResource(R.string.no_children_found),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = if (isSearch)
                    "Попробуйте изменить параметры поиска"
                else
                    "Добавьте детей, нажав кнопку ниже",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
            )

            if (!isSearch) {
                val rotationAnimation = remember { Animatable(initialValue = 0f) }
                LaunchedEffect(startAnimation) {
                    if (startAnimation) {
                        delay(500)
                        rotationAnimation.animateTo(
                            targetValue = -20f,
                            animationSpec = tween(300)
                        )
                        rotationAnimation.animateTo(
                            targetValue = 20f,
                            animationSpec = tween(600)
                        )
                        rotationAnimation.animateTo(
                            targetValue = 0f,
                            animationSpec = spring()
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.ArrowDownward,
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)
                        .rotate(rotationAnimation.value),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}