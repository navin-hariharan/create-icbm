package com.createicbm.config.blocks;

import com.createicbm.ICBMConstants;
import com.createicbm.lib.config.Config;

@Config(modid = ICBMConstants.DOMAIN, name = "create_icbm/blocks")
@Config.LangKey("config.create_icbm:blocks.title")
public class ConfigBlocks
{
    @Config.Name("radioactive")
    @Config.Comment("Config for radioactive blocks")
    public static ConfigRadioactive radioactive = new ConfigRadioactive();
}
