package app.vancedlight.patches.tradingview

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.matchSingle
import app.morphe.patcher.util.returnEarly
import app.vancedlight.patches.shared.COMPATIBILITY_TRADINGVIEW

@Suppress("unused")
val hideAdsAndPromotionsPatch = bytecodePatch(
    name = "Hide ads and upgrade promotions",
    description = "Hides AdMob ads, GoPro ads in feed, promotional banners in profile and watchlists, and paywall sheets.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_TRADINGVIEW)

    execute {
        AreAdsAvailableFingerprint.matchSingle().method.returnEarly(false)
        ShowGoProAdFingerprint.matchSingle().method.returnEarly()

        WatchlistBannerUpdateFingerprint.matchSingle().method.addInstructions(
            0,
            """
                const/4 p1, 0x0
            """
        )

        GoProActionDispatchFingerprint.matchSingle().method.returnEarly()

        PaywallEventPushFingerprint.matchSingle().method.addInstructions(
            0,
            """
                sget-object v0, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
                return-object v0
            """
        )
    }
}
