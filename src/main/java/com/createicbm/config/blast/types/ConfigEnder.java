package com.createicbm.config.blast.types;

import com.createicbm.lib.config.Config;

public class ConfigEnder {

    @Config.Name("abduct_players")
    @Config.Comment("Players that get pulled into the middle of the vortex are taken by a giant enderman in a short cutscene instead of being teleported instantly.\nOther entities are always teleported instantly.")
    public boolean abductPlayers = true;

    @Config.Name("stare_ticks")
    @Config.Comment("Ticks the giant enderman looms over the player before the warp starts")
    @Config.RangeInt(min = 10, max = 200)
    public int stareTicks = 45;

    @Config.Name("min_ticks_in_warp")
    @Config.Comment("Least ticks of the warp tunnel, it goes on longer if the destination is still loading")
    @Config.RangeInt(min = 10, max = 200)
    public int warpTicks = 45;

    @Config.Name("arrival_ticks")
    @Config.Comment("Ticks the giant enderman stays at the destination before it vanishes and the player is free again")
    @Config.RangeInt(min = 10, max = 200)
    public int arrivalTicks = 60;

    @Config.Name("min_distance")
    @Config.Comment("Closest the player can be taken (blocks)")
    @Config.RangeInt(min = 16, max = 5000)
    public int minDistance = 60;

    @Config.Name("max_distance")
    @Config.Comment("Furthest the player can be taken (blocks). Keep inside the world border and in mind that the destination chunks are loaded for the cutscene.")
    @Config.RangeInt(min = 32, max = 5000)
    public int maxDistance = 150;

    @Config.Name("giant_scale")
    @Config.Comment("Size of the abducting enderman compared to a normal one")
    @Config.RangeDouble(min = 1, max = 16)
    public double giantScale = 3.0;

    @Config.Name("cooldown_ticks")
    @Config.Comment("Ticks after a cutscene in which the same player cannot be taken again")
    @Config.RangeInt(min = 0, max = 24000)
    public int cooldownTicks = 200;
}
