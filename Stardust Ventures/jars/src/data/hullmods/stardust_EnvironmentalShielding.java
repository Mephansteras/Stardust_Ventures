package data.hullmods;

import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.hullmods.BaseLogisticsHullMod;

public class stardust_EnvironmentalShielding extends BaseLogisticsHullMod {

    public static final float CORONA_EFFECT_MULT = 0f;

    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getDynamic().getStat(Stats.CORONA_EFFECT_MULT).modifyMult(id, CORONA_EFFECT_MULT);
    }

    public boolean showInRefitScreenModPickerFor(ShipAPI ship) {
        return ship != null && ship.getVariant().getHullMods().contains("stardust_StardustCore");
    }
}
