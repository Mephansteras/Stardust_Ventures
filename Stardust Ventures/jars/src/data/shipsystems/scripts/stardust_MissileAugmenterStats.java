package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

public class stardust_MissileAugmenterStats extends BaseShipSystemScript {

    public static final float DMG_BONUS_MULT = 10.0f;
    public static final float SPD_BONUS_MULT = 10.0f;
    public static final float RNG_BONUS_MULT = 10.0f;
    public static final float ACL_BONUS_MULT = 10.0f;

    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {

        stats.getMissileWeaponDamageMult().modifyMult(id, 1f + (DMG_BONUS_MULT * 0.01f));
        stats.getMissileWeaponRangeBonus().modifyMult(id, 1f + (SPD_BONUS_MULT * 0.01f));
        stats.getMissileMaxSpeedBonus().modifyMult(id, 1f + (RNG_BONUS_MULT * 0.01f));
        stats.getMissileAccelerationBonus().modifyMult(id, 1f + (ACL_BONUS_MULT * 0.01f));

    }
    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getMissileWeaponDamageMult().unmodify(id);
        stats.getMissileWeaponRangeBonus().unmodify(id);
        stats.getMissileMaxSpeedBonus().unmodify(id);
        stats.getMissileAccelerationBonus().unmodify(id);
    }

    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData("missile damage +" + (int) DMG_BONUS_MULT + "%", false);
        }
        if (index == 1) {
            return new StatusData("missile max speed +" + (int) SPD_BONUS_MULT + "%", false);
        }
        if (index == 2) {
            return new StatusData("missile range +" + (int) RNG_BONUS_MULT + "%", false);
        }
        if (index == 3) {
            return new StatusData("missile acceleration +" + (int) ACL_BONUS_MULT + "%", false);
        }
        return null;
    }
}

