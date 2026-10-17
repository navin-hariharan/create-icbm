package com.createicbm.config;

import com.createicbm.ICBMConstants;
import com.createicbm.lib.config.Config;

/**
 * Client only settings
 */
@Config(modid = ICBMConstants.DOMAIN, name = "create_icbm/client", type = Config.Type.CLIENT)
@Config.LangKey("config.create_icbm:client.title")
public class ConfigClient
{
    @Config.Name("missile_engine_smoke")
    @Config.Comment("Enables engine smoke effect for missiles")
    public static boolean MISSILE_ENGINE_SMOKE = true;

    @Config.Name("missile_launch_smoke")
    @Config.Comment("Enables smoke effect for launched missiles")
    public static boolean MISSILE_LAUNCH_SMOKE = true;
}
