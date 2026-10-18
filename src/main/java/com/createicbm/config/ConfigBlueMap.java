package com.createicbm.config;

import com.createicbm.ICBMConstants;
import com.createicbm.lib.config.Config;

/**
 * Settings for showing missiles on BlueMap web maps, only used when BlueMap is installed
 */
@Config(modid = ICBMConstants.DOMAIN, name = "create_icbm/mods/bluemap")
@Config.LangKey("config.create_icbm:bluemap.title")
public class ConfigBlueMap
{
    @Config.Name("enabled")
    @Config.Comment("Show flying missiles and their impacts on BlueMap")
    public static boolean ENABLED = true;

    @Config.Name("update_ticks")
    @Config.Comment("How often, in ticks, missile markers are moved. BlueMap's page fetches markers every 10 seconds, "
        + "updating much more often than that only costs server time")
    @Config.RangeInt(min = 5, max = 200)
    public static int UPDATE_TICKS = 100;

    @Config.Name("impact_marker_seconds")
    @Config.Comment("How long the mushroom cloud stays on the map after impact, in seconds. Also the parcel of a Delivery "
        + "Missile that put everything into a container, parcels of unopened mail stay until it is opened")
    @Config.RangeInt(min = 0, max = 3600)
    public static int IMPACT_SECONDS = 60;
}
