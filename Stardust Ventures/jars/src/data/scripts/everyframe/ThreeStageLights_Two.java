package data.scripts.everyframe;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import data.scripts.everyframe.AdjustableLights;

public class ThreeStageLights_Two implements EveryFrameWeaponEffectPlugin {

    AdjustableLights LightEffect = new AdjustableLights();
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (engine.isPaused()) return;

        // These 3 combined should add up to 1.5
        float startDelay = 0.5f;
        float runTime = 0.5f;
        float afterDelay = 0.5f;

        LightEffect.advance(amount, engine, weapon, startDelay, runTime, afterDelay);

    }
}