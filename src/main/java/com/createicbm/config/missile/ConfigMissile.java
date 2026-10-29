package com.createicbm.config.missile;

import com.createicbm.ICBMConstants;
import com.createicbm.lib.config.Config;

/**
 * Configs for Missiles
 *
 * Created by Dark(DarkGuardsman, Robin) on 2/28/2018.
 */
@Config(modid = ICBMConstants.DOMAIN, name = "create_icbm/missile")
@Config.LangKey("config.create_icbm:missile.title")
public class ConfigMissile
{
    /**  */
    @Config.Name("speed_direct_fire")
    @Config.Comment("Speed (meters per tick) limiter of the missile when fired directly without a target")
    @Config.RangeDouble(min = 0.0001, max = 10)
    public static float DIRECT_FLIGHT_SPEED = 2;

    @Config.Name("simulation_flight_speed")
    @Config.Comment("Speed (meters per second) of missiles while simulated, outside loaded chunks")
    @Config.RangeDouble(min = 0.0001)
    public static float SIMULATION_FLIGHT_SPEED = 20;

    @Config.Name("simulation_exit_height")
    @Config.Comment("Height (y level) to start simulating a missile when it travels above the map")
    @Config.RangeInt(min = 1)
    public static int SIMULATION_EXIT_HEIGHT = 500;

    @Config.Name("simulation_enter_height")
    @Config.Comment("Height (y level) to spawn missiles when they enter the map")
    @Config.RangeInt(min = 1)
    public static int SIMULATION_ENTER_HEIGHT = 450;

    @Config.Name("arc_distance_limit")
    @Config.Comment("Distance, ignoring y, to allow flying in an arc towards target. Over distance will fly strait up for better performance.")
    @Config.RangeInt(min = 0)
    public static int ARC_DISTANCE_LIMIT = 500;

    @Config.Name("simulation_enter_speed")
    @Config.Comment("Speed (meters per tick) to spawn the missile")
    @Config.RangeDouble(min = 0)
    public static float SIMULATION_ENTER_SPEED = 1;

    @Config.Name("cruise_fuel")
    @Config.Comment("Fuel (ticks) before a missile fired from a cruise launcher starts to fall out of the air")
    @Config.RangeInt(min = 0)
    public static int CRUISE_FUEL = 200;

    @Config.Name("handheld_fuel")
    @Config.Comment("Fuel (ticks) before a missile fired from a handheld launcher starts to fall out of the air")
    @Config.RangeInt(min = 0)
    public static int HANDHELD_FUEL = 200;

    @Config.Name("direct_fire_chunk_loading")
    @Config.Comment("Missiles flying in a straight line (handheld, cruise and surface-to-air missiles), and missiles the missile tracker could not take, keep the chunks just ahead of them loaded and entity-ticking. Turning this off can make remote missiles freeze at the edge of simulation distance.")
    public static boolean DIRECT_FIRE_CHUNK_LOADING = true;

    @Config.Name("keep_visible_in_flight")
    @Config.Comment("A missile stays a real, visible entity while a player is within the server view distance of it, instead of vanishing into the missile tracker as soon as the chunks ahead stop ticking. The chunks ahead are kept entity-ticking for it (see max_missile_chunk_tickets). Far from every player it is simulated as before.")
    public static boolean KEEP_VISIBLE_IN_FLIGHT = true;

    @Config.Name("max_missile_chunk_tickets")
    @Config.Comment("Most chunks all missiles of one dimension hold loaded and entity-ticking ahead of them together (see direct_fire_chunk_loading).\nOn a server with a big salvo in the air this keeps chunk loading and world generation bounded; each missile always keeps its own chunk and the next one.")
    @Config.RangeInt(min = 2, max = 100000)
    public static int MAX_MISSILE_CHUNK_TICKETS = 512;

    @Config.Name("target_chunk_preload_seconds")
    @Config.Comment("How many simulated-flight seconds before arrival to start preloading the target area. Preload tickets warm chunks but do not entity-tick them; a separate ticking ticket is acquired before the missile is spawned. A larger value gives slow world generation more time but holds target chunks loaded for longer. Set to 0 to preload only when the missile arrives.")
    @Config.RangeInt(min = 0, max = 120)
    public static int TARGET_CHUNK_PRELOAD_SECONDS = 15;

    @Config.Name("health_tier_1")
    @Config.Comment("Amount of damage a missile can take from any source before death")
    @Config.RangeInt(min = 1)
    public static int TIER_1_HEALTH = 50;

    @Config.Name("health_tier_2")
    @Config.Comment("Amount of damage a missile can take from any source before death")
    @Config.RangeInt(min = 1)
    public static int TIER_2_HEALTH = 80;

    @Config.Name("health_tier_3")
    @Config.Comment("Amount of damage a missile can take from any source before death")
    @Config.RangeInt(min = 1)
    public static int TIER_3_HEALTH = 100;

    @Config.Name("health_tier_4")
    @Config.Comment("Amount of damage a missile can take from any source before death")
    @Config.RangeInt(min = 1)
    public static int TIER_4_HEALTH = 200;

    @Config.Name("impact_damage_limit")
    @Config.Comment("Max amount of damage a missile can apply to a target on impact. Set to -1 to have no limit. Set to 0 to disable")
    @Config.RangeInt(min = -1)
    public static int DAMAGE_LIMIT = -1;

    @Config.Name("impact_damage_scale")
    @Config.Comment("scale * velocity = damage. RPG fired missiles often impact with a velocity of 2")
    @Config.RangeDouble(min = 0)
    public static float DAMAGE_SCALE = 20f;

    @Config.LangKey("config.create_icbm:missile.sam.title")
    public static ConfigSAMMissile SAM_MISSILE = new ConfigSAMMissile();

}
