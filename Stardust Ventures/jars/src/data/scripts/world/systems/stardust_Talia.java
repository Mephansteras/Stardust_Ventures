package data.scripts.world.systems;

import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.procgen.NebulaEditor;
import com.fs.starfarer.api.impl.campaign.procgen.StarAge;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.terrain.MagneticFieldTerrainPlugin.MagneticFieldParams;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
public class stardust_Talia implements SectorGeneratorPlugin { //A SectorGeneratorPlugin is a class from the game, that identifies this as a script that will have a 'generate' method
    @Override
    public void generate(SectorAPI sector) { //the parameter sector is passed. This is the instance of the campaign map that this script will add a star system to
        //initialise system
        StarSystemAPI system = sector.createStarSystem("Talia"); //create a new variable called system. this is assigned an instance of the new star system added to the Sector at the same time
        system.getLocation().set(9000, 8000); //sets location of system in hyperspace. map size is in the order of 100000x100000, and 0, 0 is the center of the map, this will set the location to the east and slightly south of the center
        system.setBackgroundTextureFilename("graphics/backgrounds/background1.jpg"); //sets the background image for when in the system. this is a filepath to an image in the core game files

        //set up star
        PlanetAPI star = system.initStar( //stars and planets are technically the same category of object, so stars use PlanetAPI
                "stardust_talia", //set star id, this should be unique
                "star_orange", //set star type, the type IDs come from starsector-core/data/campaign/procgen/star_gen_data.csv
                750f,
                500, // extent of corona outside star
                10f, // solar wind burn level
                1f, // flare probability
                3f); // CR loss multiplier, good values are in the range of 1-5

        //generate up to three entities in the centre of the system and returns the orbit radius of the furthest entity
        float innerOrbitDistance = StarSystemGenerator.addOrbitingEntities(
                system, //star system variable, used to add entities
                star, //focus object for entities to orbit
                StarAge.AVERAGE, //used by generator to decide which kind of planets to add
                3, //minimum number of entities
                3, //maximum number of entities
                2000, //the radius between the first generated entity and the focus object, in this case the star
                1, //used to assign roman numerals to the generated entities if not given special names
                true //generator will give unique names like "Ordog" instead of "Example Star System III"
        );

        // Add Comm Relay
        SectorEntityToken talia_relay = system.addCustomEntity("talia_relay", "Talia Relay", "comm_relay", "stardust_ventures");
        talia_relay.setCircularOrbitPointingDown(system.getEntityById("stardust_talia"), 90 - 60, innerOrbitDistance + 800, 90);

        //add first static planet
        PlanetAPI pentamerone = system.addPlanet( //assigns instance of newly created planet to variable planetOne
                "stardust_pentamerone", //unique id string
                star, //orbit focus for planet
                "Pentamerone", //display name of planet
                "gas_giant", //planet type id, comes from starsector-core/data/campaign/procgen/planet_gen_data.csv
                120f, //starting angle in orbit
                350f, //planet size
                innerOrbitDistance + 1750, //1750 radius gap from the outer randomly generated entity created above
                415 //number of in-game days for it to orbit once

        );
        // Copied these from Zagan's gas giant
        pentamerone.getSpec().setGlowTexture(Global.getSettings().getSpriteName("hab_glows", "banded"));
        pentamerone.getSpec().setGlowColor(new Color(255, 20, 200, 55));
        pentamerone.getSpec().setAtmosphereThickness(0.2f);
        pentamerone.getSpec().setUseReverseLightForGlow(true);
        pentamerone.getSpec().setPitch(-30f);
        pentamerone.getSpec().setTilt(20f);
        pentamerone.getSpec().setPlanetColor(new Color(200, 55, 240, 255));
        pentamerone.applySpecChanges();

        SectorEntityToken pentamerone_magfield = system.addTerrain(Terrain.MAGNETIC_FIELD,
                new MagneticFieldParams(pentamerone.getRadius() + 150f, // terrain effect band width
                        (pentamerone.getRadius() + 150f) / 2f, // terrain effect middle radius
                        pentamerone, // entity that it's around
                        pentamerone.getRadius() + 50f, // visual band start
                        pentamerone.getRadius() + 50f + 200f, // visual band end
                        new Color(50, 20, 100, 50), // base color
                        0.3f, // probability to spawn aurora sequence, checked once/day when no aurora in progress
                        new Color(90, 180, 40),
                        new Color(130, 145, 90),
                        new Color(165, 110, 145),
                        new Color(95, 55, 160),
                        new Color(45, 0, 130),
                        new Color(20, 0, 130),
                        new Color(10, 0, 150)));
        pentamerone_magfield.setCircularOrbit(pentamerone, 0, 0, 100);

        // These two planets are both moons of Pentamerone
        PlanetAPI venture_industries = system.addPlanet("stardust_venture_industries", pentamerone, "Venture Industries", "barren", 0, 75, 750, 30);
        venture_industries.getSpec().setGlowTexture(Global.getSettings().getSpriteName("hab_glows", "barren"));
        venture_industries.getSpec().setGlowColor(new Color(255, 255, 255, 255));
        venture_industries.setCustomDescriptionId("stardust_venture_industries");

        // Market details for Venture Industries
        MarketAPI venture_industries_market = addMarketplace.addMarketplace("stardust_ventures", venture_industries,
                null, //connected entities, like stations
                "Venture Industries",
                4, //size
                new ArrayList<>(Arrays.asList( //these are conditions
                        Conditions.POPULATION_4,
                        Conditions.NO_ATMOSPHERE,
                        Conditions.ORE_RICH
                )),
                new ArrayList<>(Arrays.asList( //industries
                        Industries.POPULATION,
                        Industries.SPACEPORT,
                        Industries.BATTLESTATION_MID,
                        Industries.GROUNDDEFENSES,
                        Industries.LIGHTINDUSTRY,
                        Industries.ORBITALWORKS,
                        Industries.WAYSTATION,
                        Industries.PATROLHQ

                )),
                new ArrayList<>(Arrays.asList(Submarkets.SUBMARKET_STORAGE, //aaand markets
                        Submarkets.SUBMARKET_BLACK,
                        Submarkets.SUBMARKET_OPEN)),
                0.15f
        );
        // Give Venture Industries a Stardust submarket (since those have the most SDV stuff for sale)
        venture_industries_market.addSubmarket("stardust_market");
        // Give Venture Industries a corrupted nanoforge
        venture_industries_market.getIndustry(Industries.ORBITALWORKS).setSpecialItem(new SpecialItemData(Items.CORRUPTED_NANOFORGE, null));

        // Now for HQ
        PlanetAPI stardust_hall = system.addPlanet("stardust_stardust_hall", pentamerone, "Stardust Hall", "water", 45, 90, 1000, 40);
        stardust_hall.getSpec().setGlowTexture(Global.getSettings().getSpriteName("hab_glows", "volturn"));
        stardust_hall.getSpec().setGlowColor(new Color(0, 255, 150, 255));
        stardust_hall.getSpec().setUseReverseLightForGlow(true);
        stardust_hall.applySpecChanges();
        //stardust_hall.setInteractionImage("illustrations", "ilm");
        stardust_hall.setCustomDescriptionId("stardust_stardust_hall");

        // Market details for Stardust Hall
        MarketAPI stardust_hall_market = addMarketplace.addMarketplace("stardust_ventures", stardust_hall,
                null, //connected entities, like stations
                "Stardust Hall",
                5, //size
                new ArrayList<>(Arrays.asList( //these are conditions
                        Conditions.POPULATION_5,
                        Conditions.REGIONAL_CAPITAL,
                        Conditions.WATER_SURFACE,
                        Conditions.MILD_CLIMATE,
                        Conditions.HABITABLE,
                        Conditions.ORGANICS_TRACE
                )),
                new ArrayList<>(Arrays.asList( //industries
                        Industries.POPULATION,
                        Industries.MEGAPORT,
                        Industries.BATTLESTATION_MID,
                        Industries.GROUNDDEFENSES,
                        Industries.MILITARYBASE,
                        Industries.AQUACULTURE,
                        Industries.WAYSTATION,
                        Industries.COMMERCE

                )),
                new ArrayList<>(Arrays.asList(Submarkets.SUBMARKET_STORAGE, //aaand markets
                        Submarkets.GENERIC_MILITARY,
                        Submarkets.SUBMARKET_BLACK,
                        Submarkets.SUBMARKET_OPEN)),
                0.15f
        );


        // This is our indy planet, since SDV and idies are friends
        PlanetAPI brabant = system.addPlanet("stardust_brabant", star, "Brabant", "rocky_metallic", 30, 65, innerOrbitDistance + 3750, 500);
        brabant.setCustomDescriptionId("stardust_brabant");

        // Market details for Brabant
        MarketAPI brabant_market = addMarketplace.addMarketplace("independent", brabant,
                null, //connected entities, like stations
                "Brabant",
                4, //size
                new ArrayList<>(Arrays.asList( //these are conditions
                        Conditions.POPULATION_4,
                        Conditions.NO_ATMOSPHERE,
                        Conditions.ORE_RICH,
                        Conditions.RARE_ORE_ABUNDANT,
                        Conditions.LOW_GRAVITY
                )),
                new ArrayList<>(Arrays.asList( //industries
                        Industries.POPULATION,
                        Industries.SPACEPORT,
                        Industries.REFINING,
                        Industries.MINING,
                        Industries.WAYSTATION,
                        Industries.PATROLHQ

                )),
                new ArrayList<>(Arrays.asList(Submarkets.SUBMARKET_STORAGE, //aaand markets
                        Submarkets.SUBMARKET_BLACK,
                        Submarkets.SUBMARKET_OPEN)),
                0.15f
        );

        // The indies get to have the nav buoy
        SectorEntityToken talia_buoy = system.addCustomEntity(null, null, "nav_buoy_makeshift", Factions.INDEPENDENT);
        talia_buoy.setCircularOrbitPointingDown(system.getEntityById("stardust_talia"), -60, innerOrbitDistance + 4300, 550);

        // Add some more random stuff after Brabant
        float radiusAfter2 = StarSystemGenerator.addOrbitingEntities(system, star, StarAge.AVERAGE,
                3, 3, // min/max entities to add
                innerOrbitDistance + 7600, // radius to start adding at
                4, // name offset - next planet will be <system name> <roman numeral of this parameter + 1>
                true, // whether to use custom or system-name based names
                false); // whether to allow habitable worlds

        // jump-point
        JumpPointAPI jumpPoint2 = Global.getFactory().createJumpPoint("brabant_jump", "Brabant Jump-point");
        jumpPoint2.setCircularOrbit( star, 180 + 60, 9500, 580);
        jumpPoint2.setRelatedPlanet(brabant);
        system.addEntity(jumpPoint2);

        // Finally, we add in the Sensor Array out on the edges
        SectorEntityToken sensorArray = system.addCustomEntity(
                "talia_domain_sensor",
                "Talia Sensor Array",
                Entities.SENSOR_ARRAY,
                "stardust_ventures"
        );
        //assign an orbit, point down ensures it rotates to point towards center while orbiting
        sensorArray.setCircularOrbitPointingDown(star, 90f, radiusAfter2 + 1600, 600f);

        SectorEntityToken gate = system.addCustomEntity("talia_gate", "Talia Gate", // name - if null, defaultName from custom_entities.json will be used
                "inactive_gate", // type of object, defined in custom_entities.json
                null);
        gate.setCircularOrbit(star, 35 - 90, 9500, 700);

        system.autogenerateHyperspaceJumpPoints(true, true); //generates jump points

        HyperspaceTerrainPlugin plugin = (HyperspaceTerrainPlugin) Misc.getHyperspaceTerrain().getPlugin(); //these lines clear the hyperspace clouds around the system
        NebulaEditor editor = new NebulaEditor(plugin);
        float minRadius = plugin.getTileSize() * 2f;

        float radius = system.getMaxRadiusInHyperspace();
        editor.clearArc(system.getLocation().x, system.getLocation().y, 0, radius + minRadius, 0, 360f);
        editor.clearArc(system.getLocation().x, system.getLocation().y, 0, radius + minRadius, 0, 360f, 0.25f);
    }

}
