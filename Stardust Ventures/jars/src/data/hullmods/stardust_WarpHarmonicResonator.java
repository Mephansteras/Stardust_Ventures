package data.hullmods;

import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class stardust_WarpHarmonicResonator extends BaseHullMod  {

    public static final float NAVIGATION_PENALTY_REDUCTION = 35f; //50f;
    public static final float ACCELERATION_BONUS = 0.5f;

    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0)
            try
            {
                return (int) Math.round((plugins.stardust_FleetStatManager.getBonus() * 100f)) + "%";
            } catch (NullPointerException e)
            {
                return ("0% (in mission mode)");
            }
        int navPenalty = plugins.stardust_FleetStatManager.getPlayerNavPenalty();
        //if (index == 1 || index == 2) return "" + (int) NAVIGATION_PENALTY_REDUCTION + "%";
        if (index == 1 || index == 2) return "" + navPenalty + "%";
        return null;
    }

}
