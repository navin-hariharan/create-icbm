package com.createicbm.config.blast.types;

import com.createicbm.lib.config.Config;

public class ConfigNuclear {

    @Config.LangKey("config.create_icbm:blast.scale.title")
    @Config.Comment("Scale of the explosive; This isn't always max size but a multiplier used in calculations.")
    @Config.RangeDouble(min = 1, max = Integer.MAX_VALUE)
    public double scale = 50;

    @Config.Comment("Scale of the block rot blast. Handles radioactive blocks and destroying plants")
    @Config.RangeDouble(min = 1, max = Integer.MAX_VALUE)
    public double rotScale = 80;

    @Config.Comment("Scale of the entity mutation blast")
    @Config.RangeDouble(min = 1, max = Integer.MAX_VALUE)
    public double mutationScale = 80;

    @Config.Comment("Scale of the entity damage")
    @Config.RangeDouble(min = 1, max = Integer.MAX_VALUE)
    public double entityDamageScale = 50;

    @Config.LangKey("config.create_icbm:blast.energy.title")
    @Config.Comment("Energy scale used for breaking blocks and doing entity damage")
    @Config.RangeDouble(min = 1, max = Integer.MAX_VALUE)
    public double energy = 80;

    @Config.Comment("Multiplier to apply to energy before scaling blast damage to entities. Damage still scales by distance and other factors.")
    @Config.RangeDouble(min = 1, max = Integer.MAX_VALUE)
    public double entityDamageMultiplier = 1000;

    @Config.Comment("Draw a mushroom cloud above the blast for everybody in range, off falls back to the old smoke ring")
    public boolean mushroomCloud = true;

    @Config.Comment("Size of the mushroom cloud compared to the blast radius, the antimatter cloud is 1.0")
    @Config.RangeDouble(min = 0.1, max = 4)
    public double mushroomCloudScale = 0.7;

    @Config.Comment("Blocks around the blast where players see the mushroom cloud")
    @Config.RangeInt(min = 32, max = 4096)
    public int mushroomCloudRange = 512;

    @Config.Name("radioactive_replacements")
    public BlockReplacements radiationReplacements = new BlockReplacements();

    public static class BlockReplacements {

        @Config.Name("list")
        @Config.Comment("Sets which block to use when replacing a block during a radiation event, as mod:block or mod:block[property:value]")
        public String[] blockStates = new String[]{};
    }
}
