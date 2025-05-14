package data.scripts.world;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.util.Misc;
import plugins.stardust_ModPlugin;
//import com.fs.starfarer.api.util.MathUtils;

import java.util.Map;

public class stardust_WorldUtils {

    private static final org.apache.log4j.Logger log = Global.getLogger(stardust_ModPlugin.class);

    // Call this when you spawn the object
    public static void markObjectSpawned(String key) {
        Map<String, Object> data = Global.getSector().getPersistentData();
        data.put(key, true);
    }

    // Call this to check if the object has already been spawned
    public static boolean hasObjectBeenSpawned(String key) {
        Map<String, Object> data = Global.getSector().getPersistentData();
        Object val = data.get(key);
        return val instanceof Boolean && (Boolean) val;
    }

    public static String hasBeacon(StarSystemAPI system) {
        if (system.hasTag(Tags.BEACON_LOW) || hasLowBeacon(system))
            {
                //log.info("DEBUG: Found low beacon around " + system);
                return "low";
            }
            if (system.hasTag(Tags.BEACON_MEDIUM) || hasMediumBeacon(system)) {
                //log.info("DEBUG: Found medium beacon around " + system);
                return "medium";
            }
            if (system.hasTag(Tags.BEACON_HIGH) || hasHighBeacon(system)) {
                //log.info("DEBUG: Found high beacon around " + system);
                return "high";
            }
            // If we haven't found a regular beacon but do get something here (like the teal sotf ones), return special
            if (hasAnyBeacon(system)) {
                //log.info("DEBUG: Found special beacon around " + system);
                return "special";
            }

            return "none";
    }

    public static boolean hasLowBeacon(StarSystemAPI system) {
        SectorEntityToken hyperspaceAnchor = system.getHyperspaceAnchor();
        if (hyperspaceAnchor == null) return false;

        for (SectorEntityToken entity : Global.getSector().getHyperspace().getEntitiesWithTag(Tags.BEACON_LOW)) {
            float distance = Misc.getDistanceLY(hyperspaceAnchor.getLocation(), entity.getLocation());
            if (distance < 1.1f) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasMediumBeacon(StarSystemAPI system) {
        SectorEntityToken hyperspaceAnchor = system.getHyperspaceAnchor();
        if (hyperspaceAnchor == null) return false;

        for (SectorEntityToken entity : Global.getSector().getHyperspace().getEntitiesWithTag(Tags.BEACON_MEDIUM)) {
            float distance = Misc.getDistanceLY(hyperspaceAnchor.getLocation(), entity.getLocation());
            if (distance < 1.1f) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasHighBeacon(StarSystemAPI system) {
        SectorEntityToken hyperspaceAnchor = system.getHyperspaceAnchor();
        if (hyperspaceAnchor == null) return false;

        for (SectorEntityToken entity : Global.getSector().getHyperspace().getEntitiesWithTag(Tags.BEACON_HIGH)) {
            float distance = Misc.getDistanceLY(hyperspaceAnchor.getLocation(), entity.getLocation());
            if (distance < 1.1f) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasAnyBeacon(StarSystemAPI system) {
        SectorEntityToken hyperspaceAnchor = system.getHyperspaceAnchor();
        if (hyperspaceAnchor == null) return false;

        for (SectorEntityToken entity : Global.getSector().getHyperspace().getEntitiesWithTag(Tags.WARNING_BEACON)) {
            float distance = Misc.getDistanceLY(hyperspaceAnchor.getLocation(), entity.getLocation());
            if (distance < 1.1f) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasFleets(StarSystemAPI system) {
        for (CampaignFleetAPI fleet : system.getFleets()) {
            if (fleet.isStationMode() || fleet.getFleetPoints() > 10) {
                return true;
            }
        }
        return false;
    }


    // could be expanded to be more generic, but good enough for now
    public static boolean isSystemUsable(StarSystemAPI system, String hasBeaconLevel, Boolean hasPlanets, Boolean hasLargeFleets)
    {
        // We pretty much never want these
        if (system.hasTag(Tags.SYSTEM_CUT_OFF_FROM_HYPER) ||
            system.hasTag(Tags.THEME_CORE) ||
            system.hasTag("theme_core_populated") || system.hasTag(Tags.THEME_HIDDEN)) {
            return false;
        }
        if (system.hasPulsar()) return false;
        // skip this check for Medium or High beacons, since those can have remnant stations
        if (!hasBeaconLevel.equals("medium") && !hasBeaconLevel.equals("high"))
        {
            if (!Global.getSector().getEconomy().getMarkets(system).isEmpty()) return false;
        }

        // These depend on what we're looking for
        if (Boolean.TRUE.equals(hasPlanets)) {
            if (system.getPlanets().isEmpty())  return false;
        } else if (Boolean.FALSE.equals(hasPlanets)) {
            if (!system.getPlanets().isEmpty())  return false;}
        // if null, we don't check it

        if (Boolean.TRUE.equals(hasLargeFleets)) {
            if (!hasFleets(system))  return false;
        } else if (Boolean.FALSE.equals(hasLargeFleets)) {
            if (hasFleets(system))  return false;}
        // if null, we don't check it

        String systemHasBeacon = hasBeacon(system);
        if (!hasBeaconLevel.equals(systemHasBeacon) ) {
            return false;
        }

        return true;
    }

    // Helper: Get distance between points
    public static float getDistance(float sourceX, float sourceY, float destX, float destY) {
        float dx = destX - sourceX;
        float dy = destY - sourceY;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

}
