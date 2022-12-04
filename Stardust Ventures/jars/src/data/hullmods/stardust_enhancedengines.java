package data.hullmods;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.hullmods.BaseLogisticsHullMod;
import data.scripts.util.MagicIncompatibleHullmods;

import java.util.HashSet;
import java.util.Set;

public class stardust_enhancedengines extends BaseLogisticsHullMod {

    private static final int BURN_LEVEL_BONUS = 1;
    public static float MANEUVER_BONUS = 20f;
    public static float SPEED_BONUS = 10f;

    private static final Set<String> BLOCKED_HULLMODS = new HashSet<>(1);

    static {
        BLOCKED_HULLMODS.add("augmentedengines");
    }

    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        for (String tmp : BLOCKED_HULLMODS) {
            if (ship.getVariant().getHullMods().contains(tmp)) {
                MagicIncompatibleHullmods.removeHullmodWithWarning(ship.getVariant(), tmp, "stardust_enhancedengines");
            }
        }
    }

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


