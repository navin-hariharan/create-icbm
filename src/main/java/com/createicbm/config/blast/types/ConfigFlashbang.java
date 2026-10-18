package com.createicbm.config.blast.types;

import com.createicbm.ICBMConstants;
import com.createicbm.lib.config.Config;

public class ConfigFlashbang {

    @Config.Comment("Radius (meters) in which players are blinded by the flash")
    @Config.Name("size")
    @Config.RangeInt(min = 1, max = 256)
    public int size = 16;

    @Config.Comment("Duration (ticks) the screen stays fully white, it then fades out over half a second")
    @Config.Name("duration")
    @Config.RangeInt(min = 1, max = ICBMConstants.TICKS_MIN)
    public int duration = ICBMConstants.TICKS_SEC;
}
