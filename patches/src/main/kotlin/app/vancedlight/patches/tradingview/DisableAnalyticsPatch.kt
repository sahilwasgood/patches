package app.vancedlight.patches.tradingview

import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.matchSingle
import app.morphe.patcher.util.returnEarly
import app.vancedlight.patches.shared.COMPATIBILITY_TRADINGVIEW

@Suppress("unused")
val disableAnalyticsPatch = bytecodePatch(
    name = "Disable analytics",
    description = "Disables telemetry, screen tracking, and analytics reporting to Firebase and SnowPlow.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_TRADINGVIEW)

    execute {
        AnalyticsSendEventFingerprint.matchSingle().method.returnEarly()
        FirebaseLogEventFingerprint.matchSingle().method.returnEarly()
    }
}
