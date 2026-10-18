package com.createicbm.config.blast.types;

import com.createicbm.ICBMConstants;
import com.createicbm.lib.config.Config;

public class ConfigPhantom {

    @Config.Comment("Radius (meters) in which players are haunted")
    @Config.Name("size")
    @Config.RangeInt(min = 1, max = 256)
    public int size = 24;

    @Config.Comment("Phantom mobs spawned around each player in the radius")
    @Config.Name("per_player")
    @Config.RangeInt(min = 1, max = 16)
    public int perPlayer = 4;

    @Config.Comment("Duration (ticks) before a phantom fades away on its own")
    @Config.Name("lifetime")
    @Config.RangeInt(min = 20)
    public int lifetime = ICBMConstants.TICKS_MIN;
}
