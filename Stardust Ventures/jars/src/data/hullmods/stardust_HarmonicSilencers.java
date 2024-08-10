package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import org.magiclib.util.MagicIncompatibleHullmods;

import java.util.HashSet;
import java.util.Set;

public class stardust_HarmonicSilencers extends BaseHullMod {

    public static final float SENSOR_PROFILE = -200f;

    private static final Set<String> BLOCKED_HULLMODS = new HashSet<>(1);

    static {
        BLOCKED_HULLMODS.add("stardust_HarmonicEnhancers");
        BLOCKED_HULLMODS.add("stardust_HarmonicStabilizers");
        //BLOCKED_HULLMODS.add("stardust_HarmonicSilencers");
        BLOCKED_HULLMODS.add("stardust_DriveFieldOvercharge");
    }

    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        for (String tmp : BLOCKED_HULLMODS) {
            if (ship.getVariant().getHullMods().contains(tmp)) {
                MagicIncompatibleHullmods.removeHullmodWithWarning(ship.getVariant(), tmp, "stardust_HarmonicSilencers");
            }
        }
    }

    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getSensorProfile().modifyFlat(id, SENSOR_PROFILE);
    }

    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) return "" + (int) SENSOR_PROFILE;
        return null;
    }

    public boolean showInRefitScreenModPickerFor(ShipAPI ship) {
        return ship != null && ship.getVariant().getHullMods().contains("stardust_WarpHarmonicResonator");
    }
}
