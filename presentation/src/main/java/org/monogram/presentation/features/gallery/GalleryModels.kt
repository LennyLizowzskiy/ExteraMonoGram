package org.monogram.presentation.features.gallery

import android.net.Uri

enum class GalleryFilter(val title: String) {
    All("All"),
    Photos("Photos"),
    Videos("Videos")
}

sealed class BucketFilter(val key: String, val title: String) {
    data object All : BucketFilter("all", "All folders")
    data object Camera : BucketFilter("camera", "Camera")
    data object Screenshots : BucketFilter("screenshots", "Screenshots")
    data class Custom(val name: String) : BucketFilter("custom_$name", name)
}

data class GalleryMediaItem(
    val uri: Uri,
    val dateAdded: Long,
    val isVideo: Boolean,
    val bucketName: String,
    val relativePath: String,
    val isCamera: Boolean,
    val isScreenshot: Boolean
)