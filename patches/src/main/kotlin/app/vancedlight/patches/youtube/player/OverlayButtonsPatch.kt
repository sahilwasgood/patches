package app.vancedlight.patches.youtube.player

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
val overlayButtonsPatch = resourcePatch(
    name = "Overlay button style",
    description = "Player overlay button style (Bold / Rounded / Thin).",
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
        var total = 0
        res.listFiles { f -> f.isDirectory && f.name.startsWith("drawable") }?.forEach { dir ->
            total += overwriteMatching("youtube/overlaybuttons/$style", dir, loader)
        }
        // Shared overlay chrome (background, layouts' drawables).
        res.listFiles { f -> f.isDirectory && f.name.startsWith("drawable") }?.forEach { dir ->
            total += overwriteMatching("youtube/overlaybuttons/shared", dir, loader)
        }
        if (total == 0) throw PatchException("No overlay drawables matched style '$style'")
    }
}
