package data.scripts.weapon_effect;

import java.awt.*;
import java.util.List;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.impl.combat.GravitonBeamEffect;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.util.TimeoutTracker;

public class stardust_MireBeamEffect implements OnHitEffectPlugin {


    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target,
                      Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        if (!(target instanceof ShipAPI)) return;

        float speedRed = 15f;
        if (shieldHit) { speedRed *= 2; }

        ShipAPI ship = (ShipAPI) target;

        ship.

        if ((float) Math.random() > 0.75f && !shieldHit && target instanceof ShipAPI) {

            float emp = projectile.getEmpAmount();
            float dam = projectile.getDamageAmount();

            engine.spawnEmpArc(projectile.getSource(), point, target, target,
                    DamageType.ENERGY,
                    dam,
                    emp, // emp
                    100000f, // max range
                    "tachyon_lance_emp_impact",
                    20f, // thickness
                    new Color(25,100,155,255),
                    new Color(255,255,255,255)
            );

            //engine.spawnProjectile(null, null, "plasma", point, 0, new Vector2f(0, 0));
        }
    }
}
