package data.scripts.world.systems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin.DerelictShipData;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.SalvageSpecialAssigner.ShipRecoverySpecialCreator;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial.PerShipData;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial.ShipCondition;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.terrain.DebrisFieldTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.DebrisFieldTerrainPlugin.DebrisFieldParams;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.campaign.SectorGeneratorPlugin;
import data.scripts.campaign.fleets.stardust_PersonalFleetSarvalKaan;
import data.scripts.world.stardust_WorldUtils;
import org.lazywizard.lazylib.MathUtils;
import plugins.stardust_ModPlugin;

import java.util.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import static data.scripts.world.stardust_WorldUtils.*;


public class stardust_Gen implements SectorGeneratorPlugin{

    private static final org.apache.log4j.Logger log = Global.getLogger(stardust_ModPlugin.class);

    private static final Random randomBool = new Random();
    private static final Random random = new Random();

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

    public void generateAfterTime(){

        new stardust_InitialColonies().initColonies();

        SectorAPI sector = Global.getSector();
        if (!sector.hasScript(stardust_PersonalFleetSarvalKaan.class)) {
            sector.addScript(new stardust_PersonalFleetSarvalKaan());
        }
    }

    public boolean generateDebrisIndy(SectorAPI sector)
    {
        // Need to find a red or white dwarf system near the core without a warning beacon
        // Other than being uninhabited, it can be unremarkable

        // Map to store candidate planets and their scores
        Map<StarSystemAPI, Float> candidateSystems = new HashMap<>();
        for (StarSystemAPI system : Global.getSector().getStarSystems()) {

            // Make sure it's actually a star
            if (system.getStar() == null)
            {
                continue;
            }
            // Only looking for dwarf systems here
            if (!system.getStar().getTypeId().equals("star_white_dwarf") && !system.getStar().getTypeId().equals("star_red_dwarf"))
            {
                continue;
            }

            if (!stardust_WorldUtils.isSystemUsable(system, "none", null, false))
            {
                continue;
            }

            // Now we want to get candidates, we're looking for usable systems near the core.
            // Score can be the total distance from the core, lower the better
            float score = getDistance(0.0F, 0.0F, system.getLocation().x, system.getLocation().y);
            // randomize the score just a tad so it's not *always* the closest to the core
            score *= MathUtils.getRandomNumberInRange(0.75f, 1.25f);
            score = Math.round(score);
            candidateSystems.put(system, score);
        }


        StarSystemAPI systemWithLowestScore = getLowestScored(candidateSystems);
        if (systemWithLowestScore != null) {
            log.info("  SDV: Generating indy debris in system " + systemWithLowestScore);
        }
        else
        {
            log.info("  WARNING: SDV - no suitable system found for indy debris gen");
            return false;
        }

        // Once we have the system, we need to decide where to put the debris field.
        // Just have it orbit the star, it's easier. (Was going to be a planet or jumppoint but...that was surprisingly annoying)
        SectorEntityToken orbitFocus = systemWithLowestScore.getStar();

        // Add in an actual debris field so it seems like there was a fight here
        DebrisFieldParams params = new DebrisFieldParams(
                500f, // field radius - should not go above 1000 for performance reasons
                -1f, // density, visual - affects number of debris pieces
                10000000f, // duration in days
                0f); // days the field will keep generating glowing pieces
        params.source = DebrisFieldTerrainPlugin.DebrisFieldSource.MIXED;
        params.baseSalvageXP = 250; // base XP for scavenging in field
        SectorEntityToken debrisIndy = Misc.addDebrisField(systemWithLowestScore, params, StarSystemGenerator.random);
        debrisIndy.setSensorProfile(50.0f);
        debrisIndy.setDiscoverable(true);
        debrisIndy.setCircularOrbit(orbitFocus, 0f, 2500f, 300f);
        float locX = 2000 + random.nextInt(1000) + 1;
        float locY = -2500f - random.nextInt(500) - 1;
        debrisIndy.getLocation().set(locX, locY);
        debrisIndy.setId("stardust_debrisIndy");

        // For these, let's just have the condition and recoverable be random for every ship
        // Spawn in some indy exploration ships, including some SDV stuff
        // Indy destroyers
        List<String> variantList = Arrays.asList("stardust_sunbeamrider_exp", "stardust_sunbeamrider_stock", "stardust_stargazer_seeker", "stardust_stargazer_stock", "stardust_stormseeker_c_stock", "phaeton_Standard", "tarsus_Standard", "gemini_Standard");
        addDerelict(systemWithLowestScore, debrisIndy, getRandomVariant(variantList), getRandomCondition(), 200f, getRandomBoolean());
        // Indy frigates
        variantList = Arrays.asList("cerberus_Shielded", "hound_Standard", "shepherd_Frontier", "stardust_moonlightwalker_stock", "stardust_ionskimmer_stock", "stardust_horizonchaser_stock", "stardust_comettail_mid_stock", "stardust_stormrunner_stock");
        addDerelict(systemWithLowestScore, debrisIndy, getRandomVariant(variantList), getRandomCondition(), 270, getRandomBoolean());
        // make one of them SDV, always
        variantList = Arrays.asList("stardust_plasmaburst_patrol", "stardust_horizonchaser_mil", "stardust_moonlightwalker_bulk", "stardust_moonlightwalker_stock", "stardust_ionskimmer_stock", "stardust_horizonchaser_stock", "stardust_comettail_mid_stock", "stardust_stormrunner_stock");
        addDerelict(systemWithLowestScore, debrisIndy, getRandomVariant(variantList), getRandomCondition(), 375, getRandomBoolean());


        // Spawn in a few pirate ships, mostly frigates plus a destroyer
        variantList = Arrays.asList("mule_d_pirates_Smuggler", "mule_d_pirates_Standard", "buffalo_pirates_Standard", "stardust_starlightguard_pirate_hellbore");
        addDerelict(systemWithLowestScore, debrisIndy, getRandomVariant(variantList), getRandomCondition(), 425, getRandomBoolean());
        // Indy frigates
        variantList = Arrays.asList("hound_d_pirates_Overdriven", "hound_d_pirates_Standard", "hound_d_pirates_Shielded", "cerberus_d_pirates_Standard", "wolf_d_pirates_Attack", "vanguard_pirates_Strike", "mudskipper2_Hellbore", "kite_pirates_Raider");
        addDerelict(systemWithLowestScore, debrisIndy, getRandomVariant(variantList), getRandomCondition(), 325, getRandomBoolean());
        addDerelict(systemWithLowestScore, debrisIndy, getRandomVariant(variantList), getRandomCondition(), 500, getRandomBoolean());

        return true;
    }

    private static StarSystemAPI getLowestScored(Map<StarSystemAPI, Float> candidateSystems)
    {
        if (candidateSystems == null || candidateSystems.isEmpty()) {
            return null;
        }

        StarSystemAPI useSystem = null;
        Float lowestFloat = null;

        for (Entry<StarSystemAPI, Float> entry : candidateSystems.entrySet()) {
            StarSystemAPI currentSystem = entry.getKey();
            Float currentFloat = entry.getValue();

            if (lowestFloat == null || currentFloat < lowestFloat) {
                lowestFloat = currentFloat;
                useSystem = currentSystem;
            }
        }

        return useSystem;
    }

    private static boolean getRandomBoolean() {
        return randomBool.nextBoolean();
    }

    private static String getRandomVariant(List<String> list)
    {
        if (list == null || list.isEmpty()) {
            return null; // or throw an exception, depending on your needs
        }
        int index = random.nextInt(list.size());
        return list.get(index);
    }

    private static ShipCondition getRandomCondition()
    {
        List<ShipCondition> conditionsAvailable = Arrays.asList(ShipCondition.AVERAGE, ShipCondition.BATTERED);
        int index = random.nextInt(conditionsAvailable.size());
        return conditionsAvailable.get(index);
    }

    public boolean generateDebrisMonoco(SectorAPI sector)
    {
        // Need to find system with a medium warning beacon, ideally as close as possible to the Talia sysem
        // If we don't have Talia, we'll use Zagan. If no Zagan then just close to 0,0

        // Map to store candidate planets and their scores
        Map<StarSystemAPI, Float> candidateSystems = new HashMap<>();
        for (StarSystemAPI system : Global.getSector().getStarSystems()) {

            // Make sure it's actually a star
            if (system.getStar() == null)
            {
                continue;
            }

            // Probably don't need all these checks, just being a medium beacon system should be ok
            //if (!stardust_WorldUtils.isSystemUsable(system, "medium", null, null))
            //{
            //    continue;
            //}
            //log.info("DEBUG SDV: Beacon for system " + system + " : " + hasBeacon(system));
            if (!hasBeacon(system).equals("medium"))
            {
                continue;
            }

            // Now we want to get candidates, we're looking for usable systems near the core.
            // Score can be the total distance from the core, lower the better
            float sourceX = 0.0f;
            float sourceY = 0.0f;
            if (sector.getEntityById("stardust_talia") != null)
            {
                sourceX = sector.getEntityById("stardust_talia").getLocation().x;
                sourceY = sector.getEntityById("stardust_talia").getLocation().y;
            }
            else if (sector.getEntityById("Zagan") != null)
            {
                sourceX = sector.getEntityById("Zagan").getLocation().x;
                sourceY = sector.getEntityById("Zagan").getLocation().y;
            }
            float score = getDistance(sourceX, sourceY, system.getLocation().x, system.getLocation().y);
            // randomize the score just a tad so it's not *always* the closest
            score *= MathUtils.getRandomNumberInRange(0.75f, 1.25f);
            score = Math.round(score);
            candidateSystems.put(system, score);
        }


        StarSystemAPI systemWithLowestScore = getLowestScored(candidateSystems);
        if (systemWithLowestScore != null) {
            log.info("SDV: Generating monoco debris in system " + systemWithLowestScore);
        }
        else
        {
            log.info("WARNING: SDV - no suitable system found for monoco debris gen");
            return false;
        }

        // Will need to add a few ships here, SDV and Pirate
        // maybe a bit of randomness here, so not all the same ships show up every playthrough
        // mostly smaller warships, maybe a cruiser

        // Then, add in debris field two (SDV vs Remnants)
        // The Monoco flagship is here, plus a few escort/logi ships and remnants

        // Once we have the system, we need to decide where to put the debris fields.
        // Just have it orbit the star, it's easier. (Was going to be a planet or jumppoint but...that was surprisingly annoying)
        SectorEntityToken orbitFocus = systemWithLowestScore.getStar();

        // Add in an actual debris field so it seems like there was a fight here
        DebrisFieldParams params = new DebrisFieldParams(
                500f, // field radius - should not go above 1000 for performance reasons
                -1f, // density, visual - affects number of debris pieces
                10000000f, // duration in days
                0f); // days the field will keep generating glowing pieces
        params.source = DebrisFieldTerrainPlugin.DebrisFieldSource.MIXED;
        params.baseSalvageXP = 250; // base XP for scavenging in field
        SectorEntityToken debrisMonocoOne = Misc.addDebrisField(systemWithLowestScore, params, StarSystemGenerator.random);
        debrisMonocoOne.setSensorProfile(50.0f);
        debrisMonocoOne.setDiscoverable(true);
        debrisMonocoOne.setCircularOrbit(orbitFocus, 0f, 5000, 300f);
        float locX = 5000 + random.nextInt(1000) + 1;
        float locY = -3500f - random.nextInt(500) - 1;
        debrisMonocoOne.getLocation().set(locX, locY);
        debrisMonocoOne.setId("stardust_debrisMonocoOne");

        // For these, let's just have the condition and recoverable be random for every ship
        // Only four ships here, so we don't add *too* much loot to the system
        List<String> variantList = Arrays.asList("stardust_vortexhunter_brawler", "stardust_stormseeker_stock", "stardust_stormseeker_attack", "stardust_stargazer_stock", "stardust_stormseeker_c_stock", "stardust_stormseeker_picket", "stardust_starlightguard_stock", "stardust_starlightguard_flak");
        addDerelict(systemWithLowestScore, debrisMonocoOne, getRandomVariant(variantList), getRandomCondition(), 200f, getRandomBoolean());
        variantList = Arrays.asList("stardust_plasmaburst_patrol", "stardust_horizonchaser_mil", "stardust_stormrunner_assault", "stardust_comettail_support", "stardust_ionskimmer_stock", "stardust_plasmaburst_stock", "stardust_stormrunner_mid_attack", "stardust_stormrunner_stock");
        addDerelict(systemWithLowestScore, debrisMonocoOne, getRandomVariant(variantList), getRandomCondition(), 375, getRandomBoolean());


        // Spawn in a few pirate ships, a capital or cruiser, then a frigate or destroyer
        variantList = Arrays.asList("atlas2_Standard", "eradicator_pirates_Overdriven");
        addDerelict(systemWithLowestScore, debrisMonocoOne, getRandomVariant(variantList), getRandomCondition(), 425, getRandomBoolean());
        variantList = Arrays.asList("hound_d_pirates_Overdriven", "hound_d_pirates_Standard", "hound_d_pirates_Shielded", "cerberus_d_pirates_Standard", "wolf_d_pirates_Attack", "vanguard_pirates_Strike", "mule_d_pirates_Standard", "stardust_starlightguard_pirate_hellbore");
        addDerelict(systemWithLowestScore, debrisMonocoOne, getRandomVariant(variantList), getRandomCondition(), 325, getRandomBoolean());


        // Add in an actual debris field so it seems like there was a fight here
        params = new DebrisFieldParams(
                500f, // field radius - should not go above 1000 for performance reasons
                -1f, // density, visual - affects number of debris pieces
                10000000f, // duration in days
                0f); // days the field will keep generating glowing pieces
        params.source = DebrisFieldTerrainPlugin.DebrisFieldSource.MIXED;
        params.baseSalvageXP = 250; // base XP for scavenging in field
        SectorEntityToken debrisMonocoTwo = Misc.addDebrisField(systemWithLowestScore, params, StarSystemGenerator.random);
        debrisMonocoTwo.setSensorProfile(50.0f);
        debrisMonocoTwo.setDiscoverable(true);
        debrisMonocoTwo.setCircularOrbit(orbitFocus, 0f, 10000, 400);
        locX = 10000 + random.nextInt(2000) + 1;
        locY = 10000 - random.nextInt(2000) - 1;
        debrisMonocoTwo.getLocation().set(locX, locY);
        debrisMonocoTwo.setId("stardust_debrisMonocoTwo");

        // The Monoco Stormbringer is going to be here, with some non-random settings
        // There will also be a couple of SDV ships with it, randomized
        addDerelict(systemWithLowestScore, debrisMonocoTwo, "stardust_stormbringer_m_lancer", ShipCondition.BATTERED, 200f, true);
        // Larger escort ship
        variantList = Arrays.asList("stardust_stormherald_stock", "stardust_twilightdefiance_breaker", "stardust_oorttraveler_armor_patrol", "stardust_stormseeker_c_stock", "stardust_cosmicwind_assault", "stardust_starlightguard_escort", "stardust_stormseeker_strike", "stardust_stormseeker_mid_escort");
        addDerelict(systemWithLowestScore, debrisMonocoTwo, getRandomVariant(variantList), getRandomCondition(), 270, getRandomBoolean());
        // Smaller escort or logistics ship
        variantList = Arrays.asList("stardust_stormseeker_stock", "stardust_stormrunner_stock", "stardust_moonlightwalker_bulk", "stardust_moonlightwalker_stock", "stardust_ionskimmer_stock", "stardust_sunbeamrider_exp", "stardust_comettail_support", "stardust_stargazer_seeker");
        addDerelict(systemWithLowestScore, debrisMonocoTwo, getRandomVariant(variantList), getRandomCondition(), 375, getRandomBoolean());


        // Spawn in a few remnant ships. A cruiser first
        variantList = Arrays.asList("brilliant_Standard", "brilliant_Support");
        addDerelict(systemWithLowestScore, debrisMonocoTwo, getRandomVariant(variantList), getRandomCondition(), 425, false);
        // And some smaller ones
        variantList = Arrays.asList("fulgent_Assault", "fulgent_Support", "glimmer_Assault", "glimmer_Support", "lumen_Standard", "lumen_Standard");
        addDerelict(systemWithLowestScore, debrisMonocoTwo, getRandomVariant(variantList), getRandomCondition(), 325, false);
        addDerelict(systemWithLowestScore, debrisMonocoTwo, getRandomVariant(variantList), getRandomCondition(), 500, false);

        return true;
    }



    // Taken from the vanilla Galatia system code
    // Should have a special version of this for the Monoco so it can be named, have S-mods, etc. But for now this is ok.
    protected void addDerelict(StarSystemAPI system, SectorEntityToken focus, String variantId,
                               ShipCondition condition, float orbitRadius, boolean recoverable) {
        DerelictShipData params = new DerelictShipData(new PerShipData(variantId, condition, 0f), false);
        SectorEntityToken ship = BaseThemeGenerator.addSalvageEntity(system, Entities.WRECK, Factions.NEUTRAL, params);
        ship.setDiscoverable(true);

        float orbitDays = orbitRadius / (10f + (float) Math.random() * 5f);
        ship.setCircularOrbit(focus, (float) Math.random() * 360f, orbitRadius, orbitDays);

        if (recoverable) {
            ShipRecoverySpecialCreator creator = new ShipRecoverySpecialCreator(null, 0, 0, false, null, null);
            Misc.setSalvageSpecial(ship, creator.createSpecial(ship, null));
        }

    }
}
