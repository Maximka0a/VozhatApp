package com.example.vozhatapp.presentation.children.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.vozhatapp.R
import com.example.vozhatapp.data.local.entity.Child
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AnimatedChildListItem(
    child: Child,
    onClick: () -> Unit,
    index: Int
) {
    val visible = remember { mutableStateOf(false) }

    // Delayed appearance for staggered animation
    LaunchedEffect(key1 = child.id) {
        delay(index * 15L)
        visible.value = true
    }

    AnimatedVisibility(
        visible = visible.value,
        enter = fadeIn(initialAlpha = 0.3f) +
                slideInVertically(
                    initialOffsetY = { it / 5 },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ),
    ) {
        ChildListItem(
            child = child,
            onClick = onClick,
            index = index
        )
    }
}

@Composable
fun ChildListItem(
    child: Child,
    onClick: () -> Unit,
    index: Int = 0
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(100),
        label = "card_scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .border(1.dp, Color.LightGray, RoundedCornerShape(16.dp))
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        isPressed = event.type == PointerEventType.Press
                        if (event.type == PointerEventType.Release) {
                            onClick()
                        }
                    }
                }
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Child avatar
            ChildAvatar(child = child)

            // Child details
            ChildDetails(
                child = child,
                modifier = Modifier
                    .padding(start = 16.dp)
                    .weight(1f)
            )

            // Animated chevron
            val chevronRotation by animateFloatAsState(
                targetValue = if (isPressed) 10f else 0f,
                label = "chevron_rotation"
            )

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Подробнее",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .rotate(chevronRotation)
            )
        }
    }
}

@Composable
fun ChildAvatar(child: Child, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(60.dp)
            .clip(CircleShape)
            .background(
                if (child.hasMedicalNotes)
                    MaterialTheme.colorScheme.errorContainer
                else
                    MaterialTheme.colorScheme.secondaryContainer
            )
            .border(
                width = 2.dp,
                color = if (child.hasMedicalNotes)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.secondary,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        child.photoUrl?.let {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp),
                contentScale = ContentScale.Crop
            )
        } ?: Text(
            text = child.fullName.take(2).uppercase(),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = if (child.hasMedicalNotes)
                MaterialTheme.colorScheme.onErrorContainer
            else
                MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Composable
fun ChildDetails(child: Child, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = child.fullName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Medical notes indicator with animation
            if (child.hasMedicalNotes) {
                MedicalNotesIndicator()
            }
        }

        Text(
            text = stringResource(R.string.age_years_template, child.age),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp)
        )

        // Squad tag
        SquadTag(
            squadName = child.squadName,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun MedicalNotesIndicator() {
    val rotation = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Small attention-grabbing animation
        delay(300L)
        rotation.animateTo(
            targetValue = 10f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        rotation.animateTo(
            targetValue = -5f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        rotation.animateTo(
            targetValue = 0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    Icon(
        imageVector = Icons.Default.MedicalServices,
        contentDescription = stringResource(R.string.has_special_notes),
        tint = MaterialTheme.colorScheme.error,
        modifier = Modifier
            .padding(start = 8.dp)
            .size(18.dp)
            .rotate(rotation.value)
    )
}

@Composable
fun SquadTag(squadName: String, modifier: Modifier = Modifier) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(4.dp),
        modifier = modifier
    ) {
        Text(
            text = squadName,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}