package data.hullmods;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import org.magiclib.util.MagicIncompatibleHullmods;

import java.util.HashSet;
import java.util.Set;

public class stardust_HarmonicStabilizers extends BaseHullMod {

    public static final float ACCELERATION_BONUS = 0.5f;
    private static final float SUPPLY_USE_MULT = 1.125f;
    public static final float SENSOR_PROFILE = 100f;

    private static final Set<String> BLOCKED_HULLMODS = new HashSet<>(1);

    static {
        BLOCKED_HULLMODS.add("stardust_HarmonicEnhancers");
        //BLOCKED_HULLMODS.add("stardust_HarmonicStabilizers");
        BLOCKED_HULLMODS.add("stardust_HarmonicSilencers");
        BLOCKED_HULLMODS.add("stardust_DriveFieldOvercharge");
    }

    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        for (String tmp : BLOCKED_HULLMODS) {
            if (ship.getVariant().getHullMods().contains(tmp)) {
                MagicIncompatibleHullmods.removeHullmodWithWarning(ship.getVariant(), tmp, "stardust_HarmonicStabilizers");
            }
        }
    }

    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getSuppliesPerMonth().modifyMult(id, SUPPLY_USE_MULT);
    }

    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) return "" + (int) ((ACCELERATION_BONUS) * 100) + "%";
        if (index == 1) return "" + (int) (((SUPPLY_USE_MULT - 1) * 100) * 2) + "%";
        if (index == 2) return "" + (int) SENSOR_PROFILE;
        return null;
    }

    @Override
    public String getSModDescriptionParam(int index, ShipAPI.HullSize hullSize, ShipAPI ship) {
        if (index == 0) return "" + (int) SENSOR_PROFILE;
        return null;
    }

    @Override
    public boolean isSModEffectAPenalty() {
        return true;
    }

    public boolean showInRefitScreenModPickerFor(ShipAPI ship) {
        return ship != null && ship.getVariant().getHullMods().contains("stardust_WarpHarmonicResonator");
    }

}

