package data.scripts.world.systems;

import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import data.scripts.world.systems.stardust_Talia;
import com.fs.starfarer.api.campaign.SectorGeneratorPlugin;

public class stardust_Gen implements SectorGeneratorPlugin{
    public static void initFactionRelationships(SectorAPI sector) {
        FactionAPI player = sector.getFaction(Factions.PLAYER);
        FactionAPI hegemony = sector.getFaction(Factions.HEGEMONY);
        FactionAPI tritachyon = sector.getFaction(Factions.TRITACHYON);
        FactionAPI pirates = sector.getFaction(Factions.PIRATES);
        FactionAPI independent = sector.getFaction(Factions.INDEPENDENT);
        FactionAPI church = sector.getFaction(Factions.LUDDIC_CHURCH);
        FactionAPI path = sector.getFaction(Factions.LUDDIC_PATH);
        FactionAPI diktat = sector.getFaction(Factions.DIKTAT);
        FactionAPI kol = sector.getFaction(Factions.KOL);
        FactionAPI persean = sector.getFaction(Factions.PERSEAN);
        FactionAPI guard = sector.getFaction(Factions.LIONS_GUARD);
        FactionAPI remnant = sector.getFaction(Factions.REMNANTS);
        FactionAPI derelict = sector.getFaction(Factions.DERELICT);
        FactionAPI stardust_ventures = sector.getFaction("stardust_ventures");

        //vanilla factions
        stardust_ventures.setRelationship(path.getId(), RepLevel.HOSTILE);
        stardust_ventures.setRelationship(hegemony.getId(), RepLevel.FAVORABLE);
        stardust_ventures.setRelationship(pirates.getId(), RepLevel.HOSTILE);
        stardust_ventures.setRelationship(tritachyon.getId(), RepLevel.SUSPICIOUS);
        stardust_ventures.setRelationship(church.getId(), RepLevel.SUSPICIOUS);
        stardust_ventures.setRelationship(kol.getId(), RepLevel.SUSPICIOUS);
        stardust_ventures.setRelationship(persean.getId(), RepLevel.FAVORABLE);
        stardust_ventures.setRelationship(diktat.getId(), RepLevel.FAVORABLE);
        stardust_ventures.setRelationship(guard.getId(), RepLevel.FAVORABLE);
        stardust_ventures.setRelationship(player.getId(), RepLevel.NEUTRAL);
        stardust_ventures.setRelationship(independent.getId(), RepLevel.WELCOMING);

        //vanilla exploration
        stardust_ventures.setRelationship(derelict.getId(), RepLevel.NEUTRAL);
        stardust_ventures.setRelationship(remnant.getId(), RepLevel.SUSPICIOUS);

        //modded factions - going off best guess in many cases
        stardust_ventures.setRelationship("metelson", RepLevel.FAVORABLE);
        stardust_ventures.setRelationship("blackrock_driveyards", RepLevel.SUSPICIOUS);
        stardust_ventures.setRelationship("dassault_mikoyan", RepLevel.SUSPICIOUS);
        stardust_ventures.setRelationship("interstellarimperium", RepLevel.NEUTRAL);
        stardust_ventures.setRelationship("apex_design", RepLevel.NEUTRAL);
        stardust_ventures.setRelationship("diableavionics", RepLevel.SUSPICIOUS);
        stardust_ventures.setRelationship("junk_pirates", RepLevel.HOSTILE);
        stardust_ventures.setRelationship("cabal", RepLevel.HOSTILE);
        stardust_ventures.setRelationship("the_deserter", RepLevel.HOSTILE);
        stardust_ventures.setRelationship("blade_breakers", RepLevel.HOSTILE);
        stardust_ventures.setRelationship("scalartech", RepLevel.FAVORABLE);
        stardust_ventures.setRelationship("kadur_remnant", RepLevel.NEUTRAL);
        stardust_ventures.setRelationship("brighton", RepLevel.NEUTRAL);
        stardust_ventures.setRelationship("hmi", RepLevel.SUSPICIOUS);
        stardust_ventures.setRelationship("tahlan_legioinfernalis", RepLevel.HOSTILE);
        stardust_ventures.setRelationship("tahlan_greathouses", RepLevel.FAVORABLE);
        stardust_ventures.setRelationship("ORA", RepLevel.NEUTRAL);
        stardust_ventures.setRelationship("roider", RepLevel.FAVORABLE);
        stardust_ventures.setRelationship("uaf", RepLevel.NEUTRAL);
        stardust_ventures.setRelationship("ironshell", RepLevel.FAVORABLE);
        stardust_ventures.setRelationship("mayasura", RepLevel.FAVORABLE);
        stardust_ventures.setRelationship("vic", RepLevel.SUSPICIOUS);
        stardust_ventures.setRelationship("ae_ixbattlegroup", RepLevel.INHOSPITABLE);
        stardust_ventures.setRelationship("Goat_Aviation_Bureau", RepLevel.NEUTRAL);
        stardust_ventures.setRelationship("star_federation", RepLevel.FAVORABLE);

        // Void wildlife (Symbiotic Void Creatures)
        stardust_ventures.setRelationship("vwl", RepLevel.WELCOMING);
        stardust_ventures.setRelationship("svc", RepLevel.INHOSPITABLE);



    }

    public void generate(SectorAPI sector) {

        initFactionRelationships(sector);
        new stardust_Talia().generate(sector);


    }
}
