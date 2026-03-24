package org.monogram.presentation.features.gallery.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryTopBar(
    onDismiss: () -> Unit,
    onPickFromOtherSources: () -> Unit,
    onCameraClick: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = "Attachments",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = {
            IconButton(onClick = onDismiss) {
                Icon(Icons.Filled.Close, contentDescription = "Close")
            }
        },
        actions = {
            IconButton(onClick = onPickFromOtherSources) {
                Icon(Icons.Filled.Extension, contentDescription = "Other sources")
            }
            IconButton(onClick = onCameraClick) {
                Icon(Icons.Filled.PhotoCamera, contentDescription = "Camera")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            scrolledContainerColor = MaterialTheme.colorScheme.background
        )
    )
}
