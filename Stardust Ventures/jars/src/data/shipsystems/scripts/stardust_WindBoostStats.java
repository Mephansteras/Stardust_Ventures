package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.Misc;

import java.awt.*;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import java.awt.Color;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.util.Misc;

public class stardust_WindBoostStats extends BaseShipSystemScript {
    public static final Object KEY_JITTER = new Object();

    public static final float SPEED_INCREASE_PERCENT = 50;
    public static final float RANGE_INCREASE_PERCENT = 33;
    public static final float ACCL_INCREASE_PERCENT = 33;
    public static final float DCL_INCREASE_PERCENT = 33;
    public static final float TURN_INCREASE_PERCENT = 100;

    public static final Color JITTER_UNDER_COLOR = new Color(168,233,233,125);
    public static final Color JITTER_COLOR = new Color(168,233,233,75);


    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        ShipAPI ship = null;
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }


        if (effectLevel > 0) {
            float jitterLevel = effectLevel;
            float maxRangeBonus = 5f;
            float jitterRangeBonus = jitterLevel * maxRangeBonus;
            for (ShipAPI fighter : getFighters(ship)) {
                if (fighter.isHulk()) continue;
                MutableShipStatsAPI fStats = fighter.getMutableStats();

                fStats.getMaxSpeed().modifyMult(id, 1f + 0.01f * SPEED_INCREASE_PERCENT * effectLevel);
                fStats.getAcceleration().modifyMult(id, 1f + 0.01f * ACCL_INCREASE_PERCENT * effectLevel);
                fStats.getDeceleration().modifyMult(id, 1f + 0.01f * DCL_INCREASE_PERCENT * effectLevel);
                fStats.getTurnAcceleration().modifyMult(id, 1f + 0.01f * TURN_INCREASE_PERCENT * effectLevel);
                fStats.getMaxTurnRate().modifyMult(id, 1f + 0.01f * TURN_INCREASE_PERCENT * effectLevel);
                fStats.getFighterWingRange().modifyMult(id, 1f + 0.01f * RANGE_INCREASE_PERCENT * effectLevel);


                if (jitterLevel > 0) {
                    //fighter.setWeaponGlow(effectLevel, new Color(255,50,0,125), EnumSet.allOf(WeaponType.class));
                    fighter.setWeaponGlow(effectLevel, Misc.setAlpha(JITTER_UNDER_COLOR, 255), EnumSet.allOf(WeaponAPI.WeaponType.class));

                    fighter.setJitterUnder(KEY_JITTER, JITTER_COLOR, jitterLevel, 5, 0f, jitterRangeBonus);
                    fighter.setJitter(KEY_JITTER, JITTER_UNDER_COLOR, jitterLevel, 2, 0f, 0 + jitterRangeBonus * 1f);
                    Global.getSoundPlayer().playLoop("system_targeting_feed_loop", ship, 1f, 1f, fighter.getLocation(), fighter.getVelocity());
                }
            }
        }
    }

    private java.util.List<ShipAPI> getFighters(ShipAPI carrier) {
        List<ShipAPI> result = new ArrayList<ShipAPI>();

//		this didn't catch fighters returning for refit
//		for (FighterLaunchBayAPI bay : carrier.getLaunchBaysCopy()) {
//			if (bay.getWing() == null) continue;
//			result.addAll(bay.getWing().getWingMembers());
//		}

        for (ShipAPI ship : Global.getCombatEngine().getShips()) {
            if (!ship.isFighter()) continue;
            if (ship.getWing() == null) continue;
            if (ship.getWing().getSourceShip() == carrier) {
                result.add(ship);
            }
        }

        return result;
    }


    public void unapply(MutableShipStatsAPI stats, String id) {
        ShipAPI ship = null;
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }
        for (ShipAPI fighter : getFighters(ship)) {
            if (fighter.isHulk()) continue;
            MutableShipStatsAPI fStats = fighter.getMutableStats();
            fStats.getMaxSpeed().unmodify(id);
            fStats.getAcceleration().unmodify(id);
            fStats.getDeceleration().unmodify(id);
            fStats.getTurnAcceleration().unmodify(id);
            fStats.getMaxTurnRate().unmodify(id);
            fStats.getFighterWingRange().unmodify(id);
        }
    }


    public StatusData getStatusData(int index, State state, float effectLevel) {
        float percent = SPEED_INCREASE_PERCENT * effectLevel;
        if (index == 0) {
            //return new StatusData("+" + (int)percent + "% fighter damage", false);
            return new StatusData("" + Misc.getRoundedValueMaxOneAfterDecimal(1f + SPEED_INCREASE_PERCENT * effectLevel * 0.01f) + "x fighter max speed", false);
        }
        if (index == 1) {
            //return new StatusData("+" + (int)percent + "% fighter damage", false);
            return new StatusData("" + Misc.getRoundedValueMaxOneAfterDecimal(1f + ACCL_INCREASE_PERCENT * effectLevel * 0.01f) + "x fighter acceleration", false);
        }
        if (index == 2) {
            //return new StatusData("+" + (int)percent + "% fighter damage", false);
            return new StatusData("" + Misc.getRoundedValueMaxOneAfterDecimal(1f + DCL_INCREASE_PERCENT * effectLevel * 0.01f) + "x fighter deceleration", false);
        }
        if (index == 3) {
            //return new StatusData("+" + (int)percent + "% fighter damage", false);
            return new StatusData("" + Misc.getRoundedValueMaxOneAfterDecimal(1f + TURN_INCREASE_PERCENT * effectLevel * 0.01f) + "x fighter turn acceleration", false);
        }
        if (index == 4) {
            //return new StatusData("+" + (int)percent + "% fighter damage", false);
            return new StatusData("" + Misc.getRoundedValueMaxOneAfterDecimal(1f + TURN_INCREASE_PERCENT * effectLevel * 0.01f) + "x fighter turn rate", false);
        }
        if (index == 5) {
            //return new StatusData("+" + (int)percent + "% fighter damage", false);
            return new StatusData("" + Misc.getRoundedValueMaxOneAfterDecimal(1f + RANGE_INCREASE_PERCENT * effectLevel * 0.01f) + "x fighter wing range", false);
        }

        return null;
    }


}








