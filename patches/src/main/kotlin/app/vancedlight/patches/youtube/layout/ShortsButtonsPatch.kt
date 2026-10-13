package app.vancedlight.patches.youtube.layout

import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.ResourcePatchContext
import app.morphe.patcher.patch.resourcePatch
import app.morphe.patcher.patch.stringOption
import app.vancedlight.patches.shared.COMPATIBILITY_YOUTUBE
import java.io.File

private fun overwriteMatching(
    bundledDir: String,
    targetDir: File,
    loader: ClassLoader,
): Int {
    if (!targetDir.isDirectory) return 0
    var n = 0
    targetDir.listFiles { f -> f.isFile }?.forEach { target ->
        val stream = loader.getResourceAsStream("$bundledDir/${targetDir.name}/${target.name}")
            ?: return@forEach
        stream.use { src -> target.outputStream().use { src.copyTo(it) } }
        n++
    }
    return n
}

@Suppress("unused")
val shortsButtonsPatch = resourcePatch(
    name = "Shorts button style",
    description = "Shorts action button style. Stock leaves the app untouched.",
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
        var total = 0
        res.listFiles { f -> f.isDirectory && f.name.startsWith("drawable") }?.forEach { dir ->
            total += overwriteMatching("youtube/shorts/actionbuttons/$style", dir, loader)
        }
        if (total == 0) throw PatchException("No Shorts drawables matched style '$style'")
    }
}
