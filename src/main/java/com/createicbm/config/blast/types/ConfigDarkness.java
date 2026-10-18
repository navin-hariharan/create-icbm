package com.createicbm.config.blast.types;

import com.createicbm.ICBMConstants;
import com.createicbm.lib.config.Config;

public class ConfigDarkness {

    @Config.Comment("Radius (meters) of the darkness")
    @Config.Name("size")
    @Config.RangeInt(min = 1, max = 256)
    public int size = 24;

    @Config.Comment("Duration (ticks) of the Darkness effect")
    @Config.Name("duration")
    @Config.RangeInt(min = 1)
    public int duration = ICBMConstants.TICKS_MIN * 3;
}
