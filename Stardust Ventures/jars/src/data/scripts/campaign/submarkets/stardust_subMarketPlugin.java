package data.scripts.campaign.submarkets;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.FactionAPI.ShipPickMode;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.submarkets.BaseSubmarketPlugin;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.util.Highlights;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.CoreUIAPI;
import plugins.stardust_FleetStatManager;

import java.awt.*;

public class stardust_subMarketPlugin extends BaseSubmarketPlugin {

    private final RepLevel MIN_STANDING = RepLevel.SUSPICIOUS;
    private final FactionAPI STORE_FACTION = Global.getSector().getFaction("stardust_ventures_shop");

    private final FactionAPI SDV_FACTION = Global.getSector().getFaction("stardust_ventures");

    public boolean isVIP()
    {
        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
        for (FleetMemberAPI member : playerFleet.getFleetData().getMembersListCopy())
        {
            if (member.getHullId().equals("stardust_stormseeker_c_ig"))
            {
                return true;
            }
        }
        return false;
    }

    private RepLevel getStoreRep()
    {
        RepLevel marketLevel = market.getFaction().getRelationshipLevel(Global.getSector().getFaction(Factions.PLAYER));
        RepLevel sdvLevel = SDV_FACTION.getRelationshipLevel(Global.getSector().getFaction(Factions.PLAYER));

        // As long as the player is a VIP, and not in bad standing with the market or SDV, they are considered Co-Op
        boolean IsVIP = isVIP();

        if (IsVIP && marketLevel.isAtWorst(RepLevel.SUSPICIOUS) && sdvLevel.isAtWorst(RepLevel.SUSPICIOUS))
        {
            return RepLevel.COOPERATIVE;
        }

        return marketLevel;
    }

    // To do: When VIP access is implemented, change the Store Name to note that

    @Override
    public void init(SubmarketAPI submarket) {
        super.init(submarket);
    }

    @Override
    public String getName()
    {
        if (isVIP()) {
            return "Stardust\nVentures (VIP)";
        }
        else
        {
            return "Stardust\nVentures";
        }
    }

    @Override
    public float getTariff() {
        return 0.2f;
    }

    @Override
    public String getTooltipAppendix(CoreUIAPI ui) {
        // Use market faction rep for now, since Stardust Ventures has no rep to check against
        RepLevel level = getStoreRep();

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

    protected boolean requiresCommission(RepLevel req) {
        return false;
    }

    @Override
    public boolean isIllegalOnSubmarket(CargoStackAPI stack, TransferAction action) {

        if (action == TransferAction.PLAYER_SELL)
        {
            return true;
        }

        if (stack.isCommodityStack()) {
            return isIllegalOnSubmarket((String) stack.getData(), action);
        }

        RepLevel req = getRequiredLevelAssumingLegal(stack, action);
        if (req == null) return false;

        // Note that we're using the market rep here, not the store rep
        RepLevel level = getStoreRep();

        boolean legal = level.isAtWorst(req);

        return !legal;
    }

    @Override
    public String getIllegalTransferText(CargoStackAPI stack, TransferAction action) {

        if (action == TransferAction.PLAYER_SELL)
        {
            return "Sales only!";
        }

        RepLevel req = getRequiredLevelAssumingLegal(stack, action);

        if (req != null) {
            if (requiresCommission(req)) {
                return "Req: " +
                        market.getFaction().getDisplayName() + " - " + req.getDisplayName().toLowerCase() + ", " +
                        " commission";
            }
            return "Req: " +
                    market.getFaction().getDisplayName() + " - " + req.getDisplayName().toLowerCase();
        }

        return "Illegal to trade in " + stack.getDisplayName() + " here";
    }


    @Override
    public boolean isIllegalOnSubmarket(String commodityId, TransferAction action) {
        return action == TransferAction.PLAYER_SELL;
    }

    @Override
    public boolean isIllegalOnSubmarket(FleetMemberAPI member, TransferAction action) {
        if (action == TransferAction.PLAYER_SELL) {return true;}

        RepLevel req = getRequiredLevelAssumingLegal(member, action);
        if (req == null) return false;

        // Note that we're using the market rep here, not the store rep
        RepLevel level = getStoreRep();

        boolean legal = level.isAtWorst(req);

        return !legal;
    }

    @Override
    public boolean isParticipatesInEconomy() {
        return false;
    }

    private RepLevel getRequiredLevelAssumingLegal(CargoStackAPI stack, TransferAction action) {
        if (action == TransferAction.PLAYER_SELL) {return null;}

        int tier = -1;
        if (stack.isWeaponStack()) {
            WeaponSpecAPI spec = stack.getWeaponSpecIfWeapon();
            tier = spec.getTier();
        } else if (stack.isModSpecStack()) {
            HullModSpecAPI spec = stack.getHullModSpecIfHullMod();
            tier = spec.getTier();
        } else if (stack.isFighterWingStack()) {
            FighterWingSpecAPI spec = stack.getFighterWingSpecIfWing();
            tier = spec.getTier();
        }

        if (tier >= 0) {
            if (action == TransferAction.PLAYER_BUY) {
                switch (tier) {
                    case 0: return RepLevel.SUSPICIOUS;
                    case 1: return RepLevel.NEUTRAL;
                    case 2: return RepLevel.FAVORABLE;
                    case 3: return RepLevel.WELCOMING;
                }
            }
            return RepLevel.VENGEFUL;
        }

        if (!stack.isCommodityStack()) return null;
        return RepLevel.SUSPICIOUS;
    }

    @Override
    public String getIllegalTransferText(FleetMemberAPI member, TransferAction action) {
        RepLevel req = getRequiredLevelAssumingLegal(member, action);
        if (req != null) {
            String str = "";
            RepLevel level = getStoreRep();
            if (!level.isAtWorst(req)) {
                str += "Req: " + market.getFaction().getDisplayName() + " - " + req.getDisplayName().toLowerCase();
            }
            return str;
        }

        if (action == TransferAction.PLAYER_BUY) {
            return "Illegal to buy"; // this shouldn't happen
        } else {
            return "Sales only!";
        }
    }

    private RepLevel getRequiredLevelAssumingLegal(FleetMemberAPI member, TransferAction action) {
        if (action == TransferAction.PLAYER_BUY) {
            int fp = member.getFleetPointCost();
            ShipAPI.HullSize size = member.getHullSpec().getHullSize();

            if (size == ShipAPI.HullSize.CAPITAL_SHIP || fp > 15) return RepLevel.FRIENDLY;
            if (size == ShipAPI.HullSize.CRUISER || fp > 10) return RepLevel.WELCOMING;
            if (size == ShipAPI.HullSize.DESTROYER || fp > 5) return RepLevel.FAVORABLE;
            return RepLevel.SUSPICIOUS;
        }
        return null;
    }

    public Highlights getIllegalTransferTextHighlights(FleetMemberAPI member, TransferAction action) {
        if (isIllegalOnSubmarket(member, action)) return null;

        RepLevel req = getRequiredLevelAssumingLegal(member, action);
        if (req != null) {
            Color c = Misc.getNegativeHighlightColor();
            Highlights h = new Highlights();
            RepLevel level = getStoreRep();
            if (!level.isAtWorst(req)) {
                h.append("Req: " + market.getFaction().getDisplayName() + " - " + req.getDisplayName().toLowerCase(), c);
            }
            return h;
        }
        return null;
    }

}