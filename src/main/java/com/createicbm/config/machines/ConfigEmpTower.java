package com.createicbm.config.machines;

import com.createicbm.ICBMConstants;
import com.createicbm.lib.config.Config;

/**
 * Settings class for various configuration settings.
 *
 * @author Calclavia, DarkCow
 */
@Config(modid = ICBMConstants.DOMAIN, name = "create_icbm/emp_tower")
@Config.LangKey("config.create_icbm:emp_tower.title")
public class ConfigEmpTower {
    @Config.Name("extender_range_bonus")
    @Config.Comment("Bonus max range (meters) given for each extender added above the base")
    public static int BONUS_RADIUS = 20;

    @Config.Name("firing_cost_area")
    @Config.Comment("Coil charge a blast needs per meter^2 covered. Math: (range * range) * cost")
    public static int ENERGY_COST_AREA = 100;

    @Config.Name("charge_per_rpm")
    @Config.Comment("Coil charge gained per tick for each RPM of shaft speed. A blast needs range * range * firing_cost_area charge, so a 60 m blast at 64 RPM charges in about 14 seconds.")
    @Config.RangeDouble(min = 0.01, max = 100000)
    public static double CHARGE_PER_RPM = 20;

    @Config.Name("stress_impact")
    @Config.Comment("Stress impact (SU per RPM) of the EMP tower on its kinetic network")
    @Config.RangeDouble(min = 0, max = 1024)
    public static double STRESS_IMPACT = 16;

    @Config.Name("firing_cooling_ticks")
    @Config.Comment("Time in ticks (20 ticks a second) to wait before recharging, after firing.")
    public static int COOLDOWN = 10 * 20;

    @Config.Name("base_max_range")
    @Config.Comment("Max user configurable range of base emp tower (meters)")
    public static int MAX_BASE_RANGE = 150;
}
