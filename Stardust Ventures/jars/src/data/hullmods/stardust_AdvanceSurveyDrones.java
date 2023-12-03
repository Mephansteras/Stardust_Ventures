package data.hullmods;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.SurveyPluginImpl;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class stardust_AdvanceSurveyDrones extends BaseHullMod {

    private static final float SURVEYCOSTREDUCTION = 1f;
    public static final float BASEDISTANCE = 1000f;
    public static final float BASEADDITION = 500f;
    public static final float BASEMAX = 3000f;

    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {

        stats.getDynamic().getMod(Stats.getSurveyCostReductionId(Commodities.HEAVY_MACHINERY)).modifyFlat(id, SURVEYCOSTREDUCTION);
        stats.getDynamic().getMod(Stats.getSurveyCostReductionId(Commodities.SUPPLIES)).modifyFlat(id, SURVEYCOSTREDUCTION);
    }

    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) return "" + (int) BASEDISTANCE;
        if (index == 1) return "" + (int) BASEADDITION;
        if (index == 2) return "" + (int) BASEMAX;
        if (index == 3) return "" + (int) SURVEYCOSTREDUCTION;

        return null;
    }

    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        if (isForModSpec || ship == null) return;
        if (Global.getSettings().getCurrentState() == GameState.TITLE) return;

        float pad = 3f;
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();

        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
        int machinery = (int) Misc.getFleetwideTotalMod(fleet, Stats.getSurveyCostReductionId(Commodities.HEAVY_MACHINERY), 0, ship);
        int supplies = (int) Misc.getFleetwideTotalMod(fleet, Stats.getSurveyCostReductionId(Commodities.SUPPLIES), 0, ship);



        tooltip.addPara("The combined surveying equipment in your fleet reduces the survey cost by %s "
                        + "supplies and %s heavy machinery.",
                opad, h,
                "" + supplies,
                "" + machinery
        );
    }
}




