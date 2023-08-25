package data.scripts.everyframe;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import data.scripts.everyframe.AdjustableLights;

public class ThreeStageLights_Three implements EveryFrameWeaponEffectPlugin {

    AdjustableLights LightEffect = new AdjustableLights();
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (engine.isPaused()) return;

        // These 3 combined should add up to 1.5
        // Slight afterDelay since the cycle restarts
        float startDelay = 1.0f;
        float runTime = 0.45f;
        float afterDelay = 0.05f;

        LightEffect.advance(amount, engine, weapon, startDelay, runTime, afterDelay);

    }
}