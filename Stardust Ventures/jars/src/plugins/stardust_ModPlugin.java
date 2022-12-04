package plugins;

import com.fs.starfarer.api.BaseModPlugin;
import java.lang.RuntimeException;
import java.lang.ClassNotFoundException;

import com.fs.starfarer.api.campaign.SectorAPI;
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
import java.lang.Exception;
import java.util.HashMap;

public class stardust_ModPlugin extends BaseModPlugin
{
    // basic ModPlugin code taken from theDragn's High Tech Expansion mod, but no longer reliant on kotlin
    // Thanks to theDragn for directing me to it!

    @Override
    public void onGameLoad(boolean newGame)
    {
        SectorAPI sector = Global.getSector();
        if (!sector.hasScript(stardust_FleetStatManager.class))
        {
            sector.addScript(new stardust_FleetStatManager());
        }
    }

}