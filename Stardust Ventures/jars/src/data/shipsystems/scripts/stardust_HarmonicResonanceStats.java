package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipEngineControllerAPI.ShipEngineAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;
import org.lazywizard.lazylib.combat.CombatUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.awt.Color;

public class stardust_HarmonicResonanceStats extends BaseShipSystemScript {
    public static final float SPEED_INC_FLAT = 10f;
    public static final float SYSTEM_RANGE = 3000f;
    private List<ShipAPI> effectedShips = new ArrayList<>();
    private Color ncolor = new Color(0,85,255,255);

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
            ShipAPI host_ship = (ShipAPI) stats.getEntity();
            String key = host_ship.getId() + "_" + id;
            Object test = Global.getCombatEngine().getCustomData().get(key);
            // Apply the effect on itself
            if (state == ShipSystemStatsScript.State.OUT) {
                stats.getMaxSpeed().unmodify(id); // to slow down ship to its regular top speed while powering drive down
            } else {
                stats.getMaxSpeed().modifyFlat(id, SPEED_INC_FLAT);
            }
            // Extend or retract the flame to match
            if (state == State.IN) {
                if (test == null) {
                    Global.getCombatEngine().getCustomData().put(key, new Object());
                    host_ship.getEngineController().getExtendLengthFraction().advance(0.5f);
                    host_ship.getEngineController().fadeToOtherColor(this, ncolor, new Color(0,0,0,0), effectLevel, 0.67f);
                    for (ShipEngineAPI engine : host_ship.getEngineController().getShipEngines()) {
                        if (engine.isSystemActivated()) {
                            host_ship.getEngineController().setFlameLevel(engine.getEngineSlot(), 0.5f);
                        }
                    }
                }
            } else {
                Global.getCombatEngine().getCustomData().remove(key);
            }

            // Get list of all ships in range
            List<ShipAPI> shipsInRange = CombatUtils.getShipsWithinRange(host_ship.getLocation(), SYSTEM_RANGE);

            // Iterate on all ships in range and apply the effect to any of the same fleet
            Iterator i$ = shipsInRange.iterator();
            while(i$.hasNext()) {
                ShipAPI ship = (ShipAPI)i$.next();
                if (ship.isHulk()) continue; // Skip destroyed ships
                if ( host_ship.getOwner() == ship.getOwner() ) {
                    MutableShipStatsAPI shipStats = ship.getMutableStats();
                    effectedShips.add(ship); // So we can unapply the effects later regardless of where the ships are
                    // Increase the max speed of the effected ship
                    shipStats.getMaxSpeed().modifyFlat(id, SPEED_INC_FLAT);
                    // Increase the flame of the effected ship
                    // This code was taken from the Vanilla Maneuvering Jets code
                    String ship_key = ship.getId() + "_" + id;
                    Object test_engine = Global.getCombatEngine().getCustomData().get(ship_key);
                    if (test_engine == null) {
                        Global.getCombatEngine().getCustomData().put(ship_key, new Object());
                        ship.getEngineController().fadeToOtherColor(this, ncolor, new Color(0,85,255,0), effectLevel, 0.67f);
                        ship.getEngineController().getExtendLengthFraction().advance(0.5f);
                        for (ShipEngineAPI engine : ship.getEngineController().getShipEngines()) {
                            if (engine.isSystemActivated()) {
                                ship.getEngineController().setFlameLevel(engine.getEngineSlot(), 0.5f);
                            }
                        }
                    }
                }


            }
        }

    }

    public void unapply(MutableShipStatsAPI stats, String id) {

        // Unapply from this ship
        stats.getMaxSpeed().unmodify(id);

        // Loop through all other effected ships and unapply from them
        Iterator i$ = effectedShips.iterator();
        while(i$.hasNext()) {
            ShipAPI ship = (ShipAPI)i$.next();
            MutableShipStatsAPI shipStats = ship.getMutableStats();
            shipStats.getMaxSpeed().unmodify(id);
            String ship_key = ship.getId() + "_" + id;
            Global.getCombatEngine().getCustomData().remove(ship_key);
        }

    }

}
