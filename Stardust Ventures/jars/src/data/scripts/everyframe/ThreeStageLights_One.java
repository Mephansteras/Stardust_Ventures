package data.scripts.everyframe;

import java.util.Random;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;

public class ThreeStageLights_One implements EveryFrameWeaponEffectPlugin {

    private float elapsed = 0;
    boolean on = true;
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (engine.isPaused()) return;
        elapsed += amount;

        if (elapsed <= 1.0f) {
            on = true;
        }
        else if (elapsed > 1.0f && elapsed <= 2.0f) {
            on = false;
        }
        else
        {
            on = false;
            elapsed = 0.0f;
        }

        ShipAPI ship = weapon.getShip();
        if (ship.isHulk()) on = false;

        if (ship.getFluxTracker().isVenting()) {
            on = false;
        } else if (ship.getFluxTracker().isOverloaded()) {
            on = new Random().nextInt(4) == 3;
        }

        if (on) {
            weapon.getAnimation().setFrame(0);
        } else {
            weapon.getAnimation().setFrame(1);
        }
    }
}