package data.shipsystems.scripts;

        import com.fs.starfarer.api.Global;
        import com.fs.starfarer.api.combat.MutableShipStatsAPI;
        import com.fs.starfarer.api.combat.ShipAPI;
        import com.fs.starfarer.api.combat.ShipEngineControllerAPI.ShipEngineAPI;
        import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
        import com.fs.starfarer.api.plugins.ShipSystemStatsScript;
        import com.fs.starfarer.api.util.IntervalUtil;

public class stardust_StormSurgeStats extends BaseShipSystemScript {

    private boolean jetsActive = true;
    private final IntervalUtil interval = new IntervalUtil(4.0f, 4.0f);
    private float elapsedTime = 0f;

    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        if (state == ShipSystemStatsScript.State.OUT) {
            stats.getMaxSpeed().unmodify(id); // to slow down ship to its regular top speed while powering drive down
            stats.getMaxTurnRate().unmodify(id);
        } else {
            if (jetsActive) {
                stats.getMaxSpeed().modifyFlat(id, 65f);
                stats.getAcceleration().modifyPercent(id, 200f * effectLevel);
                stats.getDeceleration().modifyPercent(id, 200f * effectLevel);
                stats.getMaxTurnRate().modifyPercent(id, 50f);

            } 
        }

        if (stats.getEntity() instanceof ShipAPI && false) {
            ShipAPI ship = (ShipAPI) stats.getEntity();
            String key = ship.getId() + "_" + id;
            Object test = Global.getCombatEngine().getCustomData().get(key);
            if (state == State.IN) {
                if (test == null && effectLevel > 0.2f) {
                    Global.getCombatEngine().getCustomData().put(key, new Object());
                    ship.getEngineController().getExtendLengthFraction().advance(1f);
                    for (ShipEngineAPI engine : ship.getEngineController().getShipEngines()) {
                        if (engine.isSystemActivated()) {
                            ship.getEngineController().setFlameLevel(engine.getEngineSlot(), 1f);
                        }
                    }
                }
            } else {
                Global.getCombatEngine().getCustomData().remove(key);
            }
        }

        elapsedTime += Global.getCombatEngine().getElapsedInLastFrame();
        //interval.advance(Global.getCombatEngine().getElapsedInLastFrame());
        if (elapsedTime >= 4.0f && jetsActive) {
            jetsActive = false;
            // Turn off the jet boost changes
            stats.getMaxSpeed().unmodify(id);
            stats.getAcceleration().unmodify(id);
            stats.getDeceleration().unmodify(id);
            stats.getMaxTurnRate().unmodify(id);

            // Turn on new boosts
            stats.getWeaponTurnRateBonus().modifyPercent(id, 20f);
            stats.getBeamWeaponTurnRateBonus().modifyPercent(id, 20f);
            stats.getBallisticRoFMult().modifyPercent(id, 25);
            stats.getEnergyRoFMult().modifyPercent(id, 25);
            stats.getBallisticAmmoRegenMult().modifyPercent(id, 100f);
            stats.getEnergyAmmoRegenMult().modifyPercent(id, 100f);

            stats.getTurnAcceleration().modifyFlat(id, 30f * effectLevel);
            stats.getTurnAcceleration().modifyPercent(id, 200f * effectLevel);
            stats.getMaxTurnRate().modifyFlat(id, 15f);
            stats.getMaxTurnRate().modifyPercent(id, 100f);
        }
    }
    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getMaxTurnRate().unmodify(id);
        stats.getTurnAcceleration().unmodify(id);
        stats.getWeaponTurnRateBonus().unmodify(id);
        stats.getBeamWeaponTurnRateBonus().unmodify(id);
        stats.getBallisticRoFMult().unmodify(id);
        stats.getEnergyRoFMult().unmodify(id);
        stats.getBallisticAmmoRegenMult().unmodify(id);
        stats.getEnergyAmmoRegenMult().unmodify(id);

        // Reset so that it starts with jetsActive again
        // Also, reset the interval so the timing works the next time
        jetsActive = true;
        elapsedTime = 0;
    }

    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (jetsActive) {
            if (index == 0) {
                return new StatusData("reduced maneuverability", false);
            } else if (index == 1) {
                return new StatusData("+65 top speed", false);
            }
        }
        else
        {
            if (index == 0) {
                return new StatusData("increased maneuverability", false);
            } else if (index == 1) {
                return new StatusData("increased turret turn rate", false);
            } else if (index == 2) {
                return new StatusData("increased rate of fire", false);
            }
        }
        return null;
    }
}
