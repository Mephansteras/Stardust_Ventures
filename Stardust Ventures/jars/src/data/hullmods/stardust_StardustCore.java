package data.hullmods;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import org.magiclib.util.MagicIncompatibleHullmods;

import java.util.HashSet;
import java.util.Set;

public class stardust_StardustCore extends BaseHullMod {

    public static final float CORONA_EFFECT_REDUCTION = 0.3f;
    public static final float ACCELERATION_BONUS = 1.0f;
    public static final float ENERGY_DAMAGE_REDUCTION = 0.85f;

    private static final Set<String> BLOCKED_HULLMODS = new HashSet<>(1);

    static {
        BLOCKED_HULLMODS.add("solar_shielding");
    }

    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        for (String tmp : BLOCKED_HULLMODS) {
            if (ship.getVariant().getHullMods().contains(tmp)) {
                MagicIncompatibleHullmods.removeHullmodWithWarning(ship.getVariant(), tmp, "stardust_StardustCore");
            }
        }
    }

    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getEnergyDamageTakenMult().modifyMult(id, ENERGY_DAMAGE_REDUCTION);
        stats.getEnergyShieldDamageTakenMult().modifyMult(id, ENERGY_DAMAGE_REDUCTION);

        stats.getDynamic().getStat(Stats.CORONA_EFFECT_MULT).modifyMult(id, CORONA_EFFECT_REDUCTION);
    }

    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) return "" + (int) Math.round((1f - CORONA_EFFECT_REDUCTION) * 100f) + "%";
        if (index == 1)
            try
            {
                return (int) Math.round((plugins.stardust_FleetStatManager.getBonus() * 100f)) + "%";
            } catch (NullPointerException e)
            {
                return ("0% (in mission mode)");
            }
        if (index == 2) return "" + (int) Math.round((1f - ENERGY_DAMAGE_REDUCTION) * 100f) + "%";
        return null;
    }


}


