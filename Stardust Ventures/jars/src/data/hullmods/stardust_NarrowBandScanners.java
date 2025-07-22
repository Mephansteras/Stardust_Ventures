package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;


public class stardust_NarrowBandScanners extends BaseHullMod {

    public static final float BASEDISTANCE = 6000f;
    public static final float BASEADDITION = 2000f;
    public static final float BASEMAX = 12000f;


    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) return "" + (int) BASEDISTANCE;
        if (index == 1) return "" + (int) BASEADDITION;
        if (index == 2) return "" + (int) BASEMAX;

        return null;
    }
}





