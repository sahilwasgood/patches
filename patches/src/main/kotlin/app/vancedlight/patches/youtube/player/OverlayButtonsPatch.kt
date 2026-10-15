package app.vancedlight.patches.youtube.player

import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.resourcePatch
import app.morphe.patcher.patch.stringOption
import app.vancedlight.patches.shared.COMPATIBILITY_YOUTUBE
import app.vancedlight.patches.shared.applyBundledDir
import app.vancedlight.patches.shared.drawableNames

@Suppress("unused")
val overlayButtonsPatch = resourcePatch(
    name = "Overlay button style",
    description = "Player overlay button style (Bold, Rounded, Thin). " +
        "Covers different buttons than Morphe's Player icon style, keep both on. " +
        "Files that reference renamed drawables are skipped, " +
        "so new app versions degrade to fewer styled buttons instead of failing.",
) {
    compatibleWith(COMPATIBILITY_YOUTUBE)

    val iconType by stringOption(
        key = "iconType",
        title = "Button style",
        values = mapOf("Bold" to "bold", "Rounded" to "rounded", "Thin" to "thin"),
    ) { it == null || it in setOf("bold", "rounded", "thin") }

    execute {
        val style = (iconType ?: "rounded").lowercase()
        val loader = object {}.javaClass.classLoader
        val res = get("res")
        val have = res.drawableNames()
        val skipped = mutableListOf<String>()
        var applied = 0
        fun resDrawableDirs() =
            res.listFiles { f -> f.isDirectory && f.name.startsWith("drawable") } ?: emptyArray()

        resDrawableDirs().forEach { dir ->
            applied += applyBundledDir(loader, "youtube/overlaybuttons/$style", dir, have, skipped)
        }
        // Shared overlay chrome (background, selector wrappers).
        resDrawableDirs().forEach { dir ->
            applied += applyBundledDir(loader, "youtube/overlaybuttons/shared", dir, have, skipped)
        }
        if (applied == 0) {
            throw PatchException(
                "No overlay drawables applied for style '$style'. " +
                    (skipped.take(5).joinToString("; ").ifEmpty { "No matching files found." }),
            )
        }
    }
}
