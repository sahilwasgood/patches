package tradingview.patches

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

val COMPATIBILITY_TRADINGVIEW = Compatibility(
    name = "TradingView",
    packageName = "com.tradingview.tradingviewapp",
    apkFileType = ApkFileType.APKS_REQUIRED,
    appIconColor = 0x131722,
    signatures = setOf(
        "44f047fc6b34b0ba0c491f825bb4e43770d2bc941220d290a70f3a8467aed601",
    ),
    targets = listOf(
        AppTarget(version = "1.20.86.0.1002565", minSdk = 26, isExperimental = true)
    )
)
