package data.scripts.everyframe;

import java.util.Random;
import java.awt.Color;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.util.FaderUtil;
import com.fs.starfarer.api.util.Misc;

public class AdjustableLights {

    private float elapsed = 0;
    boolean on = true;

    private Color base = null;
    private FaderUtil fader = new FaderUtil(1f, 0.5f, 0.5f);
    private FaderUtil pulse = new FaderUtil(1f, 2f, 2f, true, true);

    public AdjustableLights() {
        fader.fadeIn();
        pulse.fadeIn();
    }


    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon, float startDelay, float runTime, float afterDelay) {
        if (engine.isPaused()) return;
        elapsed += amount;
        fader.advance(amount);
        pulse.advance(amount);

        SpriteAPI sprite = weapon.getSprite();
        if (base == null) {
            base = sprite.getColor();
        }

        if (elapsed <= startDelay) {
            on = false;
        }
        else if (elapsed > startDelay && elapsed <= (runTime + startDelay)) {
            on = true;
        }
        else if (elapsed > (runTime + startDelay) && elapsed <= (runTime + startDelay + afterDelay)) {
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
        }
        float alphaMult = fader.getBrightness() * (0.75f + pulse.getBrightness() * 0.25f);
        if (ship.getFluxTracker().isOverloaded()) {
            alphaMult = (float) Math.random() * fader.getBrightness();
        }

        if (on) {
            fader.fadeIn();
        } else {
            fader.fadeOut();
        }

        Color color = Misc.scaleAlpha(base, alphaMult);
        //System.out.println(alphaMult);
        sprite.setColor(color);
    }
}
