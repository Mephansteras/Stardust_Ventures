package plugins;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;

import data.hullmods.stardust_StardustCore;

public class stardust_FleetStatManager implements EveryFrameScript
{
    // Based on theDragn's drgFleetStatManager from High Tech expansion since it does something similar
    private static float accelBonus = 0;
    private static IntervalUtil timer = new IntervalUtil(0.25f,0.5f);

    public boolean runWhilePaused()
    {
        return false;
    }

    public boolean isDone()
    {
        return false;
    }

    public void advance(float amount)
    {   
        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
        calc(playerFleet);

        // really no need to check every frame
        timer.advance(amount);
        if (timer.intervalElapsed())
        {
            for (CampaignFleetAPI fleet : Misc.getVisibleFleets(playerFleet, true))
            {
                    calc(fleet);
            }
        }
    }

    // can't set a static variable inside a non-static function
    private static void setAccelBonus(float bonus)
    {
        accelBonus = bonus;
    }

    // called when the hullmod text gets displayed
    public static float getBonus()
    {
        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
        calc(playerFleet);

        return accelBonus;
    }

    private static float calc(CampaignFleetAPI fleet) {

        int numStardustCores = 0;
        int numShips = fleet.getNumShips();
        for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy())
        {
            if (member.getVariant().hasHullMod("stardust_StardustCore"))
                numStardustCores++;
        }
        if (numStardustCores > 0 && numShips > 0)
            accelBonus = stardust_StardustCore.ACCELERATION_BONUS * ((float)numStardustCores / numShips);
        else
            accelBonus = 0;
        if (accelBonus > 0)
        {
            fleet.getStats().getAccelerationMult().modifyMult("stardust_StardustCore", 1f + accelBonus, "Stardust Core Field");
            setAccelBonus(accelBonus);
        } else {
            fleet.getStats().getAccelerationMult().unmodify("stardust_StardustCore");
        }

        return accelBonus;
    }
}
