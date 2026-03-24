package org.monogram.presentation.features.gallery.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PermMedia
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.monogram.presentation.features.gallery.BucketFilter
import org.monogram.presentation.features.gallery.GalleryFilter

private data class GalleryTabSpec(
    val filter: GalleryFilter,
    val icon: ImageVector
)

@Composable
fun GalleryTabs(
    filter: GalleryFilter,
    onFilterChange: (GalleryFilter) -> Unit
) {
    val tabs = listOf(
        GalleryTabSpec(GalleryFilter.All, Icons.Filled.PermMedia),
        GalleryTabSpec(GalleryFilter.Photos, Icons.Filled.Image),
        GalleryTabSpec(GalleryFilter.Videos, Icons.Filled.Videocam)
    )

    Row(
        Modifier
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .fillMaxWidth()
            .height(48.dp)
            .background(
                MaterialTheme.colorScheme.surfaceContainerHigh,
                CircleShape
            )
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        tabs.forEach { tab ->
            val selected = filter == tab.filter
            val backgroundColor by animateColorAsState(
                targetValue = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                animationSpec = tween(durationMillis = 200),
                label = "tabBackground"
            )
            val contentColor by animateColorAsState(
                targetValue = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                animationSpec = tween(durationMillis = 200),
                label = "tabContent"
            )

            Box(
                Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(backgroundColor)
                    .selectable(
                        selected = selected,
                        onClick = { onFilterChange(tab.filter) },
                        role = Role.Tab
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = contentColor
                    )
                    Spacer(Modifier.size(8.dp))
                    Text(
                        text = tab.filter.title,
                        style = MaterialTheme.typography.labelLarge,
                        color = contentColor,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun FolderRow(
    buckets: List<BucketFilter>,
    selectedBucket: BucketFilter,
    onBucketChange: (BucketFilter) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(buckets, key = { it.key }) { bucket ->
            FilterChip(
                selected = bucket == selectedBucket,
                onClick = { onBucketChange(bucket) },
                label = { Text(bucket.title, maxLines = 1, overflow = TextOverflow.Ellipsis) }
            )
        }
    }
}
