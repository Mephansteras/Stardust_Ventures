package data.scripts.everyframe;

import java.util.Random;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.FaderUtil;

public class stardust_StormHeraldElectricEffect implements EveryFrameWeaponEffectPlugin {

    private float elapsed = 0;
    boolean on = false;
    boolean ending = false; // Was on, now turning off
    int shockState = 0; // 0 = start, 1 = end, 2 = pause
    int shockFrame = 0;
    float shockDelay = 0.025f;
    float whiteDelay = 0.005f;
    float betweenDelay = 0.15f;
    float runTo = 0.05f;

    Random random = new Random();
    FaderUtil fader = new FaderUtil(1f, 0.5f, 0.5f);

    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (engine.isPaused()) return;
        elapsed += amount;

        ShipAPI ship = weapon.getShip();
        if (ship == null) {
            return;
        }

        float currentBrightness = 0f; // Do we need?

        // Test - If it returns automatically does the cover just stop?
        // Dropping out if the ship dies or in refit screen
        //No glows on wrecks
        if ( ship.isPiece() || !ship.isAlive() ) {
            return;
        }

        //Glows off in refit screen
        if (ship.getOriginalOwner() == -1) {
            return;
        }

        if (ship.isHulk())
        {
            return;
        }

        //We glow when the system or overdrive is active
        if (ship.getSystem().isActive() && ship.isAlive()) {
            currentBrightness = ship.getSystem().getEffectLevel();
            on = true;
        }
        else
        {
            if (on) {
                on = false;
                ending = true;
            }
        }



        if (ship.getFluxTracker().isVenting()) {
            on = false;
            ending = true;
        } else if (ship.getFluxTracker().isOverloaded()) {
            on = false;
            ending = true;
        }

        if ( ending)
        {
            weapon.getAnimation().setFrame(0);
            fader.fadeOut();
            ending = false;
            elapsed = 0.0f;
            return;
        }

        if (on) {
            if (elapsed < 0.05f && weapon.getAnimation().getFrame() != 1) {
                weapon.getAnimation().setFrame(1);
                fader.fadeIn();
                runTo = elapsed + shockDelay;
                shockState = 0;
            }
            else
            {
                // runTo  shockDelay  whiteDelay
                if (elapsed >= runTo) {
                    if (shockState == 0) {
                        int roll = random.nextInt(5);
                        if (roll == 0) {
                            shockFrame = 1;
                            shockState = 0;
                            runTo = elapsed + whiteDelay;
                        } else if (roll == 1) {
                            shockFrame = 2;
                            shockState++;
                            runTo = elapsed + shockDelay;
                        } else if (roll == 2) {
                            shockFrame = 4;
                            shockState++;
                            runTo = elapsed + shockDelay;
                        } else if (roll == 3) {
                            shockFrame = 6;
                            shockState++;
                            runTo = elapsed + shockDelay;
                        } else if (roll == 4) {
                            shockFrame = 8;
                            shockState++;
                            runTo = elapsed + shockDelay;
                        }
                        weapon.getAnimation().setFrame(shockFrame);
                    } else if (shockState == 1) {
                        shockFrame++;
                        shockState++;
                        weapon.getAnimation().setFrame(shockFrame);
                        runTo = elapsed + whiteDelay;
                    } else {
                        shockFrame = 1;
                        shockState = 0;
                        weapon.getAnimation().setFrame(shockFrame);
                        runTo = elapsed + betweenDelay;
                    }
                }
            }
        }
        else
        {
            elapsed = 0.0f;
        }
    }
}
