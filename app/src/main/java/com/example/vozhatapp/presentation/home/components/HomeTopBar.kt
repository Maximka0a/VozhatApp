package com.example.vozhatapp.presentation.home.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.vozhatapp.R
import com.example.vozhatapp.presentation.home.HomeScreenTopBarState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(
    scrollState: ScrollState,
    topBarState: HomeScreenTopBarState,
    onSearchClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    val scrollProgress = if (scrollState.maxValue == 0) {
        0f
    } else {
        (scrollState.value.toFloat() / scrollState.maxValue).coerceIn(0f, 1f)
    }

    // Анимация изменения внешнего вида топбара при скроллинге
    val appBarElevation by animateDpAsState(
        targetValue = if (scrollProgress > 0.05f) 4.dp else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "appBarElevation"
    )

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = appBarElevation
    ) {
        TopAppBar(
            title = {
                Column {
                    AnimatedContent(
                        targetState = topBarState.isExpanded,
                        transitionSpec = {
                            if (targetState) {
                                slideInVertically { height -> -height } + fadeIn() togetherWith
                                        slideOutVertically { height -> height } + fadeOut()
                            } else {
                                slideInVertically { height -> height } + fadeIn() togetherWith
                                        slideOutVertically { height -> -height } + fadeOut()
                            }
                        },
                        label = "titleAnimation"
                    ) { expanded ->
                        if (expanded) {
                            Text(
                                text = "Доброе утро,\nМаксим!",
                                style = MaterialTheme.typography.headlineSmall
                            )
                        } else {
                            Text(
                                text = "Vozhat App",
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    }
                }
            },
            actions = {
                IconButton(onClick = onSearchClick) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Поиск"
                    )
                }
                IconButton(onClick = onProfileClick) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_launcher_foreground),
                            contentDescription = "Аватар",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        // Индикатор статуса онлайн
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(Color.Green)
                                .align(Alignment.BottomEnd)
                                .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape)
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            )
        )
    }
}