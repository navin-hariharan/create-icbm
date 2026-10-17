package com.createicbm.config.machines;

import com.createicbm.ICBMConstants;
import com.createicbm.lib.config.Config;

@Config(modid = ICBMConstants.DOMAIN, name = "create_icbm/radar")
@Config.LangKey("config.create_icbm:radar.title")
public class ConfigRadar
{
    @Config.Name("max_range")
    @Config.Comment("Max range of radar in blocks (meters) to detect entities")
    public static int MAX_RANGE = 500;

    @Config.Name("min_speed")
    @Config.Comment("Shaft speed (RPM) the radar dish needs before it scans")
    @Config.RangeDouble(min = 0, max = 256)
    public static double MIN_SPEED = 16;

    @Config.Name("stress_impact")
    @Config.Comment("Stress impact (SU per RPM) of the radar station on its kinetic network")
    @Config.RangeDouble(min = 0, max = 1024)
    public static double STRESS_IMPACT = 4;

    @Config.Name("scan_rate")
    @Config.Comment("Delay in ticks before scanning for entities. Lower reacts faster but costs more server time with many missiles in the air")
    public static int SCAN_TICKS = 5;

    @Config.Name("sam_delay")
    @Config.Comment("Delay in ticks before triggering more SAM launches")
    public static int SAM_TICKS = 40;
}
