package plugins;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FleetDataAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.MutableFleetStatsAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.combat.MutableStat.StatMod;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.MutableStat;

import data.hullmods.stardust_StardustCore;
import data.hullmods.stardust_WarpHarmonicResonator;

public class stardust_FleetStatManager implements EveryFrameScript
{
    // Based on theDragn's drgFleetStatManager from High Tech expansion since it does something similar
    private static float accelBonus = 0;
    private static IntervalUtil timer = new IntervalUtil(0.25f,0.5f);
    public static final String HARMONIC_ID = "stardust_harmonics_temp";
    public static final String HARMONIC_DESC = "Stardust Harmonics Active";

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
                    applyHarmonics(fleet);
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
        int numHarmonicResonators = 0;
        int numShips = fleet.getNumShips();
        for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy())
        {
            if (member.getVariant().hasHullMod("stardust_StardustCore"))
                numStardustCores++;
            if (member.getVariant().hasHullMod("stardust_WarpHarmonicResonator"))
                numHarmonicResonators++;
        }
        if (numStardustCores > 0 && numShips > 0)
            accelBonus = stardust_StardustCore.ACCELERATION_BONUS * ((float)numStardustCores / numShips);
        else
            accelBonus = 0;
        if (numHarmonicResonators > 0 && numShips > 0)
            accelBonus = accelBonus + stardust_WarpHarmonicResonator.ACCELERATION_BONUS;
        if (accelBonus > 0)
        {
            fleet.getStats().getAccelerationMult().modifyMult("stardust_StardustCore", 1f + accelBonus, "Stardust Core Field");
            setAccelBonus(accelBonus);
        } else {
            fleet.getStats().getAccelerationMult().unmodify("stardust_StardustCore");
        }

        return accelBonus;
    }

    private static void applyHarmonics(CampaignFleetAPI fleet)
    {
        PersonAPI commander = fleet.getCommander();
        MutableCharacterStatsAPI commanderStats = commander.getFleetCommanderStats();

        //float currentNavPenalty = commanderStats.getDynamic().getStat(Stats.NAVIGATION_PENALTY_MULT).getModifiedValue();

        FleetDataAPI fleetData = fleet.getFleetData();
        MutableFleetStatsAPI fleetStats = fleetData.getFleet().getStats();
        boolean hasHarmonics = fleetStats.hasMod(HARMONIC_ID);
        boolean hasHarmonicMod = false;

        for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy())
        {
            if (member.getVariant().hasHullMod("stardust_WarpHarmonicResonator"))
                hasHarmonicMod = true;
                break;
        }

        if (hasHarmonics == false && hasHarmonicMod == true)
        {
            commanderStats.getDynamic().getStat(Stats.NAVIGATION_PENALTY_MULT).modifyFlat(HARMONIC_ID,
                    -1.00f * stardust_WarpHarmonicResonator.NAVIGATION_PENALTY_REDUCTION);
            MutableStat fleetChecked = new MutableStat(0.0F);
            fleetStats.addTemporaryModMult(365f, HARMONIC_ID, HARMONIC_DESC, 1f, fleetChecked);
        }
        else if (hasHarmonics == true && hasHarmonicMod == false)
        {
            commanderStats.getDynamic().getStat(Stats.NAVIGATION_PENALTY_MULT).modifyFlat(HARMONIC_ID,
                    1.00f * stardust_WarpHarmonicResonator.NAVIGATION_PENALTY_REDUCTION);
            fleetStats.removeTemporaryMod(HARMONIC_ID);
        }

    }
}
