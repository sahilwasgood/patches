package app.vancedlight.patches.tradingview

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * AdvancedWebView initialization hook in TradingView.
 * Used for injecting DocumentStart JS scripts (Sync Engine, Layout Unlock).
 */
object InitWebViewFingerprint : Fingerprint(
    definingClass = "Lcom/tradingview/tradingviewapp/core/js/standartwebview/WebViewSessionBuilder;",
    name = "initWebView",
    parameters = listOf("Lcom/tradingview/tradingviewapp/core/js/standartwebview/AdvancedWebView;"),
)

/**
 * Plan determination hook.
 */
object ProPlanDefineFingerprint : Fingerprint(
    definingClass = "Lcom/tradingview/tradingviewapp/feature/profile/api/plan/ProPlan\$Companion;",
    name = "define",
    isStatic = false,
    returnType = "Lcom/tradingview/tradingviewapp/feature/profile/api/plan/ProPlan;",
    parameters = listOf("Ljava/lang/String;"),
)

/**
 * Converts CurrentUser into UserPlanState.
 */
object ToPlanStateFingerprint : Fingerprint(
    definingClass = "Lcom/tradingview/tradingviewapp/feature/profile/impl/interactor/UserStateInteractorImplKt;",
    name = "toPlanState",
    isStatic = true,
    returnType = "Lcom/tradingview/tradingviewapp/feature/profile/api/plan/UserPlanState;",
    parameters = listOf("Lcom/tradingview/tradingviewapp/feature/profile/api/user/CurrentUser;"),
)

/**
 * Badge type hook on user profile.
 */
object GetUserBadgeTypeFingerprint : Fingerprint(
    definingClass = "Lcom/tradingview/tradingviewapp/feature/profile/api/user/CurrentUser;",
    name = "getUserBadgeType",
    isStatic = false,
    returnType = "Lcom/tradingview/tradingviewapp/feature/profile/model/user/UserBadgeType;",
    parameters = listOf(),
)

/**
 * AdMob & feed ads check.
 */
object AreAdsAvailableFingerprint : Fingerprint(
    definingClass = "Lcom/tradingview/tradingviewapp/feature/ads/impl/entity/AdsInitializationEntity;",
    name = "areAdsAvailable",
    isStatic = false,
    returnType = "Z",
    parameters = listOf(
        "Z",
        "Lcom/tradingview/tradingviewapp/feature/ads/api/model/toggle/AdsConfigToggleInfo;",
        "J",
        "J",
    ),
)

/**
 * GoPro fallback ad container.
 */
object ShowGoProAdFingerprint : Fingerprint(
    definingClass = "Lcom/tradingview/tradingviewapp/feature/ads/api/view/AdvertisementContainerView;",
    name = "showGoProAd",
    isStatic = false,
    returnType = "V",
    parameters = listOf(),
)

/**
 * Watchlist banner service (GoPro / promotional teasers).
 */
object WatchlistBannerUpdateFingerprint : Fingerprint(
    definingClass = "Lcom/tradingview/tradingviewapp/watchlist/impl/module/symbols/service/WatchlistBannerServiceImpl;",
    name = "updateBanner",
    returnType = "V",
)

/**
 * Paywall event push.
 */
object PaywallEventPushFingerprint : Fingerprint(
    definingClass = "Lcom/tradingview/paywalls/impl/service/PaywallEventsDelegate;",
    name = "push",
    returnType = "Ljava/lang/Object;",
)

/**
 * GoPro action dispatcher.
 */
object GoProActionDispatchFingerprint : Fingerprint(
    definingClass = "Lcom/tradingview/tradingviewapp/gopro/impl/core/interactor/GoProTypeInteractorImpl;",
    name = "dispatchAction",
    returnType = "V",
    parameters = listOf("Lcom/tradingview/tradingviewapp/gopro/api/model/BaseGoProAction;"),
)

/**
 * GoPro profile banner interactor.
 */
object GetProfileBannerFingerprint : Fingerprint(
    definingClass = "Lcom/tradingview/tradingviewapp/gopro/impl/core/interactor/GoProBannersInteractorImpl;",
    name = "getProfileBanner",
    accessFlags = listOf(AccessFlags.PUBLIC),
    returnType = "Ljava/lang/Object;",
    parameters = listOf("Lkotlin/coroutines/Continuation;"),
)

/**
 * Analytics sendEvent.
 */
object AnalyticsSendEventFingerprint : Fingerprint(
    definingClass = "Lcom/tradingview/tradingviewapp/feature/analytics/impl/services/AnalyticsServiceImpl;",
    name = "sendEvent",
    isStatic = false,
    returnType = "V",
    parameters = listOf(
        "Lcom/tradingview/tradingviewapp/feature/analytics/api/base/AnalyticSendEvent;",
        "Ljava/util/Map;",
    ),
)

/**
 * Firebase logEvent.
 */
object FirebaseLogEventFingerprint : Fingerprint(
    definingClass = "Lcom/tradingview/tradingviewapp/feature/analytics/impl/firebase/FirebaseAnalyticsWrapper;",
    name = "logEvent",
    isStatic = false,
    returnType = "V",
    parameters = listOf(
        "Lcom/tradingview/tradingviewapp/feature/analytics/api/base/AnalyticSendEvent;",
        "Ljava/util/Map;",
    ),
)
