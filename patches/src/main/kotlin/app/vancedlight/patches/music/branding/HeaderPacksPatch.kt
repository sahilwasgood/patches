package app.vancedlight.patches.music.branding

import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.resourcePatch
import app.morphe.patcher.patch.stringOption
import app.vancedlight.patches.shared.COMPATIBILITY_YOUTUBE_MUSIC

/**
 * Static header preset for YT Music. The chosen pack's app bar logos overwrite
 * the stock resources, the same files Morphe's Change header patch manages,
 * so that patch and this one must not run together. Runtime switching stays
 * out of scope for v1.
 */
@Suppress("unused")
val musicHeaderPacksPatch = resourcePatch(
    name = "Header packs",
    description = "Applies a preset header logo matched to the logo packs. " +
        "Turn off Morphe's Change header patch, both replace the same header resources. " +
        "Stock leaves the header untouched.",
) {
    compatibleWith(COMPATIBILITY_YOUTUBE_MUSIC)

    val headerStyle by stringOption(
        key = "headerStyle",
        title = "Header style",
        values = mapOf("Stock" to "original") + PACKS,
    ) { it == null || it == "original" || it in PACKS.values }

    execute {
        val key = (headerStyle ?: "original").lowercase()
        if (key == "original") return@execute
        val res = get("res")

        var copied = 0
        DENSITIES.forEach { density ->
            MUSIC_HEADER_NAMES.forEach { name ->
                copyBundled(
                    "music/branding/$key/header/drawable-$density/$name",
                    res.resolve("drawable-$density/$name"),
                )
                copied++
            }
        }
        if (copied == 0) throw PatchException("No bundled header files for pack: $key")
    }
}
