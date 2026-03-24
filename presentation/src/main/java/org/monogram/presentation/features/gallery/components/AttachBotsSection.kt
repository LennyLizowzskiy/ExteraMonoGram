package org.monogram.presentation.features.gallery.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Extension
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
import coil3.compose.AsyncImage
import org.monogram.domain.models.AttachMenuBotModel

@Composable
fun AttachBotsSection(
    modifier: Modifier = Modifier,
    bots: List<AttachMenuBotModel>,
    selectedCount: Int,
    onSendSelected: () -> Unit,
    onAttachBotClick: (AttachMenuBotModel) -> Unit
) {
    if (bots.isEmpty() && selectedCount == 0) return

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(animationSpec = tween(durationMillis = 220))
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AnimatedVisibility(
                visible = selectedCount > 0,
                enter = fadeIn(tween(180)) + slideInVertically(tween(180)) { it / 3 },
                exit = fadeOut(tween(130)) + slideOutVertically(tween(130)) { it / 4 }
            ) {
                SendSelectedButton(
                    selectedCount = selectedCount,
                    onSendSelected = onSendSelected
                )
            }

            AnimatedVisibility(
                visible = bots.isNotEmpty(),
                enter = fadeIn(tween(220)) + slideInVertically(tween(220)) { it / 2 },
                exit = fadeOut(tween(140))
            ) {
                LazyRow(
                    contentPadding = PaddingValues(bottom = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(bots, key = { it.botUserId }) { bot ->
                        AttachBotTile(
                            bot = bot,
                            onClick = { onAttachBotClick(bot) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SendSelectedButton(
    selectedCount: Int,
    onSendSelected: () -> Unit
) {
    val suffix = if (selectedCount == 1) "" else "s"
    Button(
        onClick = onSendSelected,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.tertiary,
            contentColor = MaterialTheme.colorScheme.onTertiary
        )
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.Send,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.size(8.dp))
        Text(
            text = "Send $selectedCount item$suffix",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun AttachBotTile(
    bot: AttachMenuBotModel,
    onClick: () -> Unit
) {
    val colors = botTileColors(bot.name)
    Row(
        modifier = Modifier
            .size(width = 118.dp, height = 58.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(colors.first)
            .border(1.dp, colors.second.copy(alpha = 0.28f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 9.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val iconPath = bot.icon?.icon?.local?.path
        if (!iconPath.isNullOrBlank()) {
            AsyncImage(
                model = iconPath,
                contentDescription = bot.name,
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(colors.second.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Extension,
                    contentDescription = null,
                    tint = colors.second,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        Text(
            text = bot.name,
            style = MaterialTheme.typography.labelLarge,
            color = colors.second,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun botTileColors(seed: String): Pair<Color, Color> {
    val hue = ((seed.hashCode() and 0x7fffffff) % 360).toFloat()
    val accent = Color.hsv(hue, 0.44f, 0.80f)
    val background = accent.copy(alpha = 0.12f)
    return background to accent
}
