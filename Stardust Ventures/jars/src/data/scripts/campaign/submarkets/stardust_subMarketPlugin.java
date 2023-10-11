package data.scripts.campaign.submarkets;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.FactionAPI.ShipPickMode;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.submarkets.BaseSubmarketPlugin;
import com.fs.starfarer.api.util.Misc;

public class stardust_subMarketPlugin extends BaseSubmarketPlugin {

    private final RepLevel MIN_STANDING = RepLevel.NEUTRAL;
    private final FactionAPI STORE_FACTION = Global.getSector().getFaction("stardust_ventures_shop");

    @Override
    public void init(SubmarketAPI submarket) {
        super.init(submarket);
    }


    @Override
    public float getTariff() {
        return 0.2f;
    }

    @Override
    public String getTooltipAppendix(CoreUIAPI ui) {
        // Use market faction rep for now, since Stardust Ventures has no rep to check against
        RepLevel level = market.getFaction().getRelationshipLevel(Global.getSector().getFaction(Factions.PLAYER));

        return super.getTooltipAppendix(ui);
    }

    @Override
    public boolean isEnabled(CoreUIAPI ui)
    {

        return true;
    }

    @Override
    public void updateCargoPrePlayerInteraction() {
        float seconds = Global.getSector().getClock().convertToSeconds(sinceLastCargoUpdate);
        addAndRemoveStockpiledResources(seconds, false, true, true);
        sinceLastCargoUpdate = 0f;

        if (okToUpdateShipsAndWeapons()) {
            sinceSWUpdate = 0f;

            pruneWeapons(0f);

            int weapons = 7 + Math.max(0, market.getSize() - 1) * 2;
            int fighters = 2 + Math.max(0, market.getSize() - 3);

            addWeapons(weapons, weapons + 2, 3, STORE_FACTION.getId());
            addFighters(fighters, fighters + 2, 3, STORE_FACTION.getId());

            float stability = market.getStabilityValue();
            //float sMult = Math.max(0.1f, stability / 10f);
            getCargo().getMothballedShips().clear();

            // larger ships at lower stability to compensate for the reduced number of ships
            // so that low stability doesn't restrict the options to more or less just frigates
            // and the occasional destroyer
            int size = STORE_FACTION.getDoctrine().getShipSize();
            int add = 0;
            if (stability <= 4) {
                add = 2;
            } else if (stability <= 6) {
                add = 1;
            }

            size += add;
            if (size > 5) size = 5;

            FactionDoctrineAPI doctrineOverride = STORE_FACTION.getDoctrine().clone();
            doctrineOverride.setShipSize(size);

            addShips(STORE_FACTION.getId(),
                    //(150f + market.getSize() * 25f) * sMult, // combat
                    200f, // combat
                    15f, // freighter
                    10f, // tanker
                    20f, // transport
                    10f, // liner
                    10f, // utilityPts
                    null, // qualityOverride
                    0f, // qualityMod
                    null,
                    doctrineOverride);

            addHullMods(4, 2 + itemGenRandom.nextInt(4), STORE_FACTION.getId());
        }

        getCargo().sort();
    }

    @Override
    public boolean isIllegalOnSubmarket(CargoStackAPI stack, TransferAction action) {
        return action == TransferAction.PLAYER_SELL;
    }

    @Override
    public boolean isIllegalOnSubmarket(String commodityId, TransferAction action) {
        return action == TransferAction.PLAYER_SELL;
    }

    @Override
    public boolean isIllegalOnSubmarket(FleetMemberAPI member, TransferAction action) {
        return action == TransferAction.PLAYER_SELL;
    }

    @Override
    public String getIllegalTransferText(FleetMemberAPI member, TransferAction action) {
        return "Sales only!";
    }

    @Override
    public String getIllegalTransferText(CargoStackAPI stack, TransferAction action) {
        return "Sales only!";
    }

    @Override
    public boolean isParticipatesInEconomy() {
        return false;
    }


}