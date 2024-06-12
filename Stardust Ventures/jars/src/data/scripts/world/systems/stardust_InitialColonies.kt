package data.scripts.world.systems

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.PersonImportance
import com.fs.starfarer.api.campaign.PlanetAPI
import com.fs.starfarer.api.campaign.StarSystemAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.characters.FullName
import com.fs.starfarer.api.characters.PersonAPI
import com.fs.starfarer.api.impl.campaign.ids.*
import com.fs.starfarer.api.impl.campaign.population.PopulationComposition
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator
import exerelin.campaign.SectorManager
import org.lazywizard.lazylib.MathUtils
import plugins.stardust_ModPlugin.Companion.log
import kotlin.math.abs
import kotlin.math.round


// Finds and colonizes an appropriate planet out from the core
class stardust_InitialColonies {
    // Variables, lists, etc
    protected var CandidatePlanets: MutableMap<PlanetAPI, Float> = HashMap()

    // May want to have stuff controlled by settings. (# colonies, namelist, min/max distances, etc)
    //getCandidatePlanets(20000f, 40000f);
    private fun hasFleets(system: StarSystemAPI): Boolean {
        // Taken from the Nex colonization code. Avoids any system with stations or large fleets
        for (fleet in system.fleets) {
            if (fleet.isStationMode) return true
            if (fleet.fleetPoints > 10) return true
        }
        return false
    }

    private fun getCandidatePlanets(MinDist: Double, MaxDist: Double) {
        // Trying to stay close to the core but not too close, so Min of ~20,000.
        // Max is a 'nice to have', but will go further if nothing closer works.
        for (system in Global.getSector().starSystems) {
            // First, ditch any that are simply unusable
             var skip = false
            if (system.hasTag(Tags.SYSTEM_CUT_OFF_FROM_HYPER)) skip = true
            if (system.hasPulsar()) skip = true
            if (system.planets.isEmpty()) skip = true
            if (!Global.getSector().economy.getMarkets(system).isEmpty()) skip = true  //System was inhabited
            if (hasFleets(system)) skip = true
            var sys_x = system.location.getX()
            var sys_y = system.location.getY()
            if ( (abs(sys_x) < MinDist) or (abs(sys_x) > MaxDist) ) skip = true
            if ( (abs(sys_y) < MinDist) or (abs(sys_y) > MaxDist) ) skip = true
            if (skip)
            {
                //log.info("  Skipping system ${system.name}  x: $sys_x y: $sys_y")
                continue
            }
            val sys_name = system.name

            for (planet in system.planets) {
                val market = planet.market
                val planet_name = planet.name
                if (market == null || market.isInEconomy) continue
                if (market.hasCondition("pre_collapse_facility")) continue

                // Should be 75-225 or so
                var score = market.hazardValue * 100
                log.info("  Hazard value for planet $planet_name score:$score from $sys_name");
                // Adjust based on a few specific conditions. They are going to favor mild food growing planets most.
                // Lower score is better
                if (market.hasCondition(Conditions.MILD_CLIMATE)) {
                    score -= 10f
                    log.info("      Has Mild Climate")
                }
                if (market.hasCondition(Conditions.HABITABLE)) {
                    score -= 10f
                    log.info("      Is Habitable")
                }
                if (market.hasCondition(Conditions.FARMLAND_POOR)) {
                    score -= 5f
                    log.info("      Has Poor Farmland")
                }
                if (market.hasCondition(Conditions.FARMLAND_ADEQUATE)) {
                    score -= 10f
                    log.info("      Has Adequate Farmland")
                }
                if (market.hasCondition(Conditions.FARMLAND_RICH)) {
                    score -= 15f
                    log.info("      Has Rich Farmland")
                }
                if (market.hasCondition(Conditions.FARMLAND_BOUNTIFUL)) {
                    score -= 20f
                    log.info("      Has Bountiful Farmland")
                }

                // randomizes things so they don't just grab the best planets every time
                score *= MathUtils.getRandomNumberInRange(0.75f, 1.25f)
                score = round(score)

                log.info("    Adding planet $planet_name score:$score from $sys_name")
                CandidatePlanets[planet] = score
            }
        }
    }

    private fun getBestPlanet(): PlanetAPI? {
        var bestScore = 1000 // lower scores are better
        var bestPlanet: PlanetAPI? = null

        for (candidate in CandidatePlanets){
            if (candidate.value.toInt() < bestScore)
            {
                bestScore = candidate.value.toInt()
                bestPlanet = candidate.key
            }
        }

        CandidatePlanets.remove(bestPlanet)

        val planet_name = bestPlanet?.name
        log.info("  Using planet $planet_name score: $bestScore")
        return bestPlanet
    }

    private fun addPerson(market: MarketAPI, rank: String?, post: String): PersonAPI? {
        var ip = Global.getSector().importantPeople
        val person = market.faction.createRandomPerson()
        person.rankId = rank
        person.postId = post
        market.commDirectory.addPerson(person)
        market.addPerson(person)
        ip.addPerson(person)
        ip.getData(person).location.market = market
        ip.checkOutPerson(person, "permanent_staff")
        if (post == Ranks.POST_BASE_COMMANDER)
        {
            person.setImportanceAndVoice(PersonImportance.MEDIUM, StarSystemGenerator.random)
        }
        else
        {
            person.setImportanceAndVoice(PersonImportance.VERY_LOW, StarSystemGenerator.random)
        }

        return person
    }

    // Takes the best candidate planet and colonizes it. Size 3, farming, waystation, spaceport, and patrol HQ.
    // Borrowed a lot of this from the Nex colonization code
    private fun addColony(market: MarketAPI, planet: PlanetAPI, name: String, descID: String){
        val factionId = "stardust_ventures"

        market.size = 3
        market.addCondition("population_3")
        market.factionId = factionId
        market.isPlanetConditionMarketOnly = false
        if (market.hasCondition(Conditions.DECIVILIZED))
        {
            market.removeCondition(Conditions.DECIVILIZED);
            market.addCondition(Conditions.DECIVILIZED_SUBPOP);
        }
        market.addIndustry(Industries.POPULATION)
        market.addIndustry(Industries.SPACEPORT)
        market.addIndustry(Industries.WAYSTATION)
        market.addIndustry(Industries.PATROLHQ)
        // DEBUG - list conditions
        for (cond in market.conditions)
        {
            log.info("        Found Condition $cond")
        }
        // Aquaculture for water worlds, Farming if you can have it, Light industry as a fallback
        if (planet.typeId.contains("water"))
            market.addIndustry(Industries.AQUACULTURE)
        else if (market.hasCondition(Conditions.FARMLAND_POOR) or market.hasCondition(Conditions.FARMLAND_ADEQUATE) or market.hasCondition(Conditions.FARMLAND_RICH) or market.hasCondition(Conditions.FARMLAND_BOUNTIFUL))
            market.addIndustry(Industries.FARMING)
        else if (market.hasCondition(Conditions.ORE_MODERATE) or market.hasCondition(Conditions.ORE_ABUNDANT) or market.hasCondition(Conditions.ORE_RICH) or market.hasCondition(Conditions.ORE_ULTRARICH))
            market.addIndustry(Industries.MINING)
        else
            market.addIndustry(Industries.LIGHTINDUSTRY)
        // Just in case we decide to allow for more colonies than the default 2
        if(name != null) {
            market.name = name
            planet.name = name
        }

        if(descID != null) {
            planet.customDescriptionId = descID
        }

        // fixes insta colony upsize; see https://fractalsoftworks.com/forum/index.php?topic=5061.msg343893#msg343893
        market.incoming = PopulationComposition()

        market.tariff.modifyFlat("generator", Global.getSector().getFaction(factionId).tariffFraction);

        // submarkets
        SectorManager.updateSubmarkets(market, Factions.NEUTRAL, factionId)
        market.addSubmarket(Submarkets.SUBMARKET_STORAGE)

        market.surveyLevel = MarketAPI.SurveyLevel.FULL
        for (cond in market.conditions) {
            cond.isSurveyed = true
        }

        Global.getSector().economy.addMarket(market, true)
        market.primaryEntity.setFaction(factionId) // http://fractalsoftworks.com/forum/index.php?topic=8581.0

        // Initial growth, to simulate that they have been there at least a little while
        var growth = MathUtils.getRandomNumberInRange(10.0f, 45.0f)
        market.population.setWeight(growth);
        market.population.normalize();
        // Hazard Pay
        market.setImmigrationIncentivesOn(true);

        // Add people - Taken from NexUtilsMarket
        val admin = addPerson(market, Ranks.CITIZEN, Ranks.POST_ADMINISTRATOR)
        market.admin =admin
        val postmaster = addPerson(market, null, Ranks.POST_PORTMASTER)
        val supply = addPerson(market, Ranks.SPACE_COMMANDER, Ranks.POST_SUPPLY_OFFICER)


    }

    private fun getLargestColony(): MarketAPI? {
        val marketSet = Global.getSector().economy.marketsCopy.filter { it.factionId == "stardust_ventures" }.sortedByDescending { it.size }
        // Go through the list and get us up to [STARDUST_SUBMARKETS] submarkets.
        //  If we run out somehow before we get to that number, oh well, there just aren't enough indy markets available.
        for (biggest_market in marketSet) {
            //MarketAPI current_market = Global.getSector().getEntityById(k).getMarket();
            if (biggest_market != null) {
                return biggest_market
            }
        }

        return null
    }

    fun addCharacters() {
        val ip = Global.getSector().importantPeople
        var market = Global.getSector().economy.getMarket("stardust_stardust_hall")
        // If stardust hall doesn't exist, might be random core worlds. Then we'll need to send them to the biggest
        if (market == null) { market = getLargestColony()}


        if (market != null) {
            val stardust_ravenna = Global.getFactory().createPerson()
            stardust_ravenna.setFaction("stardust_ventures")
            stardust_ravenna.setId("stardust_ravenna");
            stardust_ravenna.gender = FullName.Gender.FEMALE
            stardust_ravenna.postId = Ranks.POST_FACTION_LEADER
            stardust_ravenna.rankId = Ranks.FACTION_LEADER
            stardust_ravenna.name.first = "Ravenna"
            stardust_ravenna.name.last = "Silverlight"
            stardust_ravenna.portraitSprite = "graphics/portraits/characters/stardust_ravenna.png"
            stardust_ravenna.setVoice(Voices.BUSINESS);
            stardust_ravenna.stats.setSkillLevel(Skills.INDUSTRIAL_PLANNING, 3f)
            market.admin = stardust_ravenna
            market.commDirectory.addPerson(stardust_ravenna, 0)
            market.addPerson(stardust_ravenna)
            ip.addPerson(stardust_ravenna)

            val stardust_sarval = Global.getFactory().createPerson()
            stardust_sarval.setFaction("stardust_ventures")
            stardust_sarval.setId("stardust_sarval");
            stardust_sarval.gender = FullName.Gender.MALE
            stardust_sarval.rankId = Ranks.SPACE_COMMANDER
            stardust_sarval.postId = Ranks.POST_FLEET_COMMANDER
            stardust_sarval.name.first = "Sarval"
            stardust_sarval.name.last = "Kaan"
            stardust_sarval.portraitSprite = "graphics/portraits/characters/stardust_sarval.png"
            stardust_sarval.setVoice(Voices.SOLDIER);
            stardust_sarval.setPersonality(Personalities.AGGRESSIVE);
            stardust_sarval.getStats().setSkillLevel(Skills.GUNNERY_IMPLANTS, 2f)
            stardust_sarval.getStats().setSkillLevel(Skills.ORDNANCE_EXPERTISE, 1f)
            stardust_sarval.getStats().setSkillLevel(Skills.FIELD_MODULATION, 2f)
            stardust_sarval.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 2f)
            stardust_sarval.getStats().setSkillLevel(Skills.MISSILE_SPECIALIZATION, 1f)
            stardust_sarval.getStats().setSkillLevel(Skills.TACTICAL_DRILLS, 1f)
            stardust_sarval.getStats().setSkillLevel(Skills.CREW_TRAINING, 1f)
            stardust_sarval.getStats().setLevel(7)
            stardust_sarval.addTag("coff_nocapture");
            //market.addPerson(stardust_sarval);
            //market.getCommDirectory().addPerson(stardust_sarval, 1);
            ip.addPerson(stardust_sarval)

            val stardust_danlia = Global.getFactory().createPerson()
            stardust_danlia.setFaction("stardust_ventures")
            stardust_danlia.setId("stardust_danlia");
            stardust_danlia.gender = FullName.Gender.FEMALE
            stardust_danlia.rankId = Ranks.SPACE_CAPTAIN
            stardust_danlia.postId = Ranks.POST_FLEET_COMMANDER
            stardust_danlia.name.first = "Danlia"
            stardust_danlia.name.last = "Star Seeker"
            stardust_danlia.portraitSprite = "graphics/portraits/characters/stardust_danlia.png"
            stardust_danlia.setVoice(Voices.SPACER);
            stardust_danlia.setPersonality(Personalities.CAUTIOUS);
            stardust_danlia.getStats().setSkillLevel(Skills.GUNNERY_IMPLANTS, 1f)
            stardust_danlia.getStats().setSkillLevel(Skills.ORDNANCE_EXPERTISE, 1f)
            stardust_danlia.getStats().setSkillLevel(Skills.FIELD_MODULATION, 2f)
            stardust_danlia.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 2f)
            stardust_danlia.getStats().setSkillLevel(Skills.MISSILE_SPECIALIZATION, 2f)
            stardust_danlia.getStats().setSkillLevel(Skills.TACTICAL_DRILLS, 1f)
            stardust_danlia.getStats().setSkillLevel(Skills.CREW_TRAINING, 1f)
            stardust_danlia.getStats().setLevel(7)
            stardust_danlia.addTag("coff_nocapture");
            market.addPerson(stardust_danlia);
            market.getCommDirectory().addPerson(stardust_danlia, 1);
            ip.addPerson(stardust_danlia)


        }


    }

    fun initColonies()
    {
        getCandidatePlanets(15000.0, 40000.0)

        // Add Mystical Grilltopia
        var nextPlanet = getBestPlanet()
        var market = nextPlanet?.market
        if (nextPlanet != null) {
            if (market != null) {
                addColony(market, nextPlanet, "Mystical Grilltopia", "stardust_mystical_grilltopia")
            }
        }

        // Add Wayfarer's Rest
        nextPlanet = getBestPlanet()
        market = nextPlanet?.market
        if (nextPlanet != null) {
            if (market != null) {
                addColony(market, nextPlanet, "Wayfarer's Rest", "stardust_wayfarers_rest")
            }
        }

        // Add in unique characters
        addCharacters()
    }
}
