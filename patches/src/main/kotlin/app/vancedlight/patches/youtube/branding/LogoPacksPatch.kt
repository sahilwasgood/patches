package app.vancedlight.patches.youtube.branding

import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.ResourcePatchContext
import app.morphe.patcher.patch.folderOption
import app.morphe.patcher.patch.resourcePatch
import app.morphe.patcher.patch.stringOption
import app.vancedlight.patches.shared.COMPATIBILITY_YOUTUBE
import org.w3c.dom.Element
import java.io.File

internal val PACKS = mapOf(
    "AFN Blue" to "afn_blue", "AFN Red" to "afn_red",
    "MMT" to "mmt", "MMT Blue" to "mmt_blue", "MMT Green" to "mmt_green",
    "MMT Orange" to "mmt_orange", "MMT Pink" to "mmt_pink",
    "MMT Turquoise" to "mmt_turquoise", "MMT Yellow" to "mmt_yellow",
    "Revancify Blue" to "revancify_blue", "Revancify Red" to "revancify_red",
    "Squid Game" to "squid_game", "Vanced Black" to "vanced_black",
    "Vanced Light" to "vanced_light", "Xisr Aurora" to "xisr_aurora",
    "Xisr Evergreen" to "xisr_evergreen", "Xisr Special" to "xisr_special",
    "Xisr White" to "xisr_white", "Xisr Winter" to "xisr_winter",
    "Xisr Yellow" to "xisr_yellow", "YouTube Black" to "youtube_black",
)

internal val DENSITIES = listOf("mdpi", "hdpi", "xhdpi", "xxhdpi", "xxxhdpi")

/** Stock header wordmarks with their theme suffix, one set per density. */
internal val HEADER_NAMES = listOf(
    "yt_premium_wordmark_header_dark.png",
    "yt_premium_wordmark_header_light.png",
    "yt_wordmark_header_dark.png",
    "yt_wordmark_header_light.png",
)

private const val BG = "adaptiveproduct_youtube_background_color_108.png"
private const val FG = "adaptiveproduct_youtube_foreground_color_108.png"
private const val MONO = "adaptive_monochrome_ic_youtube_launcher.xml"
private const val STOCK_NOTIFICATION = "ic_stat_yt_notification_logo"

internal fun copyBundled(path: String, target: File) {
    val stream = object {}.javaClass.classLoader.getResourceAsStream(path)
        ?: throw PatchException("Bundled resource missing: $path")
    target.parentFile.mkdirs()
    stream.use { src -> target.outputStream().use { src.copyTo(it) } }
}

private fun ResourcePatchContext.writeLauncherXml(style: String, hasMono: Boolean) {
    val monoLine = if (hasMono) {
        "\n                <monochrome android:drawable=\"@drawable/morphe_adaptive_monochrome_$style\" />"
    } else {
        ""
    }
    get("res").resolve("mipmap-anydpi/morphe_launcher_$style.xml").apply {
        parentFile.mkdirs()
        writeText(
            "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<adaptive-icon xmlns:android=\"http://schemas.android.com/apk/res/android\">\n" +
                "                <background android:drawable=\"@mipmap/morphe_adaptive_background_$style\" />\n" +
                "                <foreground android:drawable=\"@mipmap/morphe_adaptive_foreground_$style\" />$monoLine\n" +
                "            </adaptive-icon>\n",
        )
    }
}

private fun ResourcePatchContext.pointManifestAt(style: String) {
    document("AndroidManifest.xml").use { doc ->
        val app = doc.getElementsByTagName("application").item(0) as Element
        app.setAttribute("android:icon", "@mipmap/morphe_launcher_$style")
        app.removeAttribute("android:roundIcon")
    }
}

private fun ResourcePatchContext.deleteStockNotification() {
    get("res").listFiles { f -> f.isDirectory && f.name.startsWith("drawable") }?.forEach { dir ->
        dir.listFiles { f -> f.isFile && f.nameWithoutExtension == STOCK_NOTIFICATION }
            ?.forEach { it.delete() }
    }
}

/**
 * Replaces the stock notification icon with the pack's mark (never stock):
 * whitened monochrome vector when the pack has one, else generated silhouettes.
 */
private fun ResourcePatchContext.applyNotificationIcon(key: String, hasMono: Boolean) {
    deleteStockNotification()
    val res = get("res")
    if (hasMono) {
        val stream = object {}.javaClass.classLoader.getResourceAsStream(
            "youtube/branding/$key/monochrome/drawable/$MONO",
        ) ?: throw PatchException("Bundled resource missing: $key monochrome")
        val whitened = stream.bufferedReader().readText()
            .replace("#ff000000", "#ffffffff")
            .replace("#000000", "#ffffff")
            .replace("\"#000\"", "\"#fff\"")
        res.resolve("drawable/$STOCK_NOTIFICATION.xml").apply {
            parentFile.mkdirs()
            writeText(whitened)
        }
    } else {
        listOf("mdpi", "hdpi", "xhdpi", "xxhdpi", "xxxhdpi").forEach { density ->
            copyBundled(
                "youtube/branding/$key/notification/drawable-$density/$STOCK_NOTIFICATION.png",
                res.resolve("drawable-$density/$STOCK_NOTIFICATION.png"),
            )
        }
    }
}

/**
 * Static (patch-time) logo preset. No extension, no activity aliases:
 * the chosen pack becomes the app icon. Runtime switching is out of scope for v1.
 */
@Suppress("unused")
val logoPacksPatch = resourcePatch(
    name = "Logo packs",
    description = "Applies a preset app icon (all packs incl. Vanced Light). " +
        "Turn off Morphe's Custom branding patch, both set the launcher icon. " +
        "Use Custom icon with a folder for your own art.",
) {
    compatibleWith(COMPATIBILITY_YOUTUBE)

    val appIcon by stringOption(
        key = "appIcon",
        title = "App icon",
        values = mapOf("Stock" to "original") + PACKS + mapOf("Custom" to "custom"),
    ) { it == null || it == "original" || it == "custom" || it in PACKS.values }

    val customIcon by folderOption(
        key = "customIcon",
        title = "Custom icon",
        description = "Folder with mipmap-<dpi>/morphe_adaptive_{background,foreground}_custom.png " +
            "(108/162/216/324/432 px). Optional drawable/morphe_adaptive_monochrome_custom.xml " +
            "and drawable/morphe_notification_icon_custom.xml.",
    )

    execute {
        val key = (appIcon ?: "original").lowercase()
        if (key == "original") return@execute
        val res = get("res")

        when (key) {
            "custom" -> {
                val dir = File((customIcon ?: "").trim()).takeIf { it.path.isNotEmpty() }
                    ?: throw PatchException("Custom app icon requires the Custom icon folder.")
                if (!dir.isDirectory) throw PatchException("Custom icon path is not a directory: $dir")
                var copied = false
                DENSITIES.forEach { density ->
                    listOf("background", "foreground").forEach { role ->
                        dir.resolve("mipmap-$density/morphe_adaptive_${role}_custom.png")
                            .takeIf { it.exists() }?.let {
                                it.copyTo(res.resolve("mipmap-$density/morphe_adaptive_${role}_custom.png"), true)
                                copied = true
                            }
                    }
                }
                dir.resolve("drawable/morphe_adaptive_monochrome_custom.xml").takeIf { it.exists() }?.let {
                    it.copyTo(res.resolve("drawable/morphe_adaptive_monochrome_custom.xml"), true)
                    copied = true
                }
                if (!copied) throw PatchException("No icon files found in $dir")
                val hasMono = res.resolve("drawable/morphe_adaptive_monochrome_custom.xml").exists()
                writeLauncherXml("custom", hasMono)
                pointManifestAt("custom")
                dir.resolve("drawable/morphe_notification_icon_custom.xml").takeIf { it.exists() }?.let {
                    deleteStockNotification()
                    it.copyTo(res.resolve("drawable/$STOCK_NOTIFICATION.xml"), true)
                }
            }
            in PACKS.values -> {
                DENSITIES.forEach { density ->
                    copyBundled(
                        "youtube/branding/$key/launcher/mipmap-$density/$BG",
                        res.resolve("mipmap-$density/morphe_adaptive_background_$key.png"),
                    )
                    copyBundled(
                        "youtube/branding/$key/launcher/mipmap-$density/$FG",
                        res.resolve("mipmap-$density/morphe_adaptive_foreground_$key.png"),
                    )
                }
                var hasMono = false
                try {
                    copyBundled(
                        "youtube/branding/$key/monochrome/drawable/$MONO",
                        res.resolve("drawable/morphe_adaptive_monochrome_$key.xml"),
                    )
                    hasMono = true
                } catch (e: PatchException) {
                    // Packs without monochrome (vanced_*, youtube_black): optional slot, skip.
                }
                writeLauncherXml(key, hasMono)
                pointManifestAt(key)
                applyNotificationIcon(key, hasMono)
            }
            else -> throw PatchException("Unknown app icon: $key")
        }
    }
}
