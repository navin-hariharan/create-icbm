package com.createicbm.config;

import com.createicbm.ICBMConstants;
import com.createicbm.lib.config.Config;

/**
 *
 * Created by Dark(DarkGuardsman, Robin) on 2/22/2018.
 */
@Config(modid = ICBMConstants.DOMAIN, name = "create_icbm/mods/jei")
@Config.LangKey("config.create_icbm:jei.title")
public class ConfigJEI
{
    @Config.Name("disable")
    @Config.Comment("Set to true to disable JEI support. Requires restart to take full effect.")
    public static boolean DISABLED = false;
}
