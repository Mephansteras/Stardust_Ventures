package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipEngineControllerAPI.ShipEngineAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;
import org.lazywizard.lazylib.combat.AIUtils;

import java.util.ArrayList;
import java.util.List;

public class stardust_HarmonicResonanceStats extends BaseShipSystemScript {
    public static final float SPEED_INC_PCT = 10f;
    public static final float SYSTEM_RANGE = 4000f;
    private List<ShipAPI> effectedShips;

    /*
        Get the ship using the system
        Get all ships within range (4000?)
            Get all ships from same fleet
                Apply effect
                Save effected ships
                Unapply effect on saved ships

     */

    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
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

        List<ShipAPI> shipsInRange =

        private void applyEffect(ShipAPI target) {

        }
    }

    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getMaxSpeed().unmodify(id);
        stats.getMaxTurnRate().unmodify(id);
        stats.getTurnAcceleration().unmodify(id);
        stats.getAcceleration().unmodify(id);
        stats.getDeceleration().unmodify(id);
    }

}
