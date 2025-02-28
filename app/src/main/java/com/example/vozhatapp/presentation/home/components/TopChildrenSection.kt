package com.example.vozhatapp.presentation.home.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.People
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.example.vozhatapp.data.local.entity.Child
import com.example.vozhatapp.presentation.home.common.EmptyStateItem
import com.example.vozhatapp.presentation.home.common.SectionWithTitle
import kotlinx.coroutines.launch


@Composable
fun TopChildrenSection(
    children: List<Child>,
    onChildClick: (Long) -> Unit
) {
    SectionWithTitle(
        title = "Топ достижений",
        showSeeAllButton = children.size > 5,
        onSeeAllClick = {}
    ) {
        if (children.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                EmptyStateItem(
                    icon = Icons.Outlined.People,
                    message = "Список детей пуст",
                    actionText = "Добавить ребенка",
                    onActionClick = {}
                )
            }
        } else {
            Box(modifier = Modifier.fillMaxWidth()) {
                LazyRow(
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 8.dp,
                        bottom = 16.dp
                    ),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(children) { index, child ->
                        val offset = remember { Animatable(200f) }
                        val alpha = remember { Animatable(0f) }

                        LaunchedEffect(key1 = Unit) {
                            kotlinx.coroutines.delay(index * 100L)
                            launch {
                                offset.animateTo(
                                    targetValue = 0f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
                                )
                            }
                            launch {
                                alpha.animateTo(
                                    targetValue = 1f,
                                    animationSpec = tween(durationMillis = 500)
                                )
                            }
                        }

                        ChildAchievementCard(
                            child = child,
                            rank = index + 1,
                            modifier = Modifier
                                .graphicsLayer {
                                    translationY = offset.value
                                    this.alpha = alpha.value
                                },
                            onClick = { onChildClick(child.id) }
                        )
                    }
                }
            }
        }
    }
}