package plugins;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FleetDataAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.MutableFleetStatsAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.PersonAPI;

import com.fs.starfarer.campaign.CharacterStats;
import data.hullmods.stardust_StardustCore;
import data.hullmods.stardust_WarpHarmonicResonator;
import data.hullmods.stardust_HarmonicStabilizers;
import data.hullmods.stardust_HarmonicEnhancers;
import data.hullmods.stardust_AdvanceSurveyDrones;
import data.scripts.everyframe.stardust_PartialSurvey;

public class stardust_FleetStatManager implements EveryFrameScript
{
    // Based on theDragn's drgFleetStatManager from High Tech expansion since it does something similar
    private static float accelBonus = 0;
    private static IntervalUtil timer = new IntervalUtil(0.25f,0.5f);
    public static final String HARMONIC_ID = "stardust_harmonics_temp";
    public static final String HARMONIC_DESC = "Stardust Harmonics Active";

    private stardust_PartialSurvey surveyor = new stardust_PartialSurvey();

    public boolean runWhilePaused()
    {
        return false;
    }

    public boolean isDone()
    {
        return false;
    }

    public boolean isVIP()
    {
        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
        for (FleetMemberAPI member : playerFleet.getFleetData().getMembersListCopy())
        {
            if (member.getId().equals("stardust_stormseeker_c_ig"))
            {
                return true;
            }
        }
        return false;
    }

    public void advance(float amount)
    {   
        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
        calcAgilityBonus(playerFleet);
        applyHarmonics(playerFleet);
        if (surveyor == null) { surveyor = new stardust_PartialSurvey(); }
        surveyor.advance(amount);

        // really no need to check every frame
        timer.advance(amount);
        if (timer.intervalElapsed())
        {
            float surveyProbeDist = getFleetSurveyProbes();
            if (surveyProbeDist > 0) {  surveyor.setSurveyDistance(surveyProbeDist); }
            for (CampaignFleetAPI fleet : Misc.getVisibleFleets(playerFleet, true))
            {
                    calcAgilityBonus(fleet);
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
        calcAgilityBonus(playerFleet);

        return accelBonus;
    }

    private static float calcAgilityBonus(CampaignFleetAPI fleet) {

        int numStardustCores = 0;
        int numHarmonicResonators = 0;
        int numHarmonicStabilizers = 0;
        int numShips = fleet.getNumShips();
        for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy())
        {
            if (member.getVariant().hasHullMod("stardust_StardustCore"))
                numStardustCores++;
            if (member.getVariant().hasHullMod("stardust_WarpHarmonicResonator"))
                numHarmonicResonators++;
            if (member.getVariant().hasHullMod("stardust_HarmonicStabilizers"))
                numHarmonicStabilizers++;
        }
        if (numStardustCores > 0 && numShips > 0)
            accelBonus = stardust_StardustCore.ACCELERATION_BONUS * ((float)numStardustCores / numShips);
        else
            accelBonus = 0;
        if (numHarmonicResonators > 0 && numShips > 0)
            accelBonus = accelBonus + stardust_WarpHarmonicResonator.ACCELERATION_BONUS;
        if (numHarmonicStabilizers > 0 && numShips > 0)
            accelBonus = accelBonus + stardust_HarmonicStabilizers.ACCELERATION_BONUS;
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
        //PersonAPI commander = member.getFleetCommanderForStats    Would need to get the flagship?

        MutableCharacterStatsAPI commanderStats;
        if (commander == null)
        {
            commander = Global.getFactory().createPerson();
            fleet.setCommander(commander);
        }
        commanderStats = commander.getFleetCommanderStats();

        if (fleet.isPlayerFleet())
        {
            commanderStats = Global.getSector().getPlayerStats();
        }
        else
        {
            commanderStats = fleet.getCommanderStats();
        }

        if (commanderStats == null)
        {
            commander.setStats(new CharacterStats());
        }

        // Most fleets should have 1.0
        // Ensure we can't have a penalty mult below 0
        float currentNavPenalty = commanderStats.getDynamic().getStat(Stats.NAVIGATION_PENALTY_MULT).getModifiedValue();
        float penaltyReduction = -0.01f * stardust_WarpHarmonicResonator.NAVIGATION_PENALTY_REDUCTION;
        float boostedPenaltyReduction = -0.01f * (stardust_HarmonicEnhancers.NAVIGATION_PENALTY_REDUCTION + stardust_WarpHarmonicResonator.NAVIGATION_PENALTY_REDUCTION);
        if ( (currentNavPenalty + penaltyReduction) < 0.0)
        {
            float diff = currentNavPenalty + penaltyReduction;
            penaltyReduction = penaltyReduction - diff;
        }


        FleetDataAPI fleetData = fleet.getFleetData();
        MutableFleetStatsAPI fleetStats = fleetData.getFleet().getStats();
        boolean hasHarmonics = fleetStats.hasMod(HARMONIC_ID);
        boolean hasHarmonicMod = false;
        boolean hasHarmonicEnhancerMod = false;

        for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
            if (member.getVariant().hasHullMod("stardust_WarpHarmonicResonator"))
            {
                hasHarmonicMod = true;
            }
            if (member.getVariant().hasHullMod("stardust_HarmonicEnhancers"))
            {
                hasHarmonicEnhancerMod = true;
                break;
            }
        }

        if (hasHarmonics == false && hasHarmonicMod == true)
        {
            if (hasHarmonicEnhancerMod == true) {
                commanderStats.getDynamic().getStat(Stats.NAVIGATION_PENALTY_MULT).modifyFlat(HARMONIC_ID,
                        boostedPenaltyReduction);
            }
            else
            {
                commanderStats.getDynamic().getStat(Stats.NAVIGATION_PENALTY_MULT).modifyFlat(HARMONIC_ID,
                        penaltyReduction);
            }
            MutableStat fleetChecked = new MutableStat(0.0F);
            fleetStats.addTemporaryModMult(365f, HARMONIC_ID, HARMONIC_DESC, 1f, fleetChecked);
        }
        else if (hasHarmonics == true && hasHarmonicMod == false)
        {
            //commanderStats.getDynamic().getStat(Stats.NAVIGATION_PENALTY_MULT).modifyFlat(HARMONIC_ID,
            //        0.01f * stardust_WarpHarmonicResonator.NAVIGATION_PENALTY_REDUCTION);
            commanderStats.getDynamic().getStat(Stats.NAVIGATION_PENALTY_MULT).unmodify(HARMONIC_ID);
            fleetStats.removeTemporaryMod(HARMONIC_ID);
        }

    }

    private static float getFleetSurveyProbes()
    {
        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();

        float tempDist = 0f;

        for (FleetMemberAPI member : playerFleet.getFleetData().getMembersListCopy()) {
            if (member.getVariant().hasHullMod("stardust_AdvanceSurveyDrones"))
            {
                if (tempDist == 0) { tempDist = stardust_AdvanceSurveyDrones.BASEDISTANCE; }
                tempDist += stardust_AdvanceSurveyDrones.BASEADDITION;
            }
        }

        if (tempDist > stardust_AdvanceSurveyDrones.BASEMAX) { tempDist = stardust_AdvanceSurveyDrones.BASEMAX; }

        return tempDist;
    }
}
