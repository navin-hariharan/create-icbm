package com.createicbm.config.machines;

import com.createicbm.ICBMConstants;
import com.createicbm.lib.config.Config;

/**
 * Settings class for various configuration settings.
 *
 * @author Calclavia, DarkCow
 */
@Config(modid = ICBMConstants.DOMAIN, name = "create_icbm/launcher")
@Config.LangKey("config.create_icbm:launcher.title")
public class ConfigLauncher
{
    @Config.Name("max_range")
    @Config.Comment("Range of silo launcher in blocks (meters)")
    @Config.RangeInt(min = 1)
    public static int RANGE = 10000;

    @Config.Name("min_inaccuracy_range")
    @Config.Comment("Inaccuracy offset to apply by default (meters)")
    public static double MIN_INACCURACY = 2;

    @Config.Name("scaled_inaccuracy_range")
    @Config.Comment("Inaccuracy offset to apply based on range (meters) with max range being full value. In addition to min inaccuracy already applied.")
    public static double SCALED_INACCURACY_DISTANCE = 10;

    @Config.Name("scaled_inaccuracy_per_launcher")
    @Config.Comment("Inaccuracy offset to apply per launcher fired in the same circuit.")
    public static double SCALED_INACCURACY_LAUNCHERS = 1;

    @Config.Name("launch_charge")
    @Config.Comment("Charge a launch pad or cruise turret winds up from its shaft before it can fire, used up by each launch")
    @Config.RangeInt(min = 1)
    public static int POWER_COST = 1200;

    @Config.Name("wind_up_seconds")
    @Config.Comment("Seconds a launch pad or cruise turret needs to wind up a full launch charge at 64 RPM. Twice the speed halves it.")
    @Config.RangeDouble(min = 0.05, max = 600)
    public static double WIND_UP_SECONDS = 4;

    @Config.Name("launch_pad_stress")
    @Config.Comment("Stress impact (SU per RPM) of the launch pad on its kinetic network")
    @Config.RangeDouble(min = 0, max = 1024)
    public static double LAUNCH_PAD_STRESS = 8;

    @Config.Name("cruise_turret_stress")
    @Config.Comment("Stress impact (SU per RPM) of the cruise turret on its kinetic network")
    @Config.RangeDouble(min = 0, max = 1024)
    public static double CRUISE_TURRET_STRESS = 6;

    @Config.Name("cruise_turret_launch_cooldown")
    @Config.Comment("Ticks a cruise turret waits after each launch before it fires or answers a radar alert again (20 ticks = 1 second). "
        + "The default lets a turret fire two SAMs well inside the 15 second radar warning, so 8 turrets with 2 SAMs each can answer 16 incoming missiles. "
        + "It runs next to the wind-up (wind_up_seconds), the slower of the two sets the pace. 0 = no extra delay.")
    @Config.RangeInt(min = 0, max = 1200)
    public static int CRUISE_LAUNCH_COOLDOWN = 20;

    @Config.Name("cruise_turret_aim_rpm")
    @Config.Comment("Shaft speed at which the cruise turret turns at its normal aim speed (10 degrees a tick). It aims slower below and faster above, up to 4x.")
    @Config.RangeDouble(min = 1, max = 256)
    public static double CRUISE_AIM_RPM = 32;
}
