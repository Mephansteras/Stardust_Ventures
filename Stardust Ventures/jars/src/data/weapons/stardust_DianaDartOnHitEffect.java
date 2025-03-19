package data.weapons;

import java.awt.Color;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;

public class stardust_DianaDartOnHitEffect implements OnHitEffectPlugin {


    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit,
                      ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {


        if (target instanceof ShipAPI && shieldHit) {
            DamagingProjectileAPI e = engine.spawnDamagingExplosion(createExplosionSpec(), projectile.getSource(), point);
        }
    }

    public DamagingExplosionSpec createExplosionSpec() {
        float damage = 40f;
        DamagingExplosionSpec spec = new DamagingExplosionSpec(
                0.1f, // duration
                50f, // radius
                25f, // coreRadius
                damage, // maxDamage
                damage / 2f, // minDamage
                CollisionClass.PROJECTILE_FF, // collisionClass
                CollisionClass.PROJECTILE_FIGHTER, // collisionClassByFighter
                3f, // particleSizeMin
                3f, // particleSizeRange
                0.5f, // particleDuration
                150, // particleCount
                new Color(255,255,255,200), // particleColor
                new Color(130, 247, 247, 255)  // explosionColor
        );

        spec.setDamageType(DamageType.KINETIC);
        spec.setUseDetailedExplosion(false);
        spec.setSoundSetId("explosion_flak");
        return spec;
    }
}

