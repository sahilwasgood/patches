package app.vancedlight.patches.tradingview

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.matchSingle
import app.vancedlight.patches.shared.COMPATIBILITY_TRADINGVIEW

private const val EXTENSION_CLASS = "Lapp/template/extension/tradingview/ChartLayouts;"

@Suppress("unused")
val unlockChartLayoutsPatch = bytecodePatch(
    name = "Unlock multi-chart layouts",
    description = "Unlocks chart layouts with up to 8 charts on one screen.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_TRADINGVIEW)
    extendWith("extensions/extension.mpe")

    execute {
        InitWebViewFingerprint.matchSingle().method.addInstructions(
            0,
            "invoke-static { p1 }, $EXTENSION_CLASS->addLayoutLimitScript(Landroid/webkit/WebView;)V",
        )
    }
}
