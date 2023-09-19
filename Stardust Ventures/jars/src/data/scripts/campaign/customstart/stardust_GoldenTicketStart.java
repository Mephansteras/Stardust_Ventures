package data.scripts.campaign.customstart;

        import com.fs.starfarer.api.Global;
        import com.fs.starfarer.api.Script;
        import com.fs.starfarer.api.campaign.CampaignFleetAPI;
        import com.fs.starfarer.api.campaign.CargoAPI;
        import com.fs.starfarer.api.campaign.SpecialItemData;
        import com.fs.starfarer.api.campaign.InteractionDialogAPI;
        import com.fs.starfarer.api.campaign.RepLevel;
        import com.fs.starfarer.api.campaign.rules.MemKeys;
        import com.fs.starfarer.api.campaign.rules.MemoryAPI;
        import com.fs.starfarer.api.characters.CharacterCreationData;
        import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
        import com.fs.starfarer.api.characters.PersonAPI;
        import com.fs.starfarer.api.fleet.FleetMemberAPI;
        import com.fs.starfarer.api.fleet.FleetMemberType;
        import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
        import com.fs.starfarer.api.impl.campaign.ids.*;
        import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity;
        import com.fs.starfarer.api.impl.campaign.rulecmd.NGCAddStandardStartingScript;
        import com.fs.starfarer.api.util.Misc;
        import exerelin.campaign.ExerelinSetupData;
        import exerelin.campaign.PlayerFactionStore;
        import exerelin.campaign.customstart.CustomStart;
        import exerelin.utilities.StringHelper;

        import java.util.Map;

/**
 *	A Golden Ticket: Begin with a single gold Storm Seeker, credits, and bundle of blueprints
 */

public class stardust_GoldenTicketStart extends CustomStart {

    @Override
    public void execute(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
        CharacterCreationData data = (CharacterCreationData) memoryMap.get(MemKeys.LOCAL).get("$characterData");

        PlayerFactionStore.setPlayerFactionIdNGC(Factions.PLAYER);

        CampaignFleetAPI tempFleet = FleetFactoryV3.createEmptyFleet(
                PlayerFactionStore.getPlayerFactionIdNGC(), FleetTypes.PATROL_SMALL, null);

        addFleetMember("stardust_stormseeker_c_ig_stock", dialog, data, tempFleet, "flagship");

        dialog.getTextPanel().setFontSmallInsignia();

        dialog.getTextPanel().setFontInsignia();

        data.getStartingCargo().getCredits().add(30000);
        AddRemoveCommodity.addCreditsGainText(30000, dialog.getTextPanel());

        tempFleet.getFleetData().setSyncNeeded();
        tempFleet.getFleetData().syncIfNeeded();
        tempFleet.forceSync();

        int crew = 70;
        int fuel = 80;
        int supplies = 40;
        //for (FleetMemberAPI member : tempFleet.getFleetData().getMembersListCopy()) {
        //    crew += member.getMinCrew() + (int) ((member.getMaxCrew() - member.getMinCrew()) * 0.3f);
        //    fuel += (int) member.getFuelCapacity() * 0.85f;
        //    supplies += (int) member.getBaseDeploymentCostSupplies() * 3;
        // }
        data.getStartingCargo().addItems(CargoAPI.CargoItemType.RESOURCES, Commodities.CREW, crew);
        data.getStartingCargo().addItems(CargoAPI.CargoItemType.RESOURCES, Commodities.FUEL, fuel);
        data.getStartingCargo().addItems(CargoAPI.CargoItemType.RESOURCES, Commodities.SUPPLIES, supplies);
        data.getStartingCargo().addItems(CargoAPI.CargoItemType.RESOURCES, Commodities.HEAVY_MACHINERY, 30);
        data.getStartingCargo().addSpecial(new SpecialItemData("stardust_core_package", null), 1);
        data.getStartingCargo().addSpecial(new SpecialItemData("stardust_mil_package", null), 1);

        AddRemoveCommodity.addCommodityGainText(Commodities.CREW, crew, dialog.getTextPanel());
        AddRemoveCommodity.addCommodityGainText(Commodities.FUEL, fuel, dialog.getTextPanel());
        AddRemoveCommodity.addCommodityGainText(Commodities.SUPPLIES, supplies, dialog.getTextPanel());
        AddRemoveCommodity.addCommodityGainText(Commodities.HEAVY_MACHINERY, 30, dialog.getTextPanel());

        PlayerFactionStore.setPlayerFactionIdNGC(Factions.PLAYER);
        ExerelinSetupData.getInstance().freeStart = true;

        data.addScript(new Script() {
            public void run() {
                CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();

                NGCAddStandardStartingScript.adjustStartingHulls(fleet);

                fleet.getFleetData().ensureHasFlagship();

                for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
                    float max = member.getRepairTracker().getMaxCR();
                    member.getRepairTracker().setCR(max);
                }
                fleet.getFleetData().setSyncNeeded();

                Global.getSector().getPlayerFaction().setRelationship("independent", 0.25f);
            }
        });

        dialog.getVisualPanel().showFleetInfo(StringHelper.getString("exerelin_ngc", "playerFleet", true),
                tempFleet, null, null);

        dialog.getOptionPanel().addOption(StringHelper.getString("done", true), "nex_NGCDone");
        dialog.getOptionPanel().addOption(StringHelper.getString("back", true), "nex_NGCStartBack");
    }

    public void addFleetMember(String vid, InteractionDialogAPI dialog, CharacterCreationData data, CampaignFleetAPI fleet, String special) {
        data.addStartingFleetMember(vid, FleetMemberType.SHIP);
        FleetMemberAPI temp = Global.getFactory().createFleetMember(FleetMemberType.SHIP, vid);
        fleet.getFleetData().addFleetMember(temp);
        temp.getRepairTracker().setCR(0.7f);

        if (special.equals("flagship")) {
            fleet.getFleetData().setFlagship(temp);
            temp.setCaptain(data.getPerson());
            temp.setShipName("ISS Golden Ticket");
        }
    }
}
