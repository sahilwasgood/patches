package app.vancedlight.patches.shared

import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.AppTarget

// Targets use version = null (any) so new stock versions keep working; pin after testing.
val COMPATIBILITY_YOUTUBE = Compatibility(
    packageName = "com.google.android.youtube",
    name = "YouTube",
    targets = listOf(AppTarget(version = null)),
)

val COMPATIBILITY_YOUTUBE_MUSIC = Compatibility(
    packageName = "com.google.android.apps.youtube.music",
    name = "YT Music",
    targets = listOf(AppTarget(version = null)),
)
