package data.hullmods;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.hullmods.BaseLogisticsHullMod;

public class stardust_enhancedengines extends BaseLogisticsHullMod {

    private static final int BURN_LEVEL_BONUS = 1;
    public static float MANEUVER_BONUS = 20f;
    public static float SPEED_BONUS = 10f;

    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {

        stats.getMaxBurnLevel().modifyFlat(id, BURN_LEVEL_BONUS);

        stats.getAcceleration().modifyPercent(id, MANEUVER_BONUS * 2f);
        stats.getDeceleration().modifyPercent(id, MANEUVER_BONUS);
        stats.getTurnAcceleration().modifyPercent(id, MANEUVER_BONUS * 2f);
        stats.getMaxTurnRate().modifyPercent(id, MANEUVER_BONUS);

        stats.getMaxSpeed().modifyPercent(id, SPEED_BONUS);

    }

    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) return "" + BURN_LEVEL_BONUS;
        if (index == 1) return "" + (int) MANEUVER_BONUS + "%";
        if (index == 2) return "" + (int) SPEED_BONUS + "%";

        return null;
    }


}


