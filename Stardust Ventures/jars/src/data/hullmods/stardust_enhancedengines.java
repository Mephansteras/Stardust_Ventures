package data.hullmods;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.hullmods.BaseLogisticsHullMod;
import org.magiclib.util.MagicIncompatibleHullmods;

import java.util.HashSet;
import java.util.Set;

public class stardust_EnhancedEngines extends BaseLogisticsHullMod {

    private static final int BURN_LEVEL_BONUS = 1;
    private static int SMOD_BURN_LEVEL_BONUS = 1;
    public static float MANEUVER_BONUS = 20f;
    public static float SPEED_BONUS = 10f;

    private static final Set<String> BLOCKED_HULLMODS = new HashSet<>(1);

    static {
        BLOCKED_HULLMODS.add("augmentedengines");
    }

    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        for (String tmp : BLOCKED_HULLMODS) {
            if (ship.getVariant().getHullMods().contains(tmp)) {
                MagicIncompatibleHullmods.removeHullmodWithWarning(ship.getVariant(), tmp, "stardust_EnhancedEngines");
            }
        }
    }

    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {

        boolean sMod = isSMod(stats);
        stats.getMaxBurnLevel().modifyFlat(id, BURN_LEVEL_BONUS + (sMod ? SMOD_BURN_LEVEL_BONUS : 0));

        stats.getAcceleration().modifyPercent(id, MANEUVER_BONUS * 2f);
        stats.getDeceleration().modifyPercent(id, MANEUVER_BONUS);
        stats.getTurnAcceleration().modifyPercent(id, MANEUVER_BONUS * 2f);
        stats.getMaxTurnRate().modifyPercent(id, MANEUVER_BONUS);

        stats.getMaxSpeed().modifyPercent(id, SPEED_BONUS);

    }

    public String getSModDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) return "+" + SMOD_BURN_LEVEL_BONUS;
        return null;
    }

    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) return "" + BURN_LEVEL_BONUS;
        if (index == 1) return "" + (int) MANEUVER_BONUS + "%";
        if (index == 2) return "" + (int) SPEED_BONUS + "%";

        return null;
    }


}


