package tradingview.patches

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

@Suppress("unused")
val forceLocalProPlanPatch = bytecodePatch(
    name = "Force local Pro plan",
    description = "Forces the local plan lookup and plan state to report Pro/Ultimate.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_TRADINGVIEW)

    execute {
        ProPlanDefineFingerprint.method.addInstructions(
            0,
            """
                sget-object v0, Lcom/tradingview/tradingviewapp/feature/profile/api/plan/ProPlan;->ULTIMATE:Lcom/tradingview/tradingviewapp/feature/profile/api/plan/ProPlan;
                return-object v0
            """
        )

        ToPlanStateFingerprint.method.addInstructions(
            0,
            """
                if-eqz p0, :no_plan
                invoke-virtual { p0 }, Lcom/tradingview/tradingviewapp/feature/profile/api/user/CurrentUser;->getPlan()Lcom/tradingview/tradingviewapp/feature/profile/api/plan/UserPlan;
                move-result-object v0
                if-eqz v0, :no_plan
                goto :wrap_plan
                :no_plan
                new-instance v0, Lcom/tradingview/tradingviewapp/feature/profile/api/plan/UserPlan;
                invoke-direct { v0 }, Lcom/tradingview/tradingviewapp/feature/profile/api/plan/UserPlan;-><init>()V
                :wrap_plan
                new-instance v1, Lcom/tradingview/tradingviewapp/feature/profile/api/plan/UserPlanState${'$'}Pro;
                invoke-direct { v1, v0 }, Lcom/tradingview/tradingviewapp/feature/profile/api/plan/UserPlanState${'$'}Pro;-><init>(Lcom/tradingview/tradingviewapp/feature/profile/api/plan/UserPlan;)V
                return-object v1
            """
        )
    }
}
