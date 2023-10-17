package plugins;

import com.fs.starfarer.api.BaseModPlugin;
import java.lang.RuntimeException;
import java.lang.ClassNotFoundException;
import lunalib.lunaSettings.LunaSettings;

import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.procgen.ProcgenUsedNames;
import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.PluginPick;
import com.fs.starfarer.api.combat.MissileAIPlugin;
import com.fs.starfarer.api.campaign.CampaignPlugin;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.Exception;
import java.util.*;
import java.util.stream.Collectors;

public class stardust_ModPlugin extends BaseModPlugin
{
    // basic ModPlugin code taken from theDragn's High Tech Expansion mod, but no longer reliant on kotlin
    // Thanks to theDragn for directing me to it!

    // Check for mods that we care about
    public static final boolean HAVE_LUNALIB = Global.getSettings().getModManager().isModEnabled("lunalib");

    // General constants/variables we want in one place for easy access
    private static String SETTINGS_FILE = "stardust_settings.json";

    // Stuff from the configs, values here are the defaults if needed for some reason
    public int STARDUST_SUBMARKETS = 3;
    private boolean GEN_SHOPS = true;


    @Override
    public void onGameLoad(boolean newGame)
    {
        SectorAPI sector = Global.getSector();
        if (!sector.hasScript(stardust_FleetStatManager.class))
        {
            sector.addScript(new stardust_FleetStatManager());
        }

        // Get our settings, from LunaLib or the config files
        try {
            loadSettings();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Generate the shops if enabled
        if (GEN_SHOPS) {
            openSubMarkets();
        }


    }

    public void loadSettings() throws JSONException, IOException {
        // Use Lunalib config if that is installed, otherwise use the settings file
        if (HAVE_LUNALIB)
        {
            GEN_SHOPS = Boolean.TRUE.equals(LunaSettings.getBoolean("stardustventures", "stardust_generate_shops"));
            try {
                STARDUST_SUBMARKETS = LunaSettings.getInt("stardustventures", "stardust_num_shops");
            } catch (NullPointerException npe) {
                STARDUST_SUBMARKETS = 3;
            }

        }
        else {
            JSONObject setting = Global.getSettings().loadJSON(SETTINGS_FILE);
            STARDUST_SUBMARKETS = setting.getInt("numShops");
            GEN_SHOPS = setting.getBoolean("enableShops");
        }
    }

    public void openSubMarkets()
    {
        // Add in Stardust Ventures submarkets to indie markets
        // We want them in ilm, agreus, and baetis if they exist.
        // If not (for random core or whatever), put them in the 3 biggest indy markets
        SectorAPI sector = Global.getSector();
        Set<String> sdv_markets= new HashSet<>();

        // First off, get a list of all markets and check if we've got the required number of SDV subs

        // If not, we need to add them. First, try the standard core ones

        // If we still don't have enough, just get the biggest indy markets until we have enough
        // But avoid blacklisted markets, like Kassadar and Prism

        SectorEntityToken loc = sector.getEntityById("ilm");
        MarketAPI current_market = null;
        if (loc != null) { current_market = loc.getMarket(); }
        if (current_market != null){
            if (current_market.hasSubmarket("stardust_market") && !sdv_markets.contains(current_market.getName())) {
                sdv_markets.add(current_market.getName());
            } else if (!current_market.hasSubmarket("stardust_market") && Objects.equals(current_market.getFactionId(), "independent")) {
                current_market.addSubmarket("stardust_market");
                sdv_markets.add(current_market.getName());
            }
        }
        loc = sector.getEntityById("agreus");
        current_market = null;
        if (loc != null) { current_market = loc.getMarket(); }
        if (current_market != null){
            if (current_market.hasSubmarket("stardust_market") && !sdv_markets.contains(current_market.getName())) {
                sdv_markets.add(current_market.getName());
            } else if (!current_market.hasSubmarket("stardust_market") && Objects.equals(current_market.getFactionId(), "independent")) {
                current_market.addSubmarket("stardust_market");
                sdv_markets.add(current_market.getName());
            }
        }
        loc = sector.getEntityById("baetis");
        current_market = null;
        if (loc != null) { current_market = loc.getMarket(); }
        if (current_market != null){
            if (current_market.hasSubmarket("stardust_market") && !sdv_markets.contains(current_market.getName())) {
                sdv_markets.add(current_market.getName());
            } else if (!current_market.hasSubmarket("stardust_market") && Objects.equals(current_market.getFactionId(), "independent")) {
                current_market.addSubmarket("stardust_market");
                sdv_markets.add(current_market.getName());
            }
        }
        // If we didn't populate 3 markets, go find some and populate them until we have 3
        if (sdv_markets.size() < STARDUST_SUBMARKETS)
        {
            // Get candidate markets
            // Note: Need to add in a more generic blacklist so we can avoid some mod locations
            // But for now, just exclude Kassadar
            Map<MarketAPI, Integer> indy_markets = new HashMap<>();
            for (MarketAPI place : sector.getEconomy().getMarketsCopy()) {
                if (place.getFactionId().equals("independent"))
                {
                    String db_place = place.getId(); // && !place.getId().equals(("tahlan_lethia_p05"))
                    indy_markets.put(place, place.getSize());
                }
            }
            // Sort the Map so we can get the biggest markets easily
            LinkedHashMap<MarketAPI, Integer> sortedMarkets = indy_markets.entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (oldValue, newValue) -> oldValue, LinkedHashMap::new));
            Set<MarketAPI> market_set = sortedMarkets.keySet();
            // Go through the list and get us up to [STARDUST_SUBMARKETS] submarkets.
            //  If we run out somehow before we get to that number, oh well, there just aren't enough indy markets available.
            for(MarketAPI indy_market:market_set){
                //MarketAPI current_market = Global.getSector().getEntityById(k).getMarket();
                if (indy_market != null) {
                    if (indy_market.hasSubmarket("stardust_market") && !sdv_markets.contains(indy_market.getName())) {
                        sdv_markets.add(indy_market.getName());
                    } else if (!indy_market.hasSubmarket("stardust_market")) {
                        indy_market.addSubmarket("stardust_market");
                        sdv_markets.add(indy_market.getName());
                    }
                }
                if (sdv_markets.size() >= STARDUST_SUBMARKETS)
                {
                    break;
                }
            }

        }
    }

}