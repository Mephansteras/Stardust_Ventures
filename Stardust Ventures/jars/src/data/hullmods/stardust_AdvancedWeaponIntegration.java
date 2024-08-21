package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class stardust_AdvancedWeaponIntegration extends BaseHullMod {

    public static final float COST_REDUCTION = 5;
    public static float TURRET_SPEED_BONUS = 25f;
    public static float RECOIL_BONUS = 10f;

    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getDynamic().getMod(Stats.LARGE_BALLISTIC_MOD).modifyFlat(id, -COST_REDUCTION);
        stats.getDynamic().getMod(Stats.LARGE_ENERGY_MOD).modifyFlat(id, -COST_REDUCTION);
        stats.getDynamic().getMod(Stats.LARGE_MISSILE_MOD).modifyFlat(id, -COST_REDUCTION);

        stats.getWeaponTurnRateBonus().modifyPercent(id, TURRET_SPEED_BONUS);
        stats.getBeamWeaponTurnRateBonus().modifyPercent(id, TURRET_SPEED_BONUS);

        stats.getMaxRecoilMult().modifyMult(id, 1f - (0.01f * RECOIL_BONUS));
        stats.getRecoilPerShotMult().modifyMult(id, 1f - (0.01f * RECOIL_BONUS));
        // slower recoil recovery, also, to match the reduced recoil-per-shot
        // overall effect is same as without skill but halved in every respect
        stats.getRecoilDecayMult().modifyMult(id, 1f - (0.01f * RECOIL_BONUS));
    }

    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) return "" + (int) COST_REDUCTION + "";
        if (index == 1) return "" + (int) TURRET_SPEED_BONUS + "%";
        if (index == 2) return "" + (int) RECOIL_BONUS + "%";
        return null;
    }

    @Override
    public boolean affectsOPCosts() {
        return true;
    }

}

