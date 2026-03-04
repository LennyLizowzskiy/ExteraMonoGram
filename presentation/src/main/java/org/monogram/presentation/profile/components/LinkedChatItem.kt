package org.monogram.presentation.profile.components


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.monogram.domain.models.ChatModel
import org.monogram.presentation.chatsScreen.currentChat.components.VideoPlayerPool
import org.monogram.presentation.uikit.Avatar

@Composable
fun LinkedChatItem(
    chat: ChatModel,
    isDiscussion: Boolean,
    videoPlayerPool: VideoPlayerPool,
    onClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        onClick =  onClick

    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Avatar(
                path = chat.avatarPath,
                name = chat.title,
                size = 64.dp,
                modifier = Modifier.clip(CircleShape),
                videoPlayerPool = videoPlayerPool
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = chat.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val subtitle = buildString {
                    if (chat.memberCount > 0) {
                        append("${chat.memberCount} subscribers")
                    }
                    if (!chat.description.isNullOrEmpty()) {
                        if (isNotEmpty()) append(" • ")
                        append(chat.description)
                    }
                }
                if (subtitle.isNotEmpty()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }


        }
        Box(Modifier.fillMaxHeight()) {
             Surface(
                 color = MaterialTheme.colorScheme.primaryContainer,
                 shape = CircleShape,
                 modifier = Modifier.padding(end = 16.dp, top = 16.dp).align(Alignment.TopEnd)

             ) {
                 Text(
                     text = if (isDiscussion) "Discussion" else "Channel",
                     modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                     style = MaterialTheme.typography.labelSmall,
                     color = MaterialTheme.colorScheme.onPrimaryContainer,
                     fontWeight = FontWeight.Bold
                 )
             }
         }
    }
}