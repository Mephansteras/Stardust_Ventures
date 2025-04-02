package data.scripts.campaign.fleets;

import com.fs.starfarer.api.impl.campaign.fleets.PersonalFleetScript;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.People;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.missions.FleetCreatorMission;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.FleetQuality;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.FleetSize;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.OfficerNum;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.OfficerQuality;
import com.fs.starfarer.api.impl.campaign.missions.hub.MissionFleetAutoDespawn;
import com.fs.starfarer.api.loading.VariantSource;

public class stardust_PersonalFleetSarvalKaan extends PersonalFleetScript {

    // TODO Add in logic to spawn in largest world if stardust hall does not exist
    public stardust_PersonalFleetSarvalKaan() {
        super("stardust_sarval");
        setMinRespawnDelayDays(10f);
        setMaxRespawnDelayDays(20f);
    }

    @Override
    protected MarketAPI getSourceMarket() {
        return Global.getSector().getEconomy().getMarket("stardust_stardust_hall");
    }

    @Override
    public CampaignFleetAPI spawnFleet() {

        MarketAPI stardust_stardust_hall = Global.getSector().getEconomy().getMarket("stardust_stardust_hall");

        FleetCreatorMission m = new FleetCreatorMission(random);
        m.beginFleet();

        Vector2f loc = stardust_stardust_hall.getLocationInHyperspace();

        m.triggerCreateFleet(FleetSize.HUGE, FleetQuality.SMOD_2, "stardust_ventures", FleetTypes.TASK_FORCE, loc);
        m.triggerSetFleetOfficers( OfficerNum.MORE, OfficerQuality.HIGHER);
        m.triggerSetFleetCommander(getPerson());
        m.triggerSetFleetFaction("stardust_ventures");
        m.triggerSetPatrol();
        m.triggerSetFleetMemoryValue(MemFlags.MEMORY_KEY_SOURCE_MARKET, stardust_stardust_hall);
        m.triggerFleetSetNoFactionInName();
        m.triggerPatrolAllowTransponderOff();
        m.triggerFleetSetName("System Defense Fleet");
        //m.triggerFleetSetPatrolActionText("patrolling");
        m.triggerOrderFleetPatrol(stardust_stardust_hall.getStarSystem());

        CampaignFleetAPI fleet = m.createFleet();
        FleetMemberAPI oldFlagship = fleet.getFlagship();
        FleetMemberAPI newFlagship = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "stardust_stormbringer_m_lancer");
        fleet.getFleetData().addFleetMember(newFlagship);
        if (newFlagship != null && oldFlagship != null) {
            newFlagship.setCaptain(oldFlagship.getCaptain());
            oldFlagship.setFlagship(false);
            newFlagship.setFlagship(true);
            fleet.getFleetData().setFlagship(newFlagship);
            fleet.getFleetData().removeFleetMember(oldFlagship);}
        fleet.removeScriptsOfClass(MissionFleetAutoDespawn.class);
        stardust_stardust_hall.getContainingLocation().addEntity(fleet);
        fleet.setLocation(stardust_stardust_hall.getPlanetEntity().getLocation().x, stardust_stardust_hall.getPlanetEntity().getLocation().y);
        fleet.setFacing((float) random.nextFloat() * 360f);
        fleet.getFlagship().setShipName("SDV Tiger Lord");
        fleet.getFleetData().sort();

        return fleet;
    }

    @Override
    public boolean canSpawnFleetNow() {
        MarketAPI stardust_stardust_hall = Global.getSector().getEconomy().getMarket("stardust_stardust_hall");
        if (stardust_stardust_hall == null || stardust_stardust_hall.hasCondition(Conditions.DECIVILIZED)) return false;
        if (!stardust_stardust_hall.getFactionId().equals("stardust_ventures")) return false;
        return true;
    }

    @Override
    public boolean shouldScriptBeRemoved() {
        return false;
    }

}
