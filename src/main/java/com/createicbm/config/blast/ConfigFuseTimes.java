package com.createicbm.config.blast;

import com.createicbm.lib.config.Config;

/**
 * Created by Dark(DarkGuardsman, Robin) on 2/9/2020.
 */
public class ConfigFuseTimes
{
    @Config.LangKey("config.create_icbm:blast.fusetimes.bombcarts.title")
    @Config.Comment("Set fuse times for bomb carts here. The times are written in ticks, where 20 ticks = 1 second.")
    public BombCarts BOMB_CARTS = new BombCarts();

    @Config.LangKey("config.create_icbm:blast.fusetimes.explosives.title")
    @Config.Comment("Set fuse times for explosives (the block) here. The times are written in ticks, where 20 ticks = 1 second.")
    public Explosives EXPLOSIVES = new Explosives();

    public class BombCarts
    {
        @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
        public int CONDENSED = 100;

        @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
        public int SHRAPNEL = 100;

        @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
        public int INCENDIARY = 100;

        @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
        public int DEBILITATION = 100;

        @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
        public int CHEMICAL = 100;

        @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
        public int ANVIL = 100;

        @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
        public int REPULSIVE = 120;

        @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
        public int ATTRACTIVE = 120;

        @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
        public int FRAGMENTATION = 100;

        @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
        public int CONTAGIOUS = 100;

        @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
        public int SONIC = 100;

        @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
        public int BREACHING = 0;

        @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
        public int THERMOBARIC = 100;

        @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
        public int NUCLEAR = 100;

        @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
        public int EMP = 100;

        @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
        public int EXOTHERMIC = 100;

        @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
        public int ENDOTHERMIC = 100;

        @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
        public int ANTI_GRAVITATIONAL = 100;

        @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
        public int ENDER = 100;

        @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
        public int ANTIMATTER = 300;

        @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
        public int REDMATTER = 100;

        @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
        public int FLASHBANG = 100;

        @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
        public int RECON = 100;

        @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
        public int PHANTOM = 100;

        @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
        public int DARKNESS = 100;
    }

    public class Explosives
    {
        @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
        public int CONDENSED = 0;

        @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
        public int SHRAPNEL = 100;

        @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
        public int INCENDIARY = 100;

        @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
        public int DEBILITATION = 100;

        @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
        public int CHEMICAL = 100;

        @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
        public int ANVIL = 100;

        @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
        public int REPULSIVE = 120;

        @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
        public int ATTRACTIVE = 120;

        @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
        public int FRAGMENTATION = 100;

        @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
        public int CONTAGIOUS = 100;

        @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
        public int SONIC = 100;

        @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
        public int BREACHING = 0;

        @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
        public int THERMOBARIC = 100;

        @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
        public int NUCLEAR = 100;

        @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
        public int EMP = 100;

        @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
        public int EXOTHERMIC = 100;

        @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
        public int ENDOTHERMIC = 100;

        @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
        public int ANTI_GRAVITATIONAL = 100;

        @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
        public int ENDER = 100;

        @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
        public int ANTIMATTER = 300;

        @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
        public int REDMATTER = 100;

        @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
        public int FLASHBANG = 100;

        @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
        public int RECON = 100;

        @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
        public int PHANTOM = 100;

        @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
        public int DARKNESS = 100;
    }
}
