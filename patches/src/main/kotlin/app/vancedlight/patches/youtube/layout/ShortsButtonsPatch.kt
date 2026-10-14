package app.vancedlight.patches.youtube.layout

import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.resourcePatch
import app.morphe.patcher.patch.stringOption
import app.vancedlight.patches.shared.COMPATIBILITY_YOUTUBE
import app.vancedlight.patches.shared.applyBundledDir
import app.vancedlight.patches.shared.drawableNames

@Suppress("unused")
val shortsButtonsPatch = resourcePatch(
    name = "Shorts button style",
    description = "Shorts action button style. Stock leaves the app untouched. " +
        "Files whose references no longer exist in the app are skipped.",
) {
    compatibleWith(COMPATIBILITY_YOUTUBE)

    val iconType by stringOption(
        key = "iconType",
        title = "Button style",
        values = mapOf(
            "Cairo" to "cairo", "Outline" to "outline", "Outline Circle" to "outlinecircle",
            "Round" to "round", "YouTube Outline" to "youtubeoutline", "Stock" to "youtube",
        ),
    ) { it == null || it in setOf("cairo", "outline", "outlinecircle", "round", "youtubeoutline", "youtube") }

    execute {
        val style = (iconType ?: "cairo").lowercase()
        if (style == "youtube") return@execute
        val loader = object {}.javaClass.classLoader
        val res = get("res")
        val have = res.drawableNames()
        val skipped = mutableListOf<String>()
        var applied = 0
        res.listFiles { f -> f.isDirectory && f.name.startsWith("drawable") }?.forEach { dir ->
            applied += applyBundledDir(loader, "youtube/shorts/actionbuttons/$style", dir, have, skipped)
        }
        if (applied == 0) {
            throw PatchException(
                "No Shorts drawables applied for style '$style'. " +
                    (skipped.take(5).joinToString("; ").ifEmpty { "No matching files found." }),
            )
        }
    }
}
