package app.vancedlight.patches.shared

import app.morphe.patcher.patch.PatchException
import java.io.File

private val DRAWABLE_REF = Regex("""@(?:drawable|mipmap)/([A-Za-z0-9_]+)""")

/** Basenames (no extension) of every file under the decoded res/drawable* dirs. */
fun File.drawableNames(): Set<String> =
    listFiles { f -> f.isDirectory && f.name.startsWith("drawable") }
        ?.flatMap { dir ->
            dir.listFiles { f -> f.isFile }?.map { it.nameWithoutExtension } ?: emptyList()
        }
        ?.toSet() ?: emptySet()

fun ClassLoader.bundledText(path: String): String? =
    getResourceAsStream(path)?.bufferedReader()?.readText()

fun ClassLoader.bundledBytes(path: String): ByteArray? =
    getResourceAsStream(path)?.use { it.readBytes() }

/** @drawable/@mipmap names referenced inside a bundled xml. */
fun refsOf(xml: String): Set<String> =
    DRAWABLE_REF.findAll(xml).map { it.groupValues[1] }.toSet()

fun copyBundledBytes(loader: ClassLoader, path: String, target: File) {
    val bytes = loader.bundledBytes(path)
        ?: throw PatchException("Bundled resource missing: $path")
    target.parentFile.mkdirs()
    target.outputStream().use { it.write(bytes) }
}

/**
 * Overwrites same-named files of one decoded res dir with bundled counterparts.
 * PNGs are inherently safe (same file replaced). XMLs are validated first:
 * every drawable they reference must already exist in the decoded resources,
 * otherwise the file is skipped (stock renamed something in a newer app version,
 * drift) instead of shipping a dangling reference that kills resource encoding.
 *
 * Returns applied count; appends human readable skip reasons to [skipped].
 */
fun applyBundledDir(
    loader: ClassLoader,
    bundledDir: String,
    targetDir: File,
    have: Set<String>,
    skipped: MutableList<String>,
): Int {
    if (!targetDir.isDirectory) return 0
    var applied = 0
    targetDir.listFiles { f -> f.isFile }?.forEach { target ->
        val rel = "$bundledDir/${targetDir.name}/${target.name}"
        if (target.extension == "xml") {
            val text = loader.bundledText(rel) ?: return@forEach
            val missing = refsOf(text) - have
            if (missing.isNotEmpty()) {
                skipped += "${target.name} needs missing ${missing.sorted().joinToString()}"
                return@forEach
            }
            target.writeText(text)
            applied++
        } else {
            val bytes = loader.bundledBytes(rel) ?: return@forEach
            target.outputStream().use { it.write(bytes) }
            applied++
        }
    }
    return applied
}
