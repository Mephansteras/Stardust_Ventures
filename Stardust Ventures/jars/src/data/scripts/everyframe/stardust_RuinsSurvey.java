package data.scripts.everyframe;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI.SurveyLevel;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.util.Misc;

import java.util.HashMap;
import java.util.Map;

import static com.fs.starfarer.api.impl.campaign.ids.Conditions.RUINS_SCATTERED;

public class stardust_RuinsSurvey implements EveryFrameScript {

    private float surveyDistance = 0;
    // Create the map
    private Map<MarketAPI, SectorEntityToken> FlaggedPlanetsMap = new HashMap<>();

    @Override
    public void advance(float amount) {
        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
        StarSystemAPI system = fleet.getStarSystem();
        if (system == null) {
            return;
        }
        for (PlanetAPI planet : system.getPlanets()) {
            process(fleet, planet);
        }
        unmark_explored();
    }

    public void setSurveyDistance(float distance)
    {
        surveyDistance = distance;
    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public boolean runWhilePaused() {
        return false;
    }

    private boolean canScan(MarketAPI market) {
        if (market == null || FlaggedPlanetsMap.containsKey(market)) {
            return false;
        }
        if (!Misc.hasRuins(market)) {
            return false;
        }
        SurveyLevel level = market.getSurveyLevel();
        return level != SurveyLevel.FULL;
    }

    private boolean isInRange(CampaignFleetAPI fleet, SectorEntityToken token) {
        return Misc.getDistance(fleet, token) < surveyDistance;
    }

    private void process(CampaignFleetAPI fleet, PlanetAPI planet) {
        String name = planet.getName();
        if (!isInRange(fleet, planet)) {
            return;
        }
        MarketAPI market = planet.getMarket();
        if (!canScan(market)) {
            return;
        }
        StarSystemAPI system = planet.getStarSystem();
        SectorEntityToken planet_token = system.getEntityById(planet.getId());
        Misc.makeImportant(planet_token, "stardust_RuinsSurvey");
        FlaggedPlanetsMap.put(market, planet_token);
        boolean beenNotified = market.getMemoryWithoutUpdate().getBoolean("$stardust_RuinsNotified");
        if (!beenNotified) {
            String title = "Ruins Detected: " + planet.getName();
            String msg = "Narrow Band Scanning shows ruins on " + planet.getName() + ".";
            MessageIntel intel = new MessageIntel(title, Misc.getBasePlayerColor(), new String[] {msg});
            //intel.setIcon(Global.getSettings().getSpriteName("icons", "stardust_NarrowBandScanners"));
            intel.setIcon(Global.getSettings().getSpriteName("intel", "new_planet_info"));
            Global.getSector().getCampaignUI().addMessage(intel, CommMessageAPI.MessageClickAction.INTEL_TAB, planet);
            market.getMemoryWithoutUpdate().set("$stardust_RuinsNotified", true);
        }
    }

    private void unmark_explored()
    {
        FlaggedPlanetsMap.forEach((market, entityToken) -> {
            PlanetAPI currPlanet = (PlanetAPI) entityToken;
            MarketAPI currMarket = currPlanet.getMarket();
            SurveyLevel level = currMarket.getSurveyLevel();
            if (level == SurveyLevel.FULL) {
                Misc.makeUnimportant(entityToken, "stardust_RuinsSurvey");
            };
        });
    }

    public void clear_list() {
        // Remove the Important markers that we set, don't want to clog things when out of system
        FlaggedPlanetsMap.forEach((market, entityToken) -> {
            Misc.makeUnimportant(entityToken, "stardust_RuinsSurvey");
        });

        FlaggedPlanetsMap = new HashMap<>();
    };

    // Remove a pair by Market
    // FlaggedPlanetsMap.remove(market);

    // Or remove by value (SectorEntityToken) if needed:
    // FlaggedPlanetsMap.values().remove(entity);

}
