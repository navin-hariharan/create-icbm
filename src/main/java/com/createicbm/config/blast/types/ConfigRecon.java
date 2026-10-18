package com.createicbm.config.blast.types;

import com.createicbm.ICBMConstants;
import com.createicbm.lib.config.Config;

public class ConfigRecon {

    @Config.Comment("Radius (meters) in which mobs and enemy players are marked")
    @Config.Name("size")
    @Config.RangeInt(min = 1, max = 256)
    public int size = 32;

    @Config.Comment("Duration (ticks) of the glowing outline")
    @Config.Name("duration")
    @Config.RangeInt(min = 1)
    public int duration = ICBMConstants.TICKS_MIN * 3;
}
