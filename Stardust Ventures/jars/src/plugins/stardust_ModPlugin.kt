package plugins

import com.fs.starfarer.api.BaseModPlugin
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.impl.campaign.ids.Factions
import data.scripts.world.systems.stardust_Gen
import exerelin.campaign.SectorManager
import lunalib.lunaSettings.LunaSettings.getBoolean
import lunalib.lunaSettings.LunaSettings.getInt
import org.json.JSONException
import java.io.IOException


class stardust_ModPlugin : BaseModPlugin() {
    //private static String SHOP_BLACKLIST_FILE = "stardust_market_blacklist.csv";
    // Stuff from the configs, values here are the defaults if needed for some reason
    var STARDUST_SUBMARKETS = 3
    private var GEN_SHOPS = true
    private var GEN_FACTION = true
    override fun onGameLoad(newGame: Boolean) {
        val sector = Global.getSector()
        if (!sector.hasScript(stardust_FleetStatManager::class.java)) {
            sector.addScript(stardust_FleetStatManager())
        }

        // Get our settings, from LunaLib or the config files
        try {
            loadSettings()
        } catch (e: JSONException) {
            log.error(e)
        } catch (e: IOException) {
            log.error(e)
        }

        // Generate the shops if enabled
        if (GEN_SHOPS) {
            try {
                openSubMarkets()
            } catch (e: JSONException) {
                log.error(e)
            } catch (e: IOException) {
                log.error(e)
            }
        }

        // Generate the faction if enabled
        if (GEN_FACTION && Global.getSector().getEntityById("stardust_talia") == null) {
            val isNexerelinEnabled = Global.getSettings().modManager.isModEnabled("nexerelin")
            if ((!isNexerelinEnabled || SectorManager.getManager().isCorvusMode)) {
                stardust_Gen().generate(Global.getSector())
                stardust_Gen().generateAfterTime()
            }
        }
    }

    @Throws(JSONException::class, IOException::class)
    fun loadSettings() {
        // Use Lunalib config if that is installed, otherwise use the settings file
        if (HAVE_LUNALIB) {
            GEN_SHOPS = java.lang.Boolean.TRUE == getBoolean("stardustventures", "stardust_generate_shops")
            STARDUST_SUBMARKETS = try {
                getInt("stardustventures", "stardust_num_shops")!!
            } catch (npe: NullPointerException) {
                3
            }
            GEN_FACTION = java.lang.Boolean.TRUE == getBoolean("stardustventures", "stardust_generate_faction")
        } else {
            val setting = Global.getSettings().loadJSON(SETTINGS_FILE)
            STARDUST_SUBMARKETS = setting.getInt("numShops")
            GEN_SHOPS = setting.getBoolean("enableShops")
            GEN_FACTION = setting.getBoolean("enableFaction")
        }
    }

    // Need to generate the faction. Should be able to be turned off in configs
    //  Also, add code to spawn the faction mid-game if enabled
    override fun onNewGame() {
        super.onNewGame()

        // The code below requires that Nexerelin is added as a library (not a dependency, it's only needed to compile the mod).
        val isNexerelinEnabled = Global.getSettings().modManager.isModEnabled("nexerelin")
        if ((!isNexerelinEnabled || SectorManager.getManager().isCorvusMode) && GEN_FACTION) {
            stardust_Gen().generate(Global.getSector())
        }
    }

    override fun onNewGameAfterTimePass(){
        if (GEN_FACTION) {
            stardust_Gen().generateAfterTime()
        }
    }

    @Throws(JSONException::class, IOException::class)
    fun openSubMarkets() {
        // Add in Stardust Ventures submarkets to indie markets
        // We want them in ilm, agreus, and baetis if they exist.
        // If not (for random core or whatever), put them in the 3 biggest indy markets
        //   Or however many the config says to use
        val sector = Global.getSector()
        val sdv_markets: MutableSet<String> = HashSet()
        var sdv_count = 0

        // Get blacklist for allowed markets
        val blacklist_markets: MutableSet<String> = HashSet()
        val path = "data/config/stardust_market_blacklist.csv"
        try {
            val blacklist = Global.getSettings().getMergedSpreadsheetDataForMod("id", path, "stardustventures")
            for (i in 0 until blacklist.length()) {
                val row = blacklist.getJSONObject(i)
                val marketId = row.getString("id")
                blacklist_markets.add(marketId)
                log.debug("Blacklisting $marketId")
            }
        } catch (ex: IOException) {
            log.error(ex)
        } catch (ex: JSONException) {
            log.error(ex)
        }


        // First off, get a list of all markets and check if we've got the required number of SDV subs
        // Not limited to indy markets for this, in case some got taken over
        for (place in sector.economy.marketsCopy) {
            if (place.hasSubmarket("stardust_market")) {
                sdv_count++
                log.info("Found SDV shop at " + place.id)
            }
        }

        // If not, we need to add them. First, try the standard core ones
        // If we still don't have enough, just get the biggest indy markets until we have enough
        // But avoid blacklisted markets, like Kassadar and Galatia
        if (sdv_count < STARDUST_SUBMARKETS) {
            var loc = sector.getEntityById("ilm")
            var current_market: MarketAPI? = null
            if (loc != null) {
                current_market = loc.market
            }
            if (current_market != null) {
                if (current_market.hasSubmarket("stardust_market") && !sdv_markets.contains(current_market.name)) {
                    sdv_markets.add(current_market.name)
                    log.info("Found SDV shop at " + current_market.id + "(size " + current_market.size + ")")
                } else if (!current_market.hasSubmarket("stardust_market") && current_market.factionId == "independent") {
                    current_market.addSubmarket("stardust_market")
                    sdv_markets.add(current_market.name)
                    log.info("Adding SDV shop to " + current_market.id)
                }
            }
            loc = sector.getEntityById("agreus")
            current_market = null
            if (loc != null) {
                current_market = loc.market
            }
            if (current_market != null) {
                if (current_market.hasSubmarket("stardust_market") && !sdv_markets.contains(current_market.name)) {
                    sdv_markets.add(current_market.name)
                    log.info("Found SDV shop at " + current_market.id + "(size " + current_market.size + ")")
                } else if (!current_market.hasSubmarket("stardust_market") && current_market.factionId == "independent") {
                    current_market.addSubmarket("stardust_market")
                    sdv_markets.add(current_market.name)
                    log.info("Adding SDV shop to " + current_market.id)
                }
            }
            loc = sector.getEntityById("baetis")
            current_market = null
            if (loc != null) {
                current_market = loc.market
            }
            if (current_market != null) {
                if (current_market.hasSubmarket("stardust_market") && !sdv_markets.contains(current_market.name)) {
                    sdv_markets.add(current_market.name)
                    log.info("Found SDV shop at " + current_market.id + "(size " + current_market.size + ")")
                } else if (!current_market.hasSubmarket("stardust_market") && current_market.factionId == "independent") {
                    current_market.addSubmarket("stardust_market")
                    sdv_markets.add(current_market.name)
                    log.info("Adding SDV shop to " + current_market.id)
                }
            }
            // If we didn't populate 3 markets, go find some and populate them until we have 3
            if (sdv_markets.size < STARDUST_SUBMARKETS) {
                // Only get indy markets, and sort the Map so we can get the biggest markets easily
                val marketSet = Global.getSector().economy.marketsCopy.filter { it.factionId == Factions.INDEPENDENT }.sortedByDescending { it.size }
                // Go through the list and get us up to [STARDUST_SUBMARKETS] submarkets.
                //  If we run out somehow before we get to that number, oh well, there just aren't enough indy markets available.
                for (indy_market in marketSet) {
                    //MarketAPI current_market = Global.getSector().getEntityById(k).getMarket();
                    if (indy_market != null) {
                        if (blacklist_markets.contains(indy_market.id)) // Skip blacklisted markets
                        {
                            log.info("Skipping SDV shop at " + indy_market.id + ", it is blacklisted")
                        }
                        else if (indy_market.hasSubmarket("stardust_market") && !sdv_markets.contains(indy_market.name)) {
                            sdv_markets.add(indy_market.name)
                            log.info("Found SDV shop at " + indy_market.id)
                        } else if (!indy_market.hasSubmarket("stardust_market")) {
                            indy_market.addSubmarket("stardust_market")
                            sdv_markets.add(indy_market.name)
                            log.info("Adding SDV shop to " + indy_market.id)
                        }
                    }
                    if (sdv_markets.size >= STARDUST_SUBMARKETS) {
                        break
                    }
                }
            }
        }
    }

    companion object {
        // basic ModPlugin code taken from theDragn's High Tech Expansion mod
        // Thanks to theDragn for directing me to it!
        val log = Global.getLogger(stardust_ModPlugin::class.java)

        // Check for mods that we care about
        val HAVE_LUNALIB = Global.getSettings().modManager.isModEnabled("lunalib")

        // General constants/variables we want in one place for easy access
        private const val SETTINGS_FILE = "stardust_settings.json"
    }
}