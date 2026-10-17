package com.createicbm.config;

import com.createicbm.ICBMConstants;
import com.createicbm.lib.config.Config;

/**
 * Created by Dark(DarkGuardsman, Robin) on 2/10/2019.
 */
@Config(modid = ICBMConstants.DOMAIN, name = "create_icbm/thread")
@Config.LangKey("config.create_icbm:thread.title")
public class ConfigThread
{
    @Config.Name("thread_count")
    @Config.Comment("Number of worker threads to run to handle blast calculations. " +
            "Try to only match 50% of the number of cores your machine can support. " +
            "Otherwise the main game thread will slow down while the workers are processing. " +
            "Which is counter to the reason threads exist.")
    @Config.RangeInt(min = 1, max = 8)
    public static int THREAD_COUNT = 1;
}
