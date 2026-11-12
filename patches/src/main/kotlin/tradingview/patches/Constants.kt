package tradingview.patches

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

val COMPATIBILITY_TRADINGVIEW = Compatibility(
    name = "TradingView",
    packageName = "com.tradingview.tradingviewapp",
    apkFileType = ApkFileType.APK_REQUIRED,
    appIconColor = 0x131722,
    targets = listOf(
        AppTarget(version = "1.20.86.0.1002565", minSdk = 26),
        AppTarget(version = "1.20.86.0", minSdk = 26),
        AppTarget(version = "1.20.85.0", minSdk = 26),
        AppTarget(version = "1.20.0", minSdk = 26),
        AppTarget(version = "1.21.0", minSdk = 26),
        AppTarget(version = "1.22.0", minSdk = 26),
        AppTarget(version = "1.23.0", minSdk = 26),
        AppTarget(version = "1.24.0", minSdk = 26),
        AppTarget(version = "1.25.0", minSdk = 26),
    )
)
