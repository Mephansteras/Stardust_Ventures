package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import org.magiclib.util.MagicIncompatibleHullmods;

import java.util.HashSet;
import java.util.Set;

public class stardust_DriveFieldOvercharge extends BaseHullMod {

    public static final float BURN_BONUS = 1;
    public static final float SENSOR_PROFILE = 300f;

    private static final float SUPPLY_USE_MULT = 1.25f;

    private static final Set<String> BLOCKED_HULLMODS = new HashSet<>(1);

    static {
        BLOCKED_HULLMODS.add("stardust_HarmonicEnhancers");
        BLOCKED_HULLMODS.add("stardust_HarmonicStabilizers");
        BLOCKED_HULLMODS.add("stardust_HarmonicSilencers");
        //BLOCKED_HULLMODS.add("stardust_DriveFieldOvercharge");
    }

    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        for (String tmp : BLOCKED_HULLMODS) {
            if (ship.getVariant().getHullMods().contains(tmp)) {
                MagicIncompatibleHullmods.removeHullmodWithWarning(ship.getVariant(), tmp, "stardust_DriveFieldOvercharge");
            }
        }
    }

    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getDynamic().getMod(Stats.FLEET_BURN_BONUS).modifyFlat(id, BURN_BONUS);
        stats.getSensorProfile().modifyFlat(id, SENSOR_PROFILE);
        stats.getSuppliesPerMonth().modifyMult(id, (1 + SUPPLY_USE_MULT));

        boolean sMod = isSMod(stats);
        if (sMod) {
            stats.getSensorProfile().modifyFlat(id, SENSOR_PROFILE);
        }
    }

    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) return "" + (int) BURN_BONUS;
        if (index == 1) return "" + (int) SENSOR_PROFILE;
        if (index == 2) return "" + (int) (((SUPPLY_USE_MULT - 1) * 100) * 2) + "%";
        if (index == 3) return "" + (int) SENSOR_PROFILE;
        return null;
    }

    @Override
    public String getSModDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
        if (index == 0) return "" + (int) SENSOR_PROFILE;
        return null;
    }

    @Override
    public boolean isSModEffectAPenalty() {
        return true;
    }

    public boolean showInRefitScreenModPickerFor(ShipAPI ship) {
        return ship != null && ship.getVariant().getHullMods().contains("stardust_WarpHarmonicResonator") && ship.getVariant().getHullMods().contains("drive_field_stabilizer");
    }
}
