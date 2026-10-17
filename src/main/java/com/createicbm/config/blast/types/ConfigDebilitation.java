package com.createicbm.config.blast.types;

import com.createicbm.ICBMConstants;
import com.createicbm.lib.config.Config;

public class ConfigDebilitation {

    @Config.Comment("Size (meters) of the gas cloud")
    @Config.Name("size")
    @Config.RangeInt(min = 1)
    public int size = 20;

    @Config.Comment("Duration (ticks) of the gas cloud")
    @Config.Name("duration")
    @Config.RangeInt(min = 1)
    public int duration = ICBMConstants.TICKS_SEC * 30;
}
