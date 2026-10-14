package app.vancedlight.patches.youtube.layout

import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.resourcePatch
import app.morphe.patcher.patch.stringOption
import app.vancedlight.patches.shared.COMPATIBILITY_YOUTUBE
import app.vancedlight.patches.shared.applyBundledDir
import app.vancedlight.patches.shared.drawableNames
import org.w3c.dom.Element

private enum class ShortsActionButtons(val newResource: String, vararg val resources: String) {
    LIKE("youtube_shorts_like_outline_32dp", "ic_right_like_off_shadowed"),
    LIKE_FILLED("youtube_shorts_like_fill_32dp", "ic_right_like_on_32c", "ic_right_like_on_shadowed"),
    DISLIKE("youtube_shorts_dislike_outline_32dp", "ic_right_dislike_off_shadowed"),
    DISLIKE_FILLED("youtube_shorts_dislike_fill_32dp", "ic_right_dislike_on_32c", "ic_right_dislike_on_shadowed"),
    COMMENT("youtube_shorts_comment_outline_32dp", "ic_right_comment_shadowed"),
    SHARE("youtube_shorts_share_outline_32dp", "ic_right_share_shadowed"),
    REMIX("youtube_shorts_remix_outline_32dp", "ic_remix_filled_white_24", "ic_remix_filled_white_shadowed"),
}

// Modern Shorts names requested by server layouts, aliased to the legacy art
// this bundle ships. An alias is added only when the name is missing and its
// target exists, so this can never dangle.
private val DRAWABLE_ALIASES = mapOf(
    "youtube_shorts_like_outline_32dp" to "@drawable/ic_right_like_off_shadowed",
    "youtube_shorts_like_fill_32dp" to "@drawable/ic_right_like_on_shadowed",
    "youtube_shorts_thumbs_up_outline_28dp" to "@drawable/ic_right_like_off_shadowed",
    "youtube_shorts_thumbs_up_fill_28dp" to "@drawable/ic_right_like_on_shadowed",
    "youtube_shorts_dislike_outline_32dp" to "@drawable/ic_right_dislike_off_shadowed",
    "youtube_shorts_dislike_fill_32dp" to "@drawable/ic_right_dislike_on_shadowed",
    "youtube_shorts_thumbs_down_outline_28dp" to "@drawable/ic_right_dislike_off_shadowed",
    "youtube_shorts_thumbs_down_fill_28dp" to "@drawable/ic_right_dislike_on_shadowed",
    "youtube_shorts_comment_outline_28dp" to "@drawable/ic_right_comment_shadowed",
    "youtube_shorts_comment_outline_32dp" to "@drawable/ic_right_comment_shadowed",
    "youtube_shorts_share_outline_28dp" to "@drawable/ic_right_share_shadowed",
    "youtube_shorts_share_outline_32dp" to "@drawable/ic_right_share_shadowed",
    "youtube_shorts_remix_outline_28dp" to "@drawable/ic_remix_filled_white_shadowed",
    "youtube_shorts_remix_outline_32dp" to "@drawable/ic_remix_filled_white_shadowed",
    "youtube_shorts_heart_outline_28dp" to "@drawable/youtube_shorts_heart_off_32dp",
    "youtube_shorts_heart_outline_32dp" to "@drawable/youtube_shorts_heart_off_32dp",
    "youtube_shorts_heart_fill_28dp" to "@drawable/youtube_shorts_heart_on_32dp",
    "youtube_shorts_heart_fill_32dp" to "@drawable/youtube_shorts_heart_on_32dp",
    "youtube_shorts_save_outline_28dp" to "@drawable/yt_outline_bookmark_black_24",
    "youtube_shorts_save_outline_32dp" to "@drawable/yt_outline_bookmark_black_24",
    "youtube_shorts_save_fill_28dp" to "@drawable/yt_fill_bookmark_black_24",
    "youtube_shorts_save_fill_32dp" to "@drawable/yt_fill_bookmark_black_24",
    "youtube_shorts_save_fill_selected_32dp" to "@drawable/yt_fill_bookmark_black_24",
    "youtube_shorts_save_fill_unselected_32dp" to "@drawable/yt_outline_bookmark_black_24",
    "youtube_shorts_original_sound_16dp" to "@drawable/quantum_ic_music_note_white_24",
    "youtube_shorts_pivot_fab" to "@drawable/ic_youtube_shorts_24",
    "ic_youtube_shorts_24_cairo" to "@drawable/ic_youtube_shorts_24",
    "shorts_creation_secondary_rounded_button_background" to "@drawable/shorts_creation_rounded_button_background",
)

@Suppress("unused")
val shortsButtonsPatch = resourcePatch(
    name = "Shorts button style",
    description = "Shorts action button style. Stock leaves the app untouched. " +
        "Turn off Morphe's Shorts icon style patch, both style the same buttons.",
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

        fun drawableDirs() =
            res.listFiles { f -> f.isDirectory && f.name.startsWith("drawable") } ?: emptyArray()

        // Legacy art over legacy stock files, and onto modern stock filenames
        // (existence-gated, so old and new app versions both work).
        val resourceMap = ShortsActionButtons.entries.map { it.newResource to it.resources }
        val exts = listOf("webp", "png", "xml")
        for ((newName, oldNames) in resourceMap) {
            for (old in oldNames) {
                drawableDirs().forEach { dir ->
                    val byStem = dir.listFiles { f -> f.isFile }
                        ?.associateBy { it.nameWithoutExtension.removeSuffix(".9") }
                        ?: return@forEach
                    // Binary-safe: pick the first bundled encoding present for this density.
                    val bytes = exts.firstNotNullOfOrNull { ext ->
                        loader.getResourceAsStream(
                            "youtube/shorts/actionbuttons/$style/${dir.name}/$old.$ext",
                        )?.use { it.readBytes() }
                    } ?: return@forEach
                    byStem[old]?.let {
                        it.outputStream().use { out -> out.write(bytes) }
                        applied++
                    }
                    byStem[newName]?.let {
                        it.outputStream().use { out -> out.write(bytes) }
                        applied++
                    }
                }
            }
        }

        // Wrappers/aliases for names the server layouts request.
        var aliases = 0
        try {
            document("res/values/drawables.xml").use { doc ->
                val root = doc.documentElement
                val present = mutableSetOf<String>()
                val nodes = root.getElementsByTagName("drawable")
                for (i in 0 until nodes.length) {
                    (nodes.item(i) as? Element)?.getAttribute("name")?.let { present += it }
                }
                DRAWABLE_ALIASES.forEach { (name, value) ->
                    val target = value.removePrefix("@drawable/")
                    if (name in present || target !in have) return@forEach
                    val el = doc.createElement("drawable")
                    el.setAttribute("name", name)
                    el.textContent = value
                    root.appendChild(el)
                    present += name
                    aliases++
                }
            }
        } catch (e: Exception) {
            skipped += "drawables.xml aliases unavailable (${e.message})"
        }
        applied += aliases

        // Style xmls + shared reel icons (validated, may skip on new versions).
        drawableDirs().forEach { dir ->
            if (dir.name == "drawable") {
                applied += applyBundledDir(loader, "youtube/shorts/actionbuttons/$style", dir, have, skipped)
            }
        }
        if (style != "cairo") {
            res.resolve("drawable").takeIf { it.isDirectory }?.let { base ->
                applied += applyBundledDir(loader, "youtube/shorts/actionbuttons/shared", base, have, skipped)
            }
        }

        if (applied == 0) {
            throw PatchException(
                "No Shorts drawables applied for style '$style'. " +
                    (skipped.take(5).joinToString("; ").ifEmpty { "No matching files found." }),
            )
        }
    }
}
