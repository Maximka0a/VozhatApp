package com.example.vozhatapp.presentation.home.components


import android.annotation.SuppressLint
import androidx.compose.animation.core.*
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.MutatorMutex
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.pow

@Composable
fun rememberPullRefreshState(
    refreshing: Boolean,
    onRefresh: () -> Unit,
    refreshThreshold: Float = 80f,
    refreshingOffset: Float = 80f
): PullRefreshState {
    require(refreshThreshold > 0f) { "The refresh trigger must be greater than zero!" }

    val scope = rememberCoroutineScope()
    val state = remember(scope) { PullRefreshState(scope = scope) }

    LaunchedEffect(refreshing) {
        state.setRefreshing(refreshing)
    }

    LaunchedEffect(refreshThreshold, refreshingOffset) {
        state.updateOffsets(
            refreshThreshold = refreshThreshold,
            refreshingOffset = refreshingOffset
        )
    }

    LaunchedEffect(onRefresh) {
        state.setOnRefresh(onRefresh)
    }

    return state
}

class PullRefreshState(
    private val scope: CoroutineScope
) {
    private val mutatorMutex = MutatorMutex()
    private val indicatorOffsetFlow = MutableStateFlow(0f)
    private var _refreshingOffset = 80f
    private var _threshold = 80f
    private var _onRefresh: () -> Unit = {}

    private val offsetAnimation = Animatable(0f)
    private var refreshing = false

    val indicatorOffset: Float
        get() = offsetAnimation.value

    val progress: Float
        get() = (offsetAnimation.value / _threshold).coerceIn(0f, 1f)

    val refreshing1: Boolean
        get() = refreshing

    fun setRefreshing(value: Boolean) {
        refreshing = value
        if (value) {
            scope.offsetAnimation(refresh = true)
        } else {
            scope.launch {
                mutatorMutex.mutate {
                    offsetAnimation.animateTo(0f)
                }
            }
        }
    }

    fun updateOffsets(refreshThreshold: Float, refreshingOffset: Float) {
        _threshold = refreshThreshold
        _refreshingOffset = refreshingOffset
    }

    fun setOnRefresh(onRefresh: () -> Unit) {
        _onRefresh = onRefresh
    }
    private val nestedScrollConnection = object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            return when {
                // Если сейчас идет обновление или прокрутка вниз - пропускаем
                refreshing || available.y >= 0 -> Offset.Zero
                // Если пользователь прокручивает вверх
                else -> Offset(0f, onScroll(available.y))
            }
        }

        override fun onPostScroll(
            consumed: Offset,
            available: Offset,
            source: NestedScrollSource
        ): Offset {
            return when {
                // Если сейчас идет обновление или прокрутка вверх - пропускаем
                refreshing || available.y <= 0 -> Offset.Zero
                // Если пользователь прокручивает вниз
                else -> Offset(0f, onScroll(available.y))
            }
        }

        override suspend fun onPreFling(available: Velocity): Velocity {
            // Если смещение индикатора больше порога, запускаем обновление
            if (!refreshing && offsetAnimation.value > _threshold) {
                _onRefresh()
            }

            // Запускаем анимацию возврата, если не обновляем в данный момент
            if (!refreshing) {
                scope.offsetAnimation(refresh = false)
            }

            return Velocity.Zero
        }

        private fun onScroll(delta: Float): Float {
            val newOffset = (offsetAnimation.value + delta * 0.5f).coerceAtLeast(0f)
            val dragConsumed = newOffset - offsetAnimation.value
            scope.launch {
                offsetAnimation.snapTo(newOffset)
            }
            return dragConsumed
        }
    }

    suspend fun snapToOffset(value: Float) {
        mutatorMutex.mutate(MutatePriority.UserInput) {
            offsetAnimation.snapTo(value)
        }
    }

    internal fun Offset.consumed(): Offset = this

    private fun CoroutineScope.offsetAnimation(refresh: Boolean) {
        launch {
            try {
                mutatorMutex.mutate {
                    if (refresh) {
                        offsetAnimation.animateTo(_refreshingOffset)
                    } else {
                        if (offsetAnimation.value <= 0f) return@mutate

                        val animationSpec = spring<Float>(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )

                        offsetAnimation.animateTo(0f, animationSpec)
                    }
                }
            } catch (e: CancellationException) {
                // Обрабатываем отмену анимации
            }
        }
    }
}

@SuppressLint("ModifierFactoryUnreferencedReceiver")
@Composable
fun Modifier.pullRefresh(
    state: PullRefreshState
): Modifier {
    TODO("Implement pull refresh modifier with nestedScrollConnection")
}

@Composable
fun PullRefreshIndicator(
    refreshing: Boolean,
    state: PullRefreshState,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.primary
) {
    TODO("Implement pull refresh indicator")
}