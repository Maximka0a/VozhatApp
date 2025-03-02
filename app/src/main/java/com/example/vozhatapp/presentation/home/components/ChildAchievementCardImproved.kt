package com.example.vozhatapp.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.vozhatapp.presentation.home.model.ChildRankingItem
import com.example.vozhatapp.ui.theme.VozhatAppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildAchievementCardImproved(
    child: ChildRankingItem,
    onClick: () -> Unit
) {
    val rank = when {
        child.points >= 100 -> AchievementRank.GOLD
        child.points >= 50 -> AchievementRank.SILVER
        child.points >= 25 -> AchievementRank.BRONZE
        else -> AchievementRank.STANDARD
    }

    val badgeColor = when (rank) {
        AchievementRank.GOLD -> VozhatAppTheme.extendedColors.badgeGold
        AchievementRank.SILVER -> VozhatAppTheme.extendedColors.badgeSilver
        AchievementRank.BRONZE -> VozhatAppTheme.extendedColors.badgeBronze
        AchievementRank.STANDARD -> MaterialTheme.colorScheme.surfaceVariant
    }

    Card(
        onClick = onClick,
        modifier = Modifier.width(160.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                contentAlignment = Alignment.TopEnd,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Аватар
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .border(2.dp, badgeColor, CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .align(Alignment.Center),
                    contentAlignment = Alignment.Center
                ) {
                    if (child.photoUrl != null) {
                        AsyncImage(
                            model = child.photoUrl,
                            contentDescription = "Фото ${child.name}",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Бейдж рейтинга
                if (rank != AchievementRank.STANDARD) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(badgeColor)
                            .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Star,
                            contentDescription = null,
                            tint = Color.Black.copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Имя
            Text(
                text = "${child.name} ${child.lastName}",
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Отряд
            Text(
                text = child.squadName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Очки
            Surface(
                color = badgeColor.copy(alpha = 0.2f),
                shape = MaterialTheme.shapes.small
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Star,
                        contentDescription = null,
                        tint = badgeColor,
                        modifier = Modifier.size(14.dp)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = "${child.points}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

enum class AchievementRank {
    GOLD, SILVER, BRONZE, STANDARD
}